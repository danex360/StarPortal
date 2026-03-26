package me.danex360.starportal.util;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.model.Portal;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class PortalVFX {

    public static void spawnMembrane(StarPortal plugin, Portal portal) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!portal.isActive() || portal.isExpired()) {
                    this.cancel();
                    return;
                }

                Location core = portal.getCoreLocation();
                int thresholdSec = plugin.getConfig().getInt("portal-settings.blinking-threshold-seconds", 10);
                boolean isBlinking = portal.getConnectionExpiry() - System.currentTimeMillis() < (thresholdSec * 1000L);
                boolean northSouth = portal.isNorthSouth();
                
                double centerX = northSouth ? core.getX() + portal.getPlaneOffset() + 0.5 : core.getX() + portal.getMembraneOffset() + 0.5;
                double centerZ = northSouth ? core.getZ() + portal.getMembraneOffset() + 0.5 : core.getZ() + portal.getPlaneOffset() + 0.5;

                Particle pType = Particle.DUST;
                Particle.DustOptions baseColor = (portal.isIrisActive() && portal.getDialerId() == null) ? 
                    new Particle.DustOptions(org.bukkit.Color.GRAY, 1.5f) : 
                    new Particle.DustOptions(org.bukkit.Color.AQUA, 1.2f);
                
                boolean showRed = isBlinking && (ticks % 10 < 4);
                Particle.DustOptions redColor = new Particle.DustOptions(org.bukkit.Color.RED, 1.4f);

                // Denser loop for Iris or normal membrane
                double step = portal.isIrisActive() ? 0.1 : 0.2;
                for (double h = 1.0; h <= 4.1; h += step) {
                    for (double w = -1.5; w <= 1.5; w += step) {
                        double x = northSouth ? centerX : centerX + w;
                        double z = northSouth ? centerZ + w : centerZ;
                        
                        Location pLoc = new Location(core.getWorld(), x, core.getY() + h, z);
                        pLoc.getWorld().spawnParticle(pType, pLoc, 1, 0.01, 0.01, 0.01, 0.01, baseColor);
                        
                        if (showRed && (h + w) % 0.8 < 0.2) {
                            pLoc.getWorld().spawnParticle(pType, pLoc, 1, 0.05, 0.05, 0.05, 0.01, redColor);
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public static void spawnKwoosh(StarPortal plugin, Portal portal) {
        Location core = portal.getCoreLocation();
        if (core == null) return;
        
        boolean northSouth = portal.isNorthSouth();
        double centerX = northSouth ? core.getX() + portal.getPlaneOffset() + 0.5 : core.getX() + portal.getMembraneOffset() + 0.5;
        double centerZ = northSouth ? core.getZ() + portal.getMembraneOffset() + 0.5 : core.getZ() + portal.getPlaneOffset() + 0.5;
        Location membraneCenter = new Location(core.getWorld(), centerX, core.getY() + 2.5, centerZ);

        // Subtle "Watery" burst instead of generic explosion
        membraneCenter.getWorld().spawnParticle(Particle.PORTAL, membraneCenter, 80, 0.4, 0.8, 0.4, 0.1);
        membraneCenter.getWorld().spawnParticle(Particle.SPLASH, membraneCenter, 40, 0.3, 0.6, 0.3, 0.05);
        membraneCenter.getWorld().playSound(membraneCenter, Sound.BLOCK_WATER_AMBIENT, 1.5f, 0.5f);
        membraneCenter.getWorld().playSound(membraneCenter, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }
}
