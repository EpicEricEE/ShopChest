package de.epiceric.shopchest.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.ShopChest;

public class ShopUpdater {
    
    private final ShopChest plugin;
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

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
                    try {
                        queue.take().run();
                    } catch (InterruptedException e) {
                        break;
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
     * Queue a task to update shops for the given player
     * 
     * @param player Player to show updates
     */
    public void updateShops(Player player) {
        queue(() -> plugin.getShopUtils().updateShops(player));
    }

    /**
     * Queue a task to update shops for players in the given world
     * 
     * @param world World in whose players to show updates
     */
    public void updateShops(World world) {
        queue(() -> {
            for (Player player : world.getPlayers()) {
                plugin.getShopUtils().updateShops(player);
            }
        });
    }

    /**
     * Queue a task to update shops for all players
     */
    public void updateShops() {
        queue(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getShopUtils().updateShops(player);
            }
        });
    }

    /**
     * Register a task to run before next loop
     *
     * @param runnable task to run
     */
    public void queue(Runnable runnable) {
        queue.add(runnable);
    }
}
