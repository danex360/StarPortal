package me.danex360.starportal.listener;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.model.Portal;
import me.danex360.starportal.gui.PortalInventoryHolder;
import me.danex360.starportal.util.PortalVFX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MenuListener implements Listener {
    private final StarPortal plugin;
    private final Map<UUID, Portal> activePortals = new HashMap<>();
    private final Map<UUID, String> dialingInputs = new HashMap<>();
    private final Map<UUID, Portal> renamingPortals = new HashMap<>();
    private final Set<UUID> transitioningPlayers = new HashSet<>();
    private final Map<UUID, Portal> whitelistAdding = new HashMap<>();
    private final Map<UUID, Location> initialLocations = new HashMap<>();

    public MenuListener(StarPortal plugin) {
        this.plugin = plugin;
        startRefreshTask();
    }

    private void startRefreshTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, Portal> entry : activePortals.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        plugin.getDialingMenu().updateMenu(player, entry.getValue());
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void setPlayerActivePortal(Player player, Portal portal) {
        activePortals.put(player.getUniqueId(), portal);
    }

    public void addTransitioningPlayer(UUID uuid) {
        transitioningPlayers.add(uuid);
    }

    public void updateAllViewers(Portal portal) {
        for (Map.Entry<UUID, Portal> entry : activePortals.entrySet()) {
            if (entry.getValue().getId().equals(portal.getId())) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    plugin.getDialingMenu().updateMenu(player, portal);
                }
            }
        }
    }

    public String getDialingInput(Player player) {
        return dialingInputs.getOrDefault(player.getUniqueId(), "");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (transitioningPlayers.contains(uuid)) {
            transitioningPlayers.remove(uuid);
            return;
        }
        activePortals.remove(uuid);
        dialingInputs.remove(uuid);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PortalInventoryHolder)) return;
        
        event.setCancelled(true);
        PortalInventoryHolder holder = (PortalInventoryHolder) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        Portal portal = holder.getPortal();
        String type = holder.getMenuType();
        
        // Bukkit.getLogger().info("[StarPortal] Menu Click: Player=" + player.getName() + " Type=" + type + " Slot=" + event.getRawSlot());

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        // Common Back Button (Slot 45 for most menus)
        if (slot == 45 && !type.equals("MAIN") && !type.equals("CONFIG")) {
            if (type.equals("WHITELIST")) {
                plugin.getDialingMenu().openConfig(player, portal);
            } else {
                plugin.getDialingMenu().openMain(player, portal);
            }
            return;
        }

        switch (type) {
            case "MAIN":
                handleMainClick(player, portal, slot, event);
                break;
            case "NUMPAD":
                handleNumPadClick(player, portal, slot);
                break;
            case "SAVED":
                handleSavedClick(player, portal, slot, event.getCurrentItem());
                break;
            case "CONFIG":
                handleConfigClick(player, portal, slot);
                break;
            case "WHITELIST":
                handleWhitelistClick(player, portal, slot, event.getCurrentItem());
                break;
        }
    }

    private void handleMainClick(Player player, Portal portal, int slot, InventoryClickEvent event) {
        if (slot == 19) { // Dial / Shutdown
            if (portal.isActive()) {
                // NEW: Only the initiating portal can shutdown
                if (portal.getDialerId() == null) {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                            plugin.getMessageManager().getMessage("messages.portal-busy"));
                    return;
                }
                plugin.getConnectionManager().deactivate(portal, true);
                updateAllViewers(portal);
            } else {
                long cooldownTime = plugin.getConfig().getLong("portal-settings.dialing-cooldown-seconds", 30) * 1000;
                long elapsed = System.currentTimeMillis() - portal.getLastDeactivationTime();
                if (elapsed < cooldownTime) {
                    long remaining = (cooldownTime - elapsed) / 1000;
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                            plugin.getMessageManager().getMessage("messages.portal-cooldown-error").replace("{time}", String.valueOf(remaining)));
                    return;
                }
                dialingInputs.put(player.getUniqueId(), "");
                transitioningPlayers.add(player.getUniqueId());
                plugin.getDialingMenu().openNumberPad(player, portal, "");
            }
        } else if (slot == 22) { // History
            if (portal.isActive()) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.portal-busy"));
                return;
            }
            transitioningPlayers.add(player.getUniqueId());
            plugin.getDialingMenu().openSavedList(player, portal);
        } else if (slot == 25) { // Iris Toggle
            if (portal.hasIris()) {
                // Anyone can toggle Iris in the DHD menu per user request
                portal.setIrisActive(!portal.isIrisActive());
                plugin.getPortalManager().markDirty();
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 1f);
                updateAllViewers(portal);
                String status = portal.isIrisActive() ? plugin.getMessageManager().getMessage("messages.gui.status-closed") : plugin.getMessageManager().getMessage("messages.gui.status-open");
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.iris-status-format").replace("{status}", status));
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.iris-not-installed-error"));
            }
        } else if (slot == 40) { // Pearls (Now in slot 40)
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == org.bukkit.Material.ENDER_PEARL) {
                int max = plugin.getConfig().getInt("portal-settings.max-pearl-balance", 64);
                int current = portal.getPearlBalance();
                if (current >= max) {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.pearls-max-reached"));
                    return;
                }
                int toAdd = Math.min(hand.getAmount(), max - current);
                if (toAdd > 0) {
                    portal.setPearlBalance(current + toAdd);
                    hand.setAmount(hand.getAmount() - toAdd);
                    plugin.getPortalManager().markDirty();
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                     plugin.getMessageManager().getMessage("messages.pearls-deposited").replace("{amount}", String.valueOf(toAdd)));
                    plugin.getDialingMenu().updateMenu(player, portal);
                }
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-pearls-in-hand"));
            }
        }
    }

    private void handleConfigClick(Player player, Portal portal, int slot) {
        if (!portal.isAllowed(player.getUniqueId()) && !player.hasPermission("starportal.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission-portal"));
            player.closeInventory();
            return;
        }
        if (slot == 20) { // Rename
            // Allowed for Owner/Admin and Whitelist per user instruction
            transitioningPlayers.add(player.getUniqueId());
            player.closeInventory();
            renamingPortals.put(player.getUniqueId(), portal);
            initialLocations.put(player.getUniqueId(), player.getLocation());
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.enter-name"));
        } else if (slot == 22) { // Install Iris (Status is informative only)
            if (!portal.hasIris()) {
                // Allowed for Owner/Admin and Whitelist
                ItemStack hand = player.getInventory().getItemInMainHand();
                Material irisMat = Material.valueOf(plugin.getConfig().getString("iris-settings.item-material", "NETHERITE_BLOCK"));
                if (hand.getType() == irisMat) {
                    hand.setAmount(hand.getAmount() - 1);
                    portal.setHasIris(true);
                    plugin.getPortalManager().markDirty();
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1f);
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.iris-installed"));
                    updateAllViewers(portal);
                } else {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                        plugin.getMessageManager().getMessage("messages.iris-install-fail").replace("{material}", plugin.getDialingMenu().getFriendlyMaterialName(irisMat)));
                }
            }
        } else if (slot == 24) { // DESTROY PORTAL
            if (!portal.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("starportal.admin")) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission"));
                return;
            }
            if (portal.isActive()) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                        plugin.getMessageManager().getMessage("messages.cannot-delete-active-portal"));
                return;
            }
            
            Location loc = portal.getCoreLocation();
            if (loc != null) {
                Block b = loc.getBlock();
                if (b.getType() == Material.BEACON) {
                    // Use scheduler for safety against POI/Block mismatches
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (b.getType() == Material.BEACON) b.setType(Material.AIR);
                    });
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.BEACON));
                }
                plugin.getPortalManager().dropPearls(portal, loc);
                
                // Return Iris material if present
                if (portal.hasIris()) {
                    Material irisMat = Material.valueOf(plugin.getConfig().getString("iris-settings.item-material", "NETHERITE_BLOCK"));
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(irisMat));
                }
            }

            plugin.getPortalManager().removePortal(portal.getId());
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                    plugin.getMessageManager().getMessage("messages.portal-deleted"));
        } else if (slot == 40) { // Whitelist
            if (!portal.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("starportal.admin")) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission"));
                return;
            }
            transitioningPlayers.add(player.getUniqueId());
            plugin.getDialingMenu().openWhitelistMenu(player, portal);
        }
    }

    private void handleWhitelistClick(Player player, Portal portal, int slot, ItemStack clicked) {
        if (slot == 49) { // Add Player
            transitioningPlayers.add(player.getUniqueId());
            player.closeInventory();
            whitelistAdding.put(player.getUniqueId(), portal);
            initialLocations.put(player.getUniqueId(), player.getLocation());
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.gui.enter-whitelist-name"));
        } else if (slot == 45) { // Back
            transitioningPlayers.add(player.getUniqueId());
            plugin.getDialingMenu().openConfig(player, portal);
        } else if (clicked != null && clicked.getType() == Material.PLAYER_HEAD && slot < 45) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
                org.bukkit.inventory.meta.SkullMeta sm = (org.bukkit.inventory.meta.SkullMeta) meta;
                if (sm.getOwningPlayer() != null) {
                    UUID toRemove = sm.getOwningPlayer().getUniqueId();
                    portal.getWhitelist().remove(toRemove);
                    plugin.getPortalManager().markDirty();
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                        plugin.getMessageManager().getMessage("messages.gui.player-removed-whitelist").replace("{player}", sm.getOwningPlayer().getName()));
                    plugin.getDialingMenu().openWhitelistMenu(player, portal);
                }
            }
        }
    }

    private void handleNumPadClick(Player player, Portal portal, int slot) {
        String current = dialingInputs.getOrDefault(player.getUniqueId(), "");
        if (slot == 39) { // Clear All
            dialingInputs.put(player.getUniqueId(), "");
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
            transitioningPlayers.add(player.getUniqueId());
            plugin.getDialingMenu().openNumberPad(player, portal, "");
        } else if (slot == 41 && current.length() == 6) { // Dial Button (Synced to slot 41)
            plugin.getConnectionManager().dial(player, portal, current);
        } else {
            int num = -1;
            if (slot == 12) num = 1; else if (slot == 13) num = 2; else if (slot == 14) num = 3;
            else if (slot == 21) num = 4; else if (slot == 22) num = 5; else if (slot == 23) num = 6;
            else if (slot == 30) num = 7; else if (slot == 31) num = 8; else if (slot == 32) num = 9;
            else if (slot == 40) num = 0;

            if (num != -1 && current.length() < 6) {
                current += num;
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 2f);
                dialingInputs.put(player.getUniqueId(), current);
                transitioningPlayers.add(player.getUniqueId());
                plugin.getDialingMenu().openNumberPad(player, portal, current);
            }
        }
    }

    private void handleSavedClick(Player player, Portal portal, int slot, ItemStack clicked) {
        if (portal.isActive()) {
            player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.portal-busy"));
            player.closeInventory();
            return;
        }
        if (clicked != null && clicked.getType() == org.bukkit.Material.MAP) {
            org.bukkit.inventory.meta.ItemMeta meta = clicked.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey addressKey = new org.bukkit.NamespacedKey(plugin, "address");
                String addr = meta.getPersistentDataContainer().get(addressKey, org.bukkit.persistence.PersistentDataType.STRING);
                if (addr != null) {
                    plugin.getConnectionManager().dial(player, portal, addr.trim());
                }
            }
        } else if (slot == 45) { // Back button (Arrow at slot 45)
            plugin.getDialingMenu().openMain(player, portal);
        }
    }

    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!initialLocations.containsKey(uuid)) return;

        Location initial = initialLocations.get(uuid);
        if (event.getTo() != null && initial.distanceSquared(event.getTo()) > 1.0) {
            if (renamingPortals.containsKey(uuid)) {
                renamingPortals.remove(uuid);
                initialLocations.remove(uuid);
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                    plugin.getMessageManager().getMessage("messages.rename-cancelled-move"));
            } else if (whitelistAdding.containsKey(uuid)) {
                whitelistAdding.remove(uuid);
                initialLocations.remove(uuid);
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                    plugin.getMessageManager().getMessage("messages.whitelist-cancelled-move"));
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (renamingPortals.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Portal p = renamingPortals.remove(player.getUniqueId());
            initialLocations.remove(player.getUniqueId());
            String name = event.getMessage().trim();
            
            if (name.length() > 16) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                 plugin.getMessageManager().getMessage("messages.error-name-too-long"));
            } else {
                p.setName(name);
                plugin.getPortalManager().markDirty();
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                 plugin.getMessageManager().getMessage("messages.name-changed").replace("{name}", name));
            }
            new BukkitRunnable() { @Override public void run() { plugin.getDialingMenu().openConfig(player, p); } }.runTaskLater(plugin, 1L);
        } else if (whitelistAdding.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Portal p = whitelistAdding.remove(player.getUniqueId());
            initialLocations.remove(player.getUniqueId());
            String name = event.getMessage().trim();
            org.bukkit.OfflinePlayer target = null;
            try {
                // First try online players for performance
                target = Bukkit.getPlayer(name);
                if (target == null) {
                    @SuppressWarnings("deprecation")
                    org.bukkit.OfflinePlayer off = Bukkit.getOfflinePlayer(name);
                    target = off;
                }
            } catch (Exception e) {
                // This happens if name is not found in Mojang (online-mode true) or connection error
            }

            if (target != null && (target.hasPlayedBefore() || target.isOnline() || !Bukkit.getOnlineMode())) {
                if (!p.getWhitelist().contains(target.getUniqueId())) {
                    p.getWhitelist().add(target.getUniqueId());
                    plugin.getPortalManager().markDirty();
                }
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                    plugin.getMessageManager().getMessage("messages.gui.player-added-whitelist").replace("{player}", name));
            } else {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.gui.player-not-found"));
            }
            new BukkitRunnable() { @Override public void run() { plugin.getDialingMenu().openWhitelistMenu(player, p); } }.runTaskLater(plugin, 1L);
        }
    }
}
