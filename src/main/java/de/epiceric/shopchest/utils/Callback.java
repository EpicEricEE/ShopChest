package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Callback {
    private ShopChest plugin;

    public Callback(ShopChest plugin) {
        this.plugin = plugin;
    }

    public void onResult(Object result) {}

    public void onError(Throwable throwable) {}

    public void callSyncResult(final Object result) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onResult(result);
            }
        }.runTask(plugin);
    }

    public void callSyncError(final Throwable throwable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onError(throwable);
            }
        }.runTask(plugin);
    }
}
