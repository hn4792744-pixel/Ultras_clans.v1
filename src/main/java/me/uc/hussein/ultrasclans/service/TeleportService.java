package me.uc.hussein.ultrasclans.service;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportService {
    public record Pending(UUID player,Location target,Location origin,int seconds,boolean protect){ }
    private final JavaPlugin plugin; private final Map<UUID,Pending> pending=new ConcurrentHashMap<>();
    public TeleportService(JavaPlugin plugin){this.plugin=plugin;}
    public boolean isPending(UUID p){return pending.containsKey(p);}
    public void cancel(UUID p,String reason){pending.remove(p);if(reason!=null){Player pl=plugin.getServer().getPlayer(p);if(pl!=null)pl.sendActionBar(net.kyori.adventure.text.Component.text(reason));}}
    public void start(Player p,Location target,int seconds,boolean protect){cancel(p.getUniqueId(),null);Pending x=new Pending(p.getUniqueId(),target,p.getLocation().clone(),seconds,protect);pending.put(p.getUniqueId(),x);new BukkitRunnable(){int left=seconds;@Override public void run(){Player pl=plugin.getServer().getPlayer(p.getUniqueId());if(pl==null){pending.remove(p.getUniqueId());cancel();return;}Pending cur=pending.get(p.getUniqueId());if(cur==null){cancel();return;}if(left<=0){pending.remove(p.getUniqueId());pl.teleportAsync(target).thenAccept(ok->{if(ok&&protect)plugin.getServer().getScheduler().runTaskLater(plugin,()->{},40L);});pl.sendActionBar(net.kyori.adventure.text.Component.text("Teleported."));cancel();return;}pl.sendActionBar(net.kyori.adventure.text.Component.text("Teleporting in "+left+"…"));left--;}}.runTaskTimer(plugin,0L,20L);}
    public void movement(Player p){if(pending.containsKey(p.getUniqueId())){Pending x=pending.get(p.getUniqueId());Location a=x.origin(),b=p.getLocation();if(a.getWorld()!=b.getWorld()||a.distanceSquared(b)>0.04)cancel(p.getUniqueId(),"Teleport cancelled: movement.");}}
    public void damage(Player p){if(pending.containsKey(p.getUniqueId()))cancel(p.getUniqueId(),"Teleport cancelled: damage.");}
}
