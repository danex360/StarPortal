package me.danex360.starportal.listener;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.model.Portal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Particle;

public class PortalListener implements Listener {
    private final StarPortal plugin;

    public PortalListener(StarPortal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        
        // CHECK 1: Protection - Cannot place inside the 3x3 hole of an ACTIVE portal base
        for (Portal p : plugin.getPortalManager().getPortals()) {
            if (isBlockInHole(p, block)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                            plugin.getMessageManager().getMessage("messages.cannot-place-in-hole"));
                return;
            }
        }

        if (block.getType() == Material.LECTERN) {
            handleLecternPlacement(event);
        } else if (block.getType() == Material.BEACON) {
            // handleBeaconPlacement(event); // REMOVED - Creation is now manual on interact
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block to = event.getToBlock();
        for (Portal p : plugin.getPortalManager().getPortals()) {
            if (isBlockInHole(p, to)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block to = event.getBlockClicked().getRelative(event.getBlockFace());
        for (Portal p : plugin.getPortalManager().getPortals()) {
            if (isBlockInHole(p, to)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() == Material.BEACON) {
            Portal portal = plugin.getPortalManager().getPortalAt(block.getLocation());
            if (portal != null) {
                event.setCancelled(true);
                if (portal.isAllowed(event.getPlayer().getUniqueId()) || event.getPlayer().hasPermission("starportal.admin")) {
                    plugin.getDialingMenu().openConfig(event.getPlayer(), portal);
                } else {
                    event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                                plugin.getMessageManager().getMessage("messages.no-permission-portal"));
                }
            } else {
                // MANUAL CREATION ON FIRST INTERACT
                if (handleBeaconPlacement(event, block, event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
            // Si portal == null, no cancelamos -> funciona como beacon normal
        } else if (block.getType() == Material.LECTERN) {
            for (Portal p : plugin.getPortalManager().getPortals()) {
                if (block.getLocation().equals(p.getControlPanelLocation())) {
                    event.setCancelled(true);
                    plugin.getDialingMenu().openMain(event.getPlayer(), p);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() instanceof BeaconInventory) {
            Location loc = event.getInventory().getLocation();
            if (loc != null && plugin.getPortalManager().getPortalAt(loc) != null) {
                event.setCancelled(true);
            }
        }
    }

    private void handleLecternPlacement(BlockPlaceEvent event) {
        Block block = event.getBlock();
        int range = plugin.getConfig().getInt("portal-settings.lectern-distance", 7);
        
        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    Block pot = block.getRelative(x, y, z);
                    if (pot.getType() == Material.BEACON) {
                        Portal p = plugin.getPortalManager().getPortalAt(pot.getLocation());
                        if (p != null) {
                            if (!p.isAllowed(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("starportal.admin")) {
                                // Don't cancel, remove next tick to avoid POI mismatch
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    if (block.getType() == Material.LECTERN) {
                                        block.setType(Material.AIR);
                                        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                                            event.getPlayer().getInventory().addItem(new ItemStack(Material.LECTERN)).values().forEach(item -> 
                                                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), item));
                                        }
                                    }
                                });
                                event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                                            plugin.getMessageManager().getMessage("messages.no-permission-portal"));
                                return;
                            }
                            if (p.getControlPanelLocation() != null) {
                                // Already has a controller! Don't cancel, remove next tick
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    if (block.getType() == Material.LECTERN) {
                                        block.setType(Material.AIR);
                                        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                                            event.getPlayer().getInventory().addItem(new ItemStack(Material.LECTERN)).values().forEach(item -> 
                                                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), item));
                                        }
                                    }
                                });
                                event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                                            plugin.getMessageManager().getMessage("messages.only-one-dhd"));
                                return;
                            }
                            p.setControlPanelLocation(block.getLocation());
                            // Clear any existing book in the lectern (deferred for POI safety)
                            Bukkit.getScheduler().runTask(plugin, () -> clearLectern(block));
                            // UPDATE FACE YAW based on relative DHD position
                            updatePortalFacing(p, block.getLocation());
                            plugin.getPortalManager().markDirty();
                            String msg = plugin.getMessageManager().getMessage("messages.dhd-linked");
                            event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + msg);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void updatePortalFacing(Portal p, Location dhdLoc) {
        if (p.isActive() || p.isDialing()) return; // Lock orientation during connection or dialing
        Location core = p.getCoreLocation();
        double dx = dhdLoc.getBlockX() - core.getBlockX();
        double dz = dhdLoc.getBlockZ() - core.getBlockZ();

        float yaw;
        if (p.isNorthSouth()) {
            // Plane along Z (Y-Z plane). Faces East (+X) or West (-X).
            yaw = (dx >= 0) ? 270f : 90f;
        } else {
            // Plane along X (X-Y plane). Faces South (+Z) or North (-Z).
            yaw = (dz >= 0) ? 0f : 180f;
        }
        p.setFacingYaw(yaw);
    }

    private boolean handleBeaconPlacement(PlayerInteractEvent event, Block core, Player player) {
        if (plugin.getPortalManager().getPortalAt(core.getLocation()) != null) return false;

        // NEW: Check for creation permission (Default: true)
        if (!player.hasPermission("starportal.create")) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                             plugin.getMessageManager().getMessage("messages.no-permission-create"));
            return true; // Cancel event
        }

        int limit = plugin.getConfig().getInt("portal-settings.max-portals-per-player", 5);
        // NEW: Check for infinite permission (Default: false)
        if (limit != -1 && !player.hasPermission("starportal.infinite")) {
            long count = plugin.getPortalManager().getPortals().stream()
                .filter(p -> p.getOwnerId().equals(player.getUniqueId())).count();
            if (count >= limit) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                 plugin.getMessageManager().getMessage("messages.portal-limit-reached").replace("{limit}", String.valueOf(limit)));
                return true; // Cancel event even if it failed due to limit
            }
        }

        // Detect frame relative to beacon
        Portal portal = detectFrame(core, player);
        if (portal != null) {
            // CHECK 2: Min distance from other portals (Only for Portal Creation)
            int minDist = plugin.getConfig().getInt("portal-settings.min-distance", 20);
            for (Portal p : plugin.getPortalManager().getPortals()) {
                if (p.getCoreLocation().getWorld().equals(core.getWorld())) {
                    double dist = p.getCoreLocation().distance(core.getLocation());
                    if (dist < minDist) {
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                         plugin.getMessageManager().getMessage("messages.portal-too-near").replace("{min}", String.valueOf(minDist)));
                        return true;
                    }
                }
            }
            // Found a valid frame shape! Always return true to cancel Beacon GUI
            
            // Check if the interior is clear before creating
            if (!isInteriorClearForPortal(portal, core)) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                 plugin.getMessageManager().getMessage("messages.portal-obstructed"));
                return true; 
            }

            plugin.getPortalManager().addPortal(portal);
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                             plugin.getMessageManager().getMessage("messages.portal-created").replace("{address}", portal.getAddress()));
            
            // Auto-link nearest Lectern
            linkNearestLectern(portal, core);
            return true;
        }
        return false;
    }

    private boolean isInteriorClearForPortal(Portal p, Block beacon) {
        int xDir = p.isNorthSouth() ? 0 : 1;
        int zDir = p.isNorthSouth() ? 1 : 0;
        int xOff = p.isNorthSouth() ? (int) p.getPlaneOffset() : (int) (p.getMembraneOffset() - 2.0);
        int zOff = p.isNorthSouth() ? (int) (p.getMembraneOffset() - 2.0) : (int) p.getPlaneOffset();
        return isInteriorClear(beacon, xDir, zDir, xOff, zOff, 5, 5);
    }

    private void linkNearestLectern(Portal portal, Block beacon) {
        int range = plugin.getConfig().getInt("portal-settings.lectern-distance", 7);
        Block nearest = null;
        double minDist = Double.MAX_VALUE;
        java.util.List<Block> allFound = new java.util.ArrayList<>();

        for (int x = -range; x <= range; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -range; z <= range; z++) {
                    Block b = beacon.getRelative(x, y, z);
                    if (b.getType() == Material.LECTERN) {
                        allFound.add(b);
                        double dist = b.getLocation().distanceSquared(beacon.getLocation());
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = b;
                        }
                    }
                }
            }
        }

        if (nearest != null) {
            final Block finalNearest = nearest;
            portal.setControlPanelLocation(finalNearest.getLocation());
            Bukkit.getScheduler().runTask(plugin, () -> clearLectern(finalNearest)); // Clear book safely
            updatePortalFacing(portal, finalNearest.getLocation());
            plugin.getPortalManager().markDirty();
            
            // Destroy all others
            boolean redundantDestroyed = false;
            for (Block b : allFound) {
                if (!b.equals(nearest)) {
                    // Use scheduler to avoid POI mismatch errors
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (b.getType() == Material.LECTERN) {
                            b.setType(Material.AIR);
                            b.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, b.getLocation().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, 0.05);
                            b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.LECTERN));
                        }
                    });
                    redundantDestroyed = true;
                }
            }
            
            if (redundantDestroyed) {
                Bukkit.getPlayer(portal.getOwnerId()).sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                                               plugin.getMessageManager().getMessage("messages.redundant-dhd-destroyed"));
            }
        }
    }

    private void clearLectern(Block block) {
        if (block.getType() == Material.LECTERN) {
            org.bukkit.block.Lectern lectern = (org.bukkit.block.Lectern) block.getState();
            lectern.getInventory().clear();
        }
    }

    private Portal detectFrame(Block beacon, Player player) {
        Material mat = Material.CRYING_OBSIDIAN;
        
        // Check North-South orientation (frame extends along Z axis)
        for (int xOff = -4; xOff <= 0; xOff++) {
            for (int zOff = -4; zOff <= 0; zOff++) {
                if (checkFrame(beacon, 0, 1, xOff, zOff, 5, 5, mat)) {
                    String addr = plugin.getPortalManager().generateUniqueAddress();
                    Portal p = new Portal(UUID.randomUUID(), "Portal " + addr, beacon.getLocation(), player.getUniqueId(), addr);
                    p.setNorthSouth(true);
                    p.setHoleWidth(3);
                    p.setHoleHeight(3);
                    p.setPlaneOffset(xOff);
                    p.setMembraneOffset(zOff + 2.0);
                    return p;
                }
            }
        }

        // Check East-West orientation (frame extends along X axis)
        for (int zOff = -4; zOff <= 0; zOff++) {
            for (int xOff = -4; xOff <= 0; xOff++) {
                if (checkFrame(beacon, 1, 0, xOff, zOff, 5, 5, mat)) {
                    String addr = plugin.getPortalManager().generateUniqueAddress();
                    Portal p = new Portal(UUID.randomUUID(), "Portal " + addr, beacon.getLocation(), player.getUniqueId(), addr);
                    p.setNorthSouth(false);
                    p.setHoleWidth(3);
                    p.setHoleHeight(3);
                    p.setPlaneOffset(zOff);
                    p.setMembraneOffset(xOff + 2.0);
                    return p;
                }
            }
        }
        return null;
    }

    private boolean checkFrame(Block beacon, int dx, int dz, int xOff, int zOff, int w, int h, Material mat) {
        // dx=0 (NS), dx=1 (EW)
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (i == 0 || i == w - 1 || j == 0 || j == h - 1) {
                    // Calculate relative block
                    int rx = (dx == 0) ? xOff : (xOff + i);
                    int ry = j;
                    int rz = (dx == 0) ? (zOff + i) : zOff;
                    
                    Block b = beacon.getRelative(rx, ry, rz);
                    
                    // SKIP CORNER CHECKS - Make them optional
                    boolean isCorner = (i == 0 || i == w - 1) && (j == 0 || j == h - 1);
                    if (isCorner) continue;

                    Material type = b.getType();
                    if (type != Material.CRYING_OBSIDIAN && b.getLocation().distanceSquared(beacon.getLocation()) > 0.1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Portal p = plugin.getPortalManager().getPortalAt(block.getLocation());
        if (p == null) {
            p = plugin.getPortalManager().getPortalByControlPanel(block.getLocation());
        }

        if (p != null) {
            boolean isDHD = p.getControlPanelLocation() != null && isSameBlock(block.getLocation(), p.getControlPanelLocation());
            
            // Whitelist Check: Allowed if owner, admin, or on whitelist
            if (!p.isAllowed(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("starportal.admin")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                            plugin.getMessageManager().getMessage("messages.no-permission-portal"));
                return;
            }

            if (isDHD) {
                // If active, ONLY force deactivate if we are the CALLER (the one who has dialerId)
                if (p.isActive() && p.getDialerId() != null) {
                    plugin.getConnectionManager().deactivate(p);
                }
                
                p.setControlPanelLocation(null);
                plugin.getPortalManager().markDirty();
                event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                            plugin.getMessageManager().getMessage("messages.dhd-unlinked"));
                return; // Event NOT cancelled
            }

            // TOTALLY block manual core/frame break. Must use GUI.
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                        plugin.getMessageManager().getMessage("messages.use-gui-to-delete"));
            return;
        }

        // Check frame protection for other players
        for (Portal frameP : plugin.getPortalManager().getPortals()) {
            if (isBlockInFrame(frameP, block)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                            plugin.getMessageManager().getMessage("messages.use-gui-to-delete"));
                return;
            }
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler
    public void onBlockBurn(org.bukkit.event.block.BlockBurnEvent event) {
        if (isPortalBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent event) {
        if (isPortalBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isPortalBlock(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isPortalBlock(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(org.bukkit.event.entity.EntityChangeBlockEvent event) {
        if (isPortalBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private void handleExplosion(List<Block> blocks) {
        blocks.removeIf(this::isPortalBlock);
    }

    /**
     * Checks if a block is part of any portal (Core, DHD, or Frame).
     */
    private boolean isPortalBlock(Block block) {
        // Protect Core
        if (plugin.getPortalManager().getPortalAt(block.getLocation()) != null) return true;
        
        // Protect DHD and Frame
        for (Portal p : plugin.getPortalManager().getPortals()) {
            if (p.getControlPanelLocation() != null && isSameBlock(block.getLocation(), p.getControlPanelLocation())) return true;
            if (isBlockInFrame(p, block)) return true;
        }
        return false;
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
               loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }

    public boolean isBlockInFrame(Portal portal, Block block) {
        Location core = portal.getCoreLocation();
        if (core == null || !block.getWorld().equals(core.getWorld())) return false;
        
        int dx = block.getX() - core.getBlockX();
        int dy = block.getY() - core.getBlockY();
        int dz = block.getZ() - core.getBlockZ();
        
        if (portal.isNorthSouth()) {
            if (dx != (int) portal.getPlaneOffset()) return false;
            int relZ = (int) Math.round(dz - portal.getMembraneOffset());
            // FRAME: y 0-4, relZ -2 o 2. Pero EXCLUIR esquinas (0,-2), (0,2), (4,-2), (4,2)
            boolean isVertical = (dy > 0 && dy < 4) && (relZ == -2 || relZ == 2);
            boolean isHorizontal = (relZ > -2 && relZ < 2) && (dy == 0 || dy == 4);
            return isVertical || isHorizontal;
        } else {
            if (dz != (int) portal.getPlaneOffset()) return false;
            int relX = (int) Math.round(dx - portal.getMembraneOffset());
            // FRAME: y 0-4, relX -2 o 2. Pero EXCLUIR esquinas
            boolean isVertical = (dy > 0 && dy < 4) && (relX == -2 || relX == 2);
            boolean isHorizontal = (relX > -2 && relX < 2) && (dy == 0 || dy == 4);
            return isVertical || isHorizontal;
        }
    }

    private boolean isInteriorClear(Block beacon, int dx, int dz, int xOff, int zOff, int w, int h) {
        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                int rx = (dx == 0) ? xOff : (xOff + i);
                int ry = j;
                int rz = (dx == 0) ? (zOff + i) : zOff;
                Block b = beacon.getRelative(rx, ry, rz);
                if (b.getType() != Material.AIR && b.getType() != Material.CAVE_AIR && b.getType() != Material.VOID_AIR) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBlockInHole(Portal portal, Block block) {
        Location core = portal.getCoreLocation();
        if (core == null || !block.getWorld().equals(core.getWorld())) return false;
        
        int dx = block.getX() - core.getBlockX();
        int dy = block.getY() - core.getBlockY();
        int dz = block.getZ() - core.getBlockZ();
        
        // Hole is the 3x3 area in the middle of the portal frame (y 1-3)
        if (dy < 1 || dy > 3) return false;
        
        if (portal.isNorthSouth()) {
            if (dx != (int) portal.getPlaneOffset()) return false;
            int relZ = (int) Math.round(dz - portal.getMembraneOffset());
            boolean inside = relZ >= -1 && relZ <= 1;
            return inside;
        } else {
            if (dz != (int) portal.getPlaneOffset()) return false;
            int relX = (int) Math.round(dx - portal.getMembraneOffset());
            boolean inside = relX >= -1 && relX <= 1;
            return inside;
        }
    }

    public boolean isHoleClear(Portal portal) {
        Location core = portal.getCoreLocation();
        if (core == null) return true;
        for (int dy = 1; dy <= 3; dy++) {
            for (int off = -1; off <= 1; off++) {
                Block b;
                if (portal.isNorthSouth()) {
                    b = core.clone().add(portal.getPlaneOffset(), dy, off + portal.getMembraneOffset()).getBlock();
                } else {
                    b = core.clone().add(off + portal.getMembraneOffset(), dy, portal.getPlaneOffset()).getBlock();
                }
                if (!b.isEmpty() && !b.isLiquid()) {
                    return false;
                }
            }
        }
        return true;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.removeMetadata("stp_teleport_cooldown", plugin);
        player.removeMetadata("stp_iris_sfx_cooldown", plugin);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        player.removeMetadata("stp_teleport_cooldown", plugin);
        player.removeMetadata("stp_iris_sfx_cooldown", plugin);
    }
}
