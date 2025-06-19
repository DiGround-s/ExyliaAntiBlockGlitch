
package net.diground.exyliaAntiBlockGlitch.listeners;

import net.diground.exyliaAntiBlockGlitch.ExyliaAntiBlockGlitch;
import net.diground.exyliaAntiBlockGlitch.managers.WarningManager;
import net.diground.exyliaAntiBlockGlitch.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BlockListener implements Listener {

    private final ExyliaAntiBlockGlitch plugin;
    private final Set<UUID> ourTeleports = new HashSet<>();

    public BlockListener(ExyliaAntiBlockGlitch plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            BoundingBox boundingBox = player.getBoundingBox();
            Block block = event.getBlock();
            BlockData blockData = block.getBlockData();

            if (isDoubleSlab(blockData)) {
                setToBottomSlab(block);
            } else {
                block.setType(Material.AIR, false);
            }

            if (isNearPlayerFeet(block, location)) {
                Set<Block> firstBlocks = getFloorBlocks(location, boundingBox);
                Set<Block> secondBlocks = new HashSet<>();

                if (getLowerBlocks(firstBlocks, secondBlocks)) {
                    Set<Block> thirdBlocks = new HashSet<>();
                    if (getLowerBlocks(secondBlocks, thirdBlocks)) {
                        handlePunishments(player, thirdBlocks);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();

            if (item != null && (item.getType() == Material.WATER_BUCKET || item.getType() == Material.LAVA_BUCKET)) {
                Location location = player.getLocation();
                BoundingBox boundingBox = player.getBoundingBox();
                Block clickedBlock = event.getClickedBlock();

                if (clickedBlock != null) {
                    Block targetBlock = clickedBlock.getRelative(event.getBlockFace());

                    if (isNearPlayerFeet(targetBlock, location)) {
                        Set<Block> firstBlocks = getFloorBlocks(location, boundingBox);
                        Set<Block> secondBlocks = new HashSet<>();

                        if (getLowerBlocks(firstBlocks, secondBlocks)) {
                            Set<Block> thirdBlocks = new HashSet<>();
                            if (getLowerBlocks(secondBlocks, thirdBlocks)) {
                                handlePunishments(player, thirdBlocks);
                            }
                        }
                    }
                }
            }
        }
    }


    private boolean isDoubleSlab(BlockData blockData) {
        return blockData instanceof Slab && ((Slab) blockData).getType().equals(Slab.Type.DOUBLE);
    }

    private void setToBottomSlab(Block block) {
        BlockData bd = block.getBlockData().clone();
        ((Slab) bd).setType(Slab.Type.BOTTOM);
        block.setBlockData(bd, false);
    }

    private boolean isNearPlayerFeet(Block block, Location location) {
        double xDiff = block.getX() - location.getX();
        double zDiff = block.getZ() - location.getZ();
        return xDiff <= 0.3D && xDiff >= -1.3D && zDiff <= 0.3D && zDiff >= -1.3D && block.getY() <= location.getBlockY();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (ourTeleports.contains(player.getUniqueId())) {
            event.setCancelled(false);
        }
    }

    private void handlePunishments(Player player, Set<Block> blocks) {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Teleport handling
        if (config.getBoolean("punishments.teleport")) {
            Location location = player.getLocation();
            location.setY((location.getBlockY() - 2) + getMaxY(blocks));

            ourTeleports.add(player.getUniqueId());

            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);

            Bukkit.getScheduler().runTask(plugin, () -> {
                ourTeleports.remove(player.getUniqueId());
            });
        }

        // Effects
        applyEffects(player, config);

        // Warnings
        int maxWarnings = config.getInt("warnings.max");
        int currentWarnings = handleWarnings(player, config, maxWarnings);

        // Damage handling
        if (config.getBoolean("punishments.damage")) {
            player.damage(config.getInt("damage"));
        }

        // Sound handling
        playSound(player, config);

        // Messages
        sendMessage(player, config, maxWarnings, currentWarnings);
    }

    private void applyEffects(Player player, FileConfiguration config) {
        if (config.getBoolean("punishments.effect")) {
            for (String effect : config.getStringList("effects")) {
                String[] effectData = effect.split("\\|");
                try {
                    PotionEffectType potionEffectType = PotionEffectType.getByName(effectData[0]);
                    if (potionEffectType != null) {
                        int duration = Integer.parseInt(effectData[1]);
                        int amplifier = Integer.parseInt(effectData[2]);
                        player.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
                    }
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Invalid potion effect configuration: " + effect);
                }
            }
        }
    }

    private int handleWarnings(Player player, FileConfiguration config, int maxWarnings) {
        if (!config.getBoolean("punishments.warning")) {
            return 0;
        }

        UUID playerUUID = player.getUniqueId();
        WarningManager warningManager = plugin.getWarningManager();
        int currentWarnings = warningManager.getWarnings(playerUUID);

        warningManager.addWarning(playerUUID);
        currentWarnings++;

        if (currentWarnings >= maxWarnings) {
            String consoleCommand = Objects.requireNonNull(config.getString("warnings.punish-command", "default command")).replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
            warningManager.resetWarnings(playerUUID);
        }

        return currentWarnings;
    }

    private void playSound(Player player, FileConfiguration config) {
        if (config.getBoolean("punishments.sound")) {
            try {
                String[] soundData = Objects.requireNonNull(config.getString("sound")).split("\\|");
                Sound soundEnum = Sound.valueOf(soundData[0]);
                float volume = Float.parseFloat(soundData[1]);
                float pitch = Float.parseFloat(soundData[2]);
                player.playSound(player.getLocation(), soundEnum, volume, pitch);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Invalid sound configuration");
            }
        }
    }

    private void sendMessage(Player player, FileConfiguration config, int maxWarnings, int currentWarnings) {
        if (config.getBoolean("punishments.message")) {
            int leftWarnings = maxWarnings - currentWarnings;
            String messageKey = config.getBoolean("punishments.warning") ? "with-waring" : "default";
            String message = plugin.getConfigManager().getMessage(messageKey, "%prefix% <#a33b53>You can''t do that!").replace("%warnings%", String.valueOf(leftWarnings));
            ColorUtils.sendPlayerMessage(player, message);
        }
    }

    private Set<Block> getFloorBlocks(Location location, BoundingBox boundingBox) {
        Set<Block> floorBlocks = new HashSet<>();
        int blx = NumberConversions.floor(boundingBox.getMinX());
        int bgx = NumberConversions.ceil(boundingBox.getMaxX());
        int blz = NumberConversions.floor(boundingBox.getMinZ());
        int bgz = NumberConversions.ceil(boundingBox.getMaxZ());
        for (int x = blx; x < bgx; x++) {
            for (int z = blz; z < bgz; z++)
                floorBlocks.add((new Location(location.getWorld(), x, location.getY(), z)).getBlock());
        }
        return floorBlocks;
    }

    private boolean getLowerBlocks(Set<Block> sourceBlocks, Set<Block> destinationBlocks) {
        boolean climbing = true;

        for (Block b : sourceBlocks) {
            Material type = b.getType();

            if (!b.isPassable() || type == Material.WATER || type == Material.LAVA) {
                climbing = false;
                break;
            }

            Block lowerBlock = b.getRelative(BlockFace.DOWN);
            destinationBlocks.add(lowerBlock);
        }

        return climbing;
    }



    private double getMaxY(Set<Block> blocks) {
        double y = 0.0D;
        for (Block b : blocks) {
            BoundingBox boundingBox = b.getBoundingBox();
            double maxY = boundingBox.getMaxY() - b.getY();
            if (maxY > y) {
                y = maxY;
            }
        }
        return y;
    }
}