package com.scroam.db.command;

import com.scroam.db.ScroamDB;
import com.scroam.db.gui.SignInGUI;
import com.scroam.db.manager.LoginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignInCommand implements CommandExecutor {

    private final ScroamDB plugin;
    private final SignInGUI signInGUI;
    private final LoginManager loginManager;

    public SignInCommand(ScroamDB plugin) {
        this.plugin = plugin;
        this.signInGUI = plugin.getSignInGUI();
        this.loginManager = plugin.getLoginManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用签到命令");
            return false;
        }

        Player player = (Player) sender;

        if (!loginManager.isLoggedIn(player)) {
            player.sendMessage(ChatColor.RED + "请先登录后再签到");
            return false;
        }

        signInGUI.open(player);
        return true;
    }
}