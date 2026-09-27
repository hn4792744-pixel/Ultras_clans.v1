package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.*;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;

public final class RequestService {
    private final ClanRepository repo; private final ClanService clans; private final long expiry;
    public RequestService(ClanRepository repo,ClanService clans,long expirySeconds){this.repo=repo;this.clans=clans;this.expiry=expirySeconds*1000L;}
    public CompletableFuture<Boolean> request(Clan clan,Player player){
        if(clan==null||player==null||clans.of(player.getUniqueId())!=null||clan.open())return CompletableFuture.completedFuture(false);
        long now=System.currentTimeMillis();return repo.dbSaveRequest(new JoinRequest(UUID.randomUUID(),clan.id(),player.getUniqueId(),now,now+expiry));
    }
    public CompletableFuture<List<JoinRequest>> list(Clan clan){return repo.dbListRequests(clan.id());}
    public CompletableFuture<Boolean> remove(UUID id){return repo.dbDeleteRequest(id);}
    public CompletableFuture<Boolean> accept(JoinRequest r,Player actor){
        Clan c=clans.byId(r.clanId());if(c==null||!c.hasMember(actor.getUniqueId()))return CompletableFuture.completedFuture(false);
        Player target=actor.getServer().getPlayer(r.player());if(target==null)return CompletableFuture.completedFuture(false);
        return clans.join(c,target.getUniqueId(),target.getName(),false).thenCompose(ok->ok?remove(r.id()).thenApply(x->true):CompletableFuture.completedFuture(false));
    }
}
