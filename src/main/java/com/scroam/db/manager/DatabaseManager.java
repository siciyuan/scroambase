package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import com.scroam.db.data.*;
import java.sql.Timestamp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心数据库管理器 - 提供所有数据库操作
 */
public class DatabaseManager {

    private final ScroamDB plugin;
    private Connection connection;
    private final Map<UUID, UserData> userCache = new ConcurrentHashMap<>();
    private final Map<String, WorldData> worldCache = new ConcurrentHashMap<>();
    private final Map<String, Land> landCache = new ConcurrentHashMap<>();
    private final Map<String, Waypoint> waypointCache = new ConcurrentHashMap<>();

    public DatabaseManager(ScroamDB plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            String dbPath = plugin.getDataFolder().getAbsolutePath() + "/scroamdb.db";
            plugin.getDataFolder().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTables();
            loadCache();
            plugin.getLogger().info("Database initialized at: " + dbPath);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 世界表
            stmt.execute("CREATE TABLE IF NOT EXISTS worlds (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "world_name VARCHAR(64) UNIQUE NOT NULL," +
                    "display_name VARCHAR(64) DEFAULT ''," +
                    "spawn_x DOUBLE DEFAULT 0," +
                    "spawn_y DOUBLE DEFAULT 64," +
                    "spawn_z DOUBLE DEFAULT 0," +
                    "spawn_yaw FLOAT DEFAULT 0," +
                    "spawn_pitch FLOAT DEFAULT 0," +
                    "is_main_world BOOLEAN DEFAULT FALSE," +
                    "enabled BOOLEAN DEFAULT TRUE," +
                    "properties TEXT DEFAULT '{}'" +
                    ")");

            // 用户表
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid VARCHAR(36) UNIQUE NOT NULL," +
                    "username VARCHAR(16) NOT NULL," +
                    "balance DECIMAL(18,2) DEFAULT 0," +
                    "rtp_free_count INTEGER DEFAULT 3," +
                    "rtp_max_free_count INTEGER DEFAULT 3," +
                    "rtp_price DECIMAL(10,2) DEFAULT 100," +
                    "homes_limit INTEGER DEFAULT 3," +
                    "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "play_time BIGINT DEFAULT 0," +
                    "stats TEXT DEFAULT '{}'," +
                    "settings TEXT DEFAULT '{}'," +
                    "extra_data TEXT DEFAULT '{}'" +
                    ")");

            // 家数据表
            stmt.execute("CREATE TABLE IF NOT EXISTS homes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_uuid VARCHAR(36) NOT NULL," +
                    "home_name VARCHAR(32) NOT NULL," +
                    "world_name VARCHAR(64) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "yaw FLOAT DEFAULT 0," +
                    "pitch FLOAT DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE(user_uuid, home_name)" +
                    ")");

            // 领地数据表
            stmt.execute("CREATE TABLE IF NOT EXISTS lands (" +
                    "id VARCHAR(64) PRIMARY KEY," +
                    "owner_uuid VARCHAR(36) NOT NULL," +
                    "name VARCHAR(64) NOT NULL," +
                    "world_name VARCHAR(64) NOT NULL," +
                    "min_x INTEGER NOT NULL," +
                    "max_x INTEGER NOT NULL," +
                    "min_z INTEGER NOT NULL," +
                    "max_z INTEGER NOT NULL," +
                    "members TEXT DEFAULT '{}'," +
                    "member_perms TEXT DEFAULT '{}'," +
                    "default_perms TEXT DEFAULT 'BUILD,DESTROY,INTERACT,CHEST_ACCESS,ANIMALS,ITEM_DROP,ITEM_PICKUP'," +
                    "buy_price DECIMAL(18,2) DEFAULT 100," +
                    "created_time BIGINT DEFAULT 0" +
                    ")");

