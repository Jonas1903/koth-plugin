package com.koth;

import com.koth.commands.KOTHCommand;
import com.koth.listeners.PlayerListener;
import com.koth.managers.KOTHManager;
import com.koth.managers.RegionManager;
import com.koth.managers.RewardManager;
import com.koth.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class KOTHPlugin extends JavaPlugin {
    private RegionManager regionManager;
    private RewardManager rewardManager;
    private KOTHManager kothManager;
    private MessageUtils messageUtils;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        messageUtils = new MessageUtils(getConfig());
        regionManager = new RegionManager(this);
        rewardManager = new RewardManager(this);
        kothManager = new KOTHManager(this, regionManager, rewardManager);
        
        // Register commands
        KOTHCommand kothCommand = new KOTHCommand(this);
        getCommand("koth").setExecutor(kothCommand);
        getCommand("koth").setTabCompleter(kothCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Start scheduler
        kothManager.startScheduler();
        
        getLogger().info("KOTH Plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Shutdown manager (cleanup tasks and boss bars)
        if (kothManager != null) {
            kothManager.shutdown();
        }
        
        getLogger().info("KOTH Plugin has been disabled!");
    }
    
    public void reload() {
        // Reinitialize message utils with new config
        messageUtils = new MessageUtils(getConfig());
        
        // Restart scheduler with new timings
        kothManager.startScheduler();
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public RewardManager getRewardManager() {
        return rewardManager;
    }
    
    public KOTHManager getKothManager() {
        return kothManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
}
