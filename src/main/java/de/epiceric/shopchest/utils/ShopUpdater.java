package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ShopUpdater {
    
    private final ShopChest plugin;
    private final Queue<Runnable> beforeNext = new ConcurrentLinkedQueue<>();

    private volatile Thread thread;

    public ShopUpdater(ShopChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Start task, except if it is already
     */
    public void start() {
        if (!isRunning()) {
            thread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    for (Runnable runnable : beforeNext) {
                        runnable.run();
                    }
                    beforeNext.clear();
        
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        plugin.getShopUtils().updateShops(p);
                    }
                }
            }, "Shop Updater");
            thread.start();
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
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * @return whether task is running or not
     */
    public boolean isRunning() {
        return thread != null;
    }

    /**
     * Register a task to run before next loop
     *
     * @param runnable task to run
     */
    public void beforeNext(Runnable runnable) {
        beforeNext.add(runnable);
    }
}
