package com.scroam.db.gui;

import com.scroam.db.ScroamDB;
import com.scroam.db.manager.SignInManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SignInGUI {

    private static final String INVENTORY_NAME = ChatColor.GOLD + "每日签到";
    private static final int INVENTORY_SIZE = 27;

    private final ScroamDB plugin;
    private final SignInManager signInManager;

    public static final int SIGN_IN_BUTTON_SLOT = 13;
    public static final int INFO_BUTTON_SLOT = 11;
    public static final int CLOSE_BUTTON_SLOT = 15;

    public SignInGUI(ScroamDB plugin) {
        this.plugin = plugin;
        this.signInManager = plugin.getSignInManager();
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_NAME);

        fillBackground(inventory);
        addInfoButton(inventory, player);
        addSignInButton(inventory, player);
        addCloseButton(inventory);

        player.openInventory(inventory);
    }

    private void fillBackground(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, glass);
        }
    }

    private void addInfoButton(Inventory inventory, Player player) {
        SignInManager.SignInInfo info = signInManager.getSignInInfo(player);

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta meta = infoItem.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "签到信息");

        String status = info.signedToday ? ChatColor.GREEN + "✓ 今日已签到" : ChatColor.RED + "✗ 今日未签到";
        String consecutive = "连续签到: " + info.consecutiveDays + " 天";
        String total = "累计签到: " + info.totalDays + " 天";
        String reward = info.signedToday ? "今日奖励: 已领取" : String.format("今日奖励: %.0f Coins", info.todayReward);

        meta.setLore(Arrays.asList(
                "",
                status,
                consecutive,
                total,
                reward,
                "",
                ChatColor.GRAY + "连续签到可获得额外奖励"
        ));

        infoItem.setItemMeta(meta);
        inventory.setItem(INFO_BUTTON_SLOT, infoItem);
    }

    private void addSignInButton(Inventory inventory, Player player) {
        SignInManager.SignInInfo info = signInManager.getSignInInfo(player);

        Material material = info.signedToday ? Material.RED_WOOL : Material.LIME_WOOL;
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();

        if (info.signedToday) {
            meta.setDisplayName(ChatColor.RED + "今日已签到");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "明天再来签到吧"
            ));
        } else {
            meta.setDisplayName(ChatColor.GREEN + "点击签到");
            meta.setLore(Arrays.asList(
                    "",
                    String.format(ChatColor.YELLOW + "奖励: %.0f Coins", info.todayReward),
                    "",
                    ChatColor.GRAY + "点击领取奖励"
            ));
        }

        button.setItemMeta(meta);
        inventory.setItem(SIGN_IN_BUTTON_SLOT, button);
    }

    private void addCloseButton(Inventory inventory) {
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "关闭");
        closeItem.setItemMeta(meta);
        inventory.setItem(CLOSE_BUTTON_SLOT, closeItem);
    }

    public void refresh(Player player) {
        if (player.getOpenInventory().getTitle().equals(INVENTORY_NAME)) {
            open(player);
        }
    }
}