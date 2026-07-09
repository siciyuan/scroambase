package com.scroam.db;

import com.scroam.db.api.ScroamDBAPI;
import com.scroam.db.command.EconomyCommand;
import com.scroam.db.command.LoginCommand;
import com.scroam.db.command.SignInCommand;
import com.scroam.db.gui.SignInGUI;
import com.scroam.db.listener.LoginListener;
import com.scroam.db.listener.PlayerListener;
import com.scroam.db.listener.SignInListener;
import com.scroam.db.manager.DatabaseManager;
import com.scroam.db.manager.EconomyManager;
import com.scroam.db.manager.LoginManager;
import com.scroam.db.manager.PaymentManager;
import com.scroam.db.manager.SignInManager;
import com.scroam.db.manager.TreasuryManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ScroamDB extends JavaPlugin {

    private static ScroamDB instance;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private TreasuryManager treasuryManager;
    private PaymentManager paymentManager;
    private LoginManager loginManager;
    private SignInManager signInManager;
    private SignInGUI signInGUI;
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

        loginManager = new LoginManager(this);
        getLogger().info("Login system initialized!");

        signInManager = new SignInManager(this);
        getLogger().info("Sign-in system initialized!");

        signInGUI = new SignInGUI(this);
        getLogger().info("Sign-in GUI initialized!");

        api = new ScroamDBAPI();
        getLogger().info("API initialized!");

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new SignInListener(this), this);

        getCommand("eco").setExecutor(new EconomyCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new LoginCommand(this));
        getCommand("changepassword").setExecutor(new LoginCommand(this));
        getCommand("logout").setExecutor(new LoginCommand(this));
        getCommand("signin").setExecutor(new SignInCommand(this));

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

    public LoginManager getLoginManager() {
        return loginManager;
    }

    public SignInManager getSignInManager() {
        return signInManager;
    }

    public SignInGUI getSignInGUI() {
        return signInGUI;
    }

    public ScroamDBAPI getApi() {
        return api;
    }
}