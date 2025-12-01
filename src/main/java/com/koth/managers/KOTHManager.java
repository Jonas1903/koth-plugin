package com.koth.managers;

import com.koth.KOTHPlugin;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KOTHManager {
    private final KOTHPlugin plugin;
    private final RegionManager regionManager;
    private final RewardManager rewardManager;
    
    private boolean active = false;
    private Player capturingPlayer = null;
    private int captureProgress = 0;
    private int captureTimeRequired;
    
    private BukkitTask captureTask = null;
    private BukkitTask scheduleTask = null;
    private final Map<Integer, BukkitTask> announcementTasks = new HashMap<>();
    
    private BossBar bossBar = null;
    private long nextKothTime = 0;
    
    public KOTHManager(KOTHPlugin plugin, RegionManager regionManager, RewardManager rewardManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        this.rewardManager = rewardManager;
        this.captureTimeRequired = plugin.getConfig().getInt("capture-time", 60);
    }
    
    public void startScheduler() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
        
        int intervalMinutes = plugin.getConfig().getInt("koth-interval", 60);
        nextKothTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);
        
        scheduleNextKoth(intervalMinutes);
    }
    
    private void scheduleNextKoth(int intervalMinutes) {
        // Cancel any existing announcement tasks
        cancelAnnouncementTasks();
        
        // Schedule announcement tasks
        List<Integer> announcementTimes = plugin.getConfig().getIntegerList("announcement-times");
        for (int time : announcementTimes) {
            if (time < intervalMinutes) {
                long delayTicks = (intervalMinutes - time) * 60 * 20L; // Convert minutes to ticks
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        announceKoth(time);
                    }
                }.runTaskLater(plugin, delayTicks);
                announcementTasks.put(time, task);
            }
        }
        
        // Schedule the actual KOTH start
        long delayTicks = intervalMinutes * 60 * 20L; // Convert minutes to ticks
        scheduleTask = new BukkitRunnable() {
            @Override
            public void run() {
                startKoth();
            }
        }.runTaskLater(plugin, delayTicks);
    }
    
    private void announceKoth(int minutesRemaining) {
        String timeStr = plugin.getMessageUtils().formatTime(minutesRemaining * 60);
        String message = plugin.getMessageUtils().getMessage("announcement", "%time%", timeStr);
        
        Bukkit.broadcast(plugin.getMessageUtils().getComponent("announcement", "%time%", timeStr));
    }
    
    public void startKoth() {
        if (active) {
            return;
        }
        
        if (!regionManager.isRegionSet()) {
            return;
        }
        
        active = true;
        captureProgress = 0;
        capturingPlayer = null;
        captureTimeRequired = plugin.getConfig().getInt("capture-time", 60);
        
        // Broadcast start message
        Bukkit.broadcast(plugin.getMessageUtils().getComponent("koth-begin"));
        
        // Create boss bar
        createBossBar();
        
        // Start capture check task
        startCaptureTask();
    }
    
    public void stopKoth() {
        if (!active) {
            return;
        }
        
        active = false;
        capturingPlayer = null;
        captureProgress = 0;
        
        // Cancel capture task
        if (captureTask != null) {
            captureTask.cancel();
            captureTask = null;
        }
        
        // Remove boss bar
        removeBossBar();
        
        // Broadcast stop message
        Bukkit.broadcast(plugin.getMessageUtils().getComponent("koth-stopped"));
        
        // Schedule next KOTH
        int intervalMinutes = plugin.getConfig().getInt("koth-interval", 60);
        nextKothTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);
        scheduleNextKoth(intervalMinutes);
    }
    
    private void startCaptureTask() {
        captureTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateCapture();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void updateCapture() {
        if (!active) {
            return;
        }
        
        // Find players in region
        Player playerInRegion = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (regionManager.isPlayerInRegion(player)) {
                playerInRegion = player;
                break; // Only one player can capture at a time
            }
        }
        
        // Check if capturing player left the region
        if (capturingPlayer != null && !regionManager.isPlayerInRegion(capturingPlayer)) {
            // Reset capture
            String message = plugin.getMessageUtils().getRawMessage("capture-interrupted", "%player%", capturingPlayer.getName());
            if (!message.isEmpty()) {
                Bukkit.broadcast(plugin.getMessageUtils().getComponent("capture-interrupted", "%player%", capturingPlayer.getName()));
            }
            capturingPlayer = null;
            captureProgress = 0;
        }
        
        // Check if a different player entered
        if (playerInRegion != null && capturingPlayer != null && !playerInRegion.equals(capturingPlayer)) {
            // Reset capture - player was knocked out
            String message = plugin.getMessageUtils().getRawMessage("capture-interrupted", "%player%", capturingPlayer.getName());
            if (!message.isEmpty()) {
                Bukkit.broadcast(plugin.getMessageUtils().getComponent("capture-interrupted", "%player%", capturingPlayer.getName()));
            }
            capturingPlayer = playerInRegion;
            captureProgress = 0;
        }
        
        // Start capture for new player
        if (playerInRegion != null && capturingPlayer == null) {
            capturingPlayer = playerInRegion;
            captureProgress = 0;
        }
        
        // Increment progress
        if (capturingPlayer != null) {
            captureProgress++;
            updateBossBar();
            
            // Check if capture is complete
            if (captureProgress >= captureTimeRequired) {
                completeCapture();
            }
        } else {
            // No one in region
            updateBossBar();
        }
    }
    
    private void completeCapture() {
        if (capturingPlayer == null) {
            return;
        }
        
        // Give reward
        rewardManager.giveReward(capturingPlayer);
        
        // Broadcast win message
        Bukkit.broadcast(plugin.getMessageUtils().getComponent("player-won", "%player%", capturingPlayer.getName()));
        
        // Stop KOTH
        stopKoth();
    }
    
    private void createBossBar() {
        bossBar = BossBar.bossBar(
                plugin.getMessageUtils().getComponent("player-capturing", "%player%", "Waiting...", "%time%", ""),
                0.0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );
        
        // Add to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }
    
    private void updateBossBar() {
        if (bossBar == null) {
            return;
        }
        
        if (capturingPlayer != null) {
            int remaining = captureTimeRequired - captureProgress;
            float progress = captureTimeRequired > 0 ? (float) captureProgress / captureTimeRequired : 0.0f;
            
            String timeStr = plugin.getMessageUtils().formatTime(remaining);
            
            bossBar.name(plugin.getMessageUtils().getComponent("player-capturing", 
                    "%player%", capturingPlayer.getName(), 
                    "%time%", timeStr));
            bossBar.progress(Math.min(1.0f, Math.max(0.0f, progress)));
        } else {
            bossBar.name(plugin.getMessageUtils().getComponent("koth-begin"));
            bossBar.progress(0.0f);
        }
    }
    
    private void removeBossBar() {
        if (bossBar == null) {
            return;
        }
        
        // Remove from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }
        
        bossBar = null;
    }
    
    public void addPlayerToBossBar(Player player) {
        if (bossBar != null && active) {
            player.showBossBar(bossBar);
        }
    }
    
    public void removePlayerFromBossBar(Player player) {
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }
    
    private void cancelAnnouncementTasks() {
        for (BukkitTask task : announcementTasks.values()) {
            task.cancel();
        }
        announcementTasks.clear();
    }
    
    public void shutdown() {
        // Cancel all tasks
        if (captureTask != null) {
            captureTask.cancel();
        }
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
        cancelAnnouncementTasks();
        
        // Remove boss bar
        removeBossBar();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public Player getCapturingPlayer() {
        return capturingPlayer;
    }
    
    public int getCaptureProgress() {
        return captureProgress;
    }
    
    public long getNextKothTime() {
        return nextKothTime;
    }
    
    public void resetCaptureIfPlayer(Player player) {
        if (capturingPlayer != null && capturingPlayer.equals(player)) {
            String message = plugin.getMessageUtils().getRawMessage("capture-interrupted", "%player%", capturingPlayer.getName());
            if (!message.isEmpty()) {
                Bukkit.broadcast(plugin.getMessageUtils().getComponent("capture-interrupted", "%player%", capturingPlayer.getName()));
            }
            capturingPlayer = null;
            captureProgress = 0;
        }
    }
}
