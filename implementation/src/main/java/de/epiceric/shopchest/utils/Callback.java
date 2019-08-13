package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Callback<T> {
    private ShopChest plugin;

    public Callback(ShopChest plugin) {
        this.plugin = plugin;
    }

    public void onResult(T result) {}

    public void onError(Throwable throwable) {}

    public final void callSyncResult(final T result) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onResult(result);
            }
        }.runTask(plugin);
    }

    public final void callSyncError(final Throwable throwable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onError(throwable);
            }
        }.runTask(plugin);
    }
}
