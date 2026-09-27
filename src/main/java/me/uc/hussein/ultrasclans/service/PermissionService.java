package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.model.ClanMember;
import org.bukkit.entity.Player;

import java.util.*;

public final class PermissionService {
    private final Map<String, Set<String>> defaults = new HashMap<>();
    public PermissionService() {
        Set<String> owner = all();
        defaults.put("owner", owner);
        defaults.put("coleader", Set.of("CLAN_VIEW","CLAN_INVITE","CLAN_KICK","CLAN_BAN","CLAN_MUTE","CLAN_PROMOTE","CLAN_DEMOTE","CLAN_BANK_DEPOSIT","CLAN_BANK_WITHDRAW","CLAN_BANK_UPGRADE","CLAN_STORAGE_OPEN","CLAN_STORAGE_DEPOSIT","CLAN_STORAGE_WITHDRAW","CLAN_STORAGE_UPGRADE","CLAN_WARP_USE","CLAN_WARP_CREATE","CLAN_WARP_DELETE","CLAN_SPAWN_USE","CLAN_SPAWN_SET","CLAN_ALLIANCE_VIEW","CLAN_ALLIANCE_MANAGE","CLAN_EVENT_CREATE","CLAN_EVENT_ACCEPT","CLAN_CHAT","CLAN_TAG","CLAN_UPGRADES","CLAN_TASKS","CLAN_STATS","CLAN_SETTINGS"));
        defaults.put("general", new HashSet<>(defaults.get("coleader")));
        defaults.put("officer", Set.of("CLAN_VIEW","CLAN_INVITE","CLAN_KICK","CLAN_BANK_DEPOSIT","CLAN_STORAGE_OPEN","CLAN_STORAGE_DEPOSIT","CLAN_STORAGE_WITHDRAW","CLAN_WARP_USE","CLAN_CHAT","CLAN_TAG","CLAN_TASKS","CLAN_STATS"));
        defaults.put("veteran", Set.of("CLAN_VIEW","CLAN_INVITE","CLAN_BANK_DEPOSIT","CLAN_STORAGE_OPEN","CLAN_STORAGE_DEPOSIT","CLAN_STORAGE_WITHDRAW","CLAN_WARP_USE","CLAN_CHAT","CLAN_TAG","CLAN_TASKS","CLAN_STATS"));
        defaults.put("member", Set.of("CLAN_VIEW","CLAN_BANK_DEPOSIT","CLAN_STORAGE_OPEN","CLAN_STORAGE_DEPOSIT","CLAN_STORAGE_WITHDRAW","CLAN_WARP_USE","CLAN_CHAT","CLAN_TAG","CLAN_TASKS","CLAN_STATS"));
        defaults.put("recruit", Set.of("CLAN_VIEW","CLAN_BANK_DEPOSIT","CLAN_STORAGE_OPEN","CLAN_STORAGE_DEPOSIT","CLAN_WARP_USE","CLAN_CHAT","CLAN_TAG","CLAN_STATS"));
    }
    public boolean has(Player player, Clan clan, String permission) {
        if (clan == null) return false;
        ClanMember m = clan.member(player.getUniqueId()); if (m == null) return false;
        if (clan.owner().equals(player.getUniqueId())) return true;
        return defaults.getOrDefault(m.rank(), Set.of()).contains(permission);
    }
    private Set<String> all() {
        return new HashSet<>(Arrays.asList("CLAN_VIEW","CLAN_INVITE","CLAN_KICK","CLAN_BAN","CLAN_MUTE","CLAN_PROMOTE","CLAN_DEMOTE","CLAN_MANAGE_RANKS","CLAN_MANAGE_PERMISSIONS","CLAN_BANK_DEPOSIT","CLAN_BANK_WITHDRAW","CLAN_BANK_UPGRADE","CLAN_STORAGE_OPEN","CLAN_STORAGE_DEPOSIT","CLAN_STORAGE_WITHDRAW","CLAN_STORAGE_UPGRADE","CLAN_WARP_USE","CLAN_WARP_CREATE","CLAN_WARP_DELETE","CLAN_SPAWN_USE","CLAN_SPAWN_SET","CLAN_ALLIANCE_VIEW","CLAN_ALLIANCE_MANAGE","CLAN_EVENT_CREATE","CLAN_EVENT_ACCEPT","CLAN_EVENT_CANCEL","CLAN_SETTINGS","CLAN_CHAT","CLAN_TAG","CLAN_UPGRADES","CLAN_TASKS","CLAN_STATS"));
    }
}
