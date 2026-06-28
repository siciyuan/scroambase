package com.scroam.db.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * 地标/景点数据类
 */
public class Waypoint {

    private final String id;
    private final String name;
    private final UUID creator;
    private final World world;
    private final double x, y, z;
    private final float yaw, pitch;
    private int createCount;
    private int teleportCount;
    private double createPrice;
    private double teleportPrice;
    private boolean requiresPermission;
    private long createdTime;

    public Waypoint(String id, String name, UUID creator, Location location) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.world = location.getWorld();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.createCount = 0;
        this.teleportCount = 3;
        this.createPrice = 0;
        this.teleportPrice = 0;
        this.requiresPermission = false;
        this.createdTime = System.currentTimeMillis();
    }

    public Waypoint(String id, String name, UUID creator, String worldName,
                    double x, double y, double z, float yaw, float pitch,
                    int createCount, int teleportCount, double createPrice,
                    double teleportPrice, boolean requiresPermission, long createdTime) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.world = org.bukkit.Bukkit.getWorld(worldName);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createCount = createCount;
        this.teleportCount = teleportCount;
        this.createPrice = createPrice;
        this.teleportPrice = teleportPrice;
        this.requiresPermission = requiresPermission;
        this.createdTime = createdTime;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public UUID getCreator() { return creator; }
    public World getWorld() { return world; }
    public String getWorldName() { return world != null ? world.getName() : ""; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public int getCreateCount() { return createCount; }
    public void setCreateCount(int createCount) { this.createCount = createCount; }
    public int getTeleportCount() { return teleportCount; }
    public void setTeleportCount(int teleportCount) { this.teleportCount = teleportCount; }
    public double getCreatePrice() { return createPrice; }
    public void setCreatePrice(double createPrice) { this.createPrice = createPrice; }
    public double getTeleportPrice() { return teleportPrice; }
    public void setTeleportPrice(double teleportPrice) { this.teleportPrice = teleportPrice; }
    public boolean isRequiresPermission() { return requiresPermission; }
    public void setRequiresPermission(boolean requiresPermission) { this.requiresPermission = requiresPermission; }
    public long getCreatedTime() { return createdTime; }

    public Location getLocation() {
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean isValid() {
        return world != null;
    }
}
