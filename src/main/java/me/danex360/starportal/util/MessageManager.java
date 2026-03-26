package me.danex360.starportal.util;

import me.danex360.starportal.StarPortal;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageManager {
    private final StarPortal plugin;
    private FileConfiguration langConfig;

    public MessageManager(StarPortal plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        // Save defaults
        saveDefaultLangs();

        String lang = plugin.getConfig().getString("language", "en");
        File langDir = new File(plugin.getDataFolder(), "languages");
        File langFile = new File(langDir, lang + ".yml");
        
        if (!langFile.exists()) {
            langFile = new File(langDir, "en.yml"); // Fallback to en
        }
        
        try {
            langConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(new java.io.FileInputStream(langFile), java.nio.charset.StandardCharsets.UTF_8));
            Bukkit.getLogger().info("[StarPortal] Loaded language file: " + langFile.getName());
        } catch (Exception e) {
            Bukkit.getLogger().severe("[StarPortal] Failed to load language file: " + langFile.getName() + " - " + e.getMessage());
            langConfig = YamlConfiguration.loadConfiguration(langFile); // Fallback to default loading if UTF-8 fails
        }
        
        // Load default values from resource
        InputStream defLangStream = plugin.getResource("languages/" + lang + ".yml");
        if (defLangStream != null) {
            langConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defLangStream, java.nio.charset.StandardCharsets.UTF_8)));
        }
    }

    private void saveDefaultLangs() {
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists()) langDir.mkdirs();

        String[] langs = {"en.yml", "es.yml"};
        for (String l : langs) {
            File f = new File(langDir, l);
            if (!f.exists()) {
                plugin.saveResource("languages/" + l, false);
            }
        }
    }

    public void reload() {
        plugin.reloadConfig();
        loadLanguage();
    }

    public String getMessage(String path) {
        if (langConfig == null) return "§c[Lang not loaded: " + path + "]";
        
        String msg = langConfig.getString(path);
        
        // Robust fallback: if "messages.key" fails, try just "key"
        if (msg == null && path.startsWith("messages.")) {
            msg = langConfig.getString(path.substring(9));
        }
        
        if (msg == null) {
            // Last resort: check defaults directly
            org.bukkit.configuration.Configuration defaults = langConfig.getDefaults();
            if (defaults != null) {
                msg = defaults.getString(path);
                if (msg == null && path.startsWith("messages.")) {
                    msg = defaults.getString(path.substring(9));
                }
            }
        }
 
        if (msg == null) return "§c[Missing: " + path + "]";
        return msg.replace("&", "§");
    }
}
