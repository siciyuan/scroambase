package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final ScroamDB plugin;
    private final DatabaseManager db;
    private final TreasuryManager treasury;

    private final Map<String, Double> exchangeRates = new HashMap<>();
    private double withdrawTax = 0.10;
    private double transferTax = 0.05;
    private double exchangeTax = 0.05;
    private String currencyName = "Coins";

    public EconomyManager(ScroamDB plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        this.treasury = plugin.getTreasuryManager();
        loadExchangeRates();
        loadTaxSettings();
        loadCurrencyName();
    }

    private void loadCurrencyName() {
        currencyName = plugin.getConfig().getString("economy.currency-name", "Coins");
    }

    public String getCurrencyName() {
        return currencyName;
    }

    private void loadExchangeRates() {
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
        exchangeTax = plugin.getConfig().getDouble("economy.exchange.exchange-tax", 0.05);
    }

    public double getBalance(UUID uuid) {
        return db.getBalance(uuid);
    }

    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    public boolean setBalance(UUID uuid, double amount) {
        if (amount < 0) return false;
        return db.setBalance(uuid, amount);
    }

    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) return;
        db.depositBalance(uuid, amount);
    }

    public void deposit(Player player, double amount) {
        deposit(player.getUniqueId(), amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        return db.withdrawBalance(uuid, amount);
    }

    public boolean withdraw(Player player, double amount) {
        return withdraw(player.getUniqueId(), amount);
    }

    public boolean hasEnough(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean hasEnough(Player player, double amount) {
        return hasEnough(player.getUniqueId(), amount);
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        double tax = amount * transferTax;
        double afterTax = amount - tax;

        if (!withdraw(from, amount)) return false;

        db.depositBalance(to, afterTax);
        treasury.addTax(tax);

        return true;
    }

    public boolean transfer(Player from, Player to, double amount) {
        return transfer(from.getUniqueId(), to.getUniqueId(), amount);
    }

    public double getExchangeRate(Material material) {
        String key = getMaterialKey(material);
        return exchangeRates.getOrDefault(key, 0.0);
    }

    public double getExchangeRate(String key) {
        return exchangeRates.getOrDefault(key.toLowerCase(), 0.0);
    }

    public boolean isExchangeable(Material material) {
        return getExchangeRate(material) > 0;
    }

    public double calculateExchangeValue(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        double rate = getExchangeRate(item.getType());
        if (rate <= 0) return 0;
        return rate * item.getAmount();
    }

    public double calculateExchangeValueAfterTax(ItemStack item) {
        double value = calculateExchangeValue(item);
        return value * (1 - exchangeTax);
    }

    public double exchangeItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        double value = calculateExchangeValue(item);
        if (value <= 0) return 0;

        double tax = value * exchangeTax;
        double afterTax = value - tax;

        int amount = item.getAmount();
        ItemStack remaining = player.getInventory().getItemInMainHand().clone();
        remaining.setAmount(amount);
        player.getInventory().setItemInMainHand(null);

        deposit(player, afterTax);
        treasury.addTax(tax);

        return afterTax;
    }

    public double exchangeAllItems(Player player) {
        double total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            total += calculateExchangeValue(item);
        }
        if (total <= 0) return 0;

        double tax = total * exchangeTax;
        double afterTax = total - tax;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && getExchangeRate(item.getType()) > 0) {
                player.getInventory().setItem(i, null);
            }
        }

        deposit(player, afterTax);
        treasury.addTax(tax);

        return afterTax;
    }

    public double calculateWithdrawTax(double amount) {
        return amount * withdrawTax;
    }

    public double calculateWithdrawAmount(double amount) {
        return amount * (1 - withdrawTax);
    }

    public double getWithdrawTax() {
        return withdrawTax;
    }

    public double getTransferTax() {
        return transferTax;
    }

    public double getExchangeTax() {
        return exchangeTax;
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

    public void setExchangeTax(double tax) {
        this.exchangeTax = Math.max(0, Math.min(1, tax));
        plugin.getConfig().set("economy.exchange.exchange-tax", this.exchangeTax);
        plugin.saveConfig();
    }

    private String getMaterialKey(Material material) {
        return material.name().toLowerCase();
    }

    public Map<String, Double> getAllExchangeRates() {
        return new HashMap<>(exchangeRates);
    }

    public String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}