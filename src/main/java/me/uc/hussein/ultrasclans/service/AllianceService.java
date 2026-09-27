package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AllianceService {
    private final ClanRepository repo; private final ClanService clans; private final Map<UUID,Set<UUID>> links=new ConcurrentHashMap<>(); private final Map<String,UUID> pending=new ConcurrentHashMap<>();
    public AllianceService(ClanRepository r,ClanService c){repo=r;clans=c;repo.dbLoadAlliances().thenAccept(rows->rows.forEach(x->{if(clans.byId(x[0])!=null&&clans.byId(x[1])!=null){links.computeIfAbsent(x[0],k->ConcurrentHashMap.newKeySet()).add(x[1]);links.computeIfAbsent(x[1],k->ConcurrentHashMap.newKeySet()).add(x[0]);}}));}
    public boolean allied(Clan a,Clan b){return a!=null&&b!=null&&links.getOrDefault(a.id(),Set.of()).contains(b.id());}
    public Set<UUID> list(Clan c){return Collections.unmodifiableSet(links.getOrDefault(c.id(),Set.of()));}
    public boolean request(Clan a,Clan b){if(a==null||b==null||a.id().equals(b.id())||allied(a,b)||links.getOrDefault(a.id(),Set.of()).size()>=a.allianceCapacity()||links.getOrDefault(b.id(),Set.of()).size()>=b.allianceCapacity())return false;pending.put(b.id()+":"+a.id(),a.id());return true;}
    public boolean accept(Clan target,Clan requester){if(!pending.containsKey(target.id()+":"+requester.id()))return false;pending.remove(target.id()+":"+requester.id());repo.dbSaveAlliance(target.id(),requester.id());links.computeIfAbsent(target.id(),k->ConcurrentHashMap.newKeySet()).add(requester.id());links.computeIfAbsent(requester.id(),k->ConcurrentHashMap.newKeySet()).add(target.id());return true;}
    public void remove(Clan a,Clan b){if(a==null||b==null)return;links.getOrDefault(a.id(),Set.of()).remove(b.id());links.getOrDefault(b.id(),Set.of()).remove(a.id());repo.dbDeleteAlliance(a.id(),b.id());}
}
