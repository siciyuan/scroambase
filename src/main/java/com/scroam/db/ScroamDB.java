package com.scroam.db;

import com.scroam.db.api.ScroamDBAPI;
import com.scroam.db.command.EconomyCommand;
import com.scroam.db.listener.PlayerListener;
import com.scroam.db.manager.DatabaseManager;
import com.scroam.db.manager.EconomyManager;
import com.scroam.db.manager.PaymentManager;
import com.scroam.db.manager.TreasuryManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ScroamDB extends JavaPlugin {

    private static ScroamDB instance;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private TreasuryManager treasuryManager;
    private PaymentManager paymentManager;
    private ScroamDBAPI api;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        getLogger().info("Database system initialized!");

        treasuryManager = new TreasuryManager(this);
        getLogger().info("Treasury system initialized!");

        economyManager = new EconomyManager(this, databaseManager);
        getLogger().info("Economy system initialized!");

        paymentManager = new PaymentManager(this);
        getLogger().info("Payment system initialized!");

        api = new ScroamDBAPI();
        getLogger().info("API initialized!");

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("eco").setExecutor(new EconomyCommand(this));

        getLogger().info("ScroamDB v1.0.0 enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("ScroamDB disabled!");
    }

    public static ScroamDB getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public TreasuryManager getTreasuryManager() {
        return treasuryManager;
    }

    public PaymentManager getPaymentManager() {
        return paymentManager;
    }

    public ScroamDBAPI getApi() {
        return api;
    }
}