package me.danex360.starportal;

import me.danex360.starportal.manager.PortalManager;
import me.danex360.starportal.manager.ConnectionManager;
import me.danex360.starportal.manager.StarCommand;
import me.danex360.starportal.listener.PortalListener;
import me.danex360.starportal.listener.MenuListener;
import me.danex360.starportal.listener.PortalListener;
import me.danex360.starportal.model.Portal;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.logging.Logger;

public class StarPortal extends JavaPlugin {
    private static StarPortal instance;
    private PortalManager portalManager;
    private ConnectionManager connectionManager;
    private me.danex360.starportal.util.MessageManager messageManager;
    private me.danex360.starportal.gui.DialingMenu dialingMenu;
    private MenuListener menuListener;
    private PortalListener portalListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        this.portalManager = new PortalManager(this);
        this.connectionManager = new ConnectionManager(this);
        this.messageManager = new me.danex360.starportal.util.MessageManager(this);
        this.dialingMenu = new me.danex360.starportal.gui.DialingMenu(this);
        this.portalListener = new PortalListener(this);

        this.portalManager.loadPortals();
        
        // Register Events
        getServer().getPluginManager().registerEvents(this.portalListener, this);
        this.menuListener = new MenuListener(this);
        getServer().getPluginManager().registerEvents(this.menuListener, this);

        // Register Commands
        StarCommand starCommand = new StarCommand(this);
        getCommand("stp").setExecutor(starCommand);
        getCommand("stp").setTabCompleter(starCommand);

        Bukkit.getConsoleSender().sendMessage("§b§m--------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§b  ____  _              §fPortal");
        Bukkit.getConsoleSender().sendMessage("§b / ___|| |_ __ _ _ __  §fEnabled!");
        Bukkit.getConsoleSender().sendMessage("§b \\___ \\| __/ _` | '__| §bAuthor: §f" + getDescription().getAuthors().get(0));
        Bukkit.getConsoleSender().sendMessage("§b  ___) | || (_| | |    §bVersion: §f" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§b |____/ \\__\\__,_|_|    §7(Stargate Style)");
        Bukkit.getConsoleSender().sendMessage("§b§m--------------------------------------");
    }

    @Override
    public void onDisable() {
        if (portalManager != null) {
            Bukkit.getLogger().info("[StarPortal] Saving portal data before shutdown...");
            portalManager.savePortals();
            Bukkit.getLogger().info("[StarPortal] Data save complete.");
        }
    }

    public static StarPortal getInstance() {
        return instance;
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public ConnectionManager getConnectionManager() { return connectionManager; }
    public me.danex360.starportal.util.MessageManager getMessageManager() { return messageManager; }
    public me.danex360.starportal.gui.DialingMenu getDialingMenu() { return dialingMenu; }
    public MenuListener getMenuListener() { return menuListener; }
    public PortalListener getPortalListener() { return portalListener; }
}
