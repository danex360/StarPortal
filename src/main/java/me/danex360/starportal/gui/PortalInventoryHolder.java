package me.danex360.starportal.gui;

import me.danex360.starportal.model.Portal;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PortalInventoryHolder implements InventoryHolder {
    private final Portal portal;
    private final String menuType;

    public PortalInventoryHolder(Portal portal, String menuType) {
        this.portal = portal;
        this.menuType = menuType;
    }

    public Portal getPortal() {
        return portal;
    }

    public String getMenuType() {
        return menuType;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
