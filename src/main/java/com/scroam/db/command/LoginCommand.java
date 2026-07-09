package com.scroam.db.command;

import com.scroam.db.ScroamDB;
import com.scroam.db.manager.LoginManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final ScroamDB plugin;
    private final LoginManager loginManager;

    public LoginCommand(ScroamDB plugin) {
        this.plugin = plugin;
        this.loginManager = plugin.getLoginManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return false;
        }

        Player player = (Player) sender;
        String subCommand = cmd.getName().toLowerCase();

        switch (subCommand) {
            case "login":
                return handleLogin(player, args);
            case "register":
                return handleRegister(player, args);
            case "changepassword":
            case "cp":
                return handleChangePassword(player, args);
            case "logout":
                return handleLogout(player);
            default:
                return false;
        }
    }

    private boolean handleLogin(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§c用法: /login <密码>");
            return false;
        }

        String password = args[0];
        LoginManager.LoginResult result = loginManager.login(player, password);

        if (result.success) {
            player.sendMessage("§a" + result.message);
            player.setInvulnerable(false);
        } else {
            player.sendMessage("§c" + result.message);
        }

        return true;
    }

    private boolean handleRegister(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /register <密码> <确认密码>");
            return false;
        }

        String password = args[0];
        String confirmPassword = args[1];
        LoginManager.LoginResult result = loginManager.register(player, password, confirmPassword);

        if (result.success) {
            player.sendMessage("§a" + result.message);
        } else {
            player.sendMessage("§c" + result.message);
        }

        return true;
    }

    private boolean handleChangePassword(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /changepassword <旧密码> <新密码>");
            return false;
        }

        String oldPassword = args[0];
        String newPassword = args[1];
        LoginManager.LoginResult result = loginManager.changePassword(player, oldPassword, newPassword);

        if (result.success) {
            player.sendMessage("§a" + result.message);
        } else {
            player.sendMessage("§c" + result.message);
        }

        return true;
    }

    private boolean handleLogout(Player player) {
        if (!loginManager.isLoggedIn(player)) {
            player.sendMessage("§c你还没有登录");
            return false;
        }

        loginManager.logout(player);
        player.sendMessage("§a已退出登录");
        player.setInvulnerable(true);

        return true;
    }
}