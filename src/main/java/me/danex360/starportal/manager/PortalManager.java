package me.danex360.starportal.manager;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.model.Portal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PortalManager {
    private final StarPortal plugin;
    private final Map<UUID, Portal> portals = new HashMap<>();
    private final File dataFile;
    private boolean dirty = false;

    public PortalManager(StarPortal plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        startSaveTask();
    }

    private void startSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (dirty) {
                savePortals();
                dirty = false;
            }
        }, 600L, 600L); // Guardar cada 30 segundos (600 ticks) si hay cambios
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void loadPortals() {
        if (!dataFile.exists()) {
            plugin.getLogger().info("No data.yml found, skipping portal load.");
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("portals");
        if (section == null) {
            plugin.getLogger().info("No portals found in data.yml.");
            return;
        }

        int loadedCount = 0;
        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String name = section.getString(key + ".name");
                Location loc = section.getLocation(key + ".location");
                String ownerStr = section.getString(key + ".ownerId");
                if (ownerStr == null || loc == null) continue;
                
                UUID ownerId = UUID.fromString(ownerStr);
                String address = section.getString(key + ".address");
                int balance = section.getInt(key + ".pearlBalance");
                
                Portal portal = new Portal(id, name, loc, ownerId, address);
                portal.setPearlBalance(balance);
                portal.setHasIris(section.getBoolean(key + ".hasIris"));
                portal.setIrisActive(section.getBoolean(key + ".irisActive"));
                portal.setControlPanelLocation(section.getLocation(key + ".controlPanelLocation"));
                portal.setFacingYaw((float) section.getDouble(key + ".facingYaw", loc.getYaw()));
                portal.setMembraneOffset(section.getDouble(key + ".membraneOffset", 0.0));
                portal.setNorthSouth(section.getBoolean(key + ".isNorthSouth", true));
                portal.setHoleWidth(section.getInt(key + ".holeWidth", 2));
                portal.setHoleHeight(section.getInt(key + ".holeHeight", 3));
                portal.setPlaneOffset(section.getDouble(key + ".planeOffset", 0.0));

                List<String> whitelistStr = section.getStringList(key + ".whitelist");
                for (String w : whitelistStr) {
                    portal.addAllowedPlayer(UUID.fromString(w));
                }
                
                ConfigurationSection saved = section.getConfigurationSection(key + ".savedAddresses");
                if (saved != null) {
                    for (String addrKey : saved.getKeys(false)) {
                        String portalName = saved.getString(addrKey);
                        if (portalName != null) {
                            // Revertir reemplazo de puntos si se usó (aunque son números)
                            String finalAddr = addrKey.replace("_", ".");
                            portal.getSavedAddresses().put(finalAddr, portalName);
                        }
                    }
                }
                
                portals.put(id, portal);
                loadedCount++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load portal " + key + ": " + e.getMessage());
            }
        }
    }

    public void savePortals() {
        YamlConfiguration config = new YamlConfiguration();
        int saveCount = 0;
        for (Portal portal : portals.values()) {
            String path = "portals." + portal.getId().toString();
            config.set(path + ".name", portal.getName());
            config.set(path + ".location", portal.getCoreLocation());
            config.set(path + ".ownerId", portal.getOwnerId().toString());
            config.set(path + ".address", portal.getAddress());
            config.set(path + ".pearlBalance", portal.getPearlBalance());
            config.set(path + ".hasIris", portal.hasIris());
            config.set(path + ".irisActive", portal.isIrisActive());
                
            config.set(path + ".facingYaw", portal.getFacingYaw());
            config.set(path + ".controlPanelLocation", portal.getControlPanelLocation());
            config.set(path + ".membraneOffset", portal.getMembraneOffset());
            config.set(path + ".isNorthSouth", portal.isNorthSouth());
            config.set(path + ".holeWidth", portal.getHoleWidth());
            config.set(path + ".holeHeight", portal.getHoleHeight());
            config.set(path + ".planeOffset", portal.getPlaneOffset());

            List<String> whitelistStr = new ArrayList<>();
            for (UUID uuid : portal.getWhitelist()) {
                whitelistStr.add(uuid.toString());
            }
            config.set(path + ".whitelist", whitelistStr);
            
            // Guardar Historial
            ConfigurationSection savedSec = config.createSection(path + ".savedAddresses");
            for (Map.Entry<String, String> entry : portal.getSavedAddresses().entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                // Bukkit no permite puntos en las claves, reemplazamos por seguridad
                String safeKey = entry.getKey().replace(".", "_");
                savedSec.set(safeKey, entry.getValue());
            }
            saveCount++;
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("CRITICAL: Failed to save portals to data.yml: " + e.getMessage());
        }
    }

    public void addPortal(Portal portal) {
        portals.put(portal.getId(), portal);
        markDirty();
    }

    public void removePortal(UUID id) {
        Portal p = portals.get(id);
        if (p != null) {
            if (p.isActive()) {
                plugin.getConnectionManager().deactivate(p);
            }
            portals.remove(id);
            markDirty();
        }
    }

    public Portal getPortalAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        for (Portal portal : portals.values()) {
            Location pLoc = portal.getCoreLocation();
            if (pLoc == null || pLoc.getWorld() == null) continue;
            
            if (pLoc.getBlockX() == loc.getBlockX() &&
                pLoc.getBlockY() == loc.getBlockY() &&
                pLoc.getBlockZ() == loc.getBlockZ() &&
                pLoc.getWorld().getName().equals(loc.getWorld().getName())) {
                return portal;
            }
        }
        return null;
    }

    public Portal getPortalByControlPanel(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        for (Portal portal : portals.values()) {
            Location cpLoc = portal.getControlPanelLocation();
            if (cpLoc == null || cpLoc.getWorld() == null) continue;
            
            if (cpLoc.getBlockX() == loc.getBlockX() &&
                cpLoc.getBlockY() == loc.getBlockY() &&
                cpLoc.getBlockZ() == loc.getBlockZ() &&
                cpLoc.getWorld().getName().equals(loc.getWorld().getName())) {
                return portal;
            }
        }
        return null;
    }

    public Collection<Portal> getPortals() {
        return portals.values();
    }

    public Portal getPortalById(UUID id) {
        return portals.get(id);
    }
    
    public Portal getPortalByAddress(String address) {
        for (Portal p : portals.values()) {
            if (p.getAddress().equals(address)) return p;
        }
        return null;
    }

    public boolean isNameTaken(String name) {
        for (Portal p : portals.values()) {
            if (p.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public String generateUniqueAddress() {
        String addr;
        do {
            addr = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        } while (getPortalByAddress(addr) != null);
        return addr;
    }

    public void dropPearls(Portal portal, Location loc) {
        int amount = portal.getPearlBalance();
        if (amount <= 0) return;
        
        portal.setPearlBalance(0);
        markDirty();
        
        // Split into stacks of 16 as requested
        while (amount > 0) {
            int toDrop = Math.min(amount, 16);
            ItemStack stack = new ItemStack(Material.ENDER_PEARL, toDrop);
            loc.getWorld().dropItemNaturally(loc, stack);
            amount -= toDrop;
        }
    }
}
