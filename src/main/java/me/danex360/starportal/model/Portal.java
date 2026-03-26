package me.danex360.starportal.model;

import org.bukkit.Location;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class Portal {
    private final UUID id;
    private String name;
    private final Location coreLocation;
    private final UUID ownerId;
    private UUID dialerId;
    private String address;
    private int pearlBalance;
    private final Map<String, String> savedAddresses;
    private boolean active;
    private UUID targetPortalId;
    private long connectionExpiry;
    private boolean hasIris;
    private boolean irisActive;
    private float facingYaw;
    private boolean isNorthSouth;
    private final java.util.List<UUID> whitelist;
    private Location controlPanelLocation;
    private long connectionStartTime;
    private double membraneOffset;
    private double planeOffset;
    private int holeWidth = 2; // Default to 2
    private int holeHeight = 3; // Default to 3
    private boolean dialing;
    private long lastDeactivationTime;
    private long lastActivityTime;
    private boolean hasHadActivity;
    private transient org.bukkit.scheduler.BukkitTask economyTask;

    public Portal(UUID id, String name, Location coreLocation, UUID ownerId, String address) {
        this.id = id;
        this.name = name;
        this.coreLocation = coreLocation;
        this.ownerId = ownerId;
        this.address = address;
        this.pearlBalance = 0;
        this.savedAddresses = new java.util.HashMap<>();
        this.active = false;
        this.targetPortalId = null;
        this.connectionExpiry = 0;
        this.hasIris = false;
        this.irisActive = false;
        this.facingYaw = coreLocation.getYaw();
        this.isNorthSouth = false;
        this.whitelist = new java.util.ArrayList<>();
        this.dialing = false;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Location getCoreLocation() { return coreLocation; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getDialerId() { return dialerId; }
    public void setDialerId(UUID dialerId) { this.dialerId = dialerId; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public int getPearlBalance() { return pearlBalance; }
    public void setPearlBalance(int pearlBalance) { this.pearlBalance = pearlBalance; }
    public void addPearls(int amount) { this.pearlBalance += amount; }
    
    public Map<String, String> getSavedAddresses() { return savedAddresses; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public UUID getTargetPortalId() { return targetPortalId; }
    public void setTargetPortalId(UUID targetPortalId) { this.targetPortalId = targetPortalId; }
    public long getConnectionExpiry() { return connectionExpiry; }
    public void setConnectionExpiry(long connectionExpiry) { this.connectionExpiry = connectionExpiry; }
    
    public boolean isDialing() { return dialing; }
    public void setDialing(boolean dialing) { this.dialing = dialing; }

    public boolean isExpired() {
        return active && connectionExpiry > 0 && System.currentTimeMillis() > connectionExpiry;
    }

    public boolean hasIris() { return hasIris; }
    public void setHasIris(boolean hasIris) { this.hasIris = hasIris; }
    public boolean isIrisActive() { return irisActive; }
    public void setIrisActive(boolean irisActive) { this.irisActive = irisActive; }
    public float getFacingYaw() { return facingYaw; }
    public void setFacingYaw(float facingYaw) { this.facingYaw = facingYaw; }
    
    public boolean isNorthSouth() { return isNorthSouth; }
    public void setNorthSouth(boolean isNorthSouth) { this.isNorthSouth = isNorthSouth; }
    
    public java.util.List<UUID> getWhitelist() { return whitelist; }
    public void addAllowedPlayer(UUID uuid) { if (!whitelist.contains(uuid)) whitelist.add(uuid); }
    public void removeAllowedPlayer(UUID uuid) { whitelist.remove(uuid); }
    public boolean isAllowed(UUID uuid) { return ownerId.equals(uuid) || whitelist.contains(uuid); }

    public Location getControlPanelLocation() { return controlPanelLocation; }
    public void setControlPanelLocation(Location controlPanelLocation) { this.controlPanelLocation = controlPanelLocation; }

    public long getConnectionStartTime() { return connectionStartTime; }
    public void setConnectionStartTime(long connectionStartTime) { this.connectionStartTime = connectionStartTime; }
    
    public long getConnectionDuration() {
        if (!active || connectionStartTime <= 0) return 0;
        return (System.currentTimeMillis() - connectionStartTime) / 1000;
    }

    public double getMembraneOffset() { return membraneOffset; }
    public void setMembraneOffset(double membraneOffset) { this.membraneOffset = membraneOffset; }

    public int getHoleWidth() { return holeWidth; }
    public void setHoleWidth(int holeWidth) { this.holeWidth = holeWidth; }
    public int getHoleHeight() { return holeHeight; }
    public void setHoleHeight(int holeHeight) { this.holeHeight = holeHeight; }

    public org.bukkit.scheduler.BukkitTask getEconomyTask() { return economyTask; }
    public void setEconomyTask(org.bukkit.scheduler.BukkitTask economyTask) { this.economyTask = economyTask; }

    public double getPlaneOffset() { return planeOffset; }
    public void setPlaneOffset(double planeOffset) { this.planeOffset = planeOffset; }

    public long getLastDeactivationTime() { return lastDeactivationTime; }
    public void setLastDeactivationTime(long lastDeactivationTime) { this.lastDeactivationTime = lastDeactivationTime; }

    public long getLastActivityTime() { return lastActivityTime; }
    public void setLastActivityTime(long lastActivityTime) { this.lastActivityTime = lastActivityTime; }

    public boolean hasHadActivity() { return hasHadActivity; }
    public void setHasHadActivity(boolean hasHadActivity) { this.hasHadActivity = hasHadActivity; }
}
