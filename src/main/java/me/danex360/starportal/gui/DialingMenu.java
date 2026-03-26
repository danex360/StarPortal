package me.danex360.starportal.gui;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.model.Portal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DialingMenu {
    private final StarPortal plugin;
    private final String mainTitle;
    private final String configTitle;

    private static final String[] NUMBER_SKINS = {
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI4N2I0NDExMWE5ZjMzOWVkNzAxNWQwZjJjYjY0NmNlNmI4YzU5YTBiNzUwYjI3MjQ0OWFlYWMyNTYyNWRmYSJ9fX0=", // 0
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJkNGE2OTkzN2UwYmVhZGMzODQyNmMwOTk0YjUwZDk1MDQwNmZkOGRhOWYzMWM1ODJkNDZmM2I5YmZjNGM1YiJ9fX0=", // 1
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzBhNmM3YTBkNjU4YmI5MGUyN2I1OTM0ZjYyYTVlMTVjYzljMTFjODdhZTE0NjRhNGU3OWVhNjY1MjNiYTM2MSJ9fX0=", // 2
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYxYjMxYTg3Yjc4MjYyYzYzZTk0NzE0ZTU2MjRhMmFiNTk1MGY3NWRlZTMyY2MzMDI2YTVmYTc4MjM0NjhkZSJ9fX0=", // 3
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FkZmQzYzk5OTY3ZDMyNzQ5MDJlY2I2ZTk4NjU4YWNmZGIzOTE4NzE3YjJlOTAzN2Y2MWMzYjRlMDllMmExIn19fQ==", // 4
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGJiYWYwMTkwOTIyMWI5YWJlOTQ1YWZlN2RmZGI3MmYzMTczMzExZTU2MjAxOTRkZDI3MDExYTZkNTU0ZmZjOCJ9fX0=", // 5
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZhZTBmZTIyNTZhZTM1NmEyNWYxMzBhZTcxY2Y0NDMxNTE1N2M1ZmFlOTFkNjJhNGZmYjU4NWIxNjQ4NjM3MyJ9fX0=", // 6
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGYwOWVmZTczMWU3M2M4MGIxYWVlMTAwYmIzMzBhYjQxNDU5NWVlNTRhNGUyZGVjNDM5YmVkM2UzNjQ5YWM5NiJ9fX0=", // 7
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcyN2Q0ZTQ4ZjIzMWNlNGQ4NzE5OTI1NjBmNTFiZjZhM2YxNTdjMmZmZDZmOTJiODYwY2JiNTMxMjg0MjZhMiJ9fX0=", // 8
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ3M2VhMzUxZjQxZTk5NTdmOTE0ZTNiOTBmNzRlOTg2NzgzOGIzMzM5ZDQyNTEzY2EyNWVkMGY0NWJjNjBjYiJ9fX0="  // 9
    };
    
    // Use unique v4 UUIDs for number heads to avoid cache collision
    private static final java.util.UUID[] NUMBER_UUIDS = {
        UUID.fromString("c0e2a2b0-8c2c-4b6a-8b1e-6e4b1e4b1e40"),
        UUID.fromString("c0e2a2b1-8c2c-4b6a-8b1e-6e4b1e4b1e41"),
        UUID.fromString("c0e2a2b2-8c2c-4b6a-8b1e-6e4b1e4b1e42"),
        UUID.fromString("c0e2a2b3-8c2c-4b6a-8b1e-6e4b1e4b1e43"),
        UUID.fromString("c0e2a2b4-8c2c-4b6a-8b1e-6e4b1e4b1e44"),
        UUID.fromString("c0e2a2b5-8c2c-4b6a-8b1e-6e4b1e4b1e45"),
        UUID.fromString("c0e2a2b6-8c2c-4b6a-8b1e-6e4b1e4b1e46"),
        UUID.fromString("c0e2a2b7-8c2c-4b6a-8b1e-6e4b1e4b1e47"),
        UUID.fromString("c0e2a2b8-8c2c-4b6a-8b1e-6e4b1e4b1e48"),
        UUID.fromString("c0e2a2b9-8c2c-4b6a-8b1e-6e4b1e4b1e49")
    };

    public DialingMenu(StarPortal plugin) {
        this.plugin = plugin;
        this.mainTitle = plugin.getMessageManager().getMessage("messages.gui.main-title");
        this.configTitle = plugin.getMessageManager().getMessage("messages.gui.config-title");
    }

    private void fillBackground(Inventory inv) {
        ItemStack pane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, pane);
        }
    }

    public void openMain(Player player, Portal portal) {
        plugin.getMenuListener().setPlayerActivePortal(player, portal);
        Inventory inv = Bukkit.createInventory(new PortalInventoryHolder(portal, "MAIN"), 54, mainTitle);
        fillBackground(inv);

        // Info (Slot 4)
        inv.setItem(4, createInfoItem(portal));

        // Dial Pad (Slot 19)
        if (portal.isActive()) {
            if (portal.getDialerId() != null) {
                inv.setItem(19, createItem(Material.REDSTONE_BLOCK, plugin.getMessageManager().getMessage("messages.gui.shutdown"), plugin.getMessageManager().getMessage("messages.gui.shutdown-lore")));
            } else {
                inv.setItem(19, createItem(Material.COMPASS, plugin.getMessageManager().getMessage("messages.gui.status-incoming"), plugin.getMessageManager().getMessage("messages.gui.portal-busy")));
            }
        } else {
            long cooldownTime = plugin.getConfig().getLong("portal-settings.dialing-cooldown-seconds", 30) * 1000;
            long elapsed = System.currentTimeMillis() - portal.getLastDeactivationTime();
            if (elapsed < cooldownTime) {
                long remaining = (cooldownTime - elapsed) / 1000;
                inv.setItem(19, createItem(Material.COMPASS, plugin.getMessageManager().getMessage("messages.gui.status-recharging"), 
                        plugin.getMessageManager().getMessage("messages.gui.recharging-lore").replace("{time}", String.valueOf(remaining))));
            } else {
                inv.setItem(19, createItem(Material.COMPASS, plugin.getMessageManager().getMessage("messages.gui.num-pad"), plugin.getMessageManager().getMessage("messages.gui.num-pad-lore")));
            }
        }
        
        // History (Slot 22)
        inv.setItem(22, createItem(Material.BOOK, plugin.getMessageManager().getMessage("messages.gui.saved-addresses"), plugin.getMessageManager().getMessage("messages.gui.saved-addresses-lore")));
        
        // Iris Toggle (Slot 25)
        if (plugin.getConfig().getBoolean("iris-settings.enabled", true)) {
            if (portal.hasIris()) {
                Material irisIcon = portal.isIrisActive() ? Material.IRON_TRAPDOOR : Material.IRON_DOOR;
                String status = portal.isIrisActive() ? plugin.getMessageManager().getMessage("messages.gui.status-closed") : plugin.getMessageManager().getMessage("messages.gui.status-open");
                inv.setItem(25, createItem(irisIcon, plugin.getMessageManager().getMessage("messages.gui.iris-prefix") + status, plugin.getMessageManager().getMessage("messages.gui.click-to-change")));
            } else {
                inv.setItem(25, createItem(Material.GRAY_DYE, plugin.getMessageManager().getMessage("messages.gui.iris-not-installed"), 
                        plugin.getMessageManager().getMessage("messages.gui.iris-not-installed-lore1"), 
                        plugin.getMessageManager().getMessage("messages.gui.iris-not-installed-lore2")));
            }
        }
        
        // Config removed from Control Panel to keep it separate (accessible via Beacon)
        
        // Pearls (Slot 40 - Center 5th Row)
        int maxPearls = plugin.getConfig().getInt("portal-settings.max-pearl-balance", 64);
        String pearlBalance = plugin.getMessageManager().getMessage("messages.gui.pearl-balance").replace("{balance}", String.valueOf(portal.getPearlBalance()));
        String pearlStorageLore = plugin.getMessageManager().getMessage("messages.gui.pearl-storage-lore").replace("{max}", String.valueOf(maxPearls));
        inv.setItem(40, createItem(Material.ENDER_PEARL, plugin.getMessageManager().getMessage("messages.gui.pearl-storage"), pearlBalance, pearlStorageLore));

        setupBottomRow(inv, false);
        player.openInventory(inv);
    }

    public void openConfig(Player player, Portal portal) {
        plugin.getMenuListener().setPlayerActivePortal(player, portal);
        Inventory inv = Bukkit.createInventory(new PortalInventoryHolder(portal, "CONFIG"), 54, configTitle);
        fillBackground(inv);

        // Info (Slot 4)
        inv.setItem(4, createInfoItem(portal));

        // Rename (Slot 20)
        inv.setItem(20, createItem(Material.NAME_TAG, plugin.getMessageManager().getMessage("messages.gui.rename-portal"), 
                plugin.getMessageManager().getMessage("messages.gui.name-prefix") + portal.getName(), 
                plugin.getMessageManager().getMessage("messages.gui.rename-portal-lore")));

        if (plugin.getConfig().getBoolean("iris-settings.enabled", true)) {
            if (portal.hasIris()) {
                String status = portal.isIrisActive() ? plugin.getMessageManager().getMessage("messages.gui.status-closed") : plugin.getMessageManager().getMessage("messages.gui.status-open");
                Material irisIcon = portal.isIrisActive() ? Material.IRON_TRAPDOOR : Material.IRON_DOOR;
                inv.setItem(22, createItem(irisIcon, plugin.getMessageManager().getMessage("messages.gui.iris-status"), 
                        plugin.getMessageManager().getMessage("messages.gui.status-prefix") + status, plugin.getMessageManager().getMessage("messages.gui.informative-only")));
            } else {
                Material irisMat = Material.valueOf(plugin.getConfig().getString("iris-settings.item-material", "NETHERITE_BLOCK"));
                String irisName = getFriendlyMaterialName(irisMat);
                inv.setItem(22, createItem(irisMat, plugin.getMessageManager().getMessage("messages.gui.install-iris"), 
                        plugin.getMessageManager().getMessage("messages.gui.install-iris-lore").replace("{material}", irisName),
                        plugin.getMessageManager().getMessage("messages.gui.iris-install-lore-extra")));
            }
        }

        inv.setItem(24, createItem(Material.TNT, plugin.getMessageManager().getMessage("messages.gui.destroy-portal"), 
                plugin.getMessageManager().getMessage("messages.gui.destroy-portal-lore1"), 
                plugin.getMessageManager().getMessage("messages.gui.destroy-portal-lore2"), 
                plugin.getMessageManager().getMessage("messages.gui.click-to-decommission")));

        // Whitelist (Slot 40)
        inv.setItem(40, createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I0N2YyYTM0YjFjYjkzMmJmNGE1YjE1YmZjNDFlNmNlZWRhZGM1ZTFkODhiYjVlNDU3Y2RhNWYyYmQ4ZGVlIn19fQ==", 
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                plugin.getMessageManager().getMessage("messages.gui.whitelist-title")));

        // Build bottom row style like main
        setupBottomRow(inv, false);
        
        player.openInventory(inv);
    }

    private ItemStack createInfoItem(Portal portal) {
        String nameLine = plugin.getMessageManager().getMessage("messages.gui.name-prefix") + portal.getName();
        String addrLine = plugin.getMessageManager().getMessage("messages.gui.address-prefix") + portal.getAddress();
        List<String> lore = new ArrayList<>();
        lore.add(nameLine);
        lore.add(addrLine);
        
        if (portal.isActive()) {
            long seconds = portal.getConnectionDuration();
            String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
            lore.add(plugin.getMessageManager().getMessage("messages.gui.time-open").replace("{time}", timeStr));
        }
        return createItem(Material.PAPER, plugin.getMessageManager().getMessage("messages.gui.info-title"), lore.toArray(new String[0]));
    }

    public void updateMenu(Player player, Portal portal) {
        Inventory top = player.getOpenInventory().getTopInventory();
        if (top.getHolder() instanceof PortalInventoryHolder) {
            PortalInventoryHolder holder = (PortalInventoryHolder) top.getHolder();
            if (holder.getPortal().equals(portal)) {
                // Atomic Update Info only for MAIN and CONFIG
                if (holder.getMenuType().equals("MAIN") || holder.getMenuType().equals("CONFIG")) {
                    top.setItem(4, createInfoItem(portal));
                }
                
                if (holder.getMenuType().equals("MAIN")) {
                    // Update Dial Pad / Shutdown (Slot 19)
                    if (portal.isActive()) {
                        if (portal.getDialerId() != null) {
                            top.setItem(19, createItem(Material.REDSTONE_BLOCK, plugin.getMessageManager().getMessage("messages.gui.shutdown"), plugin.getMessageManager().getMessage("messages.gui.shutdown-lore")));
                        } else {
                            top.setItem(19, createItem(Material.COMPASS, plugin.getMessageManager().getMessage("messages.gui.status-incoming"), plugin.getMessageManager().getMessage("messages.gui.portal-busy")));
                        }
                    } else {
                        long cooldownTime = plugin.getConfig().getLong("portal-settings.dialing-cooldown-seconds", 30) * 1000;
                        long elapsed = System.currentTimeMillis() - portal.getLastDeactivationTime();
                        if (elapsed < cooldownTime) {
                            long remaining = (cooldownTime - elapsed) / 1000;
                            top.setItem(19, createItem(Material.COMPASS, plugin.getMessageManager().getMessage("messages.gui.status-recharging"), 
                                    plugin.getMessageManager().getMessage("messages.gui.recharging-lore").replace("{time}", String.valueOf(remaining))));
                        } else {
                            top.setItem(19, createItem(Material.COMPASS, plugin.getMessageManager().getMessage("messages.gui.num-pad"), plugin.getMessageManager().getMessage("messages.gui.num-pad-lore")));
                        }
                    }
                    
                    // Update Iris
                    if (portal.hasIris()) {
                        Material irisIcon = portal.isIrisActive() ? Material.IRON_TRAPDOOR : Material.IRON_DOOR;
                        String statusName = plugin.getMessageManager().getMessage("messages.gui.iris-prefix") + 
                                (portal.isIrisActive() ? plugin.getMessageManager().getMessage("messages.gui.status-closed") : plugin.getMessageManager().getMessage("messages.gui.status-open"));
                        top.setItem(25, createItem(irisIcon, statusName, plugin.getMessageManager().getMessage("messages.gui.click-to-change")));
                    }
                    
                    // Update Pearls (Slot 40)
                    int maxPearls = plugin.getConfig().getInt("portal-settings.max-pearl-balance", 64);
                    String pearlBalance = plugin.getMessageManager().getMessage("messages.gui.pearl-balance").replace("{balance}", String.valueOf(portal.getPearlBalance()));
                    String pearlStorageLore = plugin.getMessageManager().getMessage("messages.gui.pearl-storage-lore").replace("{max}", String.valueOf(maxPearls));
                    top.setItem(40, createItem(Material.ENDER_PEARL, plugin.getMessageManager().getMessage("messages.gui.pearl-storage"), pearlBalance, pearlStorageLore));
                }
            }
        }
    }

    public void openIrisMenu(Player player, Portal portal) {
        openConfig(player, portal);
    }

    public void openNumberPad(Player player, Portal portal, String currentInput) {
        if (portal.isActive() || portal.isDialing()) {
            openMain(player, portal);
            return;
        }
        String display = currentInput;
        while (display.length() < 6) display += "_";
        String title = plugin.getMessageManager().getMessage("messages.gui.dial-prefix").replace("{address}", display);
        Inventory inv = Bukkit.createInventory(new PortalInventoryHolder(portal, "NUMPAD"), 54, title);
        inv.setItem(4, null); // Strictly no info item here
        fillBackground(inv);

        int[] slots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
        for (int i = 0; i < 9; i++) {
            inv.setItem(slots[i], createNumberHead(i + 1));
        }
        inv.setItem(40, createNumberHead(0));
        
        inv.setItem(39, createItem(Material.BARRIER, plugin.getMessageManager().getMessage("messages.gui.clear-all"), ""));
        inv.setItem(41, createItem(Material.EMERALD_BLOCK, plugin.getMessageManager().getMessage("messages.gui.dial-button"), ""));

        inv.setItem(45, createItem(Material.ARROW, plugin.getMessageManager().getMessage("messages.gui.back"), plugin.getMessageManager().getMessage("messages.gui.back-lore")));
        player.openInventory(inv);
    }


    public void openSavedList(Player player, Portal portal) {
        if (portal.isActive() || portal.isDialing()) {
            openMain(player, portal);
            return;
        }
        Inventory inv = Bukkit.createInventory(new PortalInventoryHolder(portal, "SAVED"), 54, plugin.getMessageManager().getMessage("messages.gui.navigation-logs"));
        inv.setItem(4, null); // Strictly no info item here
        fillBackground(inv);

        java.util.Map<String, String> history = portal.getSavedAddresses();
        int i = 0;
        for (java.util.Map.Entry<String, String> entry : history.entrySet()) {
            if (i >= 45) break;
            
            String key = entry.getKey();
            String value = entry.getValue();
            String address, backupName;

            // Detect format (New: Key=Address, Old: Key=Name)
            if (key.length() == 6 && key.matches("\\d+")) {
                address = key.trim();
                backupName = value;
            } else {
                address = value.trim();
                backupName = key;
            }

            Portal target = plugin.getPortalManager().getPortalByAddress(address);
            String displayName = (target != null) ? target.getName() : backupName;

            String pName = plugin.getMessageManager().getMessage("messages.gui.portal-entry").replace("{name}", displayName);
            inv.setItem(i++, createItemWithNBTMeta(Material.MAP, pName, address, plugin.getMessageManager().getMessage("messages.gui.address-prefix") + address, plugin.getMessageManager().getMessage("messages.gui.click-to-dial")));
        }

        inv.setItem(45, createItem(Material.ARROW, plugin.getMessageManager().getMessage("messages.gui.back"), plugin.getMessageManager().getMessage("messages.gui.back-lore")));
        player.openInventory(inv);
    }

    private void setupBottomRow(Inventory inv, boolean isSubMenu) {
        if (!isSubMenu) {
            // Restore colorful crystals at bottom
            for (int i = 45; i <= 53; i++) {
                Material crystal = (i % 2 == 0) ? Material.CYAN_STAINED_GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE;
                inv.setItem(i, createItem(crystal, " ", ""));
            }
        } else {
            inv.setItem(45, createItem(Material.ARROW, plugin.getMessageManager().getMessage("messages.gui.back"), plugin.getMessageManager().getMessage("messages.gui.back-lore")));
        }
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                if (line != null && !line.isEmpty()) {
                    if (line.contains("\n")) {
                        String lastColor = "";
                        for (String part : line.split("\n")) {
                            if (part.isEmpty()) continue;
                            String formattedPart = lastColor + part;
                            lore.add(formattedPart);
                            // Track last color code
                            lastColor = getLastColorCode(formattedPart);
                        }
                    } else {
                        lore.add(line);
                    }
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getLastColorCode(String input) {
        String last = "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("§[0-9a-fk-or]").matcher(input);
        while (matcher.find()) {
            last = matcher.group();
        }
        return last;
    }

    public String getFriendlyMaterialName(Material mat) {
        if (mat == null) return "???";
        String lang = plugin.getConfig().getString("language", "en");
        String name = mat.name().toLowerCase();
        
        if (lang.equalsIgnoreCase("es")) {
            // Smart translation for Spanish
            String esName = name.replace("_", " ");
            
            // Keyword replacements
            esName = esName.replace("netherite", "Netherite");
            esName = esName.replace("diamond", "Diamante");
            esName = esName.replace("gold", "Oro");
            esName = esName.replace("iron", "Hierro");
            esName = esName.replace("emerald", "Esmeralda");
            esName = esName.replace("coal", "Carbón");
            esName = esName.replace("obsidian", "Obsidiana");
            esName = esName.replace("crying", "Llorosa");
            esName = esName.replace("ancient debris", "Escombros Ancestrales");
            esName = esName.replace("lodestone", "Magnetita");
            esName = esName.replace("amethyst", "Amatista");
            esName = esName.replace("copper", "Cobre");
            esName = esName.replace("quartz", "Cuarzo");
            esName = esName.replace("nether brick", "Ladrillo del Nether");
            esName = esName.replace("lapis", "Lapisázuli");
            esName = esName.replace("redstone", "Redstone");
            
            if (name.endsWith("_block")) {
                String matPart = esName.replace(" block", "").trim();
                if (!matPart.isEmpty()) {
                    matPart = Character.toUpperCase(matPart.charAt(0)) + matPart.substring(1);
                }
                return "Bloque de " + matPart;
            }

            // Capitalize result
            if (esName.length() > 0) {
                esName = Character.toUpperCase(esName.charAt(0)) + esName.substring(1);
            }
            return esName;
        }
        
        // Default: Prettify Enum Name (NETHERITE_BLOCK -> Netherite Block)
        String prettified = name.replace("_", " ");
        String[] words = prettified.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private ItemStack createItemWithNBTMeta(Material material, String name, String address, String... loreLines) {
        ItemStack item = createItem(material, name, loreLines);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "address");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, address);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNumberHead(int number) {
        if (number < 0 || number >= NUMBER_SKINS.length) return new ItemStack(Material.PLAYER_HEAD);
        return createSkull(NUMBER_SKINS[number], NUMBER_UUIDS[number], "§f§l" + number);
    }
    
    public ItemStack createSkull(String base64, UUID uuid, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;
        
        meta.setDisplayName(name);

        try {
            // Priority: Official 1.20+ PlayerProfile API
            org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(uuid, "SP");
            org.bukkit.profile.PlayerTextures textures = profile.getTextures();
            
            String url = extractUrlFromBase64(base64);
            if (!url.isEmpty()) {
                textures.setSkin(new java.net.URL(url));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            }
        } catch (Throwable t) {
            // Fallback for extreme environments using Reflection
            try {
                Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                java.lang.reflect.Constructor<?> profileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
                Object profile = profileConstructor.newInstance(uuid, "SP");

                Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                java.lang.reflect.Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class);
                Object property = propertyConstructor.newInstance("textures", base64);

                java.lang.reflect.Method getPropertiesMethod = gameProfileClass.getMethod("getProperties");
                Object properties = getPropertiesMethod.invoke(profile);

                java.lang.reflect.Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
                putMethod.invoke(properties, "textures", property);

                java.lang.reflect.Field profileField = null;
                for (java.lang.reflect.Field f : meta.getClass().getDeclaredFields()) {
                    if (f.getType().equals(gameProfileClass)) {
                        profileField = f;
                        break;
                    }
                }
                
                if (profileField != null) {
                    profileField.setAccessible(true);
                    profileField.set(meta, profile);
                }
            } catch (Exception e) {}
        }

        head.setItemMeta(meta);
        return head;
    }

    private String extractUrlFromBase64(String base64) {
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(base64));
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"url\"\\s*:\\s*\"(http[^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(decoded);
            if (matcher.find()) {
                String url = matcher.group(1);
                if (url.startsWith("http://")) return "https://" + url.substring(7);
                return url;
            }
        } catch (Exception e) {}
        return "";
    }

    public void openWhitelistMenu(Player player, Portal portal) {
        Inventory inv = Bukkit.createInventory(new PortalInventoryHolder(portal, "WHITELIST"), 54, plugin.getMessageManager().getMessage("messages.gui.whitelist-title"));
        fillBackground(inv);
        
        java.util.List<UUID> whitelist = portal.getWhitelist();
        int i = 0;
        for (UUID uuid : whitelist) {
            if (i >= 45) break;
            org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            String uName = (op.getName() != null) ? op.getName() : uuid.toString();
            inv.setItem(i++, createSkullFromUUID(uuid, "§b" + uName, "§7" + plugin.getMessageManager().getMessage("messages.gui.click-to-remove")));
        }

        inv.setItem(49, createItem(Material.NETHER_STAR, plugin.getMessageManager().getMessage("messages.gui.add-player"), ""));
        inv.setItem(45, createItem(Material.ARROW, plugin.getMessageManager().getMessage("messages.gui.back"), plugin.getMessageManager().getMessage("messages.gui.back-lore")));
        player.openInventory(inv);
    }
    private ItemStack createSkullFromUUID(UUID uuid, String name, String lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            // Try standard way first (works best with cache)
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            
            // Ensure display name and lore
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                java.util.List<String> l = new java.util.ArrayList<>();
                l.add(lore);
                meta.setLore(l);
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createSkullFromUUID(UUID uuid, String name) {
        return createSkullFromUUID(uuid, name, null);
    }
}
