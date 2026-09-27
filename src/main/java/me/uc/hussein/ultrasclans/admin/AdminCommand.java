package me.uc.hussein.ultrasclans.admin;

import me.uc.hussein.ultrasclans.gui.GuiManager;
import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.service.*;
import me.uc.hussein.ultrasclans.message.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final ClanService clans; private final GuiManager gui; private final Messages messages; private final EconomyService economy; private final SpawnService spawns; private final WarpService warps;
    public AdminCommand(ClanService c,GuiManager g,Messages m,EconomyService e,SpawnService s,WarpService w){clans=c;gui=g;messages=m;economy=e;spawns=s;warps=w;}
    @Override public boolean onCommand(CommandSender s,Command cmd,String label,String[] a){if(!s.hasPermission("ultras.clans.admin")){messages.send(s,"no-permission");return true;}if(a.length<2||!a[0].equalsIgnoreCase("clan")||!a[1].equalsIgnoreCase("admin")){s.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/uc clan admin ...</yellow>"));return true;}if(a.length==2){if(s instanceof Player p)gui.openTop(p);return true;}String sub=a[2].toLowerCase(Locale.ROOT);try{
        switch(sub){
            case "reload" -> {Bukkit.getPluginManager().getPlugin("Ultras_clans").reloadConfig();messages.load();messages.send(s,"reload");}
            case "list" -> {s.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Clans: <white>"+clans.all().size()+"</white></gold>"));for(Clan c:clans.all())s.sendMessage(MiniMessage.miniMessage().deserialize("<gray>-</gray> <white>"+c.name()+"</white> <gray>"+c.cp()+" CP</gray>"));}
            case "inspect","open" -> {if(a.length<4){s.sendMessage("/uc clan admin inspect <clan>");return true;}Clan c=clans.byName(a[3]);if(c==null){messages.send(s,"not-found");return true;}if(s instanceof Player p)gui.openClanInfo(p,c);}
            case "remove","delete" -> {if(a.length<4)return true;if(a[3].equalsIgnoreCase("all")){for(Clan c:new ArrayList<>(clans.all()))clans.delete(c);s.sendMessage("All clans removed.");}else{Clan c=clans.byName(a[3]);if(c!=null)clans.delete(c);}}
            case "reset" -> {if(a.length<4)return true;if(a[3].equalsIgnoreCase("all")){for(Clan c:clans.all())reset(c);}else{Clan c=clans.byName(a[3]);if(c!=null)reset(c);}}
            case "add" -> {if(a.length<5)return true;Player p=Bukkit.getPlayerExact(a[3]);Clan c=clans.byName(a[4]);if(p!=null&&c!=null)clans.join(c,p.getUniqueId(),p.getName(),true);}
            case "remove-member" -> {if(a.length<4)return true;Player p=Bukkit.getPlayerExact(a[3]);if(p!=null){Clan c=clans.of(p.getUniqueId());if(c!=null&&!c.owner().equals(p.getUniqueId()))clans.kick(c,p.getUniqueId());}}
            case "transfer" -> {if(a.length<5)return true;Clan c=clans.byName(a[3]);Player p=Bukkit.getPlayerExact(a[4]);if(c!=null&&p!=null&&c.hasMember(p.getUniqueId()))clans.transfer(c,c.owner(),p.getUniqueId());}
            case "points" -> {if(a.length<6)return true;Clan c=clans.byName(a[4]);long n=Long.parseLong(a[5]);if(c!=null){if(a[3].equalsIgnoreCase("remove"))n=-n;clans.addCp(c,n,"ADMIN",s instanceof Player p?p.getUniqueId():null);}}
            case "level" -> {if(a.length<6)return true;Clan c=clans.byName(a[4]);if(c!=null)c.setLevel(Integer.parseInt(a[5]));if(c!=null)clans.save(c);}
            case "bank" -> {if(a.length<6)return true;Clan c=clans.byName(a[4]);if(c==null)return true;double n=Double.parseDouble(a[5]);switch(a[3].toLowerCase(Locale.ROOT)){case "add"->c.setBank(Math.min(c.bankCapacity(),c.bank()+n));case "remove"->c.setBank(Math.max(0,c.bank()-n));case "set"->c.setBank(n);default-> {return true;}}clans.save(c);}
            case "bank-capacity" -> {if(a.length<5)return true;Clan c=clans.byName(a[3]);if(c!=null){c.setBankCapacity(Double.parseDouble(a[4]));clans.save(c);}}
            case "storage" -> {if(a.length<5)return true;Clan c=clans.byName(a[3]);if(c!=null){c.setStorageLevel(Integer.parseInt(a[4]));clans.save(c);}}
            case "members-capacity" -> {if(a.length<5)return true;Clan c=clans.byName(a[3]);if(c!=null){c.setMemberCapacity(Integer.parseInt(a[4]));clans.save(c);}}
            case "spawn" -> {if(a.length>=5&&a[3].equalsIgnoreCase("set")&&s instanceof Player p&&a.length>=5){Clan c=clans.byName(a[4]);if(c!=null)spawns.set(c.id(),p.getLocation());}}
            case "event" -> {if(s instanceof Player p && a.length>=4){String which=a[3].toLowerCase(Locale.ROOT);if(which.equals("setevent1")||which.equals("setevent2")){String key="events."+which.substring(3);getConfigWrite(key,p);p.sendMessage("Event spawn set.");}else if(which.equals("setrest1")||which.equals("setrest2")){getConfigWrite(which.equals("setrest1")?"events.rest1":"events.rest2",p);p.sendMessage("Event restore point set.");}}}
            case "warp" -> {if(a.length>=5&&a[3].equalsIgnoreCase("add")&&s instanceof Player p){Clan c=clans.byName(a[4]);if(c!=null)warps.save(c.id(),p.getLocation());}}
            default -> s.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Admin command not recognized.</gray>"));
        }}catch(Exception ex){s.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid admin arguments.</red>"));}return true;}
    private void getConfigWrite(String key,Player p){String path=key;((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).getConfig().set(path+".world",p.getWorld().getName());((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).getConfig().set(path+".x",p.getX());((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).getConfig().set(path+".y",p.getY());((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).getConfig().set(path+".z",p.getZ());((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).getConfig().set(path+".yaw",p.getYaw());((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).getConfig().set(path+".pitch",p.getPitch());((org.bukkit.plugin.java.JavaPlugin)Bukkit.getPluginManager().getPlugin("Ultras_clans")).saveConfig();}
    private void reset(Clan c){c.setCp(0);c.setLevel(1);c.setBank(0);c.setStorageLevel(1);c.setWarpCapacity(1);c.setAllianceCapacity(1);c.setMemberCapacity(10);for(var m:c.members().values()){m.setLevel(1);m.setXp(0);m.setContribution(0);}clans.save(c);}
    @Override public List<String> onTabComplete(CommandSender s,Command cmd,String label,String[] a){if(!s.hasPermission("ultras.clans.admin"))return List.of();if(a.length<=2)return List.of("clan");if(a.length==3)return List.of("admin");if(a.length==4)return List.of("reload","restart","list","logs","inspect","open","remove","reset","add","remove-member","transfer","points","level","rank","bank","bank-capacity","storage","members-capacity","warp","spawn","event","force-challenge","ban","unban","chatban","chatunban");return List.of();}
}
