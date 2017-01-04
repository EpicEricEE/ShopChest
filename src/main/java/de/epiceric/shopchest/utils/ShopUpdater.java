package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopUpdateEvent;
import org.bukkit.Bukkit;

public class ShopUpdater extends Thread {

    private boolean running;
    private long maxDelta;
    private long lastTime;

    public ShopUpdater(ShopChest plugin) {
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

    @Override
    public void run() {
        while(running) {
            long timeNow = System.currentTimeMillis();
            long timeElapsed = timeNow - lastTime;

            if (timeElapsed >= maxDelta) {
                Bukkit.getPluginManager().callEvent(new ShopUpdateEvent());
                lastTime = timeNow;
            }
        }
    }
}
