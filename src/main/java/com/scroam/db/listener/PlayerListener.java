package com.scroam.db.listener;

import com.scroam.db.ScroamDB;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器 - 自动创建用户数据
 */
public class PlayerListener implements Listener {

    private final ScroamDB plugin;

    public PlayerListener(ScroamDB plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 自动创建用户数据
        plugin.getDatabaseManager().createUser(player.getUniqueId(), player.getName());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 保存用户数据
        com.scroam.db.data.UserData user = plugin.getDatabaseManager().getUser(player.getUniqueId());
        if (user != null) {
            user.username = player.getName();
            plugin.getDatabaseManager().saveUser(user);
        }
    }
}
