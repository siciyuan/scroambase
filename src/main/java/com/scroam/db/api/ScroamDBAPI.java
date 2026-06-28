package com.scroam.db.api;

import com.scroam.db.ScroamDB;
import com.scroam.db.data.*;
import com.scroam.db.manager.DatabaseManager;
import com.scroam.db.manager.EconomyManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * ScroamDB API - 其他插件可以通过这个接口调用ScroamDB的功能
 */
public class ScroamDBAPI {

    private static ScroamDBAPI instance;
    private final DatabaseManager db;
    private final EconomyManager economy;

    public ScroamDBAPI() {
        instance = this;
        ScroamDB plugin = ScroamDB.getInstance();
        this.db = plugin.getDatabaseManager();
        this.economy = plugin.getEconomyManager();
    }

    public static ScroamDBAPI getInstance() {
        return instance;
    }

    // ========== 世界数据操作 ==========

    /**
     * 获取世界数据
     */
    public WorldData getWorld(String worldName) {
        return db.getWorld(worldName);
    }

    /**
     * 保存世界数据
     */
    public void saveWorld(WorldData worldData) {
        db.saveWorld(worldData);
    }

    /**
     * 获取世界出生点
     */
    public Location getWorldSpawn(String worldName) {
        return db.getWorldSpawn(worldName);
    }

    /**
     * 设置世界出生点
     */
    public void setWorldSpawn(String worldName, Location location) {
        db.setWorldSpawn(worldName, location);
    }

    /**
     * 获取主世界
     */
    public WorldData getMainWorld() {
        return db.getMainWorld();
    }

    /**
     * 设置主世界
     */
    public void setMainWorld(String worldName) {
        db.setMainWorld(worldName);
    }

    /**
     * 获取所有世界
     */
    public Collection<WorldData> getAllWorlds() {
        return db.getAllWorlds();
    }

    // ========== 用户数据操作 ==========

    /**
     * 获取用户数据
     */
    public UserData getUser(UUID uuid) {
        return db.getUser(uuid);
    }

    /**
     * 创建用户
     */
    public UserData createUser(UUID uuid, String username) {
        return db.createUser(uuid, username);
    }

    /**
     * 保存用户数据
     */
    public void saveUser(UserData userData) {
        db.saveUser(userData);
    }

    /**
     * 获取所有用户
     */
    public Collection<UserData> getAllUsers() {
        return db.getAllUsers();
    }

    // ========== 经济系统 ==========

    /**
     * 获取玩家余额
     */
    public double getBalance(UUID uuid) {
        return economy.getBalance(uuid);
    }

    /**
     * 获取玩家余额
     */
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    /**
     * 设置玩家余额
     */
    public boolean setBalance(UUID uuid, double amount) {
        return economy.setBalance(uuid, amount);
    }

    /**
     * 给予玩家货币
     */
    public void deposit(UUID uuid, double amount) {
        economy.deposit(uuid, amount);
    }

    /**
     * 给予玩家货币
     */
    public void deposit(Player player, double amount) {
        economy.deposit(player, amount);
    }

    /**
     * 扣除玩家货币
     */
    public boolean withdraw(UUID uuid, double amount) {
        return economy.withdraw(uuid, amount);
    }

    /**
     * 扣除玩家货币
     */
    public boolean withdraw(Player player, double amount) {
        return economy.withdraw(player, amount);
    }

    /**
     * 检查玩家是否有足够货币
     */
    public boolean hasEnough(UUID uuid, double amount) {
        return economy.hasEnough(uuid, amount);
    }

    /**
     * 检查玩家是否有足够货币
     */
    public boolean hasEnough(Player player, double amount) {
        return economy.hasEnough(player, amount);
    }

    /**
     * 转账
     */
    public boolean transfer(UUID from, UUID to, double amount) {
        return economy.transfer(from, to, amount);
    }

    /**
     * 计算物品兑换价值
     */
    public double calculateExchangeValue(org.bukkit.inventory.ItemStack item) {
        return economy.calculateExchangeValue(item);
    }

    /**
     * 玩家兑换物品为货币
     */
    public double exchangeItem(Player player, org.bukkit.inventory.ItemStack item) {
        return economy.exchangeItem(player, item);
    }

    /**
     * 获取汇率
     */
    public double getExchangeRate(String key) {
        return economy.getExchangeRate(key);
    }

    /**
     * 格式化为货币字符串
     */
    public String formatMoney(double amount) {
        return economy.formatMoney(amount);
    }

    // ========== 家数据操作 ==========

    /**
     * 保存家
     */
    public void saveHome(UUID userUuid, String homeName, Location location) {
        db.saveHome(userUuid, homeName, location);
    }

    /**
     * 获取家
     */
    public Location getHome(UUID userUuid, String homeName) {
        return db.getHome(userUuid, homeName);
    }

    /**
     * 删除家
     */
    public boolean deleteHome(UUID userUuid, String homeName) {
        return db.deleteHome(userUuid, homeName);
    }

    /**
     * 获取玩家所有家
     */
    public Map<String, Location> getHomes(UUID userUuid) {
        return db.getHomes(userUuid);
    }

    /**
     * 获取玩家家数量
     */
    public int getHomeCount(UUID userUuid) {
        return db.getHomeCount(userUuid);
    }

    // ========== 领地数据操作 ==========

    /**
     * 保存领地
     */
    public void saveLand(Land land) {
        db.saveLand(land);
    }

    /**
     * 获取领地
     */
    public Land getLand(String landId) {
        return db.getLand(landId);
    }

    /**
     * 删除领地
     */
    public void deleteLand(String landId) {
        db.deleteLand(landId);
    }

    /**
     * 获取所有领地
     */
    public Collection<Land> getAllLands() {
        return db.getAllLands();
    }

    /**
     * 获取指定位置的领地
     */
    public Land getLandAt(Location location) {
        return db.getLandAt(location);
    }

    // ========== 地标/景点数据操作 ==========

    /**
     * 保存地标
     */
    public void saveWaypoint(Waypoint waypoint) {
        db.saveWaypoint(waypoint);
    }

    /**
     * 获取地标
     */
    public Waypoint getWaypoint(String waypointId) {
        return db.getWaypoint(waypointId);
    }

    /**
     * 删除地标
     */
    public void deleteWaypoint(String waypointId) {
        db.deleteWaypoint(waypointId);
    }

    /**
     * 获取所有地标
     */
    public Collection<Waypoint> getAllWaypoints() {
        return db.getAllWaypoints();
    }
}
