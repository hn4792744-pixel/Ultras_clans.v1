package me.uc.hussein.ultrasclans.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.uc.hussein.ultrasclans.service.ClanService;
import org.bukkit.entity.Player;

public final class UltrasExpansion extends PlaceholderExpansion {
    private final ClanService clans;
    public UltrasExpansion(ClanService clans){this.clans=clans;}
    @Override public String getIdentifier(){return "ultras_clan";}
    @Override public String getAuthor(){return "UC_Hussein";}
    @Override public String getVersion(){return "v1";}
    @Override public boolean persist(){return true;}
    @Override public String onPlaceholderRequest(Player p,String params){if(p==null)return "";return new UltrasPlaceholderExpansion(clans).value(p,params);}
}
