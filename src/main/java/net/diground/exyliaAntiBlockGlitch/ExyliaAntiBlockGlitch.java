package net.diground.exyliaAntiBlockGlitch;

import net.diground.exyliaAntiBlockGlitch.commands.MainCommand;
import net.diground.exyliaAntiBlockGlitch.listeners.BlockListener;
import net.diground.exyliaAntiBlockGlitch.managers.ConfigManager;
import net.diground.exyliaAntiBlockGlitch.managers.WarningManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ExyliaAntiBlockGlitch extends JavaPlugin {
    private static ExyliaAntiBlockGlitch instance;
    private ConfigManager configManager;
    private WarningManager warningManager;

    private BukkitAudiences adventure;

    public BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        instance = this;
        loadListeners();
        loadManagers();
        loadCommands();
    }

    @Override
    public void onDisable() {
    }

    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
    }

    private void loadManagers() {
        configManager = new ConfigManager(this);
        warningManager = new WarningManager(this);
    }

    private void loadCommands() {
        Objects.requireNonNull(getCommand("abg")).setExecutor(new MainCommand(this));
    }

    public BukkitAudiences getAudience() {
        return adventure();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WarningManager getWarningManager() {
        return warningManager;
    }

    public static ExyliaAntiBlockGlitch getInstance() {
        return instance;
    }
}
