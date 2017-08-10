package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Callback;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        final Player p = e.getPlayer();

        // Wait till the chunk should have loaded on the client
        // Update IF worlds are different OR chunks are different (as many teleports are in same chunk)
        if (!from.getWorld().getName().equals(to.getWorld().getName())
                || from.getChunk().getX() != to.getChunk().getX()
                || from.getChunk().getZ() != to.getChunk().getZ()) {
            // Wait for 15 ticks before we actually put it in the queue
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getUpdater().beforeNext(new Runnable() {
                        @Override
                        public void run() {
                            if (p.isOnline()) {
                                for (Shop shop : plugin.getShopUtils().getShops()) {
                                    if (shop.hasItem()) {
                                        shop.getItem().hidePlayer(p);
                                    }
                                    if (shop.hasHologram()) {
                                        shop.getHologram().hidePlayer(p);
                                    }
                                }
                                // so next update will update correctly
                                plugin.getShopUtils().resetPlayerLocation(p);
                            }
                        }
                    });
                }
            }.runTaskLater(plugin, 15L);
        }
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
        });
    }
}
