package net.diground.exyliaAntiBlockGlitch.commands.subcommands;

import net.diground.exyliaAntiBlockGlitch.ExyliaAntiBlockGlitch;
import net.diground.exyliaAntiBlockGlitch.utils.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reload {
    private final ExyliaAntiBlockGlitch plugin;

    public Reload(ExyliaAntiBlockGlitch plugin) {
        this.plugin = plugin;
    }

    public boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("exyliaantiblockglitch.admin")) {
            ColorUtils.sendSenderMessage(sender, plugin.getConfigManager().getMessage("system.no_permission", "You don't have permission to use this command."));
            return true;
        }

        if (args.length < 1) {
            ColorUtils.sendSenderMessage(sender, plugin.getConfigManager().getMessage("usage.reload", "Use: /tictactoe reload"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            ColorUtils.sendSenderMessage(sender, plugin.getConfigManager().getMessage("system.only_players", "This command can only be used by players."));
            return true;
        }

        plugin.getConfigManager().reloadAllConfigs();
        ColorUtils.sendPlayerMessage(player, plugin.getConfigManager().getMessage("system.reloaded", "Plugin reloaded."));
        return true;
    }
}
