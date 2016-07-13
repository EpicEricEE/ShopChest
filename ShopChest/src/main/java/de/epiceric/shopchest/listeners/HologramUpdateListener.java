package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HologramUpdateListener implements Listener {

    private ShopChest plugin;

    public HologramUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location playerLocation = p.getLocation();

        for (Shop shop : plugin.getShopUtils().getShops()) {
            Block b = shop.getLocation().getBlock();

            if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
                plugin.getShopUtils().removeShop(shop, plugin.getShopChestConfig().remove_shop_on_error);
                return;
            }

            if (shop.getHologram() == null) return;

            Location shopLocation = shop.getLocation();

            if (playerLocation.getWorld().equals(shopLocation.getWorld())) {
                if (playerLocation.distance(shop.getHologram().getLocation()) <= plugin.getShopChestConfig().maximal_distance) {
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
