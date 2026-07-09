package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class SignInManager {

    private final ScroamDB plugin;
    private final DatabaseManager db;
    private final EconomyManager economy;

    private double baseReward = 100.0;
    private double consecutiveBonus = 20.0;
    private int maxConsecutiveBonus = 7;

    public SignInManager(ScroamDB plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        this.economy = plugin.getEconomyManager();
        loadSettings();
    }

    private void loadSettings() {
        baseReward = plugin.getConfig().getDouble("signin.base-reward", 100.0);
        consecutiveBonus = plugin.getConfig().getDouble("signin.consecutive-bonus", 20.0);
        maxConsecutiveBonus = plugin.getConfig().getInt("signin.max-consecutive-bonus", 7);
    }

    private int getTodayDate() {
        return (int) LocalDate.now(ZoneId.systemDefault()).toEpochDay();
    }

    public SignInResult signIn(Player player) {
        UUID uuid = player.getUniqueId();
        int todayDate = getTodayDate();

        if (db.isSignedToday(uuid, todayDate)) {
            return SignInResult.failed("今天已经签到过了");
        }

        DatabaseManager.SignInRecord record = db.getSignInRecord(uuid);
        int consecutiveDays = 0;
        int totalDays = 0;

        if (record != null) {
            int yesterdayDate = todayDate - 1;
            if (record.lastSignDate == yesterdayDate) {
                consecutiveDays = record.consecutiveDays + 1;
            } else {
                consecutiveDays = 1;
            }
            totalDays = record.totalDays + 1;
        } else {
            consecutiveDays = 1;
            totalDays = 1;
        }

        double reward = calculateReward(consecutiveDays);

        economy.deposit(player, reward);
        db.updateSignInRecord(uuid, todayDate, totalDays, consecutiveDays);
        return SignInResult.success(reward, consecutiveDays, totalDays);
    }

    public double calculateReward(int consecutiveDays) {
        int bonusDays = Math.min(consecutiveDays - 1, maxConsecutiveBonus);
        return baseReward + (bonusDays * consecutiveBonus);
    }

    public SignInInfo getSignInInfo(Player player) {
        UUID uuid = player.getUniqueId();
        int todayDate = getTodayDate();

        DatabaseManager.SignInRecord record = db.getSignInRecord(uuid);
        boolean signedToday = db.isSignedToday(uuid, todayDate);
        int consecutiveDays = record != null ? record.consecutiveDays : 0;
        int totalDays = record != null ? record.totalDays : 0;
        double todayReward = signedToday ? 0 : calculateReward(consecutiveDays + 1);

        return new SignInInfo(signedToday, consecutiveDays, totalDays, todayReward);
    }

    public static class SignInResult {
        public final boolean success;
        public final String message;
        public final double reward;
        public final int consecutiveDays;
        public final int totalDays;

        private SignInResult(boolean success, String message, double reward, int consecutiveDays, int totalDays) {
            this.success = success;
            this.message = message;
            this.reward = reward;
            this.consecutiveDays = consecutiveDays;
            this.totalDays = totalDays;
        }

        public static SignInResult success(double reward, int consecutiveDays, int totalDays) {
            String msg = String.format("签到成功！获得 %.0f Coins，连续签到 %d 天，累计签到 %d 天",
                    reward, consecutiveDays, totalDays);
            return new SignInResult(true, msg, reward, consecutiveDays, totalDays);
        }

        public static SignInResult failed(String message) {
            return new SignInResult(false, message, 0, 0, 0);
        }
    }

    public static class SignInInfo {
        public final boolean signedToday;
        public final int consecutiveDays;
        public final int totalDays;
        public final double todayReward;

        public SignInInfo(boolean signedToday, int consecutiveDays, int totalDays, double todayReward) {
            this.signedToday = signedToday;
            this.consecutiveDays = consecutiveDays;
            this.totalDays = totalDays;
            this.todayReward = todayReward;
        }
    }
}