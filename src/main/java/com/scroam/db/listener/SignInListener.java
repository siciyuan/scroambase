package com.scroam.db.listener;

import com.scroam.db.ScroamDB;
import com.scroam.db.gui.SignInGUI;
import com.scroam.db.manager.LoginManager;
import com.scroam.db.manager.SignInManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class SignInListener implements Listener {

    private final ScroamDB plugin;
    private final SignInManager signInManager;
    private final LoginManager loginManager;
    private final SignInGUI signInGUI;

    public SignInListener(ScroamDB plugin) {
        this.plugin = plugin;
        this.signInManager = plugin.getSignInManager();
        this.loginManager = plugin.getLoginManager();
        this.signInGUI = plugin.getSignInGUI();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(ChatColor.GOLD + "每日签到")) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null ||
                event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        int slot = event.getSlot();

        if (slot == SignInGUI.SIGN_IN_BUTTON_SLOT) {
            handleSignInClick(player);
        } else if (slot == SignInGUI.CLOSE_BUTTON_SLOT) {
            player.closeInventory();
        }
    }

    private void handleSignInClick(Player player) {
        if (!loginManager.isLoggedIn(player)) {
            player.sendMessage(ChatColor.RED + "请先登录后再签到");
            player.closeInventory();
            return;
        }

        SignInManager.SignInResult result = signInManager.signIn(player);

        if (result.success) {
            player.sendMessage(ChatColor.GREEN + result.message);
            signInGUI.refresh(player);
        } else {
            player.sendMessage(ChatColor.RED + result.message);
        }
    }
}