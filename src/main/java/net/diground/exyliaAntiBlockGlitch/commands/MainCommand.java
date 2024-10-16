package net.diground.exyliaAntiBlockGlitch.commands;

import net.diground.exyliaAntiBlockGlitch.ExyliaAntiBlockGlitch;
import net.diground.exyliaAntiBlockGlitch.commands.subcommands.Reload;
import net.diground.exyliaAntiBlockGlitch.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final ExyliaAntiBlockGlitch plugin;

    public MainCommand(ExyliaAntiBlockGlitch plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 1) {
            plugin.getAudience().sender(sender).sendMessage(ColorUtils.translateColors(plugin.getConfigManager().getMessage("help_message", "Not found.")));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "reload" -> new Reload(plugin).handleReloadCommand(sender, args);

            default -> {
                plugin.getAudience().sender(sender).sendMessage(ColorUtils.translateColors(plugin.getConfigManager().getMessage("help_message", "Not found.")));
                yield true;
            }
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String currentInput = args[0];
            List<String> options = new ArrayList<>();
            if (commandSender.hasPermission("exyliaantiblockglitch.admin")) {
                options.add("reload");
            }

            completions.addAll(options.stream()
                    .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase()))
                    .toList());
        }

        return completions;
    }
}
