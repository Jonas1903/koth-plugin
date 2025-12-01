package com.koth.managers;

import com.koth.KOTHPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RewardManager {
    private final KOTHPlugin plugin;
    
    public RewardManager(KOTHPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void giveReward(Player player) {
        String reward = plugin.getConfig().getString("reward", "");
        
        if (reward.isEmpty()) {
            return;
        }
        
        // Replace player placeholder
        reward = reward.replace("%player%", player.getName());
        
        // Handle multiple commands separated by semicolons
        String[] commands = reward.split(";");
        
        for (String command : commands) {
            command = command.trim();
            if (!command.isEmpty()) {
                // Execute command from console
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
    
    public void setReward(String reward) {
        plugin.getConfig().set("reward", reward);
        plugin.saveConfig();
    }
    
    public String getReward() {
        return plugin.getConfig().getString("reward", "");
    }
}
