package com.koth.commands;

import com.koth.KOTHPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KOTHCommand implements CommandExecutor, TabCompleter {
    private final KOTHPlugin plugin;
    
    public KOTHCommand(KOTHPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Check if sender is an operator
        if (!sender.isOp()) {
            plugin.getMessageUtils().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "setpos1":
                return handleSetPos1(sender);
            case "setpos2":
                return handleSetPos2(sender);
            case "setreward":
                return handleSetReward(sender, args);
            case "start":
                return handleStart(sender);
            case "stop":
                return handleStop(sender);
            case "reload":
                return handleReload(sender);
            case "info":
                return handleInfo(sender);
            default:
                sendUsage(sender);
                return true;
        }
    }
    
    private boolean handleSetPos1(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        plugin.getRegionManager().setPos1(player.getLocation());
        plugin.getMessageUtils().sendMessage(sender, "pos1-set",
                "%x%", String.format("%.0f", player.getLocation().getX()),
                "%y%", String.format("%.0f", player.getLocation().getY()),
                "%z%", String.format("%.0f", player.getLocation().getZ()));
        
        return true;
    }
    
    private boolean handleSetPos2(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        plugin.getRegionManager().setPos2(player.getLocation());
        plugin.getMessageUtils().sendMessage(sender, "pos2-set",
                "%x%", String.format("%.0f", player.getLocation().getX()),
                "%y%", String.format("%.0f", player.getLocation().getY()),
                "%z%", String.format("%.0f", player.getLocation().getZ()));
        
        return true;
    }
    
    private boolean handleSetReward(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /koth setreward <reward>");
            return true;
        }
        
        // Join all arguments after "setreward" to form the reward string
        StringBuilder rewardBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                rewardBuilder.append(" ");
            }
            rewardBuilder.append(args[i]);
        }
        String reward = rewardBuilder.toString();
        
        plugin.getRewardManager().setReward(reward);
        plugin.getMessageUtils().sendMessage(sender, "reward-set", "%reward%", reward);
        
        return true;
    }
    
    private boolean handleStart(CommandSender sender) {
        if (!plugin.getRegionManager().isRegionSet()) {
            plugin.getMessageUtils().sendMessage(sender, "region-not-set");
            return true;
        }
        
        if (plugin.getKothManager().isActive()) {
            plugin.getMessageUtils().sendMessage(sender, "koth-already-active");
            return true;
        }
        
        plugin.getKothManager().startKoth();
        plugin.getMessageUtils().sendMessage(sender, "koth-started");
        
        return true;
    }
    
    private boolean handleStop(CommandSender sender) {
        if (!plugin.getKothManager().isActive()) {
            plugin.getMessageUtils().sendMessage(sender, "koth-not-active");
            return true;
        }
        
        plugin.getKothManager().stopKoth();
        plugin.getMessageUtils().sendMessage(sender, "koth-stopped");
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.reload();
        plugin.getMessageUtils().sendMessage(sender, "config-reloaded");
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender) {
        plugin.getMessageUtils().sendMessage(sender, "info-header");
        
        String status = plugin.getKothManager().isActive() ? "§aActive" : "§cInactive";
        plugin.getMessageUtils().sendMessage(sender, "info-status", "%status%", status);
        
        int interval = plugin.getConfig().getInt("koth-interval", 60);
        plugin.getMessageUtils().sendMessage(sender, "info-interval", "%interval%", String.valueOf(interval));
        
        int captureTime = plugin.getConfig().getInt("capture-time", 60);
        plugin.getMessageUtils().sendMessage(sender, "info-capture-time", "%time%", String.valueOf(captureTime));
        
        String region = plugin.getRegionManager().getRegionInfo();
        plugin.getMessageUtils().sendMessage(sender, "info-region", "%region%", region);
        
        String reward = plugin.getRewardManager().getReward();
        plugin.getMessageUtils().sendMessage(sender, "info-reward", "%reward%", reward);
        
        if (!plugin.getKothManager().isActive()) {
            long timeUntil = plugin.getKothManager().getNextKothTime() - System.currentTimeMillis();
            if (timeUntil > 0) {
                int secondsUntil = (int) (timeUntil / 1000);
                String timeStr = plugin.getMessageUtils().formatTime(secondsUntil);
                plugin.getMessageUtils().sendMessage(sender, "info-next", "%next%", timeStr);
            }
        }
        
        return true;
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6KOTH Commands:");
        sender.sendMessage("§7/koth setpos1 §f- Set position 1");
        sender.sendMessage("§7/koth setpos2 §f- Set position 2");
        sender.sendMessage("§7/koth setreward <reward> §f- Set reward");
        sender.sendMessage("§7/koth start §f- Start KOTH event");
        sender.sendMessage("§7/koth stop §f- Stop KOTH event");
        sender.sendMessage("§7/koth reload §f- Reload configuration");
        sender.sendMessage("§7/koth info §f- Show KOTH information");
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!sender.isOp()) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("setpos1", "setpos2", "setreward", "start", "stop", "reload", "info");
            List<String> completions = new ArrayList<>();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}
