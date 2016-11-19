package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HologramUpdateListener implements Listener {

    private ShopChest plugin;

    public HologramUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        updateHolograms(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        updateHolograms(e.getPlayer());
    }

    private void updateHolograms(Player p) {
        Location playerLocation = p.getLocation();
        double hologramDistanceSquared = Math.pow(plugin.getShopChestConfig().maximal_distance, 2);

        for (Shop shop : plugin.getShopUtils().getShops()) {
            Block b = shop.getLocation().getBlock();

            if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
                plugin.getShopUtils().removeShop(shop, plugin.getShopChestConfig().remove_shop_on_error);
                continue;
            }

            if (shop.getHologram() == null) continue;

            Location shopLocation = shop.getLocation();

            if (playerLocation.getWorld().equals(shopLocation.getWorld())) {
                if (playerLocation.distanceSquared(shop.getHologram().getLocation()) <= hologramDistanceSquared) {
                    if (!shop.getHologram().isVisible(p)) {
                        shop.getHologram().showPlayer(p);
                    }
                } else {
                    if (shop.getHologram().isVisible(p)) {
                        shop.getHologram().hidePlayer(p);
                    }
                }
            }

        }
    }

}
