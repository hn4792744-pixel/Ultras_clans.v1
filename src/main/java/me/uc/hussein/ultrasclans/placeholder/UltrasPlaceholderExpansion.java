package me.uc.hussein.ultrasclans.placeholder;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.service.ClanService;
import org.bukkit.entity.Player;

public final class UltrasPlaceholderExpansion {
    private final ClanService clans;
    public UltrasPlaceholderExpansion(ClanService c){clans=c;}
    public String value(Player p,String id){
        Clan c=clans.of(p.getUniqueId());
        if(c==null)return "";
        return switch(id){
            case "name" -> c.name();
            case "color" -> c.color();
            case "tag" -> c.name();
            case "rank" -> clans.rank(c).id();
            case "rank_name" -> clans.rank(c).name();
            case "cp" -> String.valueOf(c.cp());
            case "level" -> String.valueOf(c.level());
            case "members" -> String.valueOf(c.members().size());
            case "online" -> String.valueOf(c.members().keySet().stream().filter(x->p.getServer().getPlayer(x)!=null).count());
            case "bank" -> String.valueOf(c.bank());
            case "bank_capacity" -> String.valueOf(c.bankCapacity());
            case "storage_level" -> String.valueOf(c.storageLevel());
            case "warps" -> String.valueOf(c.warpCapacity());
            case "alliances" -> String.valueOf(c.allianceCapacity());
            default -> "";
        };
    }
}
