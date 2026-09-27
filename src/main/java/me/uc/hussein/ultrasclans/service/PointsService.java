package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.model.ClanMember;
import me.uc.hussein.ultrasclans.message.Messages;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PointsService {
    private final ClanService clans; private final Messages messages;
    private final Map<String,List<Long>> kills=new ConcurrentHashMap<>(); private final Map<UUID,Long> daily=new ConcurrentHashMap<>();
    public PointsService(ClanService clans,Messages messages){this.clans=clans;this.messages=messages;}
    public long killReward(Player killer,Player victim){Clan kc=clans.of(killer.getUniqueId()),vc=clans.of(victim.getUniqueId());if(kc==null||vc==null||kc.id().equals(vc.id()))return 0;String key=killer.getUniqueId()+":"+victim.getUniqueId();long now=System.currentTimeMillis();List<Long> times=kills.computeIfAbsent(key,k->new ArrayList<>());synchronized(times){times.removeIf(t->now-t>600_000);long reward=times.isEmpty()?10:(times.size()==1?2:0);times.add(now);long day=daily.getOrDefault(killer.getUniqueId(),0L);if(day>=250)reward=0;else reward=Math.min(reward,250-day);ClanMember m=kc.member(killer.getUniqueId());if(m!=null)m.addKills(1);clans.save(kc);if(reward>0){daily.put(killer.getUniqueId(),day+reward);clans.addCp(kc,reward,"KILL",killer.getUniqueId());if(m!=null){m.addContribution(reward);m.addXp(reward*5);}clans.recalcLevel(kc);}return reward;}}
}
