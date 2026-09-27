package me.uc.hussein.ultrasclans.gui;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.model.ClanMember;
import me.uc.hussein.ultrasclans.model.ClanRank;
import me.uc.hussein.ultrasclans.service.ClanService;
import me.uc.hussein.ultrasclans.service.PermissionService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import java.util.*;

public final class GuiManager {
    private final JavaPlugin plugin; private final YamlConfiguration guiConfig; private final ClanService clans; private final PermissionService permissions; private final me.uc.hussein.ultrasclans.service.PromptService prompts; private final me.uc.hussein.ultrasclans.service.StorageService storage; private final me.uc.hussein.ultrasclans.service.TaskService tasks; private final me.uc.hussein.ultrasclans.service.RequestService requests; private final MiniMessage mm=MiniMessage.miniMessage();
    public GuiManager(JavaPlugin plugin,ClanService clans,PermissionService permissions,me.uc.hussein.ultrasclans.service.PromptService prompts,me.uc.hussein.ultrasclans.service.StorageService storage,me.uc.hussein.ultrasclans.service.TaskService tasks,me.uc.hussein.ultrasclans.service.RequestService requests){this.plugin=plugin;this.guiConfig=YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"gui.yml"));this.clans=clans;this.permissions=permissions;this.prompts=prompts;this.storage=storage;this.tasks=tasks;this.requests=requests;}

    public void openRoot(Player p){
        Clan c=clans.of(p.getUniqueId()); if(c==null) openPreClan(p); else openMyClan(p,c);
    }
    public void openPreClan(Player p){
        Inventory inv=inventory(p,"pre-clan",27,"<gradient:#ff3333:#8b0000>ULTRAS CLANS</gradient> <dark_gray>•</dark_gray> <white>Menu</white>");
        set(inv,11,Material.EMERALD_BLOCK,"<green>ᴄʀᴇᴀᴛᴇ ᴄʟᴀɴ</green>","<gray>Create your own clan.");
        set(inv,13,Material.COMPASS,"<aqua>ᴊᴏɪɴ ᴄʟᴀɴ</aqua>","<gray>Browse and join clans.");
        set(inv,15,Material.WRITABLE_BOOK,"<yellow>ɪɴᴠɪᴛᴀᴛɪᴏɴs</yellow>","<gray>View pending invitations.");
        set(inv,22,Material.BARRIER,"<red>ᴄʟᴏsᴇ</red>","<gray>Close menu."); p.openInventory(inv);
    }
    public void openMyClan(Player p,Clan c){
        Inventory inv=inventory(p,"clan-main",54,"<gradient:#ff3333:#8b0000>ULTRAS</gradient> <white>|</white> <gray>"+escape(c.name()));
        set(inv,4,head(Bukkit.getOfflinePlayer(c.owner())),"<red>ᴄʟᴀɴ ɪɴꜰᴏ</red>","<gray>Level: <white>"+c.level(),"<gray>CP: <white>"+String.format("%,d",c.cp()),"<gray>Members: <white>"+c.members().size()+"/"+c.memberCapacity());
        set(inv,10,Material.PLAYER_HEAD,"<aqua>ᴍᴇᴍʙᴇʀs</aqua>","<gray>Manage clan members.");
        set(inv,12,Material.PAPER,"<yellow>ʀᴇǫᴜᴇsᴛs</yellow>","<gray>Join requests.");
        set(inv,14,Material.NAME_TAG,"<green>ɪɴᴠɪᴛᴇ</green>","<gray>Invite a player.");
        set(inv,16,Material.GOLD_INGOT,"<gold>ʙᴀɴᴋ</gold>","<gray>Clan balance: <white>"+String.format("%,.2f",c.bank()));
        set(inv,20,Material.BARREL,"<dark_aqua>sᴛᴏʀᴀɢᴇ</dark_aqua>","<gray>Storage level: <white>"+c.storageLevel());
        set(inv,22,Material.ENDER_CHEST,"<light_purple>ᴜᴘɢʀᴀᴅᴇs</light_purple>","<gray>Upgrade clan systems.");
        set(inv,24,Material.ENDER_PEARL,"<aqua>ᴡᴀʀᴘs</aqua>","<gray>Clan warps.");
        set(inv,28,Material.BEACON,"<green>sᴘᴀᴡɴ</green>","<gray>Clan spawn.");
        set(inv,30,Material.DIAMOND_SWORD,"<red>ᴇᴠᴇɴᴛs</red>","<gray>Clan challenges.");
        set(inv,32,Material.FILLED_MAP,"<blue>ᴛᴀsᴋs</blue>","<gray>Daily / weekly / monthly.");
        set(inv,34,Material.HEART_OF_THE_SEA,"<light_purple>ᴀʟʟɪᴀɴᴄᴇs</light_purple>","<gray>Manage alliances.");
        set(inv,38,Material.EXPERIENCE_BOTTLE,"<green>ᴄᴘ & sᴛᴀᴛs</green>","<gray>Clan statistics.");
        set(inv,40,Material.GOLDEN_HELMET,"<gold>ᴛᴏᴘ</gold>","<gray>Clan leaderboards.");
        set(inv,42,Material.WRITABLE_BOOK,"<white>ᴄʟᴀɴ ᴄʜᴀᴛ</white>","<gray>Toggle clan chat.");
        set(inv,44,Material.COMPARATOR,"<yellow>sᴇᴛᴛɪɴɢs</yellow>","<gray>Clan settings.");
        set(inv,49,Material.SPRUCE_DOOR,"<yellow>ʟᴇᴀᴠᴇ ᴄʟᴀɴ</yellow>","<red>This requires confirmation.");
        set(inv,53,Material.BARRIER,"<red>ʙᴀᴄᴋ</red>","<gray>Close menu."); p.openInventory(inv);
    }
    public void openClans(Player p,int page,String search){
        List<Clan> list=new ArrayList<>(clans.all()); list.sort(Comparator.comparingLong(Clan::cp).reversed());
        if(search!=null&&!search.isBlank()) list.removeIf(c->!c.name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)));
        int per=45, pages=Math.max(1,(list.size()+per-1)/per), pg=Math.max(0,Math.min(page,pages-1));
        Inventory inv=inventory(p,"clans-list",54,"<gradient:#ff3333:#8b0000>ᴄʟᴀɴs</gradient> <gray>Page "+(pg+1)+"/"+pages);
        for(int i=0;i<per;i++){int idx=pg*per+i;if(idx>=list.size())break;Clan c=list.get(idx);set(inv,i,head(Bukkit.getOfflinePlayer(c.owner())),"<white>"+escape(c.name()),"<gray>CP: <white>"+String.format("%,d",c.cp()),"<gray>Rank: <white>"+clans.rank(c).name(),"<gray>Members: <white>"+c.members().size()+"/"+c.memberCapacity(),"<gray>Status: "+(c.open()?"<green>Public":"<red>Private"));}
        set(inv,45,Material.ARROW,"<yellow>ᴘʀᴇᴠɪᴏᴜs</yellow>"); set(inv,49,Material.COMPASS,"<aqua>sᴇᴀʀᴄʜ</aqua>","<gray>Use <white>/clans <name></white>."); set(inv,53,Material.ARROW,"<yellow>ɴᴇxᴛ</yellow>"); p.openInventory(inv);
    }
    public void openClanInfo(Player p,Clan c){
        Inventory inv=inventory(p,"clan-info:"+c.id(),27,"<gradient:#ff3333:#8b0000>ᴄʟᴀɴ ɪɴꜰᴏ</gradient>");
        set(inv,4,head(Bukkit.getOfflinePlayer(c.owner())),"<white>"+escape(c.name()),"<gray>Owner: <white>"+Bukkit.getOfflinePlayer(c.owner()).getName(),"<gray>Level: <white>"+c.level(),"<gray>Rank: <white>"+clans.rank(c).name(),"<gray>CP: <white>"+String.format("%,d",c.cp()),"<gray>Members: <white>"+c.members().size()+"/"+c.memberCapacity(),"<gray>Online: <white>"+online(c));
        set(inv,11,c.open()?Material.LIME_DYE:Material.GRAY_DYE,c.open()?"<green>ᴊᴏɪɴ</green>":"<yellow>ʀᴇǫᴜᴇsᴛ ᴊᴏɪɴ</yellow>");
        set(inv,13,Material.PLAYER_HEAD,"<aqua>ᴍᴇᴍʙᴇʀs</aqua>"); set(inv,15,Material.FILLED_MAP,"<green>sᴛᴀᴛs</green>"); set(inv,22,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>"); p.openInventory(inv);
    }
    public void openMembers(Player p,Clan c){
        Inventory inv=inventory(p,"members",54,"<gradient:#ff3333:#8b0000>ᴍᴇᴍʙᴇʀs</gradient>"); int i=0;
        for(ClanMember m:c.members().values()){if(i>=45)break;String online=Bukkit.getPlayer(m.uuid())!=null?"<green>Online":"<gray>Offline";set(inv,i,head(Bukkit.getOfflinePlayer(m.uuid())),"<white>"+escape(m.name()),"<gray>Role: <white>"+m.rank(),"<gray>Level: <white>"+m.level(),"<gray>Contribution: <white>"+String.format("%,d",m.contribution()),online);i++;}
        set(inv,49,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>"); p.openInventory(inv);
    }
    public void openBank(Player p,Clan c){
        Inventory inv=inventory(p,"bank",27,"<gold>ᴄʟᴀɴ ʙᴀɴᴋ</gold>"); set(inv,4,Material.GOLD_BLOCK,"<gold>ʙᴀʟᴀɴᴄᴇ</gold>","<white>"+String.format("%,.2f",c.bank())+" / "+String.format("%,.2f",c.bankCapacity())); set(inv,11,Material.CHEST,"<green>ᴅᴇᴘᴏsɪᴛ</green>");set(inv,15,Material.HOPPER,"<yellow>ᴡɪᴛʜᴅʀᴀᴡ</yellow>");set(inv,13,Material.ANVIL,"<aqua>ᴜᴘɢʀᴀᴅᴇ</aqua>");set(inv,22,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv); }
    public void openTop(Player p){Inventory inv=inventory(p,"top",54,"<gold>ᴄʟᴀɴ ᴛᴏᴘ</gold>");List<Clan> l=new ArrayList<>(clans.all());l.sort(Comparator.comparingLong(Clan::cp).reversed());for(int i=0;i<Math.min(45,l.size());i++){Clan c=l.get(i);set(inv,i,head(Bukkit.getOfflinePlayer(c.owner())),"<gold>#"+(i+1)+" <white>"+escape(c.name()),"<gray>CP: <white>"+String.format("%,d",c.cp()),"<gray>Level: <white>"+c.level(),"<gray>Members: <white>"+c.members().size());}set(inv,49,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    public void openTasks(Player p,Clan c){openTaskPeriod(p,c,"daily");}
    private void openTaskPeriod(Player p,Clan c,String period){Inventory inv=inventory(p,"tasks:"+period,54,"<blue>ᴄʟᴀɴ ᴛᴀsᴋs</blue> <gray>"+period);int i=0;for(var t:tasks.list(c,period)){long pct=Math.min(100,(t.progress*100)/Math.max(1,t.target));String bar="<green>"+"█".repeat((int)(pct/10))+"<gray>"+"░".repeat(10-(int)(pct/10))+"</gray>";set(inv,i, t.claimed?Material.LIME_DYE:(t.completed?Material.EMERALD:Material.PAPER),"<white>"+t.description,"<gray>Progress: "+bar+" <white>"+pct+"%","<gray>Reward: <yellow>⚔ "+t.reward+" CP</yellow>",t.completed&&!t.claimed?"<green>Click to claim.":"<gray>Shared clan progress.");i++;}set(inv,45,Material.SUNFLOWER,"<yellow>ᴅᴀɪʟʏ</yellow>");set(inv,47,Material.CLOCK,"<aqua>ᴡᴇᴇᴋʟʏ</aqua>");set(inv,49,Material.NETHER_STAR,"<light_purple>ᴍᴏɴᴛʜʟʏ</light_purple>");set(inv,53,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    public void openSettings(Player p,Clan c){Inventory inv=inventory(p,"settings",36,"<yellow>ᴄʟᴀɴ sᴇᴛᴛɪɴɢs</yellow>");set(inv,10,Material.NAME_TAG,"<aqua>ʀᴇɴᴀᴍᴇ</aqua>");set(inv,12,c.open()?Material.LIME_DYE:Material.GRAY_DYE,"<green>ᴘᴜʙʟɪᴄ / ᴘʀɪᴠᴀᴛᴇ</green>","<gray>Current: "+(c.open()?"<green>Public":"<red>Private"));set(inv,14,Material.WHITE_DYE,"<white>ᴄʟᴀɴ ᴄᴏʟᴏʀ</white>");set(inv,16,Material.NAME_TAG,"<gold>ʀᴀɴᴋs</gold>");set(inv,31,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    private Inventory inventory(Player p,String id,int size,String title){String base=id.contains(":")?id.substring(0,id.indexOf(":")):id;String configured=guiConfig.getString("titles."+base);if(configured!=null)title=configured;int configuredSize=guiConfig.getInt("sizes."+base,size);if(configuredSize%9==0&&configuredSize>=9&&configuredSize<=54)size=configuredSize;return Bukkit.createInventory(new GuiSession(p.getUniqueId(),id),size,mm.deserialize(title));}
    private void set(Inventory inv,int slot,Material mat,String name,String... lore){ItemStack it=new ItemStack(mat);ItemMeta meta=it.getItemMeta();meta.displayName(mm.deserialize(name));if(lore.length>0)meta.lore(Arrays.stream(lore).map(mm::deserialize).toList());it.setItemMeta(meta);inv.setItem(slot,it);}
    private void set(Inventory inv,int slot,ItemStack it,String name,String... lore){ItemMeta meta=it.getItemMeta();meta.displayName(mm.deserialize(name));if(lore.length>0)meta.lore(Arrays.stream(lore).map(mm::deserialize).toList());it.setItemMeta(meta);inv.setItem(slot,it);}
    private ItemStack head(OfflinePlayer op){ItemStack it=new ItemStack(Material.PLAYER_HEAD);SkullMeta m=(SkullMeta)it.getItemMeta();m.setOwningPlayer(op);it.setItemMeta(m);return it;}
    private String escape(String s){return s.replace("<","&lt;").replace(">","&gt;");}
    private int online(Clan c){int n=0;for(UUID id:c.members().keySet())if(Bukkit.getPlayer(id)!=null)n++;return n;}
    public void click(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p) || !(e.getInventory().getHolder() instanceof GuiSession s))return;
        if(!s.player().equals(p.getUniqueId()))return; int slot=e.getRawSlot(); if(slot<0||slot>=e.getView().getTopInventory().getSize())return;
        String id=s.id(); Clan c=clans.of(p.getUniqueId()); if(id.startsWith("storage:")){ if(c==null||!permissions.has(p,c,"CLAN_STORAGE_OPEN")){e.setCancelled(true);return;} if(slot==49){e.setCancelled(true);openMyClan(p,c);} return; } e.setCancelled(true);
        if(id.equals("pre-clan")){if(slot==11){p.closeInventory();p.sendMessage(Component.text("Type your clan name in chat. Type cancel to abort."));prompts.set(p.getUniqueId(), new me.uc.hussein.ultrasclans.service.PromptService.Prompt(me.uc.hussein.ultrasclans.service.PromptService.Type.CREATE_CLAN,null,false));}else if(slot==13)openClans(p,0,"");else if(slot==22)p.closeInventory();return;}
        if(id.equals("clans-list")){if(slot==45)openClans(p,0,""); else if(slot==53)openClans(p,1,""); else if(slot<45){List<Clan> l=new ArrayList<>(clans.all());l.sort(Comparator.comparingLong(Clan::cp).reversed());if(slot<l.size())openClanInfo(p,l.get(slot));}return;}
        if(id.startsWith("clan-info:")){if(slot==22){openClans(p,0,"");return;}UUID targetId;try{targetId=UUID.fromString(id.substring(10));}catch(Exception ex){return;}Clan target=clans.byId(targetId);if(target==null)return;if(c!=null){p.sendMessage(mm.deserialize("<red>You are already in a clan.</red>"));return;}if(slot==11){if(target.open())clans.join(target,p.getUniqueId(),p.getName(),false).thenAccept(ok->plugin.getServer().getScheduler().runTask(plugin,()->p.sendMessage(mm.deserialize(ok?"<green>You joined "+target.name()+".</green>":"<red>Could not join.</red>"))));else requests.request(target,p).thenAccept(ok->plugin.getServer().getScheduler().runTask(plugin,()->p.sendMessage(mm.deserialize(ok?"<green>Join request sent.</green>":"<red>Could not send request.</red>"))));}return;}
        if(c==null)return;
        switch(id){
            case "clan-main" -> { if(slot==10)openMembers(p,c); else if(slot==14)openInvitePrompt(p,c); else if(slot==16)openBank(p,c); else if(slot==12){} else if(slot==18){} else if(slot==22){} else if(slot==20)openStorage(p,c,1); else if(slot==24)openWarps(p,c); else if(slot==28)openSpawn(p,c); else if(slot==30){} else if(slot==32)openTasks(p,c); else if(slot==34)openAlliances(p,c); else if(slot==38)openStats(p,c); else if(slot==40)openTop(p); else if(slot==42){} else if(slot==44)openSettings(p,c); else if(slot==49){} else if(slot==53)p.closeInventory(); }
            case "members" -> {if(slot==49)openMyClan(p,c);else if(slot<45){List<ClanMember> ms=new ArrayList<>(c.members().values());if(slot<ms.size())openMemberInfo(p,c,ms.get(slot));}}
            case "member-info" -> {if(slot==26)openMembers(p,c);}
            case "bank" -> {if(slot==11)openBankAmount(p,c,true);else if(slot==15)openBankAmount(p,c,false);else if(slot==22)openMyClan(p,c);}
            case "settings" -> {if(slot==31)openMyClan(p,c);else if(slot==12){c.setOpen(!c.open());plugin.getServer().getScheduler().runTaskAsynchronously(plugin,()->{});openSettings(p,c);} }
            case "tasks:daily","tasks:weekly","tasks:monthly" -> {String period=id.substring(6);if(slot==53)openMyClan(p,c);else if(slot==45)openTaskPeriod(p,c,"daily");else if(slot==47)openTaskPeriod(p,c,"weekly");else if(slot==49)openTaskPeriod(p,c,"monthly");else if(slot<45){var list=tasks.list(c,period);if(slot<list.size()){var t=list.get(slot);if(t.completed&&!t.claimed){tasks.claim(c,period,t.id);openTaskPeriod(p,c,period);}}}}
            case "top" -> {if(slot==49)openMyClan(p,c);}
            case "stats" -> {if(slot==26)openMyClan(p,c);}
            case "storage:1" -> {if(slot==49)openMyClan(p,c);}
            case "warps" -> {if(slot==49)openMyClan(p,c);}
            case "alliances" -> {if(slot==49)openMyClan(p,c);}
        }
    }
    private void openMemberInfo(Player p,Clan c,ClanMember m){Inventory inv=inventory(p,"member-info",36,"<aqua>ᴍᴇᴍʙᴇʀ</aqua> <white>"+escape(m.name()));set(inv,4,head(Bukkit.getOfflinePlayer(m.uuid())),"<white>"+escape(m.name()),"<gray>Role: <white>"+m.rank(),"<gray>Level: <white>"+m.level(),"<gray>XP: <white>"+m.xp(),"<gray>Contribution: <white>"+m.contribution());if(!m.uuid().equals(c.owner())){set(inv,12,Material.IRON_BOOTS,"<red>ᴋɪᴄᴋ</red>");set(inv,14,Material.BARRIER,"<dark_red>ʙᴀɴ</dark_red>");set(inv,16,Material.ARROW,"<yellow>ᴅᴇᴍᴏᴛᴇ</yellow>");}set(inv,31,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    private void openStats(Player p,Clan c){Inventory inv=inventory(p,"stats",36,"<green>ᴄʟᴀɴ sᴛᴀᴛs</green>");set(inv,4,Material.EXPERIENCE_BOTTLE,"<white>"+escape(c.name()),"<gray>CP: <white>"+c.cp(),"<gray>Level: <white>"+c.level(),"<gray>Rank: <white>"+clans.rank(c).name(),"<gray>Members: <white>"+c.members().size());set(inv,31,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    private void openStorage(Player p,Clan c,int page){if(page>c.storageLevel()){p.sendMessage(mm.deserialize("<red>This storage level is locked.</red>"));return;}Inventory inv=inventory(p,"storage:"+page,54,"<dark_aqua>ᴄʟᴀɴ sᴛᴏʀᴀɢᴇ</dark_aqua> <gray>"+page);storage.load(inv,c.id().toString(),page);set(inv,49,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    private void openWarps(Player p,Clan c){Inventory inv=inventory(p,"warps",27,"<aqua>ᴄʟᴀɴ ᴡᴀʀᴘs</aqua>");for(int i=0;i<c.warpCapacity();i++)set(inv,10+i,Material.ENDER_PEARL,"<aqua>ᴡᴀʀᴘ #"+(i+1)+"</aqua>","<gray>Click to use.");set(inv,22,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    private void openSpawn(Player p,Clan c){p.closeInventory();p.performCommand("clan spawn");}
    private void openAlliances(Player p,Clan c){Inventory inv=inventory(p,"alliances",36,"<light_purple>ᴀʟʟɪᴀɴᴄᴇs</light_purple>");set(inv,31,Material.ARROW,"<yellow>ʙᴀᴄᴋ</yellow>");p.openInventory(inv);}
    private void openInvitePrompt(Player p,Clan c){p.closeInventory();p.sendMessage(mm.deserialize("<yellow>Type a player name in chat to invite, or cancel.</yellow>"));prompts.set(p.getUniqueId(), new me.uc.hussein.ultrasclans.service.PromptService.Prompt(me.uc.hussein.ultrasclans.service.PromptService.Type.INVITE_PLAYER,c.id(),false));}
    private void openBankAmount(Player p,Clan c,boolean deposit){p.closeInventory();p.sendMessage(mm.deserialize("<yellow>Type amount in chat, or cancel.</yellow>"));prompts.set(p.getUniqueId(), new me.uc.hussein.ultrasclans.service.PromptService.Prompt(me.uc.hussein.ultrasclans.service.PromptService.Type.BANK_AMOUNT,c.id(),deposit));}
}
