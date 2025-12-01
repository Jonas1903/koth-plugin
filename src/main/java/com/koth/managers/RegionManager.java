package com.koth.managers;

import com.koth.KOTHPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RegionManager {
    private final KOTHPlugin plugin;
    private Location pos1;
    private Location pos2;
    
    public RegionManager(KOTHPlugin plugin) {
        this.plugin = plugin;
        loadRegion();
    }
    
    public void setPos1(Location location) {
        this.pos1 = location;
        saveRegion();
    }
    
    public void setPos2(Location location) {
        this.pos2 = location;
        saveRegion();
    }
    
    public Location getPos1() {
        return pos1;
    }
    
    public Location getPos2() {
        return pos2;
    }
    
    public boolean isRegionSet() {
        return pos1 != null && pos2 != null && pos1.getWorld() != null && pos1.getWorld().equals(pos2.getWorld());
    }
    
    public boolean isInRegion(Location location) {
        if (!isRegionSet() || location.getWorld() == null) {
            return false;
        }
        
        if (!location.getWorld().equals(pos1.getWorld())) {
            return false;
        }
        
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        return location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    
    public boolean isPlayerInRegion(Player player) {
        return isInRegion(player.getLocation());
    }
    
    private void loadRegion() {
        FileConfiguration config = plugin.getConfig();
        
        String worldName = config.getString("region.world", "");
        if (worldName.isEmpty()) {
            return;
        }
        
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return;
        }
        
        double x1 = config.getDouble("region.pos1.x");
        double y1 = config.getDouble("region.pos1.y");
        double z1 = config.getDouble("region.pos1.z");
        
        double x2 = config.getDouble("region.pos2.x");
        double y2 = config.getDouble("region.pos2.y");
        double z2 = config.getDouble("region.pos2.z");
        
        pos1 = new Location(world, x1, y1, z1);
        pos2 = new Location(world, x2, y2, z2);
    }
    
    private void saveRegion() {
        FileConfiguration config = plugin.getConfig();
        
        if (pos1 != null && pos1.getWorld() != null) {
            config.set("region.world", pos1.getWorld().getName());
            config.set("region.pos1.x", pos1.getX());
            config.set("region.pos1.y", pos1.getY());
            config.set("region.pos1.z", pos1.getZ());
        }
        
        if (pos2 != null) {
            config.set("region.pos2.x", pos2.getX());
            config.set("region.pos2.y", pos2.getY());
            config.set("region.pos2.z", pos2.getZ());
        }
        
        plugin.saveConfig();
    }
    
    public String getRegionInfo() {
        if (!isRegionSet()) {
            return "Not set";
        }
        
        return String.format("%s: (%.0f, %.0f, %.0f) to (%.0f, %.0f, %.0f)",
                pos1.getWorld().getName(),
                pos1.getX(), pos1.getY(), pos1.getZ(),
                pos2.getX(), pos2.getY(), pos2.getZ());
    }
}
