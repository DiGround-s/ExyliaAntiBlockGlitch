package net.diground.exyliaAntiBlockGlitch.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private String prefix;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig("config.yml");
        loadConfig("messages.yml");
        this.prefix = getMessages().getString("prefix", "<gradient:#8a51c4:#aa76de>&lABG&r");
    }

    private void loadConfig(String filePath) {
        File file = new File(plugin.getDataFolder(), filePath);
        if (!file.exists()) {
            plugin.saveResource(filePath, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(filePath, config);
        Bukkit.getLogger().info("Archivo " + filePath + " cargado correctamente.");
    }

    public String getMessage(String path, String defaultMessage) {
        String message = getMessages().getString(path, defaultMessage);
        if (message == null) {
            return defaultMessage;
        }
        return applyPlaceholders(message);
    }


    private String applyPlaceholders(String message) {
        return message.replace("%prefix%", prefix);
    }

    public FileConfiguration getMessages() {
        return configs.get("messages.yml");
    }

    public FileConfiguration getConfig() {
        return configs.get("config.yml");
    }

    public void reloadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
    }

    public void reloadAllConfigs() {
        for (String fileName : configs.keySet()) {
            reloadConfig(fileName);
        }
        FileConfiguration messagesConfig = getMessages();
        if (messagesConfig != null) {
            this.prefix = messagesConfig.getString("prefix", "<gradient:#8a51c4:#aa76de>&lABG&r");
        }
    }
}
