package me.danex360.starportal.manager;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.listener.PortalListener;
import me.danex360.starportal.model.Portal;
import me.danex360.starportal.util.PortalVFX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.Inventory;
import me.danex360.starportal.gui.PortalInventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;

public class ConnectionManager {
    private final StarPortal plugin;

    public ConnectionManager(StarPortal plugin) {
        this.plugin = plugin;
        startTeleportTask();
    }

    public void dial(Player player, Portal source, String targetAddress) {
        if (source.isActive() || source.isDialing()) return;

        if (source.getControlPanelLocation() == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                             plugin.getMessageManager().getMessage("messages.no-dhd-error"));
            return;
        }

        // Cooldown check (using actual config key)
        long cooldown = plugin.getConfig().getLong("portal-settings.dialing-cooldown-seconds", 30) * 1000;
        long elapsed = System.currentTimeMillis() - source.getLastDeactivationTime();
        if (elapsed < cooldown) {
            long remaining = (cooldown - elapsed) / 1000;
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                             plugin.getMessageManager().getMessage("messages.portal-cooldown-error").replace("{time}", String.valueOf(remaining)));
            return;
        }
        
        Portal target = plugin.getPortalManager().getPortalByAddress(targetAddress);
        if (target == null || target.getId().equals(source.getId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.invalid-address"));
            return;
        }

        if (target.isActive() || target.isDialing()) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.portal-busy"));
            return;
        }

        if (target.getControlPanelLocation() == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                             plugin.getMessageManager().getMessage("messages.target-no-dhd"));
            return;
        }

        // HOLE CLEAR CHECK
        if (!plugin.getPortalListener().isHoleClear(source) || !plugin.getPortalListener().isHoleClear(target)) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.portal-obstructed"));
            return;
        }

        boolean isInterdimensional = !source.getCoreLocation().getWorld().equals(target.getCoreLocation().getWorld());
        int initialCost = isInterdimensional ? 
                plugin.getConfig().getInt("portal-settings.interdimensional-initial-pearl-cost", 4) :
                plugin.getConfig().getInt("portal-settings.initial-pearl-cost", 2);
        
        if (source.getPearlBalance() < initialCost) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                             plugin.getMessageManager().getMessage("messages.not-enough-pearls"));
            return;
        }

        source.setPearlBalance(source.getPearlBalance() - initialCost);
        source.setTargetPortalId(target.getId());
        source.setDialerId(player.getUniqueId());
        source.setDialing(true);
        
        target.setTargetPortalId(source.getId());
        target.setDialerId(null); // Explicitly null for receiver
        target.setDialing(true);
        int chargeTicks = plugin.getConfig().getInt("portal-settings.charge-time-ticks", 60);
        
        // Effects at the start of charging
        target.getCoreLocation().getWorld().playSound(target.getCoreLocation(), org.bukkit.Sound.BLOCK_BEACON_POWER_SELECT, 1f, 0.5f);
        player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.dialing").replace("{address}", targetAddress));
        
        // Redirect everyone (source and target) to Main Menu (Control Panel) while dialing
        closePortalInventories(source);
        closePortalInventories(target);
        
        // Repeating dialing sound
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!source.isDialing() || source.isActive()) {
                    this.cancel();
                    return;
                }
                source.getCoreLocation().getWorld().playSound(source.getCoreLocation(), org.bukkit.Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
                target.getCoreLocation().getWorld().playSound(target.getCoreLocation(), org.bukkit.Sound.BLOCK_BEACON_AMBIENT, 1f, 1.5f);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Double check if still valid after delay
                if (!source.getCoreLocation().getBlock().getType().equals(Material.BEACON) || 
                    !target.getCoreLocation().getBlock().getType().equals(Material.BEACON)) {
                    deactivate(source);
                    return;
                }

                // Clear dialing state before activating
                source.setDialing(false);
                target.setDialing(false);

                source.setActive(true);
                source.setConnectionStartTime(System.currentTimeMillis());
                
                int durationBase = plugin.getConfig().getInt("portal-settings.duration-seconds", 120);
                source.setConnectionExpiry(System.currentTimeMillis() + (durationBase * 1000L));
                
                target.setActive(true);
                target.setTargetPortalId(source.getId());
                target.setConnectionExpiry(source.getConnectionExpiry());
                target.setConnectionStartTime(source.getConnectionStartTime());
                
                // Initialize activity timer
                source.setLastActivityTime(System.currentTimeMillis());
                target.setLastActivityTime(System.currentTimeMillis());
                source.setHasHadActivity(false);
                target.setHasHadActivity(false);
                
                closePortalInventories(source);
                closePortalInventories(target);

                PortalVFX.spawnMembrane(plugin, source);
                PortalVFX.spawnMembrane(plugin, target);
                PortalVFX.spawnKwoosh(plugin, source);
                PortalVFX.spawnKwoosh(plugin, target);
                
                source.getCoreLocation().getWorld().playSound(source.getCoreLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                target.getCoreLocation().getWorld().playSound(target.getCoreLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                
                plugin.getPortalManager().markDirty();
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.portal-opened"));
                
                startEconomyTask(source, target);
            }
        }.runTaskLater(plugin, (long) chargeTicks);

        // AUTO-SAVE ADDRESS TO ORIGIN
        source.getSavedAddresses().put(targetAddress, target.getName());
        plugin.getPortalManager().markDirty();
    }

    private void startEconomyTask(Portal source, Portal target) {
        int intervalSeconds = plugin.getConfig().getInt("portal-settings.periodic-interval-seconds", 30);
        boolean isInterdimensional = !source.getCoreLocation().getWorld().equals(target.getCoreLocation().getWorld());

        // Cancel any lingering task just in case
        if (source.getEconomyTask() != null) {
            source.getEconomyTask().cancel();
            source.setEconomyTask(null); // Ensure it's nulled after cancellation
        }

        org.bukkit.scheduler.BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!source.isActive() || source.isExpired()) {
                    source.setEconomyTask(null);
                    this.cancel();
                    return;
                }
                
                long timeLeft = source.getConnectionExpiry() - System.currentTimeMillis();
                if (timeLeft < (intervalSeconds * 1000L)) return;

                if (source.getPearlBalance() > 0) {
                    int cost = isInterdimensional ?
                            plugin.getConfig().getInt("portal-settings.interdimensional-periodic-pearl-cost", 2) :
                            plugin.getConfig().getInt("portal-settings.periodic-pearl-cost", 1);

                    source.setPearlBalance(Math.max(0, source.getPearlBalance() - cost));
                    plugin.getPortalManager().markDirty();
                    
                    // Notificar consumo al marcador
                    Player dialer = Bukkit.getPlayer(source.getDialerId());
                    if (dialer != null && dialer.isOnline()) {
                        dialer.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                         plugin.getMessageManager().getMessage("messages.pearls-consumed").replace("{amount}", String.valueOf(cost)));
                    }
                } else {
                    deactivate(source);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, intervalSeconds * 20L, intervalSeconds * 20L);
        
        source.setEconomyTask(task);
    }

    public void deactivate(Portal portal) {
        deactivate(portal, false);
    }

    public void deactivate(Portal portal, boolean manual) {
        if (!portal.isActive()) return;

        Portal target = plugin.getPortalManager().getPortalById(portal.getTargetPortalId());
        
        // EXCLUSIVE NOTIFICATION TO DIALER
        UUID dialerId = portal.getDialerId();
        if (dialerId == null && target != null) {
            dialerId = target.getDialerId(); // Check the other side
        }

        if (dialerId != null) {
            Player dialer = Bukkit.getPlayer(dialerId);
            if (dialer != null && dialer.isOnline()) {
                String msgKey = manual ? "messages.portal-shutdown-manual" : "messages.portal-closed";
                dialer.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                 plugin.getMessageManager().getMessage(msgKey));
            }
        }

        // Removed notifyPortalUsers(portal, closedMsg) to maintain privacy

        // Cancel existing economy task to prevent overlap
        if (portal.getEconomyTask() != null) {
            portal.getEconomyTask().cancel();
            portal.setEconomyTask(null);
        }

        // Get target BEFORE clearing the ID
        target = plugin.getPortalManager().getPortalById(portal.getTargetPortalId());

        portal.setActive(false);
        portal.setTargetPortalId(null);
        // Only clear dialer ID if this portal was the one that initiated the call
        if (portal.getDialerId() != null) {
            portal.setLastDeactivationTime(System.currentTimeMillis());
            portal.setDialerId(null);
        }
        portal.setDialing(false);
        portal.setConnectionExpiry(0);
        portal.setConnectionStartTime(0);
        
        if (target != null) {
            target.setTargetPortalId(null);
            target.setDialerId(null); // Always clear for target
            target.setConnectionExpiry(0);
            target.setActive(false);
            target.setConnectionStartTime(0);
            target.setDialing(false);
            target.getCoreLocation().getWorld().playSound(target.getCoreLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
        }
        
        portal.getCoreLocation().getWorld().playSound(portal.getCoreLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
        
        plugin.getPortalManager().markDirty();
    }

    // Cleanup: notifyPortalUsers removed to maintain dialer privacy as requested

    private void startTeleportTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Portal source : plugin.getPortalManager().getPortals()) {
                    if (source.isActive() && source.getDialerId() != null) {
                        if (source.isExpired()) {
                            deactivate(source);
                        } else {
                            // INACTIVITY CHECK (Only if first activity has occurred)
                            int inactivityShutdown = plugin.getConfig().getInt("portal-settings.inactivity-shutdown-seconds", 10);
                            if (inactivityShutdown != -1 && source.hasHadActivity()) {
                                long inactiveMillis = inactivityShutdown * 1000L;
                                if (System.currentTimeMillis() - source.getLastActivityTime() > inactiveMillis) {
                                    deactivate(source);
                                    continue; // Move to next portal
                                }
                            }
                            
                            // Check integrity during call
                            if (!source.getCoreLocation().getBlock().getType().equals(Material.BEACON)) {
                                deactivate(source);
                            } else {
                                checkEntityTeleportation(source);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // 1 TICK FOR MAX PRECISION
    }

    private void checkEntityTeleportation(Portal source) {
        Location core = source.getCoreLocation();
        Location membraneCenter = core.clone().add(0.5, 2.5, 0.5);
        
        org.bukkit.util.Vector forward = getForwardVector(source.getFacingYaw());
        org.bukkit.util.Vector right = forward.clone().rotateAroundY(Math.toRadians(-90));
        org.bukkit.util.Vector up = new org.bukkit.util.Vector(0, 1, 0);

        membraneCenter.add(forward.clone().multiply(source.getPlaneOffset()));
        membraneCenter.add(right.clone().multiply(source.getMembraneOffset()));

        double radius = Math.max(source.getHoleWidth(), source.getHoleHeight()) / 2.0 + 1.5;
        Collection<Entity> entities = membraneCenter.getWorld().getNearbyEntities(membraneCenter, radius, radius, radius);

        for (Entity entity : entities) {
            if (entity.isDead() || entity.hasMetadata("stp_teleport_cooldown")) continue;
            if (entity.isInsideVehicle()) continue; // The vehicle will handle the teleport for its passengers

            boolean shouldTeleport = false;
            double relLateral = 0, relVertical = 0, relDepth = 0;

            // NEW: Bounding Box Collision (Highest reliability)
            org.bukkit.util.BoundingBox entityBox = entity.getBoundingBox();
            
            // 1. Direct Overlap Check (Slow/Medium movement)
            // Restricted to 3x3 hole area (1.5 radius) as requested
            org.bukkit.util.Vector membraneMin = membraneCenter.toVector().clone()
                .add(right.clone().multiply(-1.49))
                .add(up.clone().multiply(-1.49))
                .add(forward.clone().multiply(-0.2));
            org.bukkit.util.Vector membraneMax = membraneCenter.toVector().clone()
                .add(right.clone().multiply(1.49))
                .add(up.clone().multiply(1.49))
                .add(forward.clone().multiply(0.2));
            
            org.bukkit.util.BoundingBox membraneBox = org.bukkit.util.BoundingBox.of(membraneMin, membraneMax);
            
            if (entityBox.overlaps(membraneBox)) {
                shouldTeleport = true;
                org.bukkit.util.Vector entityCenter = new org.bukkit.util.Vector(entityBox.getCenterX(), entityBox.getCenterY(), entityBox.getCenterZ());
                org.bukkit.util.Vector centerDiff = entityCenter.subtract(membraneCenter.toVector());
                relLateral = centerDiff.dot(right);
                relVertical = centerDiff.dot(up);
                relDepth = centerDiff.dot(forward);
            } else {
                // 2. Swept Collision / Raytracing (High-speed movement)
                // Catches entities that move so fast they skip the membrane thickness in a single tick
                org.bukkit.util.Vector vel = entity.getVelocity();
                double dotVel = vel.dot(forward);
                
                if (Math.abs(dotVel) > 0.05) { 
                    org.bukkit.util.Vector pos = entity.getLocation().toVector();
                    org.bukkit.util.Vector diff = pos.subtract(membraneCenter.toVector());
                    double relD = diff.dot(forward);
                    
                    double t = -relD / dotVel; // Time to plane
                    if (t >= 0 && t <= 1.2) { 
                        org.bukkit.util.Vector intersect = diff.add(vel.clone().multiply(t));
                        double iLat = intersect.dot(right);
                        double iVert = intersect.dot(up);
                        
                        if (Math.abs(iLat) <= 1.49 && Math.abs(iVert) <= 1.49) {
                            shouldTeleport = true;
                            relLateral = iLat;
                            relVertical = iVert;
                            relDepth = -0.05 * Math.signum(dotVel); // Nudge past plane
                        }
                    }
                }
            }

            if (shouldTeleport) {
                Portal target = plugin.getPortalManager().getPortalById(source.getTargetPortalId());
                if (target != null) {
                    teleportEntityRelative(entity, source, target, relLateral, relVertical, relDepth, forward);
                }
            }
        }
    }

    private void teleportEntityRelative(Entity entity, Portal source, Portal target, double relLat, double relVert, double relDepth, org.bukkit.util.Vector sForward) {
        org.bukkit.util.Vector tForward = getForwardVector(target.getFacingYaw());
        org.bukkit.util.Vector tRight = tForward.clone().rotateAroundY(Math.toRadians(-90));
        org.bukkit.util.Vector up = new org.bukkit.util.Vector(0, 1, 0);

        // IRIS CHECK
        if (target.isIrisActive()) {
            handleIrisCollision(entity, source, target, sForward, relDepth);
            return;
        }

        Location targetCore = target.getCoreLocation();
        Location targetMembraneCenter = targetCore.clone().add(0.5, 2.5, 0.5);
        
        targetMembraneCenter.add(tForward.clone().multiply(target.getPlaneOffset()));
        targetMembraneCenter.add(tRight.clone().multiply(target.getMembraneOffset()));

        // Calculate Exit Location: Preserve relative position (Seamless / Nether-style)
        Location exitLoc = targetMembraneCenter.clone();
        exitLoc.add(tRight.clone().multiply(-relLat)); // RESTORED NEGATION: Mirror relative side
        exitLoc.add(up.clone().multiply(relVert));
        
        double depthOffset = relDepth;
        if (entity instanceof org.bukkit.entity.Minecart) {
            depthOffset = relDepth + (relDepth >= 0 ? 0.5 : -0.5); // Offset ahead in both directions
        }
        exitLoc.add(tForward.clone().multiply(depthOffset));

        // Preserve Orientation Relative to Portal: Standard Teleport Logic (+180 deg)
        // If you enter FACING the portal, you should exit looking AWAY from the target face.
        float deltaYaw = target.getFacingYaw() - (source.getFacingYaw() + 180f);
        while (deltaYaw > 180) deltaYaw -= 360;
        while (deltaYaw <= -180) deltaYaw += 360;

        Location entityLoc = entity.getLocation();
        exitLoc.setYaw(entityLoc.getYaw() + deltaYaw);
        exitLoc.setPitch(entityLoc.getPitch());

        // Rotate Velocity
        org.bukkit.util.Vector vel = entity.getVelocity();
        if (vel.lengthSquared() > 0.0001) {
            vel.rotateAroundY(Math.toRadians(-deltaYaw));
        }

        // VEHICLE HANDLING: Handle passengers to ensure they stay mounted and keep inertia
        java.util.List<Entity> passengers = new java.util.ArrayList<>(entity.getPassengers());
        for (Entity p : passengers) entity.removePassenger(p);

        entity.teleport(exitLoc);
        entity.setVelocity(vel);
        
        // Teleport and rotate passengers if any
        for (Entity p : passengers) {
            p.teleport(exitLoc);
            org.bukkit.util.Vector pVel = p.getVelocity();
            if (pVel.lengthSquared() > 0.0001) {
                pVel.rotateAroundY(Math.toRadians(-deltaYaw));
            }
            p.setVelocity(pVel);
            
            // Cooldown for passengers
            p.setMetadata("stp_teleport_cooldown", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            final Entity finalP = p;
            new BukkitRunnable() {
                @Override
                public void run() { finalP.removeMetadata("stp_teleport_cooldown", plugin); }
            }.runTaskLater(plugin, 10L);

            entity.addPassenger(p);
        }

        // Cooldown and Effects for main entity
        entity.setMetadata("stp_teleport_cooldown", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        new BukkitRunnable() {
            @Override
            public void run() {
                entity.removeMetadata("stp_teleport_cooldown", plugin);
            }
        }.runTaskLater(plugin, 10L);
        
        exitLoc.getWorld().playSound(exitLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        // Reset inactivity timer on successful teleport
        source.setHasHadActivity(true);
        target.setHasHadActivity(true);
        source.setLastActivityTime(System.currentTimeMillis());
        target.setLastActivityTime(System.currentTimeMillis());
    }

    private void handleIrisCollision(Entity entity, Portal source, Portal target, org.bukkit.util.Vector sForward, double relDepth) {
        String mode = plugin.getConfig().getString("iris-settings.behavior-mode", "NOTHING_PASSES");
        double contactDamage = plugin.getConfig().getDouble("iris-settings.contact-damage", 4.0);
        double killDamage = plugin.getConfig().getDouble("iris-settings.kill-damage", 1000.0);

        // Sound Cooldown (Metadata)
        if (!entity.hasMetadata("stp_iris_sfx_cooldown")) {
            entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
            entity.setMetadata("stp_iris_sfx_cooldown", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            new BukkitRunnable() {
                @Override
                public void run() { entity.removeMetadata("stp_iris_sfx_cooldown", plugin); }
            }.runTaskLater(plugin, 20L); // 1s
        }
        
        // JITTER PREVENTION: Apply teleport-style cooldown even if blocked
        entity.setMetadata("stp_teleport_cooldown", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        new BukkitRunnable() {
            @Override
            public void run() { entity.removeMetadata("stp_teleport_cooldown", plugin); }
        }.runTaskLater(plugin, 10L); // 0.5s pause before next check

        if (mode.equalsIgnoreCase("PASSES_AND_DIES")) {
            // PASSES_AND_DIES: Teleport with rotation and keep velocity (then damage)
            Location targetCore = target.getCoreLocation();
            Location exitLoc = targetCore.clone().add(0.5, 2.5, 0.5); // Center of target membrane
            
            float deltaYaw = target.getFacingYaw() - (source.getFacingYaw() + 180);
            org.bukkit.util.Vector vel = entity.getVelocity();
            if (vel.lengthSquared() > 0.0001) {
                vel.rotateAroundY(Math.toRadians(-deltaYaw));
            }
            
            entity.teleport(exitLoc);
            entity.setVelocity(vel);
            
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) entity).damage(killDamage);
            } else {
                entity.remove();
            }
            
            // PASSES_AND_DIES counts as activity because they entered the event horizon
            source.setHasHadActivity(true);
            target.setHasHadActivity(true);
            source.setLastActivityTime(System.currentTimeMillis());
            target.setLastActivityTime(System.currentTimeMillis());
        } else {
            // NOTHING_PASSES mode (Blocked entirely)
            if (entity instanceof org.bukkit.entity.Projectile) {
                entity.remove();
                return; // No push for projectiles
            } else if (entity instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) entity).damage(contactDamage);
            } else {
                entity.remove();
                return;
            }
            
            // Directional push back away from membrane (always away from portal core)
            double side = Math.signum(relDepth / 0.0001); 
            if (side == 0) side = 1.0; 

            org.bukkit.util.Vector push = sForward.clone().multiply(side * 0.6);
            push.setY(0.15); // Subtle upward nudge
            
            entity.setVelocity(push); // Natural bounce-back
        }
    }

    public void closePortalInventories(Portal portal) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Inventory top = p.getOpenInventory().getTopInventory();
            if (top != null && top.getHolder() instanceof PortalInventoryHolder) {
                PortalInventoryHolder holder = (PortalInventoryHolder) top.getHolder();
                if (holder.getPortal().getId().equals(portal.getId())) {
                    // Do NOT redirect administrative menus (CONFIG, WHITELIST)
                    String type = holder.getMenuType();
                    if (type.equals("CONFIG") || type.equals("WHITELIST")) continue;

                    // Kick to Control Panel (MAIN MENU) instead of closing
                    plugin.getMenuListener().addTransitioningPlayer(p.getUniqueId());
                    plugin.getDialingMenu().openMain(p, portal);
                }
            }
        }
    }

    private org.bukkit.util.Vector getForwardVector(float yaw) {
        // Bukkit Yaw: 0=SOUTH(+Z), 90=WEST(-X), 180=NORTH(-Z), 270=EAST(+X)
        double rad = Math.toRadians(yaw);
        double x = -Math.sin(rad);
        double z = Math.cos(rad);
        return new org.bukkit.util.Vector(x, 0, z);
    }
}
