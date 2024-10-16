package net.diground.exyliaAntiBlockGlitch.managers;

import net.diground.exyliaAntiBlockGlitch.ExyliaAntiBlockGlitch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarningManager {

    private final ExyliaAntiBlockGlitch plugin;
    private final Map<UUID, Integer> warnings;

    public WarningManager(ExyliaAntiBlockGlitch plugin) {
        this.plugin = plugin;
        this.warnings = new HashMap<>();
    }

    public void addWarning(UUID playerUUID) {
        warnings.put(playerUUID, getWarnings(playerUUID) + 1);
    }

    public int getWarnings(UUID playerUUID) {
        return warnings.getOrDefault(playerUUID, 0);
    }

    public void resetWarnings(UUID playerUUID) {
        warnings.remove(playerUUID);
    }
}