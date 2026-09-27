package me.uc.hussein.ultrasclans.command;

import me.uc.hussein.ultrasclans.gui.GuiManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public final class ClansCommand implements CommandExecutor,TabCompleter {
    private final GuiManager gui; public ClansCommand(GuiManager g){gui=g;}
    @Override public boolean onCommand(CommandSender s,Command c,String l,String[] a){if(!(s instanceof Player p))return true;gui.openClans(p,0,a.length==0?"":a[0]);return true;}
    @Override public List<String> onTabComplete(CommandSender s,Command c,String l,String[] a){return List.of();}
}
