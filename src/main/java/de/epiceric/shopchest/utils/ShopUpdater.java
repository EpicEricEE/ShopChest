package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class ShopUpdater extends BukkitRunnable {

    public enum UpdateQuality {
        SLOWEST(31L),
        SLOWER(24L),
        SLOW(17L),
        NORMAL(10L),
        FAST(7L),
        FASTER(4L),
        FASTEST(1L);

        private long interval;

        UpdateQuality(long interval) {
            this.interval = interval;
        }

        public long getInterval() {
            return interval;
        }
    }

    private ShopChest plugin;

    private boolean running;
    private long interval;

    public ShopUpdater(ShopChest plugin) {
        this.plugin = plugin;
        setInterval(plugin.getShopChestConfig().update_quality.getInterval());
    }

    public synchronized void setInterval(long interval) {
        this.interval = interval;
    }

    public synchronized void start() {
        super.runTaskTimerAsynchronously(plugin, interval, interval);
        running = true;
    }

    @Override
    public synchronized void cancel() {
        if (running) {
            running = false;
            super.cancel();
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        if (players.isEmpty()) {
            cancel();
        }

        for (Player p : players) {
            plugin.getShopUtils().updateShops(p);
        }
    }
}
