package com.scroam.db.listener;

import com.scroam.db.ScroamDB;
import com.scroam.db.manager.LoginManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class LoginListener implements Listener {

    private final ScroamDB plugin;
    private final LoginManager loginManager;

    public LoginListener(ScroamDB plugin) {
        this.plugin = plugin;
        this.loginManager = plugin.getLoginManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loginManager.onPlayerJoin(player);

        if (!loginManager.isLoggedIn(player)) {
            player.setInvulnerable(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        loginManager.onPlayerQuit(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            if (to != null && (from.getBlockX() != to.getBlockX() ||
                    from.getBlockY() != to.getBlockY() ||
                    from.getBlockZ() != to.getBlockZ())) {
                event.setCancelled(true);
                player.teleport(from);
                player.sendMessage("§c请先登录后再移动");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            event.setCancelled(true);
            player.sendMessage("§c请先登录后再传送");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleMove(VehicleMoveEvent event) {
        for (var passenger : event.getVehicle().getPassengers()) {
            if (passenger instanceof Player player && !loginManager.canInteract(player)) {
                event.getVehicle().removePassenger(player);
                player.sendMessage("§c请先登录后再乘坐交通工具");
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            event.setCancelled(true);
            player.sendMessage("§c请先登录后再聊天");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (!loginManager.canInteract(player)) {
            if (!command.startsWith("/login") &&
                    !command.startsWith("/register") &&
                    !command.startsWith("/changepassword")) {
                event.setCancelled(true);
                player.sendMessage("§c请先登录后再执行命令");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            event.setCancelled(true);
            player.sendMessage("§c请先登录后再破坏方块");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            event.setCancelled(true);
            player.sendMessage("§c请先登录后再放置方块");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            event.setCancelled(true);
            player.sendMessage("§c请先登录后再交互");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (!loginManager.isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            if (!loginManager.canInteract(player)) {
                event.setCancelled(true);
                player.sendMessage("§c请先登录后再攻击");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!loginManager.canInteract(player)) {
            event.setCancelled(true);
            player.sendMessage("§c请先登录后再丢弃物品");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (!loginManager.canInteract(player)) {
                event.setCancelled(true);
                player.sendMessage("§c请先登录后再拾取物品");
            }
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (loginManager.isAccountLocked(player.getUniqueId())) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "账户已被锁定，请稍后再试");
        }
    }
}