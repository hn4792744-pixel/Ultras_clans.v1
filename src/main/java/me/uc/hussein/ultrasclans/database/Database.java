package me.uc.hussein.ultrasclans.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.concurrent.*;

public final class Database implements AutoCloseable {
    private final JavaPlugin plugin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ultras-clans-db");
        t.setDaemon(true);
        return t;
    });
    private Connection connection;

    public Database(JavaPlugin plugin) { this.plugin = plugin; }

    public void open() throws SQLException {
        File db = new File(plugin.getDataFolder(), "data.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA synchronous=FULL");
            st.execute("PRAGMA busy_timeout=5000");
        }
        migrate();
    }

    private void migrate() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
            if (count("schema_version") == 0) st.executeUpdate("INSERT INTO schema_version(version) VALUES(1)");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clans(
                  id TEXT PRIMARY KEY, name TEXT NOT NULL COLLATE NOCASE UNIQUE, owner TEXT NOT NULL,
                  color TEXT NOT NULL, open INTEGER NOT NULL, cp INTEGER NOT NULL, level INTEGER NOT NULL,
                  bank REAL NOT NULL, bank_capacity REAL NOT NULL, member_capacity INTEGER NOT NULL,
                  storage_level INTEGER NOT NULL, warp_capacity INTEGER NOT NULL, alliance_capacity INTEGER NOT NULL,
                  created_at INTEGER NOT NULL, last_activity INTEGER NOT NULL
                )
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS members(
                  clan_id TEXT NOT NULL, uuid TEXT NOT NULL, name TEXT NOT NULL, rank_id TEXT NOT NULL,
                  level INTEGER NOT NULL, xp INTEGER NOT NULL, contribution INTEGER NOT NULL,
                  kills INTEGER NOT NULL, deaths INTEGER NOT NULL, blocks_broken INTEGER NOT NULL,
                  blocks_placed INTEGER NOT NULL, joined_at INTEGER NOT NULL, last_seen INTEGER NOT NULL,
                  muted INTEGER NOT NULL, banned INTEGER NOT NULL, tag_visible INTEGER NOT NULL, chat_enabled INTEGER NOT NULL,
                  PRIMARY KEY(clan_id, uuid), FOREIGN KEY(clan_id) REFERENCES clans(id) ON DELETE CASCADE
                )
                """);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_members_uuid ON members(uuid)");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS invitations(
                  id TEXT PRIMARY KEY, clan_id TEXT NOT NULL, sender TEXT NOT NULL, receiver TEXT NOT NULL,
                  created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL,
                  FOREIGN KEY(clan_id) REFERENCES clans(id) ON DELETE CASCADE
                )
                """);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_inv_receiver ON invitations(receiver)");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS join_requests(
                  id TEXT PRIMARY KEY, clan_id TEXT NOT NULL, player TEXT NOT NULL,
                  created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL,
                  UNIQUE(clan_id, player), FOREIGN KEY(clan_id) REFERENCES clans(id) ON DELETE CASCADE
                )
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clan_warps(
                  clan_id TEXT NOT NULL, warp_id INTEGER NOT NULL, name TEXT NOT NULL,
                  world TEXT NOT NULL, x REAL NOT NULL, y REAL NOT NULL, z REAL NOT NULL, yaw REAL NOT NULL, pitch REAL NOT NULL,
                  PRIMARY KEY(clan_id, warp_id), FOREIGN KEY(clan_id) REFERENCES clans(id) ON DELETE CASCADE
                )
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS alliances(
                  clan_a TEXT NOT NULL, clan_b TEXT NOT NULL, created_at INTEGER NOT NULL,
                  PRIMARY KEY(clan_a, clan_b), FOREIGN KEY(clan_a) REFERENCES clans(id) ON DELETE CASCADE,
                  FOREIGN KEY(clan_b) REFERENCES clans(id) ON DELETE CASCADE
                )
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clan_logs(
                  id INTEGER PRIMARY KEY AUTOINCREMENT, at INTEGER NOT NULL, actor TEXT, clan_id TEXT,
                  action TEXT NOT NULL, amount REAL, before_value TEXT, after_value TEXT, source TEXT, target_uuid TEXT
                )
                """);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_logs_clan ON clan_logs(clan_id, at DESC)");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks(
                  clan_id TEXT NOT NULL, period TEXT NOT NULL, task_id TEXT NOT NULL,
                  progress INTEGER NOT NULL, completed INTEGER NOT NULL, claimed INTEGER NOT NULL, cycle INTEGER NOT NULL,
                  PRIMARY KEY(clan_id, period, task_id), FOREIGN KEY(clan_id) REFERENCES clans(id) ON DELETE CASCADE
                )
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS member_history(
                  clan_id TEXT NOT NULL, uuid TEXT NOT NULL, rank_id TEXT NOT NULL, level INTEGER NOT NULL,
                  xp INTEGER NOT NULL, contribution INTEGER NOT NULL, kills INTEGER NOT NULL, deaths INTEGER NOT NULL,
                  blocks_broken INTEGER NOT NULL, blocks_placed INTEGER NOT NULL, saved_at INTEGER NOT NULL,
                  PRIMARY KEY(clan_id, uuid)
                )
                """);
        }
    }

    private int count(String table) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + table); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Connection connection() { return connection; }

    public <T> CompletableFuture<T> submit(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try { return task.call(); }
            catch (Exception e) { throw new CompletionException(e); }
        }, executor);
    }

    public CompletableFuture<Void> submit(Runnable task) {
        return CompletableFuture.runAsync(() -> task.run(), executor);
    }

    @Override public void close() {
        executor.shutdown();
        try { executor.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
    }
}