            // 地标数据表
            stmt.execute("CREATE TABLE IF NOT EXISTS waypoints (" +
                    "id VARCHAR(128) PRIMARY KEY," +
                    "name VARCHAR(64) NOT NULL," +
                    "creator_uuid VARCHAR(36) NOT NULL," +
                    "world_name VARCHAR(64) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "yaw FLOAT DEFAULT 0," +
                    "pitch FLOAT DEFAULT 0," +
                    "create_count INTEGER DEFAULT 0," +
                    "teleport_count INTEGER DEFAULT 3," +
                    "create_price DECIMAL(10,2) DEFAULT 0," +
                    "teleport_price DECIMAL(10,2) DEFAULT 0," +
                    "requires_permission BOOLEAN DEFAULT FALSE," +
                    "pending_permissions TEXT DEFAULT ''," +
                    "created_time BIGINT DEFAULT 0" +
                    ")");

            // 服务器公库表
            stmt.execute("CREATE TABLE IF NOT EXISTS server_treasury (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "balance DECIMAL(18,2) DEFAULT 0," +
                    "total_tax_collected DECIMAL(18,2) DEFAULT 0," +
                    "total_deposited DECIMAL(18,2) DEFAULT 0," +
                    "total_withdrawn DECIMAL(18,2) DEFAULT 0," +
                    "last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 交易记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id VARCHAR(64) PRIMARY KEY," +
                    "from_uuid VARCHAR(36)," +
                    "from_name VARCHAR(32)," +
                    "to_uuid VARCHAR(36)," +
                    "to_name VARCHAR(32)," +
                    "amount DECIMAL(18,2) NOT NULL," +
                    "tax_amount DECIMAL(18,2) DEFAULT 0," +
                    "type VARCHAR(32) NOT NULL," +
                    "service_id VARCHAR(64)," +
                    "description VARCHAR(255)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_from ON transactions(from_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_to ON transactions(to_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_created ON transactions(created_at)");

