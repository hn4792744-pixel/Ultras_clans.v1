package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.*;
import me.uc.hussein.ultrasclans.message.Messages;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.*;

public final class EventService {
    private final JavaPlugin plugin; private final ClanService clans; private final Messages messages; private final SpawnService spawns;
    private final Map<UUID,ClanEvent> events=new ConcurrentHashMap<>(); private final Map<UUID,Location> before=new ConcurrentHashMap<>(); private final Map<String,BlockSnapshot> changed=new ConcurrentHashMap<>();
    private record BlockSnapshot(Location location,Material type,byte data){}
    public EventService(JavaPlugin p,ClanService c,Messages m,SpawnService s){plugin=p;clans=c;messages=m;spawns=s;}
    public Collection<ClanEvent> events(){return Collections.unmodifiableCollection(events.values());}
    public ClanEvent create(Clan a,Clan b,double money,long cp,int minutes,int rounds){if(a==null||b==null||a.id().equals(b.id())||money<0||cp<0||minutes<=0||rounds<1)return null;if(a.members().size()==0||b.members().size()==0)return null;if(money==0&&cp==0&&plugin.getConfig().getBoolean("events.require-wager",true))return null;if(a.cp()<cp||b.cp()<cp||a.bank()<money||b.bank()<money)return null;ClanEvent e=new ClanEvent(UUID.randomUUID(),a.id(),b.id(),money,cp,minutes,rounds);events.put(e.id,e);return e;}
    public boolean accept(ClanEvent e){if(e==null||e.status!=ClanEvent.Status.PENDING)return false;Clan a=clans.byId(e.clanA),b=clans.byId(e.clanB);if(a==null||b==null)return false;for(UUID id:a.members().keySet())if(plugin.getServer().getPlayer(id)!=null)e.participantsA.add(id);for(UUID id:b.members().keySet())if(plugin.getServer().getPlayer(id)!=null)e.participantsB.add(id);if(e.participantsA.isEmpty()||e.participantsB.isEmpty()||eventSpawn(1)==null||eventSpawn(2)==null)return false;if(e.moneyStake>0){a.setBank(a.bank()-e.moneyStake);b.setBank(b.bank()-e.moneyStake);}if(e.cpStake>0){a.removeCp(e.cpStake);b.removeCp(e.cpStake);}clans.save(a);clans.save(b);e.status=ClanEvent.Status.PREPARING;prepare(e);return true;}
    private void prepare(ClanEvent e){e.startedAt=System.currentTimeMillis();for(UUID id:e.participantsA)equip(id,e);for(UUID id:e.participantsB)equip(id,e);plugin.getServer().broadcast(messages.component("event-start"));plugin.getServer().getScheduler().runTaskLater(plugin,()->startRound(e),15*20L);}
    private void equip(UUID id,ClanEvent e){Player p=plugin.getServer().getPlayer(id);if(p!=null){before.putIfAbsent(id,p.getLocation().clone());p.setGameMode(GameMode.SURVIVAL);}}
    private void startRound(ClanEvent e){if(e.status==ClanEvent.Status.FINISHED)return;e.status=ClanEvent.Status.ACTIVE;teleportTeam(e.participantsA,1);teleportTeam(e.participantsB,2);plugin.getServer().getScheduler().runTaskLater(plugin,()->checkTime(e),e.roundMinutes*60L*20L);}
    private void teleportTeam(Set<UUID> ids,int team){Location l=eventSpawn(team);if(l==null)return;for(UUID id:ids){Player p=plugin.getServer().getPlayer(id);if(p!=null&&!eEliminated(id))p.teleportAsync(l);}}
    private Location eventSpawn(int team){String key="events.event"+team;String world=plugin.getConfig().getString(key+".world","");if(world.isBlank())return null;World w=plugin.getServer().getWorld(world);if(w==null)return null;return new Location(w,plugin.getConfig().getDouble(key+".x"),plugin.getConfig().getDouble(key+".y"),plugin.getConfig().getDouble(key+".z"), (float)plugin.getConfig().getDouble(key+".yaw"),(float)plugin.getConfig().getDouble(key+".pitch"));}
    private boolean eEliminated(UUID id){for(ClanEvent e:events.values())if(e.participant(id)&&e.eliminated.contains(id))return true;return false;}
    public void death(Player p){for(ClanEvent e:events.values())if(e.participant(p.getUniqueId())&&e.status==ClanEvent.Status.ACTIVE){e.eliminated.add(p.getUniqueId());p.setGameMode(GameMode.SPECTATOR);checkRound(e);}}
    private void checkRound(ClanEvent e){boolean a=e.participantsA.stream().allMatch(e.eliminated::contains),b=e.participantsB.stream().allMatch(e.eliminated::contains);if(a==b)return; if(a)e.scoreB++;else e.scoreA++; if(e.scoreA>e.maxRounds/2||e.scoreB>e.maxRounds/2||e.round>=e.maxRounds){finish(e);return;}e.round++;e.eliminated.clear();for(UUID id:e.participantsA)equip(id,e);for(UUID id:e.participantsB)equip(id,e);plugin.getServer().getScheduler().runTaskLater(plugin,()->startRound(e),40L);}
    private void checkTime(ClanEvent e){if(e.status!=ClanEvent.Status.ACTIVE)return;checkRound(e);if(e.status==ClanEvent.Status.ACTIVE){e.round++;e.eliminated.clear();plugin.getServer().getScheduler().runTaskLater(plugin,()->startRound(e),40L);}}
    public void quit(Player p){for(ClanEvent e:events.values())if(e.participant(p.getUniqueId())&&e.status==ClanEvent.Status.ACTIVE){plugin.getServer().getScheduler().runTaskLater(plugin,()->{if(plugin.getServer().getPlayer(p.getUniqueId())==null)forfeit(e,p.getUniqueId());},plugin.getConfig().getLong("events.disconnect-grace-seconds",300)*20L);}}
    public void rejoin(Player p){
        UUID player = p.getUniqueId();
        for (ClanEvent e : events.values()) {
            if (!e.participant(player) || e.status != ClanEvent.Status.ACTIVE || e.eliminated.contains(player)) continue;
            Location l = e.clanA.equals(e.clanOf(player)) ? eventSpawn(1) : eventSpawn(2);
            if (l != null) p.teleportAsync(l);
        }
    }
    public void leave(Player p){for(ClanEvent e:events.values())if(e.participant(p.getUniqueId()))forfeit(e,p.getUniqueId());}
    private void forfeit(ClanEvent e,UUID player){e.eliminated.add(player);checkRound(e);}
    private void finish(ClanEvent e){if(e.status==ClanEvent.Status.FINISHED)return;restoreBlocks();e.status=e.scoreA==e.scoreB?ClanEvent.Status.DRAW:ClanEvent.Status.FINISHED;Clan a=clans.byId(e.clanA),b=clans.byId(e.clanB);if(e.status==ClanEvent.Status.FINISHED){Clan winner=e.scoreA>e.scoreB?a:b;if(winner!=null){if(e.moneyStake>0)winner.setBank(Math.min(winner.bankCapacity(),winner.bank()+e.moneyStake*2));if(e.cpStake>0)winner.addCp(e.cpStake*2);clans.save(winner);}}for(UUID id:e.participantsA)restore(id);for(UUID id:e.participantsB)restore(id);}
    private void restore(UUID id){Player p=plugin.getServer().getPlayer(id);Location l=before.remove(id);if(p!=null){p.setGameMode(GameMode.SURVIVAL);if(l!=null)p.teleportAsync(l);}}
    public void shutdown(){for(ClanEvent e:new ArrayList<>(events.values())){for(UUID id:e.participantsA)restore(id);for(UUID id:e.participantsB)restore(id);}restoreBlocks();events.clear();}
    public boolean isActive(Player p){for(ClanEvent e:events.values())if(e.participant(p.getUniqueId())&&e.status==ClanEvent.Status.ACTIVE)return true;return false;}
    public void blockBreak(Block b){for(ClanEvent e:events.values())if(e.status==ClanEvent.Status.ACTIVE&&insideEvent(b.getLocation())){String k=key(b.getLocation());changed.putIfAbsent(k,new BlockSnapshot(b.getLocation().clone(),b.getType(),(byte)0));}}
    public void blockPlace(Block b){for(ClanEvent e:events.values())if(e.status==ClanEvent.Status.ACTIVE&&insideEvent(b.getLocation())){String k=key(b.getLocation());changed.putIfAbsent(k,new BlockSnapshot(b.getLocation().clone(),Material.AIR,(byte)0));}}
    public boolean insideEvent(Location l){String w=plugin.getConfig().getString("events.rest1.world","");String w2=plugin.getConfig().getString("events.rest2.world","");if(w.isBlank()||!w.equals(w2)||!w.equals(l.getWorld().getName()))return false;double x1=plugin.getConfig().getDouble("events.rest1.x"),y1=plugin.getConfig().getDouble("events.rest1.y"),z1=plugin.getConfig().getDouble("events.rest1.z"),x2=plugin.getConfig().getDouble("events.rest2.x"),y2=plugin.getConfig().getDouble("events.rest2.y"),z2=plugin.getConfig().getDouble("events.rest2.z");return l.getX()>=Math.min(x1,x2)&&l.getX()<=Math.max(x1,x2)&&l.getY()>=Math.min(y1,y2)&&l.getY()<=Math.max(y1,y2)&&l.getZ()>=Math.min(z1,z2)&&l.getZ()<=Math.max(z1,z2);}
    public void restoreBlocks(){for(BlockSnapshot s:changed.values())s.location().getBlock().setType(s.type(),false);changed.clear();}
    private String key(Location l){return l.getWorld().getName()+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ();}
}
