package com.scroam.db.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * 世界数据类
 */
public class WorldData {
    public int id;
    public String worldName;
    public String displayName;
    public double spawnX;
    public double spawnY;
    public double spawnZ;
    public float spawnYaw;
    public float spawnPitch;
    public boolean isMainWorld;
    public boolean enabled;
    public String properties;

    public WorldData() {
        this.properties = "{}";
        this.spawnY = 64;
        this.spawnYaw = 0;
        this.spawnPitch = 0;
        this.isMainWorld = false;
        this.enabled = true;
    }

    public Location getSpawnLocation() {
        World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    public void setSpawnLocation(Location location) {
        this.spawnX = location.getX();
        this.spawnY = location.getY();
        this.spawnZ = location.getZ();
        this.spawnYaw = location.getYaw();
        this.spawnPitch = location.getPitch();
    }
}