            // 用户认证表
            stmt.execute("CREATE TABLE IF NOT EXISTS user_auth (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "username VARCHAR(16) NOT NULL," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "salt VARCHAR(64) NOT NULL," +
                    "last_login_ip VARCHAR(45) DEFAULT ''," +
                    "last_login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "failed_login_attempts INTEGER DEFAULT 0," +
                    "locked_until TIMESTAMP DEFAULT NULL" +
                    ")");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_auth_username ON user_auth(username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_auth_uuid ON user_auth(uuid)");

            initTreasury();
        }
    }

    private void initTreasury() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM server_treasury")) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO server_treasury (balance, total_tax_collected) VALUES (0, 0)");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to initialize treasury: " + e.getMessage());
        }
    }

    private void loadCache() {
        loadWorldCache();
        loadUserCache();
    }

    private void loadWorldCache() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM worlds")) {
            while (rs.next()) {
                WorldData worldData = resultSetToWorldData(rs);
                worldCache.put(worldData.worldName, worldData);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load world cache: " + e.getMessage());
        }
    }

    private void loadUserCache() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                UserData userData = resultSetToUserData(rs);
                userCache.put(userData.uuid, userData);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load user cache: " + e.getMessage());
        }
    }

    // ========== 世界数据操作 ==========

    public void saveWorld(WorldData worldData) {
        try {
            String sql = "INSERT OR REPLACE INTO worlds (" +
                    "world_name, display_name, spawn_x, spawn_y, spawn_z, " +
                    "spawn_yaw, spawn_pitch, is_main_world, enabled, properties) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, worldData.worldName);
                pstmt.setString(2, worldData.displayName);
                pstmt.setDouble(3, worldData.spawnX);
                pstmt.setDouble(4, worldData.spawnY);
                pstmt.setDouble(5, worldData.spawnZ);
                pstmt.setFloat(6, worldData.spawnYaw);
                pstmt.setFloat(7, worldData.spawnPitch);
                pstmt.setBoolean(8, worldData.isMainWorld);
                pstmt.setBoolean(9, worldData.enabled);
                pstmt.setString(10, worldData.properties);
                pstmt.executeUpdate();
            }
            worldCache.put(worldData.worldName, worldData);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save world: " + e.getMessage());
        }
    }

    public WorldData getWorld(String worldName) {
        WorldData data = worldCache.get(worldName);
        if (data == null) {
            data = loadWorldFromDB(worldName);
            if (data != null) {
                worldCache.put(worldName, data);
            }
        }
        return data;
    }

    private WorldData loadWorldFromDB(String worldName) {
        try {
            String sql = "SELECT * FROM worlds WHERE world_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, worldName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return resultSetToWorldData(rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get world: " + e.getMessage());
        }
        return null;
    }

    public Location getWorldSpawn(String worldName) {
        WorldData data = getWorld(worldName);
        if (data == null) return null;
        World world = Bukkit.getWorld(data.worldName);
        if (world == null) return null;
        return new Location(world, data.spawnX, data.spawnY, data.spawnZ, data.spawnYaw, data.spawnPitch);
    }

    public void setWorldSpawn(String worldName, Location location) {
        WorldData data = getWorld(worldName);
        if (data == null) {
            data = new WorldData();
            data.worldName = worldName;
        }
        data.setSpawnLocation(location);
        saveWorld(data);
    }

    public WorldData getMainWorld() {
        for (WorldData data : worldCache.values()) {
            if (data.isMainWorld) return data;
        }
        return null;
    }

    public void setMainWorld(String worldName) {
        // 清除所有世界的主世界标记
        for (WorldData data : worldCache.values()) {
            if (data.isMainWorld) {
                data.isMainWorld = false;
                saveWorld(data);
            }
        }
        // 设置新的主世界
        WorldData data = getWorld(worldName);
        if (data == null) {
            data = new WorldData();
            data.worldName = worldName;
        }
        data.isMainWorld = true;
        saveWorld(data);
    }

    public Collection<WorldData> getAllWorlds() {
        return worldCache.values();
    }

    private WorldData resultSetToWorldData(ResultSet rs) throws SQLException {
        WorldData data = new WorldData();
        data.id = rs.getInt("id");
        data.worldName = rs.getString("world_name");
        data.displayName = rs.getString("display_name");
        data.spawnX = rs.getDouble("spawn_x");
        data.spawnY = rs.getDouble("spawn_y");
        data.spawnZ = rs.getDouble("spawn_z");
        data.spawnYaw = rs.getFloat("spawn_yaw");
        data.spawnPitch = rs.getFloat("spawn_pitch");
        data.isMainWorld = rs.getBoolean("is_main_world");
        data.enabled = rs.getBoolean("enabled");
        data.properties = rs.getString("properties");
        return data;
    }

    // ========== 用户数据操作 ==========

    public UserData getUser(UUID uuid) {
        UserData data = userCache.get(uuid);
        if (data == null) {
            data = loadUserFromDB(uuid);
            if (data != null) {
                userCache.put(uuid, data);
            }
        }
        return data;
    }

    private UserData loadUserFromDB(UUID uuid) {
        try {
            String sql = "SELECT * FROM users WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return resultSetToUserData(rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get user: " + e.getMessage());
        }
        return null;
    }

    public void saveUser(UserData userData) {
        try {
            String sql = "INSERT OR REPLACE INTO users (" +
                    "uuid, username, balance, rtp_free_count, rtp_max_free_count, " +
                    "rtp_price, homes_limit, last_login, first_join, play_time, " +
                    "stats, settings, extra_data) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userData.uuid.toString());
                pstmt.setString(2, userData.username);
                pstmt.setDouble(3, userData.balance);
                pstmt.setInt(4, userData.rtpFreeCount);
                pstmt.setInt(5, userData.rtpMaxFreeCount);
                pstmt.setDouble(6, userData.rtpPrice);
                pstmt.setInt(7, userData.homesLimit);
                pstmt.setTimestamp(8, userData.lastLogin);
                pstmt.setTimestamp(9, userData.firstJoin);
                pstmt.setLong(10, userData.playTime);
                pstmt.setString(11, userData.stats);
                pstmt.setString(12, userData.settings);
                pstmt.setString(13, userData.extraData);
                pstmt.executeUpdate();
            }
            userCache.put(userData.uuid, userData);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save user: " + e.getMessage());
        }
    }

    public UserData createUser(UUID uuid, String username) {
        UserData data = getUser(uuid);
        if (data != null) {
            data.username = username;
            data.lastLogin = new Timestamp(System.currentTimeMillis());
            saveUser(data);
            return data;
        }

        data = new UserData(uuid, username);
        data.rtpFreeCount = 3;
        data.rtpMaxFreeCount = 3;
        data.rtpPrice = 100;
        data.homesLimit = 3;
        data.lastLogin = new Timestamp(System.currentTimeMillis());
        data.firstJoin = new Timestamp(System.currentTimeMillis());
        saveUser(data);
        return data;
    }

    public Collection<UserData> getAllUsers() {
        return userCache.values();
    }

    private UserData resultSetToUserData(ResultSet rs) throws SQLException {
        UserData data = new UserData();
        data.id = rs.getInt("id");
        data.uuid = UUID.fromString(rs.getString("uuid"));
        data.username = rs.getString("username");
        data.balance = rs.getDouble("balance");
        data.rtpFreeCount = rs.getInt("rtp_free_count");
        data.rtpMaxFreeCount = rs.getInt("rtp_max_free_count");
        data.rtpPrice = rs.getDouble("rtp_price");
        data.homesLimit = rs.getInt("homes_limit");
        data.lastLogin = rs.getTimestamp("last_login");
        data.firstJoin = rs.getTimestamp("first_join");
        data.playTime = rs.getLong("play_time");
        data.stats = rs.getString("stats");
        data.settings = rs.getString("settings");
        data.extraData = rs.getString("extra_data");
        return data;
    }

    // ========== 经济系统操作 ==========

    public boolean withdrawBalance(UUID uuid, double amount) {
        UserData user = getUser(uuid);
        if (user == null || user.balance < amount) return false;
        user.balance -= amount;
        saveUser(user);
        return true;
    }

    public void depositBalance(UUID uuid, double amount) {
        UserData user = getUser(uuid);
        if (user == null) {
            plugin.getLogger().warning("User not found: " + uuid);
            return;
        }
        user.balance += amount;
        saveUser(user);
    }

    public double getBalance(UUID uuid) {
        UserData user = getUser(uuid);
        return user != null ? user.balance : 0;
    }

    public boolean setBalance(UUID uuid, double amount) {
        UserData user = getUser(uuid);
        if (user == null) return false;
        user.balance = amount;
        saveUser(user);
        return true;
    }

    // ========== 家数据操作 ==========

    public void saveHome(UUID userUuid, String homeName, Location location) {
        try {
            String sql = "INSERT OR REPLACE INTO homes (" +
                    "user_uuid, home_name, world_name, x, y, z, yaw, pitch, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userUuid.toString());
                pstmt.setString(2, homeName);
                pstmt.setString(3, location.getWorld().getName());
                pstmt.setDouble(4, location.getX());
                pstmt.setDouble(5, location.getY());
                pstmt.setDouble(6, location.getZ());
                pstmt.setFloat(7, location.getYaw());
                pstmt.setFloat(8, location.getPitch());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save home: " + e.getMessage());
        }
    }

    public Location getHome(UUID userUuid, String homeName) {
        try {
            String sql = "SELECT * FROM homes WHERE user_uuid = ? AND home_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userUuid.toString());
                pstmt.setString(2, homeName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        World world = Bukkit.getWorld(rs.getString("world_name"));
                        if (world == null) return null;
                        return new Location(world, rs.getDouble("x"), rs.getDouble("y"),
                                rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get home: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteHome(UUID userUuid, String homeName) {
        try {
            String sql = "DELETE FROM homes WHERE user_uuid = ? AND home_name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userUuid.toString());
                pstmt.setString(2, homeName);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete home: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Location> getHomes(UUID userUuid) {
        Map<String, Location> homes = new HashMap<>();
        try {
            String sql = "SELECT * FROM homes WHERE user_uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userUuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        World world = Bukkit.getWorld(rs.getString("world_name"));
                        if (world == null) continue;
                        Location loc = new Location(world, rs.getDouble("x"),
                                rs.getDouble("y"), rs.getDouble("z"),
                                rs.getFloat("yaw"), rs.getFloat("pitch"));
                        homes.put(rs.getString("home_name"), loc);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get homes: " + e.getMessage());
        }
        return homes;
    }

    public int getHomeCount(UUID userUuid) {
        try {
            String sql = "SELECT COUNT(*) FROM homes WHERE user_uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, userUuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get home count: " + e.getMessage());
        }
        return 0;
    }

    // ========== 领地数据操作 ==========

    public void saveLand(Land land) {
        try {
            String sql = "INSERT OR REPLACE INTO lands (" +
                    "id, owner_uuid, name, world_name, min_x, max_x, min_z, max_z, " +
                    "members, member_perms, default_perms, buy_price, created_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, land.getId());
                pstmt.setString(2, land.getOwnerId().toString());
                pstmt.setString(3, land.getName());
                pstmt.setString(4, land.getWorld().getName());
                pstmt.setInt(5, land.getMinX());
                pstmt.setInt(6, land.getMaxX());
                pstmt.setInt(7, land.getMinZ());
                pstmt.setInt(8, land.getMaxZ());
                pstmt.setString(9, serializeMembers(land.getMembers()));
                pstmt.setString(10, serializeMemberPermissions(land.getMemberPermissions()));
                pstmt.setString(11, serializeDefaultPermissions(land.getDefaultPermissions()));
                pstmt.setDouble(12, land.getBuyPrice());
                pstmt.setLong(13, land.getCreatedTime());
                pstmt.executeUpdate();
            }
            landCache.put(land.getId(), land);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save land: " + e.getMessage());
        }
    }

    public Land getLand(String landId) {
        Land land = landCache.get(landId);
        if (land == null) {
            land = loadLandFromDB(landId);
            if (land != null) {
                landCache.put(landId, land);
            }
        }
        return land;
    }

    private Land loadLandFromDB(String landId) {
        try {
            String sql = "SELECT * FROM lands WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, landId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        try {
                            return resultSetToLand(rs);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to deserialize land: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load land: " + e.getMessage());
        }
        return null;
    }

    public void deleteLand(String landId) {
        try {
            String sql = "DELETE FROM lands WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, landId);
                pstmt.executeUpdate();
            }
            landCache.remove(landId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete land: " + e.getMessage());
        }
    }

    public Collection<Land> getAllLands() {
        return landCache.values();
    }

    public Land getLandAt(Location location) {
        String worldName = location.getWorld().getName();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        for (Land land : landCache.values()) {
            if (land.getWorld().getName().equals(worldName) && land.contains(x, z)) {
                return land;
            }
        }
        return null;
    }

    private Land resultSetToLand(ResultSet rs) throws Exception {
        String id = rs.getString("id");
        UUID ownerId = UUID.fromString(rs.getString("owner_uuid"));
        String name = rs.getString("name");
        String worldName = rs.getString("world_name");
        int minX = rs.getInt("min_x");
        int maxX = rs.getInt("max_x");
        int minZ = rs.getInt("min_z");
        int maxZ = rs.getInt("max_z");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new Exception("World not found: " + worldName);
        }

        Land land = new Land(id, ownerId, name, world, minX, maxX, minZ, maxZ);

        String membersStr = rs.getString("members");
        if (membersStr != null && !membersStr.isEmpty()) {
            Map<UUID, Land.MemberType> members = deserializeMembers(membersStr);
            for (Map.Entry<UUID, Land.MemberType> entry : members.entrySet()) {
                land.addMember(entry.getKey(), entry.getValue());
            }
        }

        String permsStr = rs.getString("member_perms");
        if (permsStr != null && !permsStr.isEmpty()) {
            Map<UUID, Set<Land.Permission>> perms = deserializeMemberPermissions(permsStr);
            for (Map.Entry<UUID, Set<Land.Permission>> entry : perms.entrySet()) {
                for (Land.Permission p : entry.getValue()) {
                    land.setMemberPermission(entry.getKey(), p, true);
                }
            }
        }

        String defaultPermsStr = rs.getString("default_perms");
        if (defaultPermsStr != null && !defaultPermsStr.isEmpty()) {
            Set<Land.Permission> defaultPerms = deserializeDefaultPermissions(defaultPermsStr);
            for (Land.Permission p : defaultPerms) {
                land.setDefaultPermission(p);
            }
        }

        land.setBuyPrice(rs.getDouble("buy_price"));
        return land;
    }

    private String serializeMembers(Map<UUID, Land.MemberType> members) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, Land.MemberType> entry : members.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(entry.getKey().toString()).append(":").append(entry.getValue().getKey());
        }
        return sb.toString();
    }

    private Map<UUID, Land.MemberType> deserializeMembers(String str) {
        Map<UUID, Land.MemberType> result = new HashMap<>();
        if (str == null || str.isEmpty()) return result;
        for (String part : str.split(";")) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                UUID uuid = UUID.fromString(kv[0]);
                Land.MemberType type = Land.MemberType.fromKey(kv[1]);
                if (type != null) result.put(uuid, type);
            }
        }
        return result;
    }

    private String serializeMemberPermissions(Map<UUID, Set<Land.Permission>> perms) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, Set<Land.Permission>> entry : perms.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(entry.getKey().toString()).append(":");
            StringBuilder permSb = new StringBuilder();
            for (Land.Permission p : entry.getValue()) {
                if (permSb.length() > 0) permSb.append(",");
                permSb.append(p.getKey());
            }
            sb.append(permSb.toString());
        }
        return sb.toString();
    }

    private Map<UUID, Set<Land.Permission>> deserializeMemberPermissions(String str) {
        Map<UUID, Set<Land.Permission>> result = new HashMap<>();
        if (str == null || str.isEmpty()) return result;
        for (String part : str.split(";")) {
            String[] kv = part.split(":");
            if (kv.length == 2) {
                UUID uuid = UUID.fromString(kv[0]);
                Set<Land.Permission> permSet = new HashSet<>();
                for (String pKey : kv[1].split(",")) {
                    Land.Permission p = Land.Permission.fromKey(pKey);
                    if (p != null) permSet.add(p);
                }
                result.put(uuid, permSet);
            }
        }
        return result;
    }

    private String serializeDefaultPermissions(Set<Land.Permission> perms) {
        StringBuilder sb = new StringBuilder();
        for (Land.Permission p : perms) {
            if (sb.length() > 0) sb.append(",");
            sb.append(p.getKey());
        }
        return sb.toString();
    }

    private Set<Land.Permission> deserializeDefaultPermissions(String str) {
        Set<Land.Permission> result = new HashSet<>();
        if (str == null || str.isEmpty()) return result;
        for (String pKey : str.split(",")) {
            Land.Permission p = Land.Permission.fromKey(pKey);
            if (p != null) result.add(p);
        }
        return result;
    }

    // ========== 地标数据操作 ==========

    public void saveWaypoint(Waypoint waypoint) {
        try {
            String sql = "INSERT OR REPLACE INTO waypoints (" +
                    "id, name, creator_uuid, world_name, x, y, z, yaw, pitch, " +
                    "create_count, teleport_count, create_price, teleport_price, " +
                    "requires_permission, pending_permissions, created_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, waypoint.getId());
                pstmt.setString(2, waypoint.getName());
                pstmt.setString(3, waypoint.getCreator().toString());
                pstmt.setString(4, waypoint.getWorldName());
                pstmt.setDouble(5, waypoint.getX());
                pstmt.setDouble(6, waypoint.getY());
                pstmt.setDouble(7, waypoint.getZ());
                pstmt.setFloat(8, waypoint.getYaw());
                pstmt.setFloat(9, waypoint.getPitch());
                pstmt.setInt(10, waypoint.getCreateCount());
                pstmt.setInt(11, waypoint.getTeleportCount());
                pstmt.setDouble(12, waypoint.getCreatePrice());
                pstmt.setDouble(13, waypoint.getTeleportPrice());
                pstmt.setBoolean(14, waypoint.isRequiresPermission());
                pstmt.setString(15, "");
                pstmt.setLong(16, waypoint.getCreatedTime());
                pstmt.executeUpdate();
            }
            waypointCache.put(waypoint.getId(), waypoint);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save waypoint: " + e.getMessage());
        }
    }

    public Waypoint getWaypoint(String waypointId) {
        Waypoint waypoint = waypointCache.get(waypointId);
        if (waypoint == null) {
            waypoint = loadWaypointFromDB(waypointId);
            if (waypoint != null) {
                waypointCache.put(waypointId, waypoint);
            }
        }
        return waypoint;
    }

    private Waypoint loadWaypointFromDB(String waypointId) {
        try {
            String sql = "SELECT * FROM waypoints WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, waypointId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return resultSetToWaypoint(rs);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to load waypoint: " + e.getMessage());
        }
        return null;
    }

    public void deleteWaypoint(String waypointId) {
        try {
            String sql = "DELETE FROM waypoints WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, waypointId);
                pstmt.executeUpdate();
            }
            waypointCache.remove(waypointId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete waypoint: " + e.getMessage());
        }
    }

    public Collection<Waypoint> getAllWaypoints() {
        return waypointCache.values();
    }

    private Waypoint resultSetToWaypoint(ResultSet rs) throws SQLException {
        return new Waypoint(
                rs.getString("id"),
                rs.getString("name"),
                UUID.fromString(rs.getString("creator_uuid")),
                rs.getString("world_name"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getFloat("yaw"),
                rs.getFloat("pitch"),
                rs.getInt("create_count"),
                rs.getInt("teleport_count"),
                rs.getDouble("create_price"),
                rs.getDouble("teleport_price"),
                rs.getBoolean("requires_permission"),
                rs.getLong("created_time")
        );
    }

    // ========== 服务器公库操作 ==========

    public double getTreasuryBalance() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT balance FROM server_treasury LIMIT 1")) {
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get treasury balance: " + e.getMessage());
        }
        return 0;
    }

    public boolean depositToTreasury(double amount) {
        if (amount <= 0) return false;
        try {
            String sql = "UPDATE server_treasury SET balance = balance + ?, total_deposited = total_deposited + ?, last_update = CURRENT_TIMESTAMP";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setDouble(2, amount);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to deposit to treasury: " + e.getMessage());
            return false;
        }
    }

    public boolean withdrawFromTreasury(double amount) {
        if (amount <= 0) return false;
        try {
            String sql = "UPDATE server_treasury SET balance = balance - ?, total_withdrawn = total_withdrawn + ?, last_update = CURRENT_TIMESTAMP WHERE balance >= ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setDouble(2, amount);
                pstmt.setDouble(3, amount);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to withdraw from treasury: " + e.getMessage());
            return false;
        }
    }

    public boolean addTaxToTreasury(double amount) {
        if (amount <= 0) return false;
        try {
            String sql = "UPDATE server_treasury SET balance = balance + ?, total_tax_collected = total_tax_collected + ?, last_update = CURRENT_TIMESTAMP";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setDouble(2, amount);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add tax to treasury: " + e.getMessage());
            return false;
        }
    }

    public double getTotalTaxCollected() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT total_tax_collected FROM server_treasury LIMIT 1")) {
            if (rs.next()) {
                return rs.getDouble("total_tax_collected");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get total tax collected: " + e.getMessage());
        }
        return 0;
    }

    // ========== 交易记录操作 ==========

    public void saveTransaction(TransactionRecord record) {
        try {
            String sql = "INSERT OR REPLACE INTO transactions (" +
                    "id, from_uuid, from_name, to_uuid, to_name, amount, tax_amount, type, service_id, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, record.id);
                pstmt.setString(2, record.fromUuid != null ? record.fromUuid.toString() : null);
                pstmt.setString(3, record.fromName);
                pstmt.setString(4, record.toUuid != null ? record.toUuid.toString() : null);
                pstmt.setString(5, record.toName);
                pstmt.setDouble(6, record.amount);
                pstmt.setDouble(7, record.taxAmount);
                pstmt.setString(8, record.type.name());
                pstmt.setString(9, record.serviceId);
                pstmt.setString(10, record.description);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save transaction: " + e.getMessage());
        }
    }

    public List<TransactionRecord> getTransactions(UUID playerUuid, int limit) {
        List<TransactionRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM transactions WHERE from_uuid = ? OR to_uuid = ? ORDER BY created_at DESC LIMIT ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUuid.toString());
                pstmt.setString(2, playerUuid.toString());
                pstmt.setInt(3, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(resultSetToTransaction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get transactions: " + e.getMessage());
        }
        return records;
    }

    public List<TransactionRecord> getTransactionsByType(TransactionRecord.TransactionType type, int limit) {
        List<TransactionRecord> records = new ArrayList<>();
        try {
            String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY created_at DESC LIMIT ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, type.name());
                pstmt.setInt(2, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(resultSetToTransaction(rs));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get transactions by type: " + e.getMessage());
        }
        return records;
    }

    private TransactionRecord resultSetToTransaction(ResultSet rs) throws SQLException {
        TransactionRecord record = new TransactionRecord();
        record.id = rs.getString("id");
        record.fromUuid = rs.getString("from_uuid") != null ? UUID.fromString(rs.getString("from_uuid")) : null;
        record.fromName = rs.getString("from_name");
        record.toUuid = rs.getString("to_uuid") != null ? UUID.fromString(rs.getString("to_uuid")) : null;
        record.toName = rs.getString("to_name");
        record.amount = rs.getDouble("amount");
        record.taxAmount = rs.getDouble("tax_amount");
        record.type = TransactionRecord.TransactionType.fromString(rs.getString("type"));
        record.serviceId = rs.getString("service_id");
        record.description = rs.getString("description");
        record.createdAt = rs.getTimestamp("created_at");
        return record;
    }

    // ========== 用户认证操作 ==========

    public boolean isRegistered(UUID uuid) {
        try {
            String sql = "SELECT COUNT(*) FROM user_auth WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check registration: " + e.getMessage());
            return false;
        }
    }

    public boolean registerUser(UUID uuid, String username, String passwordHash, String salt) {
        try {
            String sql = "INSERT INTO user_auth (uuid, username, password_hash, salt, registered_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, username);
                pstmt.setString(3, passwordHash);
                pstmt.setString(4, salt);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to register user: " + e.getMessage());
            return false;
        }
    }

    public String getPasswordHash(UUID uuid) {
        try {
            String sql = "SELECT password_hash FROM user_auth WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getString("password_hash") : null;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get password hash: " + e.getMessage());
            return null;
        }
    }

    public String getSalt(UUID uuid) {
        try {
            String sql = "SELECT salt FROM user_auth WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getString("salt") : null;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get salt: " + e.getMessage());
            return null;
        }
    }

    public boolean updatePassword(UUID uuid, String newPasswordHash, String newSalt) {
        try {
            String sql = "UPDATE user_auth SET password_hash = ?, salt = ? WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, newPasswordHash);
                pstmt.setString(2, newSalt);
                pstmt.setString(3, uuid.toString());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update password: " + e.getMessage());
            return false;
        }
    }

    public boolean updateLoginInfo(UUID uuid, String ip) {
        try {
            String sql = "UPDATE user_auth SET last_login_ip = ?, last_login_time = CURRENT_TIMESTAMP, failed_login_attempts = 0 WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, ip);
                pstmt.setString(2, uuid.toString());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update login info: " + e.getMessage());
            return false;
        }
    }

    public boolean incrementFailedAttempts(UUID uuid) {
        try {
            String sql = "UPDATE user_auth SET failed_login_attempts = failed_login_attempts + 1 WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to increment failed attempts: " + e.getMessage());
            return false;
        }
    }

    public int getFailedLoginAttempts(UUID uuid) {
        try {
            String sql = "SELECT failed_login_attempts FROM user_auth WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getInt("failed_login_attempts") : 0;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get failed attempts: " + e.getMessage());
            return 0;
        }
    }

    public boolean lockAccount(UUID uuid, long lockSeconds) {
        try {
            String sql = "UPDATE user_auth SET locked_until = DATETIME('now', ?) WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, lockSeconds + " seconds");
                pstmt.setString(2, uuid.toString());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to lock account: " + e.getMessage());
            return false;
        }
    }

    public boolean isAccountLocked(UUID uuid) {
        try {
            String sql = "SELECT locked_until FROM user_auth WHERE uuid = ? AND locked_until > DATETIME('now')";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check account lock: " + e.getMessage());
            return false;
        }
    }

    public String getAccountLockTime(UUID uuid) {
        try {
            String sql = "SELECT locked_until FROM user_auth WHERE uuid = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getString("locked_until") : null;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get lock time: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database connection: " + e.getMessage());
        }
    }
}
