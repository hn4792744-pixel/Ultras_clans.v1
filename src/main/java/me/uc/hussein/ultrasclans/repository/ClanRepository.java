package me.uc.hussein.ultrasclans.repository;

import me.uc.hussein.ultrasclans.database.Database;
import me.uc.hussein.ultrasclans.model.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ClanRepository {
    private final Database db;
    public ClanRepository(Database db) { this.db = db; }

    public List<Clan> loadAll() throws SQLException {
        Map<UUID, Clan> clans = new LinkedHashMap<>();
        try (PreparedStatement ps = db.connection().prepareStatement("SELECT * FROM clans ORDER BY cp DESC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Clan c = new Clan(UUID.fromString(rs.getString("id")), rs.getString("name"), UUID.fromString(rs.getString("owner")));
                c.setColor(rs.getString("color")); c.setOpen(rs.getInt("open") == 1); c.setCp(rs.getLong("cp")); c.setLevel(rs.getInt("level"));
                c.setBankCapacity(rs.getDouble("bank_capacity")); c.setBank(rs.getDouble("bank")); c.setMemberCapacity(rs.getInt("member_capacity"));
                c.setStorageLevel(rs.getInt("storage_level")); c.setWarpCapacity(rs.getInt("warp_capacity")); c.setAllianceCapacity(rs.getInt("alliance_capacity")); c.setCreatedAt(rs.getLong("created_at")); c.setLastActivity(rs.getLong("last_activity"));
                clans.put(c.id(), c);
            }
        }
        try (PreparedStatement ps = db.connection().prepareStatement("SELECT * FROM members"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Clan c = clans.get(UUID.fromString(rs.getString("clan_id"))); if (c == null) continue;
                ClanMember m = new ClanMember(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("rank_id"), rs.getLong("joined_at"));
                m.setLevel(rs.getInt("level")); m.setXp(rs.getLong("xp")); m.setContribution(rs.getLong("contribution"));
                m.addKills(rs.getLong("kills")); m.addDeaths(rs.getLong("deaths")); m.addBlocksBroken(rs.getLong("blocks_broken")); m.addBlocksPlaced(rs.getLong("blocks_placed"));
                m.setLastSeen(rs.getLong("last_seen")); m.setMuted(rs.getInt("muted") == 1); m.setBanned(rs.getInt("banned") == 1);
                m.setTagVisible(rs.getInt("tag_visible") == 1); m.setChatEnabled(rs.getInt("chat_enabled") == 1); c.addMember(m);
            }
        }
        return new ArrayList<>(clans.values());
    }

    public void saveClan(Clan c) throws SQLException {
        db.connection().setAutoCommit(false);
        try {
            try (PreparedStatement ps = db.connection().prepareStatement("""
                INSERT INTO clans(id,name,owner,color,open,cp,level,bank,bank_capacity,member_capacity,storage_level,warp_capacity,alliance_capacity,created_at,last_activity)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET name=excluded.name,owner=excluded.owner,color=excluded.color,open=excluded.open,cp=excluded.cp,level=excluded.level,
                bank=excluded.bank,bank_capacity=excluded.bank_capacity,member_capacity=excluded.member_capacity,storage_level=excluded.storage_level,
                warp_capacity=excluded.warp_capacity,alliance_capacity=excluded.alliance_capacity,last_activity=excluded.last_activity
                """)) {
                ps.setString(1,c.id().toString()); ps.setString(2,c.name()); ps.setString(3,c.owner().toString()); ps.setString(4,c.color()); ps.setInt(5,c.open()?1:0);
                ps.setLong(6,c.cp()); ps.setInt(7,c.level()); ps.setDouble(8,c.bank()); ps.setDouble(9,c.bankCapacity()); ps.setInt(10,c.memberCapacity());
                ps.setInt(11,c.storageLevel()); ps.setInt(12,c.warpCapacity()); ps.setInt(13,c.allianceCapacity()); ps.setLong(14,c.createdAt()); ps.setLong(15,c.lastActivity()); ps.executeUpdate();
            }
            try (PreparedStatement del = db.connection().prepareStatement("DELETE FROM members WHERE clan_id=?")) { del.setString(1,c.id().toString()); del.executeUpdate(); }
            try (PreparedStatement ps = db.connection().prepareStatement("""
                INSERT INTO members(clan_id,uuid,name,rank_id,level,xp,contribution,kills,deaths,blocks_broken,blocks_placed,joined_at,last_seen,muted,banned,tag_visible,chat_enabled)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """)) {
                for (ClanMember m : c.members().values()) {
                    ps.setString(1,c.id().toString()); ps.setString(2,m.uuid().toString()); ps.setString(3,m.name()); ps.setString(4,m.rank()); ps.setInt(5,m.level());
                    ps.setLong(6,m.xp()); ps.setLong(7,m.contribution()); ps.setLong(8,m.kills()); ps.setLong(9,m.deaths()); ps.setLong(10,m.blocksBroken()); ps.setLong(11,m.blocksPlaced());
                    ps.setLong(12,m.joinedAt()); ps.setLong(13,m.lastSeen()); ps.setInt(14,m.muted()?1:0); ps.setInt(15,m.banned()?1:0); ps.setInt(16,m.tagVisible()?1:0); ps.setInt(17,m.chatEnabled()?1:0); ps.addBatch();
                }
                ps.executeBatch();
            }
            db.connection().commit();
        } catch (SQLException e) { try { db.connection().rollback(); } catch (SQLException ignored) {} throw e; }
        finally { db.connection().setAutoCommit(true); }
    }

    public void deleteClan(UUID id) throws SQLException {
        try (PreparedStatement ps = db.connection().prepareStatement("DELETE FROM clans WHERE id=?")) { ps.setString(1,id.toString()); ps.executeUpdate(); }
    }

    public void saveInvitation(Invitation i) throws SQLException {
        try (PreparedStatement ps=db.connection().prepareStatement("INSERT OR REPLACE INTO invitations(id,clan_id,sender,receiver,created_at,expires_at) VALUES(?,?,?,?,?,?)")){ps.setString(1,i.id().toString());ps.setString(2,i.clanId().toString());ps.setString(3,i.sender().toString());ps.setString(4,i.receiver().toString());ps.setLong(5,i.createdAt());ps.setLong(6,i.expiresAt());ps.executeUpdate();}
    }
    public List<Invitation> invitations(UUID receiver) throws SQLException {
        List<Invitation> out=new ArrayList<>(); try(PreparedStatement ps=db.connection().prepareStatement("SELECT * FROM invitations WHERE receiver=? AND expires_at>?")){ps.setString(1,receiver.toString());ps.setLong(2,System.currentTimeMillis());try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new Invitation(UUID.fromString(rs.getString("id")),UUID.fromString(rs.getString("clan_id")),UUID.fromString(rs.getString("sender")),UUID.fromString(rs.getString("receiver")),rs.getLong("created_at"),rs.getLong("expires_at")));}} return out;
    }
    public void deleteInvitation(UUID id) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("DELETE FROM invitations WHERE id=?")){ps.setString(1,id.toString());ps.executeUpdate();}}
    public void saveRequest(JoinRequest r) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("INSERT OR REPLACE INTO join_requests(id,clan_id,player,created_at,expires_at) VALUES(?,?,?,?,?)")){ps.setString(1,r.id().toString());ps.setString(2,r.clanId().toString());ps.setString(3,r.player().toString());ps.setLong(4,r.createdAt());ps.setLong(5,r.expiresAt());ps.executeUpdate();}}
    public List<JoinRequest> requests(UUID clanId) throws SQLException {List<JoinRequest> out=new ArrayList<>();try(PreparedStatement ps=db.connection().prepareStatement("SELECT * FROM join_requests WHERE clan_id=? AND expires_at>?")){ps.setString(1,clanId.toString());ps.setLong(2,System.currentTimeMillis());try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new JoinRequest(UUID.fromString(rs.getString("id")),UUID.fromString(rs.getString("clan_id")),UUID.fromString(rs.getString("player")),rs.getLong("created_at"),rs.getLong("expires_at")));}}return out;}
    public void deleteRequest(UUID id) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("DELETE FROM join_requests WHERE id=?")){ps.setString(1,id.toString());ps.executeUpdate();}}
    public void saveWarp(UUID clanId,ClanWarp w) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("INSERT OR REPLACE INTO clan_warps(clan_id,warp_id,name,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?,?,?)")){ps.setString(1,clanId.toString());ps.setInt(2,w.id());ps.setString(3,w.name());ps.setString(4,w.location().world());ps.setDouble(5,w.location().x());ps.setDouble(6,w.location().y());ps.setDouble(7,w.location().z());ps.setFloat(8,w.location().yaw());ps.setFloat(9,w.location().pitch());ps.executeUpdate();}}
    public List<ClanWarp> warps(UUID clanId) throws SQLException {List<ClanWarp> out=new ArrayList<>();try(PreparedStatement ps=db.connection().prepareStatement("SELECT * FROM clan_warps WHERE clan_id=? ORDER BY warp_id")){ps.setString(1,clanId.toString());try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new ClanWarp(rs.getInt("warp_id"),rs.getString("name"),new LocationData(rs.getString("world"),rs.getDouble("x"),rs.getDouble("y"),rs.getDouble("z"),rs.getFloat("yaw"),rs.getFloat("pitch"))));}}return out;}
    public void deleteWarp(UUID clanId,int id) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("DELETE FROM clan_warps WHERE clan_id=? AND warp_id=?")){ps.setString(1,clanId.toString());ps.setInt(2,id);ps.executeUpdate();}}

    public void saveAlliance(UUID a,UUID b) throws SQLException {String x=a.toString(),y=b.toString();if(x.compareTo(y)>0){String t=x;x=y;y=t;}try(PreparedStatement ps=db.connection().prepareStatement("INSERT OR REPLACE INTO alliances(clan_a,clan_b,created_at) VALUES(?,?,?)")){ps.setString(1,x);ps.setString(2,y);ps.setLong(3,System.currentTimeMillis());ps.executeUpdate();}}
    public void deleteAlliance(UUID a,UUID b) throws SQLException {String x=a.toString(),y=b.toString();if(x.compareTo(y)>0){String t=x;x=y;y=t;}try(PreparedStatement ps=db.connection().prepareStatement("DELETE FROM alliances WHERE clan_a=? AND clan_b=?")){ps.setString(1,x);ps.setString(2,y);ps.executeUpdate();}}
    public List<UUID[]> loadAlliances() throws SQLException {List<UUID[]> out=new ArrayList<>();try(PreparedStatement ps=db.connection().prepareStatement("SELECT clan_a,clan_b FROM alliances");ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new UUID[]{UUID.fromString(rs.getString(1)),UUID.fromString(rs.getString(2))});}return out;}
    public CompletableFuture<Void> dbSaveAlliance(UUID a,UUID b){return db.submit(() -> { saveAlliance(a,b); return null; });}
    public CompletableFuture<Void> dbDeleteAlliance(UUID a,UUID b){return db.submit(() -> { deleteAlliance(a,b); return null; });}
    public CompletableFuture<List<UUID[]>> dbLoadAlliances(){return db.submit(this::loadAlliances);}
    public void saveHistory(UUID clanId, ClanMember m) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("INSERT OR REPLACE INTO member_history(clan_id,uuid,rank_id,level,xp,contribution,kills,deaths,blocks_broken,blocks_placed,saved_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)")){ps.setString(1,clanId.toString());ps.setString(2,m.uuid().toString());ps.setString(3,m.rank());ps.setInt(4,m.level());ps.setLong(5,m.xp());ps.setLong(6,m.contribution());ps.setLong(7,m.kills());ps.setLong(8,m.deaths());ps.setLong(9,m.blocksBroken());ps.setLong(10,m.blocksPlaced());ps.setLong(11,System.currentTimeMillis());ps.executeUpdate();}}
    public ClanMember loadHistory(UUID clanId, UUID player, String name) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("SELECT * FROM member_history WHERE clan_id=? AND uuid=? AND saved_at>?")){ps.setString(1,clanId.toString());ps.setString(2,player.toString());ps.setLong(3,System.currentTimeMillis()-259200000L);try(ResultSet rs=ps.executeQuery()){if(!rs.next())return null;ClanMember m=new ClanMember(player,name,rs.getString("rank_id"),System.currentTimeMillis());m.setLevel(rs.getInt("level"));m.setXp(rs.getLong("xp"));m.setContribution(rs.getLong("contribution"));m.addKills(rs.getLong("kills"));m.addDeaths(rs.getLong("deaths"));m.addBlocksBroken(rs.getLong("blocks_broken"));m.addBlocksPlaced(rs.getLong("blocks_placed"));return m;}}}
    public void deleteHistory(UUID clanId, UUID player) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("DELETE FROM member_history WHERE clan_id=? AND uuid=?")){ps.setString(1,clanId.toString());ps.setString(2,player.toString());ps.executeUpdate();}}
    public CompletableFuture<Void> dbSaveHistory(UUID clanId,ClanMember m){return db.submit(() -> { saveHistory(clanId,m); return null; });}
    public CompletableFuture<ClanMember> dbLoadHistory(UUID clanId,UUID player,String name){return db.submit(()->loadHistory(clanId,player,name));}
    public CompletableFuture<Void> dbDeleteHistory(UUID clanId,UUID player){return db.submit(() -> { deleteHistory(clanId,player); return null; });}
    public void saveTask(UUID clanId, String period, ClanTask t, int cycle) throws SQLException {try(PreparedStatement ps=db.connection().prepareStatement("INSERT OR REPLACE INTO tasks(clan_id,period,task_id,progress,completed,claimed,cycle) VALUES(?,?,?,?,?,?,?)")){ps.setString(1,clanId.toString());ps.setString(2,period);ps.setString(3,t.id);ps.setLong(4,t.progress);ps.setInt(5,t.completed?1:0);ps.setInt(6,t.claimed?1:0);ps.setInt(7,cycle);ps.executeUpdate();}}
    public List<Map<String,Object>> loadTasks(UUID clanId,String period) throws SQLException {List<Map<String,Object>> out=new ArrayList<>();try(PreparedStatement ps=db.connection().prepareStatement("SELECT * FROM tasks WHERE clan_id=? AND period=?")){ps.setString(1,clanId.toString());ps.setString(2,period);try(ResultSet rs=ps.executeQuery()){while(rs.next()){Map<String,Object> m=new HashMap<>();m.put("id",rs.getString("task_id"));m.put("progress",rs.getLong("progress"));m.put("completed",rs.getInt("completed")==1);m.put("claimed",rs.getInt("claimed")==1);m.put("cycle",rs.getInt("cycle"));out.add(m);}}}return out;}
    public CompletableFuture<Void> dbSaveTask(UUID clanId,String period,ClanTask t,int cycle){return db.submit(() -> { saveTask(clanId,period,t,cycle); return null; });}
    public CompletableFuture<List<Map<String,Object>>> dbLoadTasks(UUID clanId,String period){return db.submit(()->loadTasks(clanId,period));}
    public CompletableFuture<Boolean> dbSaveInvitation(Invitation i){ return db.submit(() -> { saveInvitation(i); return true; }); }
    public CompletableFuture<List<Invitation>> dbListInvitations(UUID player){ return db.submit(() -> invitations(player)); }
    public CompletableFuture<Boolean> dbDeleteInvitation(UUID id){ return db.submit(() -> { deleteInvitation(id); return true; }); }
    public CompletableFuture<Boolean> dbSaveRequest(JoinRequest r){ return db.submit(() -> { saveRequest(r); return true; }); }
    public CompletableFuture<List<JoinRequest>> dbListRequests(UUID clan){ return db.submit(() -> requests(clan)); }
    public CompletableFuture<Boolean> dbDeleteRequest(UUID id){ return db.submit(() -> { deleteRequest(id); return true; }); }
    public CompletableFuture<Boolean> dbSaveWarp(UUID clanId,ClanWarp w){ return db.submit(() -> { saveWarp(clanId,w); return true; }); }
    public CompletableFuture<List<ClanWarp>> dbListWarps(UUID clanId){ return db.submit(() -> warps(clanId)); }
    public CompletableFuture<Boolean> dbDeleteWarp(UUID clanId,int id){ return db.submit(() -> { deleteWarp(clanId,id); return true; }); }

    public CompletableFuture<Void> saveAsync(Clan c) { return db.submit(() -> { saveClan(c); return null; }); }
    public CompletableFuture<Void> deleteAsync(UUID id) { return db.submit(() -> { deleteClan(id); return null; }); }

    public void log(UUID actor, UUID clan, String action, Double amount, String before, String after, String source, UUID target) throws SQLException {
        try (PreparedStatement ps = db.connection().prepareStatement("INSERT INTO clan_logs(at,actor,clan_id,action,amount,before_value,after_value,source,target_uuid) VALUES(?,?,?,?,?,?,?,?,?)")) {
            ps.setLong(1,System.currentTimeMillis()); ps.setString(2, actor == null ? null : actor.toString()); ps.setString(3, clan == null ? null : clan.toString()); ps.setString(4,action);
            if (amount == null) ps.setNull(5,Types.REAL); else ps.setDouble(5,amount); ps.setString(6,before); ps.setString(7,after); ps.setString(8,source); ps.setString(9,target == null ? null : target.toString()); ps.executeUpdate();
        }
    }
}
