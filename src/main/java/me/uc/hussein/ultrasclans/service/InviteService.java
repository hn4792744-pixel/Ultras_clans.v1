package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.*;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class InviteService {
    private final ClanRepository repo; private final ClanService clans; private final long expiry;
    public InviteService(ClanRepository repo,ClanService clans,long expirySeconds){this.repo=repo;this.clans=clans;this.expiry=expirySeconds*1000L;}
    public CompletableFuture<Boolean> invite(Clan clan,Player sender,Player target){
        if(clan==null||sender==null||target==null||clans.of(target.getUniqueId())!=null)return CompletableFuture.completedFuture(false);
        long now=System.currentTimeMillis();Invitation i=new Invitation(UUID.randomUUID(),clan.id(),sender.getUniqueId(),target.getUniqueId(),now,now+expiry);
        return repoSave(i);
    }
    private CompletableFuture<Boolean> repoSave(Invitation i){return repo.dbSaveInvitation(i);}
    public CompletableFuture<List<Invitation>> list(UUID player){return repo.dbListInvitations(player);}
    public CompletableFuture<Boolean> remove(UUID id){return repo.dbDeleteInvitation(id);}
    public CompletableFuture<Boolean> accept(Invitation i,Player p){Clan c=clans.byId(i.clanId());if(c==null||clans.of(p.getUniqueId())!=null)return CompletableFuture.completedFuture(false);return clans.join(c,p.getUniqueId(),p.getName(),false).thenCompose(ok->ok?remove(i.id()).thenApply(x->true):CompletableFuture.completedFuture(false));}
}
