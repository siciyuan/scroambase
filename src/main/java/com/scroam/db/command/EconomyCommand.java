package com.scroam.db.command;

import com.scroam.db.ScroamDB;
import com.scroam.db.data.TransactionRecord;
import com.scroam.db.manager.EconomyManager;
import com.scroam.db.manager.PaymentManager;
import com.scroam.db.manager.TreasuryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class EconomyCommand implements CommandExecutor {

    private final ScroamDB plugin;
    private final EconomyManager economy;
    private final PaymentManager payment;
    private final TreasuryManager treasury;

    public EconomyCommand(ScroamDB plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomyManager();
        this.payment = plugin.getPaymentManager();
        this.treasury = plugin.getTreasuryManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance":
            case "bal":
                return handleBalance(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "deposit":
                return handleDeposit(sender, args);
            case "withdraw":
                return handleWithdraw(sender, args);
            case "transfer":
                return handleTransfer(sender, args);
            case "treasury":
                return handleTreasury(sender, args);
            case "tax":
                return handleTax(sender, args);
            case "transactions":
            case "tx":
                return handleTransactions(sender, args);
            case "help":
                return showHelp(sender);
            default:
                sender.sendMessage("§c未知命令，请使用 /eco help 查看帮助");
                return false;
        }
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("§6===== ScroamDB 经济系统 =====§r");
        sender.sendMessage("§e/eco balance [玩家]§7 - 查看余额");
        sender.sendMessage("§e/eco pay <玩家> <金额>§7 - 向玩家付款");
        sender.sendMessage("§e/eco deposit <玩家> <金额>§7 - 管理存款");
        sender.sendMessage("§e/eco withdraw <玩家> <金额>§7 - 管理取款");
        sender.sendMessage("§e/eco transfer <从> <到> <金额>§7 - 管理转账");
        sender.sendMessage("§e/eco treasury [info/deposit/withdraw/distribute]§7 - 公库管理");
        sender.sendMessage("§e/eco tax [查看/设置] [类型] [税率]§7 - 税率管理");
        sender.sendMessage("§e/eco transactions [玩家] [数量]§7 - 查看交易记录");
        sender.sendMessage("§e/eco help§7 - 显示帮助");
        return true;
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c玩家不在线");
                return false;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c控制台必须指定玩家");
                return false;
            }
            target = (Player) sender;
        }

        double balance = economy.getBalance(target);
        sender.sendMessage(String.format("§a%s 的余额: %.2f %s",
                target.getName(), balance, economy.getCurrencyName()));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage("§c用法: /eco pay <玩家> <金额>");
            return false;
        }

        Player from = (Player) sender;
        Player to = Bukkit.getPlayer(args[1]);
        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c无效的金额");
            return false;
        }

        if (to == null) {
            sender.sendMessage("§c玩家不在线");
            return false;
        }

        if (from.equals(to)) {
            sender.sendMessage("§c不能向自己付款");
            return false;
        }

        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return false;
        }

        PaymentManager.PaymentResult result = payment.payPlayer(from, to, amount);

        if (result.success) {
            from.sendMessage(String.format("§a成功支付 %.2f %s 给 %s",
                    amount, economy.getCurrencyName(), to.getName()));
            from.sendMessage(String.format("§7手续费: %.2f %s",
                    result.taxAmount, economy.getCurrencyName()));
            to.sendMessage(String.format("§a收到 %s 的付款: %.2f %s",
                    from.getName(), result.afterTaxAmount, economy.getCurrencyName()));
        } else {
            sender.sendMessage("§c支付失败: " + result.message);
        }

        return true;
    }

    private boolean handleDeposit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scroamdb.admin")) {
            sender.sendMessage("§c权限不足");
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage("§c用法: /eco deposit <玩家> <金额>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c无效的金额");
            return false;
        }

        if (target == null) {
            sender.sendMessage("§c玩家不在线");
            return false;
        }

        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return false;
        }

        economy.deposit(target, amount);
        sender.sendMessage(String.format("§a成功向 %s 存入 %.2f %s",
                target.getName(), amount, economy.getCurrencyName()));
        target.sendMessage(String.format("§a管理员向你存入了 %.2f %s",
                amount, economy.getCurrencyName()));
        return true;
    }

    private boolean handleWithdraw(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scroamdb.admin")) {
            sender.sendMessage("§c权限不足");
            return false;
        }

        if (args.length < 3) {
            sender.sendMessage("§c用法: /eco withdraw <玩家> <金额>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c无效的金额");
            return false;
        }

        if (target == null) {
            sender.sendMessage("§c玩家不在线");
            return false;
        }

        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return false;
        }

        if (economy.withdraw(target, amount)) {
            sender.sendMessage(String.format("§a成功从 %s 扣除 %.2f %s",
                    target.getName(), amount, economy.getCurrencyName()));
            target.sendMessage(String.format("§c管理员从你账户扣除了 %.2f %s",
                    amount, economy.getCurrencyName()));
        } else {
            sender.sendMessage("§c操作失败，玩家余额不足");
        }

        return true;
    }

    private boolean handleTransfer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scroamdb.admin")) {
            sender.sendMessage("§c权限不足");
            return false;
        }

        if (args.length < 4) {
            sender.sendMessage("§c用法: /eco transfer <从玩家> <到玩家> <金额>");
            return false;
        }

        Player from = Bukkit.getPlayer(args[1]);
        Player to = Bukkit.getPlayer(args[2]);
        double amount;

        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c无效的金额");
            return false;
        }

        if (from == null || to == null) {
            sender.sendMessage("§c玩家不在线");
            return false;
        }

        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return false;
        }

        if (economy.transfer(from, to, amount)) {
            sender.sendMessage(String.format("§a成功从 %s 转账 %.2f %s 到 %s",
                    from.getName(), amount, economy.getCurrencyName(), to.getName()));
            from.sendMessage(String.format("§c管理员从你账户转出了 %.2f %s",
                    amount, economy.getCurrencyName()));
            to.sendMessage(String.format("§a管理员向你转入了 %.2f %s",
                    amount * (1 - economy.getTransferTax()), economy.getCurrencyName()));
        } else {
            sender.sendMessage("§c操作失败");
        }

        return true;
    }

    private boolean handleTreasury(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scroamdb.admin")) {
            sender.sendMessage("§c权限不足");
            return false;
        }

        if (args.length < 2) {
            return showTreasuryInfo(sender);
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "info":
                return showTreasuryInfo(sender);
            case "deposit":
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /eco treasury deposit <金额> [原因]");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    String reason = args.length > 3 ? String.join(" ", args).substring(args[0].length() + args[1].length() + args[2].length() + 3) : "Admin deposit";
                    if (treasury.deposit(amount, reason)) {
                        sender.sendMessage(String.format("§a成功向公库存入 %.2f %s",
                                amount, economy.getCurrencyName()));
                    } else {
                        sender.sendMessage("§c存入失败");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的金额");
                }
                return true;
            case "withdraw":
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /eco treasury withdraw <金额> [原因]");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    String reason = args.length > 3 ? String.join(" ", args).substring(args[0].length() + args[1].length() + args[2].length() + 3) : "Admin withdraw";
                    if (treasury.withdraw(amount, reason)) {
                        sender.sendMessage(String.format("§a成功从公库取出 %.2f %s",
                                amount, economy.getCurrencyName()));
                    } else {
                        sender.sendMessage("§c取出失败，公库余额不足");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的金额");
                }
                return true;
            case "distribute":
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /eco treasury distribute <每人金额> [原因]");
                    return false;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    String reason = args.length > 3 ? String.join(" ", args).substring(args[0].length() + args[1].length() + args[2].length() + 3) : "Admin distribution";
                    int count = Bukkit.getOnlinePlayers().size();
                    if (treasury.distributeToAllPlayers(amount, reason)) {
                        sender.sendMessage(String.format("§a成功向 %d 名在线玩家每人发放 %.2f %s",
                                count, amount, economy.getCurrencyName()));
                    } else {
                        sender.sendMessage("§c发放失败");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的金额");
                }
                return true;
            default:
                sender.sendMessage("§c未知子命令");
                return false;
        }
    }

    private boolean showTreasuryInfo(CommandSender sender) {
        sender.sendMessage("§6===== 服务器公库 =====§r");
        sender.sendMessage(String.format("§a余额: %.2f %s", treasury.getBalance(), economy.getCurrencyName()));
        sender.sendMessage(String.format("§e累计税收: %.2f %s", treasury.getTotalTaxCollected(), economy.getCurrencyName()));
        return true;
    }

    private boolean handleTax(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scroamdb.admin")) {
            sender.sendMessage("§c权限不足");
            return false;
        }

        if (args.length < 2) {
            return showTaxInfo(sender);
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("set")) {
            if (args.length < 4) {
                sender.sendMessage("§c用法: /eco tax set <类型> <税率>");
                sender.sendMessage("§7类型: transfer(转账), withdraw(取款), exchange(兑换), service(服务)");
                return false;
            }

            String type = args[2].toLowerCase();
            double rate;

            try {
                rate = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c无效的税率");
                return false;
            }

            if (rate < 0 || rate > 1) {
                sender.sendMessage("§c税率必须在 0-1 之间");
                return false;
            }

            switch (type) {
                case "transfer":
                    economy.setTransferTax(rate);
                    sender.sendMessage(String.format("§a转账税率已设置为 %.1f%%", rate * 100));
                    break;
                case "withdraw":
                    economy.setWithdrawTax(rate);
                    sender.sendMessage(String.format("§a取款税率已设置为 %.1f%%", rate * 100));
                    break;
                case "exchange":
                    economy.setExchangeTax(rate);
                    sender.sendMessage(String.format("§a兑换税率已设置为 %.1f%%", rate * 100));
                    break;
                case "service":
                    payment.setServiceTaxRate(rate);
                    sender.sendMessage(String.format("§a服务税率已设置为 %.1f%%", rate * 100));
                    break;
                default:
                    sender.sendMessage("§c未知税率类型");
                    return false;
            }
        } else {
            return showTaxInfo(sender);
        }

        return true;
    }

    private boolean showTaxInfo(CommandSender sender) {
        sender.sendMessage("§6===== 税率设置 =====§r");
        sender.sendMessage(String.format("§e转账税率: %.1f%%", economy.getTransferTax() * 100));
        sender.sendMessage(String.format("§e取款税率: %.1f%%", economy.getWithdrawTax() * 100));
        sender.sendMessage(String.format("§e兑换税率: %.1f%%", economy.getExchangeTax() * 100));
        sender.sendMessage(String.format("§e服务税率: %.1f%%", payment.getServiceTaxRate() * 100));
        return true;
    }

    private boolean handleTransactions(CommandSender sender, String[] args) {
        Player target;
        int limit = 10;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c玩家不在线");
                return false;
            }
            if (args.length >= 3) {
                try {
                    limit = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的数量");
                    return false;
                }
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c控制台必须指定玩家");
                return false;
            }
            target = (Player) sender;
        }

        List<TransactionRecord> records = plugin.getDatabaseManager().getTransactions(target.getUniqueId(), limit);

        if (records.isEmpty()) {
            sender.sendMessage("§c暂无交易记录");
            return true;
        }

        sender.sendMessage(String.format("§6===== %s 的交易记录 (最近 %d 条) =====§r", target.getName(), records.size()));
        for (TransactionRecord record : records) {
            String from = record.fromName != null ? record.fromName : "系统";
            String to = record.toName != null ? record.toName : "系统";
            String typeColor = switch (record.type) {
                case DEPOSIT -> "§a";
                case WITHDRAW -> "§c";
                case TRANSFER -> "§e";
                case PAYMENT -> "§b";
                case EXCHANGE -> "§d";
                case TAX -> "§6";
                case TREASURY -> "§9";
                default -> "§7";
            };

            sender.sendMessage(String.format("%s[%s] §7%s -> %s: %.2f %s (税: %.2f) %s",
                    typeColor, record.type.getDisplayName(),
                    from, to, record.amount, economy.getCurrencyName(),
                    record.taxAmount,
                    record.description != null ? "- " + record.description : ""));
        }

        return true;
    }
}