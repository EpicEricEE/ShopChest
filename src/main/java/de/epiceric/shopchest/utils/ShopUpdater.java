package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ShopUpdater extends Thread {

    private ShopChest plugin;

    private boolean running;
    private long maxDelta;
    private long lastTime;

    public ShopUpdater(ShopChest plugin) {
        this.plugin = plugin;
        setMaxDelta(plugin.getShopChestConfig().update_quality.getTime());
    }

    public synchronized void setMaxDelta(long maxDelta) {
        this.maxDelta = maxDelta * 50;
    }

    @Override
    public synchronized void start() {
        super.start();
        running = true;
        lastTime = System.currentTimeMillis();
    }

    public synchronized void cancel() {
        running = false;
        super.interrupt();
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while(running) {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                cancel();
            }

            long timeNow = System.currentTimeMillis();
            long timeElapsed = timeNow - lastTime;

            if (timeElapsed >= maxDelta) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new ShopUpdateEvent());
                    }
                }.runTask(plugin);
                lastTime = timeNow;
            }
        }
    }
}
