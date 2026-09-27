package me.uc.hussein.ultrasclans.model;

import java.util.UUID;

public final class ClanMember {
    private final UUID uuid;
    private String name;
    private String rank;
    private int level = 1;
    private long xp;
    private long contribution;
    private long kills;
    private long deaths;
    private long blocksBroken;
    private long blocksPlaced;
    private long joinedAt;
    private long lastSeen;
    private boolean muted;
    private boolean banned;
    private boolean tagVisible = true;
    private boolean chatEnabled = false;

    public ClanMember(UUID uuid, String name, String rank, long now) {
        this.uuid = uuid; this.name = name; this.rank = rank; this.joinedAt = now; this.lastSeen = now;
    }
    public UUID uuid() { return uuid; }
    public String name() { return name; }
    public String rank() { return rank; }
    public int level() { return level; }
    public long xp() { return xp; }
    public long contribution() { return contribution; }
    public long kills() { return kills; }
    public long deaths() { return deaths; }
    public long blocksBroken() { return blocksBroken; }
    public long blocksPlaced() { return blocksPlaced; }
    public long joinedAt() { return joinedAt; }
    public long lastSeen() { return lastSeen; }
    public boolean muted() { return muted; }
    public boolean banned() { return banned; }
    public boolean tagVisible() { return tagVisible; }
    public boolean chatEnabled() { return chatEnabled; }
    public void setName(String v) { name=v; }
    public void setRank(String v) { rank=v; }
    public void setLevel(int v) { level=Math.max(1,v); }
    public void addXp(long v) { xp=Math.max(0,xp+v); }
    public void setXp(long v) { xp=Math.max(0,v); }
    public void addContribution(long v) { contribution=Math.max(0,contribution+v); }
    public void setContribution(long v) { contribution=Math.max(0,v); }
    public void addKills(long v) { kills=Math.max(0,kills+v); }
    public void addDeaths(long v) { deaths=Math.max(0,deaths+v); }
    public void addBlocksBroken(long v) { blocksBroken=Math.max(0,blocksBroken+v); }
    public void addBlocksPlaced(long v) { blocksPlaced=Math.max(0,blocksPlaced+v); }
    public void setLastSeen(long v) { lastSeen=v; }
    public void setMuted(boolean v) { muted=v; }
    public void setBanned(boolean v) { banned=v; }
    public void setTagVisible(boolean v) { tagVisible=v; }
    public void setChatEnabled(boolean v) { chatEnabled=v; }
}
