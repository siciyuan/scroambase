package com.scroam.db.api;

import com.scroam.db.ScroamDB;
import com.scroam.db.data.*;
import com.scroam.db.manager.DatabaseManager;
import com.scroam.db.manager.EconomyManager;
import com.scroam.db.manager.LoginManager;
import com.scroam.db.manager.PaymentManager;
import com.scroam.db.manager.TreasuryManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScroamDBAPI {

    private static ScroamDBAPI instance;
    private final DatabaseManager db;
    private final EconomyManager economy;
    private final TreasuryManager treasury;
    private final PaymentManager payment;
    private final LoginManager login;

    public ScroamDBAPI() {
        instance = this;
        ScroamDB plugin = ScroamDB.getInstance();
        this.db = plugin.getDatabaseManager();
        this.economy = plugin.getEconomyManager();
        this.treasury = plugin.getTreasuryManager();
        this.payment = plugin.getPaymentManager();
        this.login = plugin.getLoginManager();
    }

    public static ScroamDBAPI getInstance() {
        return instance;
    }

    // ========== 世界数据操作 ==========

    public WorldData getWorld(String worldName) {
        return db.getWorld(worldName);
    }

    public void saveWorld(WorldData worldData) {
        db.saveWorld(worldData);
    }

    public Location getWorldSpawn(String worldName) {
        return db.getWorldSpawn(worldName);
    }

    public void setWorldSpawn(String worldName, Location location) {
        db.setWorldSpawn(worldName, location);
    }

    public WorldData getMainWorld() {
        return db.getMainWorld();
    }

    public void setMainWorld(String worldName) {
        db.setMainWorld(worldName);
    }

    public Collection<WorldData> getAllWorlds() {
        return db.getAllWorlds();
    }

    // ========== 用户数据操作 ==========

    public UserData getUser(UUID uuid) {
        return db.getUser(uuid);
    }

    public UserData createUser(UUID uuid, String username) {
        return db.createUser(uuid, username);
    }

    public void saveUser(UserData userData) {
        db.saveUser(userData);
    }

    public Collection<UserData> getAllUsers() {
        return db.getAllUsers();
    }

    // ========== 经济系统基础操作 ==========

    public double getBalance(UUID uuid) {
        return economy.getBalance(uuid);
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public boolean setBalance(UUID uuid, double amount) {
        return economy.setBalance(uuid, amount);
    }

    public void deposit(UUID uuid, double amount) {
        economy.deposit(uuid, amount);
    }

    public void deposit(Player player, double amount) {
        economy.deposit(player, amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        return economy.withdraw(uuid, amount);
    }

    public boolean withdraw(Player player, double amount) {
        return economy.withdraw(player, amount);
    }

    public boolean hasEnough(UUID uuid, double amount) {
        return economy.hasEnough(uuid, amount);
    }

    public boolean hasEnough(Player player, double amount) {
        return economy.hasEnough(player, amount);
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        return economy.transfer(from, to, amount);
    }

    public boolean transfer(Player from, Player to, double amount) {
        return economy.transfer(from, to, amount);
    }

    public double getExchangeRate(String key) {
        return economy.getExchangeRate(key);
    }

    public String formatMoney(double amount) {
        return economy.formatMoney(amount);
    }

    public String getCurrencyName() {
        return economy.getCurrencyName();
    }

    // ========== 支付系统 API ==========

    public PaymentManager.PaymentResult payForService(UUID playerUuid, String serviceId, double amount, String description) {
        return payment.payForService(playerUuid, serviceId, amount, description);
    }

    public PaymentManager.PaymentResult payForService(Player player, String serviceId, double amount, String description) {
        return payment.payForService(player, serviceId, amount, description);
    }

    public PaymentManager.PaymentResult payPlayer(UUID fromUuid, UUID toUuid, double amount) {
        return payment.payPlayer(fromUuid, toUuid, amount);
    }

    public PaymentManager.PaymentResult payPlayer(Player from, Player to, double amount) {
        return payment.payPlayer(from, to, amount);
    }

    public PaymentManager.PaymentResult refund(UUID playerUuid, double amount, String reason, String originalTxnId) {
        return payment.refund(playerUuid, amount, reason, originalTxnId);
    }

    public PaymentManager.PaymentResult refund(Player player, double amount, String reason, String originalTxnId) {
        return payment.refund(player, amount, reason, originalTxnId);
    }

    public boolean chargePlayer(UUID playerUuid, double amount, String serviceId, String description) {
        return payment.chargePlayer(playerUuid, amount, serviceId, description);
    }

    public boolean chargePlayer(Player player, double amount, String serviceId, String description) {
        return payment.chargePlayer(player, amount, serviceId, description);
    }

    public double getServiceTaxRate() {
        return payment.getServiceTaxRate();
    }

    public void setServiceTaxRate(double rate) {
        payment.setServiceTaxRate(rate);
    }

    // ========== 服务器公库 API ==========

    public double getTreasuryBalance() {
        return treasury.getBalance();
    }

    public boolean depositToTreasury(double amount, String reason) {
        return treasury.deposit(amount, reason);
    }

    public boolean withdrawFromTreasury(double amount, String reason) {
        return treasury.withdraw(amount, reason);
    }

    public boolean addTaxToTreasury(double amount) {
        return treasury.addTax(amount);
    }

    public double getTotalTaxCollected() {
        return treasury.getTotalTaxCollected();
    }

    public boolean distributeToPlayer(UUID playerUuid, double amount, String reason) {
        return treasury.distributeToPlayer(playerUuid, amount, reason);
    }

    public boolean distributeToAllPlayers(double amountPerPlayer, String reason) {
        return treasury.distributeToAllPlayers(amountPerPlayer, reason);
    }

    // ========== 交易记录 API ==========

    public List<TransactionRecord> getPlayerTransactions(UUID playerUuid, int limit) {
        return db.getTransactions(playerUuid, limit);
    }

    public List<TransactionRecord> getTransactionsByType(TransactionRecord.TransactionType type, int limit) {
        return db.getTransactionsByType(type, limit);
    }

    // ========== 税率管理 API ==========

    public double getWithdrawTax() {
        return economy.getWithdrawTax();
    }

    public double getTransferTax() {
        return economy.getTransferTax();
    }

    public double getExchangeTax() {
        return economy.getExchangeTax();
    }

    public void setWithdrawTax(double tax) {
        economy.setWithdrawTax(tax);
    }

    public void setTransferTax(double tax) {
        economy.setTransferTax(tax);
    }

    public void setExchangeTax(double tax) {
        economy.setExchangeTax(tax);
    }

    // ========== 家数据操作 ==========

    public void saveHome(UUID userUuid, String homeName, Location location) {
        db.saveHome(userUuid, homeName, location);
    }

    public Location getHome(UUID userUuid, String homeName) {
        return db.getHome(userUuid, homeName);
    }

    public boolean deleteHome(UUID userUuid, String homeName) {
        return db.deleteHome(userUuid, homeName);
    }

    public Map<String, Location> getHomes(UUID userUuid) {
        return db.getHomes(userUuid);
    }

    public int getHomeCount(UUID userUuid) {
        return db.getHomeCount(userUuid);
    }

    // ========== 领地数据操作 ==========

    public void saveLand(Land land) {
        db.saveLand(land);
    }

    public Land getLand(String landId) {
        return db.getLand(landId);
    }

    public void deleteLand(String landId) {
        db.deleteLand(landId);
    }

    public Collection<Land> getAllLands() {
        return db.getAllLands();
    }

    public Land getLandAt(Location location) {
        return db.getLandAt(location);
    }

    // ========== 地标/景点数据操作 ==========

    public void saveWaypoint(Waypoint waypoint) {
        db.saveWaypoint(waypoint);
    }

    public Waypoint getWaypoint(String waypointId) {
        return db.getWaypoint(waypointId);
    }

    public void deleteWaypoint(String waypointId) {
        db.deleteWaypoint(waypointId);
    }

    public Collection<Waypoint> getAllWaypoints() {
        return db.getAllWaypoints();
    }

    // ========== 登录认证 API ==========

    public boolean isLoggedIn(UUID playerUuid) {
        return login.isLoggedIn(playerUuid);
    }

    public boolean isLoggedIn(Player player) {
        return login.isLoggedIn(player);
    }

    public boolean isRegistered(UUID playerUuid) {
        return login.isRegistered(playerUuid);
    }

    public boolean isRegistered(Player player) {
        return login.isRegistered(player);
    }

    public LoginManager.LoginResult login(Player player, String password) {
        return login.login(player, password);
    }

    public LoginManager.LoginResult register(Player player, String password, String confirmPassword) {
        return login.register(player, password, confirmPassword);
    }

    public LoginManager.LoginResult changePassword(Player player, String oldPassword, String newPassword) {
        return login.changePassword(player, oldPassword, newPassword);
    }

    public void logout(Player player) {
        login.logout(player);
    }

    public void logout(UUID playerUuid) {
        login.logout(playerUuid);
    }
}