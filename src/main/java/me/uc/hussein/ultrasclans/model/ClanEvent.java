package me.uc.hussein.ultrasclans.model;

import java.util.*;

public final class ClanEvent {
    public enum Status { PENDING, PREPARING, ACTIVE, FINISHED, DRAW, CANCELLED }
    public final UUID id; public final UUID clanA, clanB; public final double moneyStake; public final long cpStake; public final int roundMinutes, maxRounds;
    public Status status=Status.PENDING; public int scoreA,scoreB,round=1; public long startedAt;
    public final Set<UUID> participantsA=new HashSet<>(),participantsB=new HashSet<>(),eliminated=new HashSet<>();
    public ClanEvent(UUID id,UUID a,UUID b,double money,long cp,int minutes,int rounds){this.id=id;clanA=a;clanB=b;moneyStake=money;cpStake=cp;roundMinutes=minutes;maxRounds=rounds;}
    public boolean participant(UUID id){return participantsA.contains(id)||participantsB.contains(id);}
    public UUID clanOf(UUID p){if(participantsA.contains(p))return clanA;if(participantsB.contains(p))return clanB;return null;}
}
