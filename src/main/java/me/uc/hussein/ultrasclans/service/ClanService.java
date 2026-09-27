package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.*;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public final class ClanService {
    private final JavaPlugin plugin;
    private final ClanRepository repository;
    private final EconomyService economy;
    private final Map<UUID, Clan> clans = new ConcurrentHashMap<>();
    private final Map<String, UUID> byName = new ConcurrentHashMap<>();
    private final Map<UUID, Long> leaveCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> createCooldown = new ConcurrentHashMap<>();

    public ClanService(JavaPlugin plugin, ClanRepository repository, EconomyService economy) {
        this.plugin=plugin; this.repository=repository; this.economy=economy;
    }
    public void load() throws Exception {
        clans.clear(); byName.clear();
        for (Clan c: repository.loadAll()) { clans.put(c.id(), c); byName.put(c.name().toLowerCase(Locale.ROOT), c.id()); }
    }
    public Collection<Clan> all() { return Collections.unmodifiableCollection(clans.values()); }
    public Clan byId(UUID id) { return clans.get(id); }
    public Clan byName(String name) { UUID id=byName.get(name.toLowerCase(Locale.ROOT)); return id == null ? null : byId(id); }
    public Clan of(UUID player) { for (Clan c: clans.values()) if (c.hasMember(player)) return c; return null; }
    public long leaveCooldown(UUID player) { return Math.max(0, leaveCooldown.getOrDefault(player, 0L)-System.currentTimeMillis()); }
    public void setLeaveCooldown(UUID player) { leaveCooldown.put(player, System.currentTimeMillis()+600_000L); }

    public String validateName(String name) {
        var cfg=plugin.getConfig();
        int min=cfg.getInt("creation.name-min-length",3), max=cfg.getInt("creation.name-max-length",16);
        if (name == null) return "empty";
        if (name.length()<min || name.length()>max) return "length";
        if (cfg.getBoolean("creation.allow-spaces",false) == false && name.contains(" ")) return "spaces";
        if (cfg.getBoolean("creation.first-character-letter-only",true) && !Character.isLetter(name.charAt(0))) return "first";
        if (!cfg.getBoolean("creation.allow-numbers",true) && name.chars().anyMatch(Character::isDigit)) return "numbers";
        if (!cfg.getBoolean("creation.allow-symbols",false) && !Pattern.matches("[\\p{L}\\p{Nd}_]+", name)) return "symbols";
        if (!cfg.getBoolean("creation.allow-arabic",false) && name.matches(".*[\\u0600-\\u06ff].*")) return "arabic";
        if (!cfg.getBoolean("creation.allow-english",true) && name.matches(".*[A-Za-z].*")) return "english";
        String lower=name.toLowerCase(Locale.ROOT);
        for(String s:cfg.getStringList("creation.blacklist-words")) if(lower.contains(s.toLowerCase(Locale.ROOT))) return "blacklist";
        for(String s:cfg.getStringList("creation.reserved-names")) if(lower.equals(s.toLowerCase(Locale.ROOT))) return "reserved";
        if(byName.containsKey(lower)) return "taken";
        return null;
    }

    public CompletableFuture<Boolean> create(UUID owner, String name) {
        if (of(owner)!=null || validateName(name)!=null) return CompletableFuture.completedFuture(false);
        long now=System.currentTimeMillis(); long cd=plugin.getConfig().getLong("creation.creation-cooldown-seconds",30)*1000L;
        if(createCooldown.getOrDefault(owner,0L)>now) return CompletableFuture.completedFuture(false);
        createCooldown.put(owner,now+cd);
        double price=plugin.getConfig().getDouble("creation.price",1000D);
        var player=plugin.getServer().getPlayer(owner);
        if(player==null || !economy.available() || !economy.withdraw(player,price)) return CompletableFuture.completedFuture(false);
        Clan clan=new Clan(UUID.randomUUID(),name,owner);
        clan.setBankCapacity(plugin.getConfig().getDouble("defaults.bank-capacity",50000D));
        clan.setMemberCapacity(plugin.getConfig().getInt("defaults.member-limit",10)); clan.setStorageLevel(1); clan.setWarpCapacity(1); clan.setAllianceCapacity(1);
        clan.setOpen(plugin.getConfig().getBoolean("creation.default-open",true));
        clan.addMember(new ClanMember(owner,player.getName(),plugin.getConfig().getString("defaults.owner-rank","owner"),now));
        clans.put(clan.id(),clan); byName.put(name.toLowerCase(Locale.ROOT),clan.id());
        return repository.saveAsync(clan).handle((v,e)->{
            if(e!=null){ clans.remove(clan.id()); byName.remove(name.toLowerCase(Locale.ROOT)); economy.deposit(player,price); return false; }
            return true;
        });
    }

    public CompletableFuture<Boolean> join(Clan clan, UUID player, String playerName, boolean bypassHistory) {
        if(clan==null || of(player)!=null || clan.members().size()>=clan.memberCapacity() || clan.member(player)!=null || leaveCooldown(player)>0) return CompletableFuture.completedFuture(false);
        CompletableFuture<ClanMember> history = bypassHistory ? CompletableFuture.completedFuture(null) : repository.dbLoadHistory(clan.id(),player,playerName);
        return history.thenCompose(snapshot->{
            if(of(player)!=null || clan.members().size()>=clan.memberCapacity()) return CompletableFuture.completedFuture(false);
            ClanMember m=snapshot==null?new ClanMember(player,playerName,plugin.getConfig().getString("defaults.default-rank","member"),System.currentTimeMillis()):snapshot;
            m.setName(playerName); clan.addMember(m); recalcLevel(clan);
            if(snapshot!=null) repository.dbDeleteHistory(clan.id(),player);
            return repository.saveAsync(clan).handle((v,e)->{ if(e!=null){clan.removeMember(player);return false;} return true; });
        });
    }

    public CompletableFuture<Boolean> leave(Clan clan, UUID player) {
        if(clan==null || !clan.hasMember(player) || clan.owner().equals(player)) return CompletableFuture.completedFuture(false);
        ClanMember removed=clan.member(player); repository.dbSaveHistory(clan.id(),removed); clan.removeMember(player); setLeaveCooldown(player); recalcLevel(clan);
        return repository.saveAsync(clan).handle((v,e)->{if(e!=null) clan.addMember(removed); return e==null;});
    }

    public CompletableFuture<Boolean> kick(Clan clan, UUID target) {
        if(clan==null || target.equals(clan.owner()) || !clan.hasMember(target)) return CompletableFuture.completedFuture(false);
        ClanMember m=clan.member(target); clan.removeMember(target); recalcLevel(clan);
        return repository.saveAsync(clan).handle((v,e)->{if(e!=null) clan.addMember(m); return e==null;});
    }

    public CompletableFuture<Boolean> transfer(Clan clan, UUID oldOwner, UUID newOwner) {
        if(clan==null || !clan.owner().equals(oldOwner) || !clan.hasMember(newOwner)) return CompletableFuture.completedFuture(false);
        ClanMember old=clan.member(oldOwner), next=clan.member(newOwner); clan.setOwner(newOwner); old.setRank(plugin.getConfig().getString("defaults.default-rank","member")); next.setRank(plugin.getConfig().getString("defaults.owner-rank","owner"));
        return repository.saveAsync(clan).handle((v,e)->{if(e!=null){clan.setOwner(oldOwner); old.setRank("owner"); next.setRank("member"); return false;} return true;});
    }

    public CompletableFuture<Boolean> delete(Clan clan) { if(clan==null)return CompletableFuture.completedFuture(false); clans.remove(clan.id()); byName.remove(clan.name().toLowerCase(Locale.ROOT)); return repository.deleteAsync(clan.id()).handle((v,e)->e==null); }
    public CompletableFuture<Void> save(Clan clan){ return repository.saveAsync(clan); }
    public CompletableFuture<Boolean> rename(Clan clan, String newName) {
        String old=clan.name(), error=validateName(newName); if(error!=null && !error.equals("taken")) return CompletableFuture.completedFuture(false);
        UUID same=byName.get(newName.toLowerCase(Locale.ROOT)); if(same!=null && !same.equals(clan.id())) return CompletableFuture.completedFuture(false);
        byName.remove(old.toLowerCase(Locale.ROOT)); byName.put(newName.toLowerCase(Locale.ROOT),clan.id()); clan.setName(newName);
        return repository.saveAsync(clan).handle((v,e)->{if(e!=null){byName.remove(newName.toLowerCase(Locale.ROOT));byName.put(old.toLowerCase(Locale.ROOT),clan.id());clan.setName(old);return false;}return true;});
    }

    public void addCp(Clan clan,long amount,String source,UUID actor) {
        if(clan==null || amount==0) return; long before=clan.cp(); clan.addCp(amount); recalcLevel(clan);
        repository.saveAsync(clan); plugin.getServer().getScheduler().runTaskAsynchronously(plugin,()->{try{repository.log(actor,clan.id(),amount>0?"CP_GAIN":"CP_SPEND",(double)Math.abs(amount),String.valueOf(before),String.valueOf(clan.cp()),source,null);}catch(Exception ignored){}});
    }
    public boolean spendCp(Clan clan,long amount,String source,UUID actor) {
        if(clan==null || amount<0 || clan.cp()<amount) return false; addCp(clan,-amount,source,actor); return true;
    }
    public void recalcLevel(Clan clan) {
        long cp=clan.cp(); int level=(int)Math.max(1, Math.min(100, 1 + cp/1000)); clan.setLevel(level);
    }
    public ClanRank rank(Clan clan) {
        if(clan==null) return new ClanRank("none","None","<gray>","",0,0,0,Set.of());
        ConfigurationSection sec=plugin.getConfig().getConfigurationSection("clan-ranks");
        String best="bronze", name="Bronze", color="<gray>"; long min=0,max=Long.MAX_VALUE;
        for(Map<?,?> raw:plugin.getConfig().getMapList("clan-ranks")) {
            long a=toLong(raw.get("min-cp"),0), b=toLong(raw.get("max-cp"),Long.MAX_VALUE);
            if(clan.cp()>=a && clan.cp()<=b && a>=min){min=a;max=b;best=String.valueOf(raw.containsKey("id")?raw.get("id"):"rank");name=String.valueOf(raw.containsKey("name")?raw.get("name"):best);color=String.valueOf(raw.containsKey("color")?raw.get("color"):"<gray>");}
        }
        return new ClanRank(best,name,color,"",min,max,0,Set.of());
    }
    private long toLong(Object o,long d){try{return Long.parseLong(String.valueOf(o));}catch(Exception e){return d;}}
}
