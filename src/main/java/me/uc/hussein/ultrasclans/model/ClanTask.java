package me.uc.hussein.ultrasclans.model;

public final class ClanTask {
    public final String id, period, description, type;
    public final long target, reward;
    public long progress; public boolean completed, claimed;
    public ClanTask(String id,String period,String description,String type,long target,long reward){this.id=id;this.period=period;this.description=description;this.type=type;this.target=target;this.reward=reward;}
}
