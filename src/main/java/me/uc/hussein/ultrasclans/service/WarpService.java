package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.*;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.*;

public final class WarpService {
    private final ClanRepository repo; private final Map<UUID,List<ClanWarp>> cache=new ConcurrentHashMap<>();
    public WarpService(ClanRepository repo){this.repo=repo;}
    public void load(Collection<Clan> clans){for(Clan c:clans)load(c.id());}
    public void load(UUID clan){repo.dbListWarps(clan).thenAccept(list->cache.put(clan,new ArrayList<>(list)));}
    public List<ClanWarp> list(UUID clan){return Collections.unmodifiableList(cache.getOrDefault(clan,List.of()));}
    public CompletableFuture<Boolean> save(UUID clanId,Location loc){List<ClanWarp> l=new ArrayList<>(cache.getOrDefault(clanId,List.of()));int id=1;while(containsId(l,id))id++;if(id>5)return CompletableFuture.completedFuture(false);ClanWarp w=new ClanWarp(id,"#"+id,new LocationData(loc.getWorld().getName(),loc.getX(),loc.getY(),loc.getZ(),loc.getYaw(),loc.getPitch()));l.add(w);cache.put(clanId,l);return repo.dbSaveWarp(clanId,w);}
    public CompletableFuture<Boolean> remove(UUID clanId,int id){List<ClanWarp> l=new ArrayList<>(cache.getOrDefault(clanId,List.of()));if(l.removeIf(w->w.id()==id)){cache.put(clanId,l);return repo.dbDeleteWarp(clanId,id);}return CompletableFuture.completedFuture(false);}
    private boolean containsId(List<ClanWarp> list,int id){for(ClanWarp w:list)if(w.id()==id)return true;return false;}
    public Location resolve(ClanWarp w){World world=Bukkit.getWorld(w.location().world());return world==null?null:new Location(world,w.location().x(),w.location().y(),w.location().z(),w.location().yaw(),w.location().pitch());}
}
