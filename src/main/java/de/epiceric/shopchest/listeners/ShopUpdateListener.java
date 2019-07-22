package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Callback;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ShopUpdateListener implements Listener {

    private ShopChest plugin;

    public ShopUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        // If done without delay, Bukkit#getOnlinePlayers() would still
        // contain the player even though he left, so the shop updater
        // would show the shop again.
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Shop shop : plugin.getShopUtils().getShops()) {
                    if (shop.hasItem()) {
                        shop.getItem().resetVisible(e.getPlayer());
                    }
                    if (shop.hasHologram()) {
                        shop.getHologram().resetVisible(e.getPlayer());
                    }
                }
        
                plugin.getShopUtils().resetPlayerLocation(e.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        final Player p = e.getPlayer();

        // Wait till the chunk should have loaded on the client
        if (!from.getWorld().getName().equals(to.getWorld().getName())
                || from.getChunk().getX() != to.getChunk().getX()
                || from.getChunk().getZ() != to.getChunk().getZ()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getUpdater().queue(() -> {
                        if (p.isOnline()) {
                            for (Shop shop : plugin.getShopUtils().getShops()) {
                                if (shop.hasItem()) {
                                    shop.getItem().hidePlayer(p);
                                }
                                if (shop.hasHologram()) {
                                    shop.getHologram().hidePlayer(p);
                                }
                            }
                            plugin.getShopUtils().resetPlayerLocation(p);
                        }
                    });
                    plugin.getUpdater().updateShops(p);
                }
            }.runTaskLater(plugin, 15L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        plugin.getUpdater().updateShops(e.getPlayer());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        final String worldName = e.getWorld().getName();

        plugin.getShopUtils().reloadShops(false, false, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                plugin.getLogger().info(String.format("Reloaded %d shops because a new world '%s' was loaded", result, worldName));
                plugin.debug(String.format("Reloaded %d shops because a new world '%s' was loaded", result, worldName));
            }

            @Override
            public void onError(Throwable throwable) {
                // Database connection probably failed => disable plugin to prevent more errors
                plugin.getLogger().severe("No database access. Disabling ShopChest");
                if (throwable != null) plugin.getLogger().severe(throwable.getMessage());
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        });
    }
}
