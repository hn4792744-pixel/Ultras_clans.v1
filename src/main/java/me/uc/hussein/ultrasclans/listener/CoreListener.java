package me.uc.hussein.ultrasclans.listener;

import me.uc.hussein.ultrasclans.gui.GuiManager;
import me.uc.hussein.ultrasclans.gui.GuiSession;
import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.model.ClanMember;
import me.uc.hussein.ultrasclans.service.*;
import me.uc.hussein.ultrasclans.message.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class CoreListener implements Listener {
    private final JavaPlugin plugin;private final GuiManager gui;private final StorageService storage;private final PromptService prompts;private final ClanService clans;private final Messages messages;private final TeleportService teleports;private final InviteService invites;private final BankService bank;
    public CoreListener(JavaPlugin p,GuiManager g,StorageService s,PromptService pr,ClanService c,Messages m,TeleportService t,InviteService i,BankService b){plugin=p;gui=g;storage=s;prompts=pr;clans=c;messages=m;teleports=t;invites=i;bank=b;}
    @EventHandler public void click(InventoryClickEvent e){gui.click(e);}
    @EventHandler public void close(InventoryCloseEvent e){if(!(e.getPlayer() instanceof Player p))return;if(!(e.getInventory().getHolder() instanceof GuiSession s))return;if(s.id().startsWith("storage:")){Clan c=clans.of(p.getUniqueId());if(c!=null){try{int page=Integer.parseInt(s.id().substring(8));storage.save(e.getInventory(),c.id().toString(),page);}catch(Exception ignored){}}}}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true) public void chat(AsyncPlayerChatEvent e){Player p=e.getPlayer();PromptService.Prompt pr=prompts.get(p.getUniqueId());if(pr!=null){e.setCancelled(true);String text=e.getMessage().trim();prompts.remove(p.getUniqueId());if(text.equalsIgnoreCase("cancel")){p.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Cancelled.</gray>"));return;}Bukkit.getScheduler().runTask(plugin,()->handlePrompt(p,pr,text));return;}Clan c=clans.of(p.getUniqueId());if(c!=null){ClanMember m=c.member(p.getUniqueId());if(m!=null&&m.chatEnabled()&&!m.muted()){e.setCancelled(true);for(Player member:c.members().keySet().stream().map(Bukkit::getPlayer).filter(x->x!=null).toList())member.sendMessage(messages.component("chat",Map.of("clan",c.name(),"rank",m.rank(),"player",p.getName(),"message",e.getMessage())));}}}
    private void handlePrompt(Player p,PromptService.Prompt pr,String text){switch(pr.type()){case CREATE_CLAN->clans.create(p.getUniqueId(),text).thenAccept(ok->Bukkit.getScheduler().runTask(plugin,()->messages.send(p,ok?"created":"operation-failed",Map.of("clan",text))));case INVITE_PLAYER->{Clan c=clans.byId(pr.clanId());Player target=Bukkit.getPlayerExact(text);if(c==null||target==null){messages.send(p,"no-player");return;}invites.invite(c,p,target).thenAccept(ok->Bukkit.getScheduler().runTask(plugin,()->messages.send(p,ok?"invited":"operation-failed",Map.of("player",target.getName(),"clan",c.name()))));}case BANK_AMOUNT->{Clan c=clans.byId(pr.clanId());if(c==null)return;try{double amount=Double.parseDouble(text);boolean ok=pr.flag()?bank.withdraw(p,c,amount):bank.deposit(p,c,amount);messages.send(p,ok?(pr.flag()?"withdraw":"deposit"):"operation-failed",Map.of("amount",String.valueOf(amount)));}catch(Exception ignored){messages.send(p,"operation-failed");}}default->{}}}
    @EventHandler(ignoreCancelled=true) public void damage(EntityDamageEvent e){if(e.getEntity() instanceof Player p)teleports.damage(p);}
}
