package me.uc.hussein.ultrasclans.model;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Clan {
    private final UUID id;
    private String name;
    private UUID owner;
    private String color;
    private boolean open;
    private long cp;
    private int level;
    private double bank;
    private double bankCapacity;
    private int memberCapacity;
    private int storageLevel;
    private int warpCapacity;
    private int allianceCapacity;
    private long createdAt;
    private long lastActivity;
    private final Map<UUID, ClanMember> members = new ConcurrentHashMap<>();

    public Clan(UUID id, String name, UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.color = "white";
        this.open = true;
        this.cp = 0L;
        this.level = 1;
        this.bank = 0D;
        this.bankCapacity = 50000D;
        this.memberCapacity = 10;
        this.storageLevel = 1;
        this.warpCapacity = 1;
        this.allianceCapacity = 1;
        this.createdAt = Instant.now().toEpochMilli();
        this.lastActivity = this.createdAt;
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public UUID owner() { return owner; }
    public String color() { return color; }
    public boolean open() { return open; }
    public long cp() { return cp; }
    public int level() { return level; }
    public double bank() { return bank; }
    public double bankCapacity() { return bankCapacity; }
    public int memberCapacity() { return memberCapacity; }
    public int storageLevel() { return storageLevel; }
    public int warpCapacity() { return warpCapacity; }
    public int allianceCapacity() { return allianceCapacity; }
    public long createdAt() { return createdAt; }
    public long lastActivity() { return lastActivity; }
    public void setCreatedAt(long value) { createdAt=value; }
    public void setLastActivity(long value) { lastActivity=value; }
    public Map<UUID, ClanMember> members() { return Collections.unmodifiableMap(members); }

    public void setName(String value) { name = value; touch(); }
    public void setOwner(UUID value) { owner = value; touch(); }
    public void setColor(String value) { color = value; touch(); }
    public void setOpen(boolean value) { open = value; touch(); }
    public void setCp(long value) { cp = Math.max(0L, value); touch(); }
    public void addCp(long value) { setCp(cp + value); }
    public void removeCp(long value) { setCp(Math.max(0L, cp - Math.max(0L, value))); }
    public void setLevel(int value) { level = Math.max(1, value); touch(); }
    public void setBank(double value) { bank = Math.max(0D, Math.min(bankCapacity, value)); touch(); }
    public void setBankCapacity(double value) { bankCapacity = Math.max(0D, value); if (bank > bankCapacity) bank = bankCapacity; touch(); }
    public void setMemberCapacity(int value) { memberCapacity = Math.max(1, value); touch(); }
    public void setStorageLevel(int value) { storageLevel = Math.max(1, Math.min(10, value)); touch(); }
    public void setWarpCapacity(int value) { warpCapacity = Math.max(0, Math.min(5, value)); touch(); }
    public void setAllianceCapacity(int value) { allianceCapacity = Math.max(0, value); touch(); }
    public void touch() { lastActivity = Instant.now().toEpochMilli(); }

    public void addMember(ClanMember member) { members.put(member.uuid(), member); touch(); }
    public void removeMember(UUID uuid) { members.remove(uuid); touch(); }
    public ClanMember member(UUID uuid) { return members.get(uuid); }
    public boolean hasMember(UUID uuid) { return members.containsKey(uuid); }
}
