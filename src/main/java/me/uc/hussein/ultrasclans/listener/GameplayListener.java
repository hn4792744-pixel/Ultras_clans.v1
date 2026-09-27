package me.uc.hussein.ultrasclans.listener;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.model.ClanMember;
import me.uc.hussein.ultrasclans.service.*;
import me.uc.hussein.ultrasclans.message.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public final class GameplayListener implements Listener {
    private final ClanService clans; private final PointsService points; private final Messages messages; private final TaskService tasks; private final TeleportService teleports; private final StorageService storage; private final EventService events;
    public GameplayListener(ClanService c,PointsService p,Messages m,TaskService t,TeleportService tp,StorageService storage,EventService events){clans=c;points=p;messages=m;tasks=t;teleports=tp;this.storage=storage;this.events=events;}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void death(PlayerDeathEvent e){Player victim=e.getPlayer();events.death(victim);Clan vc=clans.of(victim.getUniqueId());if(vc!=null){ClanMember m=vc.member(victim.getUniqueId());if(m!=null)m.addDeaths(1);}
        Player killer=victim.getKiller();if(killer!=null){Clan kc=clans.of(killer.getUniqueId());Clan vc2=clans.of(victim.getUniqueId());long reward=points.killReward(killer,victim);if(kc!=null&&vc2!=null&&!kc.id().equals(vc2.id())){String raw=messages.get("member-kill").replace("{attackerClan}",kc.name()).replace("{attacker}",killer.getName()).replace("{victimClan}",vc2.name()).replace("{victim}",victim.getName());e.deathMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(raw));if(reward>0)killer.sendMessage(messages.component("cp-gain",Map.of("amount",String.valueOf(reward))));tasks.progress(kc,"kills",1);}}}
    @EventHandler(ignoreCancelled=true) public void breakBlock(BlockBreakEvent e){if(events.isActive(e.getPlayer()))events.blockBreak(e.getBlock());Player p=e.getPlayer();Clan c=clans.of(p.getUniqueId());if(c==null)return;ClanMember m=c.member(p.getUniqueId());if(m!=null)m.addBlocksBroken(1);if(e.getBlock().getType().name().contains("ORE")){tasks.progress(c,"ores",1);}tasks.progress(c,"blocks",1);}
    @EventHandler(ignoreCancelled=true) public void placeBlock(BlockPlaceEvent e){if(events.isActive(e.getPlayer()))events.blockPlace(e.getBlockPlaced());Player p=e.getPlayer();Clan c=clans.of(p.getUniqueId());if(c==null)return;ClanMember m=c.member(p.getUniqueId());if(m!=null)m.addBlocksPlaced(1);tasks.progress(c,"blocks",1);}
    @EventHandler public void move(PlayerMoveEvent e){if(e.getTo()==null)return;teleports.movement(e.getPlayer());}
    @EventHandler public void join(PlayerJoinEvent e){events.rejoin(e.getPlayer());Clan c=clans.of(e.getPlayer().getUniqueId());if(c!=null){ClanMember m=c.member(e.getPlayer().getUniqueId());if(m!=null)m.setLastSeen(System.currentTimeMillis());}}
    @EventHandler public void quit(PlayerQuitEvent e){events.quit(e.getPlayer());Clan c=clans.of(e.getPlayer().getUniqueId());if(c!=null){ClanMember m=c.member(e.getPlayer().getUniqueId());if(m!=null)m.setLastSeen(System.currentTimeMillis());}}
}
