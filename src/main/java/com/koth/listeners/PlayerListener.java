package com.koth.listeners;

import com.koth.KOTHPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    private final KOTHPlugin plugin;
    
    public PlayerListener(KOTHPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Add player to boss bar if KOTH is active
        plugin.getKothManager().addPlayerToBossBar(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from boss bar
        plugin.getKothManager().removePlayerFromBossBar(event.getPlayer());
        
        // Reset capture if this player was capturing
        plugin.getKothManager().resetCaptureIfPlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        
        // Reset capture if this player was capturing (knocked out)
        plugin.getKothManager().resetCaptureIfPlayer(player);
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is teleporting out of region
        if (event.getTo() != null && plugin.getRegionManager().isPlayerInRegion(player) &&
            !plugin.getRegionManager().isInRegion(event.getTo())) {
            // Reset capture if this player was capturing
            plugin.getKothManager().resetCaptureIfPlayer(player);
        }
    }
}
