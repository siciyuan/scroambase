package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 经济系统管理器
 */
public class EconomyManager {

    private final ScroamDB plugin;
    private final DatabaseManager db;

    // 默认兑换比例
    private final Map<String, Double> exchangeRates = new HashMap<>();
    private double withdrawTax = 0.10;
    private double transferTax = 0.05;

    public EconomyManager(ScroamDB plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        loadExchangeRates();
        loadTaxSettings();
    }

    private void loadExchangeRates() {
        // 默认兑换比例
        exchangeRates.put("gold_ingot", 100.0);
        exchangeRates.put("gold_nugget", 11.11);
        exchangeRates.put("gold_block", 900.0);
        exchangeRates.put("raw_gold", 50.0);
        exchangeRates.put("iron_ingot", 20.0);
        exchangeRates.put("iron_nugget", 2.22);
        exchangeRates.put("iron_block", 180.0);
        exchangeRates.put("raw_iron", 10.0);
        exchangeRates.put("copper_ingot", 10.0);
        exchangeRates.put("copper_nugget", 1.11);
        exchangeRates.put("copper_block", 90.0);
        exchangeRates.put("raw_copper", 5.0);
        exchangeRates.put("diamond", 500.0);
        exchangeRates.put("diamond_block", 4500.0);
        exchangeRates.put("netherite_ingot", 2000.0);
        exchangeRates.put("netherite_block", 18000.0);
        exchangeRates.put("emerald", 300.0);
        exchangeRates.put("emerald_block", 2700.0);
        exchangeRates.put("coal", 10.0);
        exchangeRates.put("charcoal", 8.0);
        exchangeRates.put("coal_block", 90.0);
        exchangeRates.put("lapis_lazuli", 5.0);

        // 从配置文件加载
        if (plugin.getConfig().contains("economy.exchange.rates")) {
            for (String key : plugin.getConfig().getConfigurationSection("economy.exchange.rates").getKeys(false)) {
                double rate = plugin.getConfig().getDouble("economy.exchange.rates." + key);
                exchangeRates.put(key, rate);
            }
        }
    }

    private void loadTaxSettings() {
        withdrawTax = plugin.getConfig().getDouble("economy.exchange.withdraw-tax", 0.10);
        transferTax = plugin.getConfig().getDouble("economy.transfer-tax", 0.05);
    }

    // ========== 基础经济操作 ==========

    /**
     * 获取玩家余额
     */
    public double getBalance(UUID uuid) {
        return db.getBalance(uuid);
    }

    /**
     * 获取玩家余额（通过玩家对象）
     */
    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    /**
     * 设置玩家余额
     */
    public boolean setBalance(UUID uuid, double amount) {
        if (amount < 0) return false;
        return db.setBalance(uuid, amount);
    }

    /**
     * 给予玩家货币
     */
    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) return;
        db.depositBalance(uuid, amount);
    }

    /**
     * 给予玩家货币（通过玩家对象）
     */
    public void deposit(Player player, double amount) {
        deposit(player.getUniqueId(), amount);
    }

    /**
     * 扣除玩家货币
     */
    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        return db.withdrawBalance(uuid, amount);
    }

    /**
     * 扣除玩家货币（通过玩家对象）
     */
    public boolean withdraw(Player player, double amount) {
        return withdraw(player.getUniqueId(), amount);
    }

    /**
     * 检查玩家是否有足够货币
     */
    public boolean hasEnough(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    /**
     * 检查玩家是否有足够货币（通过玩家对象）
     */
    public boolean hasEnough(Player player, double amount) {
        return hasEnough(player.getUniqueId(), amount);
    }

    /**
     * 转账
     */
    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        double afterTax = amount * (1 - transferTax);
        if (!withdraw(from, amount)) return false;
        db.depositBalance(to, afterTax);
        return true;
    }

    // ========== 物品兑换 ==========

    /**
     * 获取物品对应的兑换比率
     */
    public double getExchangeRate(Material material) {
        String key = getMaterialKey(material);
        return exchangeRates.getOrDefault(key, 0.0);
    }

    /**
     * 获取物品兑换比率（通过字符串key）
     */
    public double getExchangeRate(String key) {
        return exchangeRates.getOrDefault(key.toLowerCase(), 0.0);
    }

    /**
     * 检查物品是否可以兑换
     */
    public boolean isExchangeable(Material material) {
        return getExchangeRate(material) > 0;
    }

    /**
     * 计算物品兑换价值
     */
    public double calculateExchangeValue(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        double rate = getExchangeRate(item.getType());
        if (rate <= 0) return 0;
        return rate * item.getAmount();
    }

    /**
     * 玩家兑换物品为货币
     */
    public double exchangeItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        double value = calculateExchangeValue(item);
        if (value <= 0) return 0;

        // 移除物品
        int amount = item.getAmount();
        ItemStack remaining = player.getInventory().getItemInMainHand().clone();
        remaining.setAmount(amount);
        player.getInventory().setItemInMainHand(null);

        // 给玩家货币
        deposit(player, value);
        return value;
    }

    /**
     * 玩家兑换所有物品为货币
     */
    public double exchangeAllItems(Player player) {
        double total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            total += calculateExchangeValue(item);
        }
        if (total <= 0) return 0;

        // 清除所有可兑换物品
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && getExchangeRate(item.getType()) > 0) {
                player.getInventory().setItem(i, null);
            }
        }

        deposit(player, total);
        return total;
    }

    // ========== 取款相关 ==========

    /**
     * 计算取款手续费
     */
    public double calculateWithdrawTax(double amount) {
        return amount * withdrawTax;
    }

    /**
     * 计算取款后实际金额
     */
    public double calculateWithdrawAmount(double amount) {
        return amount * (1 - withdrawTax);
    }

    /**
     * 获取当前税率
     */
    public double getWithdrawTax() {
        return withdrawTax;
    }

    public double getTransferTax() {
        return transferTax;
    }

    public void setWithdrawTax(double tax) {
        this.withdrawTax = Math.max(0, Math.min(1, tax));
        plugin.getConfig().set("economy.exchange.withdraw-tax", this.withdrawTax);
        plugin.saveConfig();
    }

    public void setTransferTax(double tax) {
        this.transferTax = Math.max(0, Math.min(1, tax));
        plugin.getConfig().set("economy.transfer-tax", this.transferTax);
        plugin.saveConfig();
    }

    // ========== 工具方法 ==========

    private String getMaterialKey(Material material) {
        return material.name().toLowerCase();
    }

    /**
     * 获取所有支持的兑换物品
     */
    public Map<String, Double> getAllExchangeRates() {
        return new HashMap<>(exchangeRates);
    }

    /**
     * 格式化为货币字符串
     */
    public String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}
