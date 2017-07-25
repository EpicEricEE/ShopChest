package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ShopUpdater {

    public enum UpdateQuality {
        SLOWEST(31L),
        SLOWER(24L),
        SLOW(17L),
        NORMAL(10L),
        FAST(7L),
        FASTER(4L),
        FASTEST(1L);

        private final long interval;

        UpdateQuality(long interval) {
            this.interval = interval;
        }

        public long getInterval() {
            return interval;
        }
    }

    private final ShopChest plugin;

    private long interval = UpdateQuality.NORMAL.getInterval();

    private volatile BukkitTask running;

    public ShopUpdater(ShopChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Start task, except if it is already
     */
    public void start() {
        if (!isRunning()) {
            interval = plugin.getShopChestConfig().update_quality.getInterval();
            running = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new ShopUpdaterTask(plugin), interval, interval);
        }
    }

    /**
     * Stop any running task then start it again
     */
    public void restart() {
        stop();
        start();
    }

    /**
     * Stop task properly
     */
    public void stop() {
        if (running != null) {
            running.cancel();
            running = null;
        }
    }

    /**
     * @return whether task is running or not
     */
    public boolean isRunning() {
        return running != null;
    }

    private static class ShopUpdaterTask implements Runnable {

        private final ShopChest plugin;

        private ShopUpdaterTask(ShopChest plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getShopUtils().updateShops(p);
            }
        }
    }
}
