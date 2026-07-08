package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import com.scroam.db.data.TransactionRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TreasuryManager {

    private final ScroamDB plugin;
    private final DatabaseManager db;

    public static final UUID TREASURY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String TREASURY_NAME = "ServerTreasury";

    public TreasuryManager(ScroamDB plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
    }

    public double getBalance() {
        return db.getTreasuryBalance();
    }

    public boolean deposit(double amount, String reason) {
        if (amount <= 0) return false;
        boolean success = db.depositToTreasury(amount);
        if (success) {
            saveTransaction(null, null, amount, 0,
                    TransactionRecord.TransactionType.TREASURY, "treasury_deposit", reason);
            plugin.getLogger().info("Treasury deposited: " + amount + " (" + reason + ")");
        }
        return success;
    }

    public boolean withdraw(double amount, String reason) {
        if (amount <= 0) return false;
        boolean success = db.withdrawFromTreasury(amount);
        if (success) {
            saveTransaction(TREASURY_UUID, TREASURY_NAME, null, null, amount, 0,
                    TransactionRecord.TransactionType.TREASURY, "treasury_withdraw", reason);
            plugin.getLogger().info("Treasury withdrawn: " + amount + " (" + reason + ")");
        }
        return success;
    }

    public boolean addTax(double amount) {
        if (amount <= 0) return false;
        boolean success = db.addTaxToTreasury(amount);
        if (success) {
            saveTransaction(null, null, amount, amount,
                    TransactionRecord.TransactionType.TAX, "tax_collection", "Tax collected");
        }
        return success;
    }

    public double getTotalTaxCollected() {
        return db.getTotalTaxCollected();
    }

    public boolean distributeToPlayer(UUID playerUuid, double amount, String reason) {
        if (amount <= 0) return false;
        if (!db.withdrawFromTreasury(amount)) return false;

        db.depositBalance(playerUuid, amount);
        saveTransaction(TREASURY_UUID, TREASURY_NAME, playerUuid, getPlayerName(playerUuid),
                amount, 0, TransactionRecord.TransactionType.TREASURY, "treasury_distribute", reason);
        return true;
    }

    public boolean distributeToAllPlayers(double amountPerPlayer, String reason) {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (distributeToPlayer(player.getUniqueId(), amountPerPlayer, reason)) {
                count++;
            }
        }
        return count > 0;
    }

    private void saveTransaction(UUID fromUuid, String fromName, UUID toUuid, String toName,
                                 double amount, double taxAmount, TransactionRecord.TransactionType type,
                                 String serviceId, String description) {
        TransactionRecord record = new TransactionRecord(
                TransactionRecord.generateId(),
                fromUuid, fromName,
                toUuid, toName,
                amount, taxAmount,
                type, serviceId, description
        );
        db.saveTransaction(record);
    }

    private void saveTransaction(UUID fromUuid, String fromName, double amount, double taxAmount,
                                 TransactionRecord.TransactionType type, String serviceId, String description) {
        saveTransaction(fromUuid, fromName, TREASURY_UUID, TREASURY_NAME, amount, taxAmount, type, serviceId, description);
    }

    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        return "Unknown";
    }
}