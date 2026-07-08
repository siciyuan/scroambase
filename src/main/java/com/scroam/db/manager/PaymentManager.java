package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import com.scroam.db.data.TransactionRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentManager {

    private final ScroamDB plugin;
    private final DatabaseManager db;
    private final TreasuryManager treasury;
    private final EconomyManager economy;

    private double serviceTaxRate = 0.05;

    public PaymentManager(ScroamDB plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        this.treasury = plugin.getTreasuryManager();
        this.economy = plugin.getEconomyManager();
        loadTaxSettings();
    }

    private void loadTaxSettings() {
        serviceTaxRate = plugin.getConfig().getDouble("economy.service-tax", 0.05);
    }

    public PaymentResult payForService(UUID playerUuid, String serviceId, double amount, String description) {
        if (amount <= 0) {
            return PaymentResult.failed("Invalid amount");
        }

        if (!economy.hasEnough(playerUuid, amount)) {
            return PaymentResult.failed("Insufficient balance");
        }

        double tax = amount * serviceTaxRate;
        double afterTax = amount - tax;

        if (!economy.withdraw(playerUuid, amount)) {
            return PaymentResult.failed("Withdrawal failed");
        }

        treasury.addTax(tax);

        String playerName = getPlayerName(playerUuid);
        TransactionRecord record = new TransactionRecord(
                TransactionRecord.generateId(),
                playerUuid, playerName,
                TreasuryManager.TREASURY_UUID, TreasuryManager.TREASURY_NAME,
                amount, tax,
                TransactionRecord.TransactionType.PAYMENT,
                serviceId,
                description
        );
        db.saveTransaction(record);

        return PaymentResult.success(amount, tax, afterTax, record.id);
    }

    public PaymentResult payForService(Player player, String serviceId, double amount, String description) {
        return payForService(player.getUniqueId(), serviceId, amount, description);
    }

    public PaymentResult payPlayer(UUID fromUuid, UUID toUuid, double amount) {
        if (fromUuid.equals(toUuid)) {
            return PaymentResult.failed("Cannot pay yourself");
        }

        if (amount <= 0) {
            return PaymentResult.failed("Invalid amount");
        }

        if (!economy.hasEnough(fromUuid, amount)) {
            return PaymentResult.failed("Insufficient balance");
        }

        double transferTax = amount * economy.getTransferTax();
        double afterTax = amount - transferTax;

        if (!economy.withdraw(fromUuid, amount)) {
            return PaymentResult.failed("Withdrawal failed");
        }

        economy.deposit(toUuid, afterTax);
        treasury.addTax(transferTax);

        String fromName = getPlayerName(fromUuid);
        String toName = getPlayerName(toUuid);
        TransactionRecord record = new TransactionRecord(
                TransactionRecord.generateId(),
                fromUuid, fromName,
                toUuid, toName,
                amount, transferTax,
                TransactionRecord.TransactionType.TRANSFER,
                "player_transfer",
                "Player transfer"
        );
        db.saveTransaction(record);

        return PaymentResult.success(amount, transferTax, afterTax, record.id);
    }

    public PaymentResult payPlayer(Player from, Player to, double amount) {
        return payPlayer(from.getUniqueId(), to.getUniqueId(), amount);
    }

    public PaymentResult refund(UUID playerUuid, double amount, String reason, String originalTxnId) {
        if (amount <= 0) {
            return PaymentResult.failed("Invalid amount");
        }

        if (!treasury.withdraw(amount, reason)) {
            return PaymentResult.failed("Insufficient treasury balance");
        }

        economy.deposit(playerUuid, amount);

        String playerName = getPlayerName(playerUuid);
        TransactionRecord record = new TransactionRecord(
                TransactionRecord.generateId(),
                TreasuryManager.TREASURY_UUID, TreasuryManager.TREASURY_NAME,
                playerUuid, playerName,
                amount, 0,
                TransactionRecord.TransactionType.PAYMENT,
                "refund_" + (originalTxnId != null ? originalTxnId : ""),
                "Refund: " + reason
        );
        db.saveTransaction(record);

        return PaymentResult.success(amount, 0, amount, record.id);
    }

    public PaymentResult refund(Player player, double amount, String reason, String originalTxnId) {
        return refund(player.getUniqueId(), amount, reason, originalTxnId);
    }

    public boolean chargePlayer(UUID playerUuid, double amount, String serviceId, String description) {
        PaymentResult result = payForService(playerUuid, serviceId, amount, description);
        return result.success;
    }

    public boolean chargePlayer(Player player, double amount, String serviceId, String description) {
        return chargePlayer(player.getUniqueId(), amount, serviceId, description);
    }

    public double getServiceTaxRate() {
        return serviceTaxRate;
    }

    public void setServiceTaxRate(double rate) {
        this.serviceTaxRate = Math.max(0, Math.min(1, rate));
        plugin.getConfig().set("economy.service-tax", this.serviceTaxRate);
        plugin.saveConfig();
    }

    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        return "Unknown";
    }

    public static class PaymentResult {
        public final boolean success;
        public final String message;
        public final double totalAmount;
        public final double taxAmount;
        public final double afterTaxAmount;
        public final String transactionId;

        private PaymentResult(boolean success, String message, double totalAmount,
                              double taxAmount, double afterTaxAmount, String transactionId) {
            this.success = success;
            this.message = message;
            this.totalAmount = totalAmount;
            this.taxAmount = taxAmount;
            this.afterTaxAmount = afterTaxAmount;
            this.transactionId = transactionId;
        }

        public static PaymentResult success(double totalAmount, double taxAmount, double afterTaxAmount, String transactionId) {
            return new PaymentResult(true, "Success", totalAmount, taxAmount, afterTaxAmount, transactionId);
        }

        public static PaymentResult failed(String message) {
            return new PaymentResult(false, message, 0, 0, 0, null);
        }
    }
}