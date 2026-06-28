package com.scroam.db;

import com.scroam.db.api.ScroamDBAPI;
import com.scroam.db.listener.PlayerListener;
import com.scroam.db.manager.DatabaseManager;
import com.scroam.db.manager.EconomyManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ScroamDB extends JavaPlugin {

    private static ScroamDB instance;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private ScroamDBAPI api;

    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化数据库管理器
        databaseManager = new DatabaseManager(this);
        getLogger().info("Database system initialized!");
        
        // 初始化经济系统
        economyManager = new EconomyManager(this, databaseManager);
        getLogger().info("Economy system initialized!");
        
        // 初始化API
        api = new ScroamDBAPI();
        getLogger().info("API initialized!");
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
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

    public ScroamDBAPI getApi() {
        return api;
    }
}
