package com.scroam.db.manager;

import com.scroam.db.ScroamDB;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LoginManager {

    private final ScroamDB plugin;
    private final DatabaseManager db;

    private final Set<UUID> loggedInPlayers = new HashSet<>();

    private int maxFailedAttempts = 5;
    private long lockDurationSeconds = 300;
    private int minPasswordLength = 4;

    public LoginManager(ScroamDB plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        loadSettings();
    }

    private void loadSettings() {
        maxFailedAttempts = plugin.getConfig().getInt("login.max-failed-attempts", 5);
        lockDurationSeconds = plugin.getConfig().getLong("login.lock-duration-seconds", 300);
        minPasswordLength = plugin.getConfig().getInt("login.min-password-length", 4);
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.contains(uuid);
    }

    public boolean isLoggedIn(Player player) {
        return isLoggedIn(player.getUniqueId());
    }

    public boolean isRegistered(UUID uuid) {
        return db.isRegistered(uuid);
    }

    public boolean isRegistered(Player player) {
        return isRegistered(player.getUniqueId());
    }

    public boolean isAccountLocked(UUID uuid) {
        return db.isAccountLocked(uuid);
    }

    public boolean isAccountLocked(Player player) {
        return isAccountLocked(player.getUniqueId());
    }

    public LoginResult register(Player player, String password, String confirmPassword) {
        UUID uuid = player.getUniqueId();

        if (!password.equals(confirmPassword)) {
            return LoginResult.failed("两次输入的密码不一致");
        }

        if (password.length() < minPasswordLength) {
            return LoginResult.failed("密码长度至少需要 " + minPasswordLength + " 个字符");
        }

        if (isRegistered(uuid)) {
            return LoginResult.failed("你已经注册过了，请直接登录");
        }

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        if (db.registerUser(uuid, player.getName(), hash, salt)) {
            return LoginResult.success("注册成功，请使用 /login <密码> 登录");
        }

        return LoginResult.failed("注册失败，请重试");
    }

    public LoginResult login(Player player, String password) {
        UUID uuid = player.getUniqueId();

        if (!isRegistered(uuid)) {
            return LoginResult.failed("你还没有注册，请使用 /register <密码> <确认密码> 注册");
        }

        if (isLoggedIn(uuid)) {
            return LoginResult.failed("你已经登录了");
        }

        if (db.isAccountLocked(uuid)) {
            return LoginResult.failed("账户已被锁定，请稍后再试");
        }

        String storedHash = db.getPasswordHash(uuid);
        String salt = db.getSalt(uuid);

        if (storedHash == null || salt == null) {
            return LoginResult.failed("账户数据异常");
        }

        String inputHash = hashPassword(password, salt);

        if (inputHash.equals(storedHash)) {
            loggedInPlayers.add(uuid);

            String ip = getPlayerIP(player);
            db.updateLoginInfo(uuid, ip);

            return LoginResult.success("登录成功！欢迎回来，" + player.getName());
        } else {
            db.incrementFailedAttempts(uuid);
            int attempts = db.getFailedLoginAttempts(uuid);

            if (attempts >= maxFailedAttempts) {
                db.lockAccount(uuid, lockDurationSeconds);
                return LoginResult.failed("密码错误次数过多，账户已被锁定 " + lockDurationSeconds / 60 + " 分钟");
            }

            return LoginResult.failed("密码错误，还剩 " + (maxFailedAttempts - attempts) + " 次尝试机会");
        }
    }

    public void logout(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }

    public void logout(UUID uuid) {
        loggedInPlayers.remove(uuid);
    }

    public LoginResult changePassword(Player player, String oldPassword, String newPassword) {
        UUID uuid = player.getUniqueId();

        if (!isLoggedIn(uuid)) {
            return LoginResult.failed("请先登录");
        }

        if (!isRegistered(uuid)) {
            return LoginResult.failed("你还没有注册");
        }

        if (newPassword.length() < minPasswordLength) {
            return LoginResult.failed("新密码长度至少需要 " + minPasswordLength + " 个字符");
        }

        String storedHash = db.getPasswordHash(uuid);
        String salt = db.getSalt(uuid);

        if (storedHash == null || salt == null) {
            return LoginResult.failed("账户数据异常");
        }

        String oldHash = hashPassword(oldPassword, salt);

        if (!oldHash.equals(storedHash)) {
            return LoginResult.failed("旧密码错误");
        }

        String newSalt = generateSalt();
        String newHash = hashPassword(newPassword, newSalt);

        if (db.updatePassword(uuid, newHash, newSalt)) {
            return LoginResult.success("密码修改成功");
        }

        return LoginResult.failed("密码修改失败");
    }

    public void onPlayerJoin(Player player) {
        if (!isRegistered(player.getUniqueId())) {
            player.sendMessage("§e欢迎来到服务器！请使用 /register <密码> <确认密码> 注册");
        } else if (!isLoggedIn(player.getUniqueId())) {
            player.sendMessage("§e请使用 /login <密码> 登录");
        }
    }

    public void onPlayerQuit(Player player) {
        logout(player);
    }

    public boolean canInteract(Player player) {
        return isLoggedIn(player) || isAdmin(player);
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("scroamdb.admin");
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            plugin.getLogger().severe("SHA-256 algorithm not available: " + e.getMessage());
            return null;
        }
    }

    private String getPlayerIP(Player player) {
        return player.getAddress() != null ? player.getAddress().getHostString() : "unknown";
    }

    public static class LoginResult {
        public final boolean success;
        public final String message;

        private LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static LoginResult success(String message) {
            return new LoginResult(true, message);
        }

        public static LoginResult failed(String message) {
            return new LoginResult(false, message);
        }
    }
}