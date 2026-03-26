package me.danex360.starportal.manager;

import me.danex360.starportal.StarPortal;
import me.danex360.starportal.model.Portal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StarCommand implements CommandExecutor, TabCompleter {
    private final StarPortal plugin;

    public StarCommand(StarPortal plugin) {
        this.plugin = plugin;
    }

    private boolean hasPerm(CommandSender sender, String node) {
        return sender.isOp() || sender.hasPermission("starportal.admin") || sender.hasPermission(node);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!hasPerm(sender, "starportal.help")) {
                sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission"));
                return true;
            }
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                if (!hasPerm(sender, "starportal.list")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission"));
                    return true;
                }
                Collection<Portal> portals = plugin.getPortalManager().getPortals();
                if (portals.isEmpty()) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                                     plugin.getMessageManager().getMessage("messages.no-portals"));
                    return true;
                }
                sender.sendMessage(plugin.getMessageManager().getMessage("messages.commands.list-header"));
                for (Portal p : portals) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("messages.commands.list-item")
                            .replace("{address}", p.getAddress())
                            .replace("{name}", p.getName())
                            .replace("{balance}", String.valueOf(p.getPearlBalance())));
                }
            }
            case "reload" -> {
                if (!hasPerm(sender, "starportal.reload")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMessageManager().reload();
                sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + 
                        plugin.getMessageManager().getMessage("messages.commands.reload-success"));
            }
            default -> {
                if (!hasPerm(sender, "starportal.help")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + plugin.getMessageManager().getMessage("messages.no-permission"));
                    return true;
                }
                sendHelp(sender);
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getMessage("messages.commands.help-header"));
        sender.sendMessage(plugin.getMessageManager().getMessage("messages.commands.help-list"));
        sender.sendMessage(plugin.getMessageManager().getMessage("messages.commands.help-reload"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("list", "reload", "help"), new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
