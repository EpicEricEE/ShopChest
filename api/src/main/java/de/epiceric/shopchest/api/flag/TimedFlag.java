package de.epiceric.shopchest.api.flag;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Represents a flag that is removed after a given time
 */
public abstract class TimedFlag implements Flag {
    private ShopChest plugin;
    private int seconds;
    private BukkitTask task;

    public TimedFlag(ShopChest plugin, int seconds) {
        this.plugin = plugin;
        this.seconds = seconds;
    }

    /**
     * {@inheritDoc}
     * <p>
     * To use the functionality of this flag, a call to {@code super.onAssign(player)} is mandatory.
     */
    @Override
    public void onAssign(ShopPlayer player) {
        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (this.equals(player.getFlag())) {
                player.removeFlag();
            }
        }, seconds * 20);
    }

    /**
     * {@inheritDoc}
     * <p>
     * To use the functionality of this flag, a call to {@code super.onAssign(player)} is mandatory.
     */
    @Override
    public void onRemove(ShopPlayer player) {
        task.cancel();
    }
    
}