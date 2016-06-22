package de.epiceric.shopchest.event;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class UpdateHolograms implements Listener {

    public UpdateHolograms() {
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        Player p = e.getPlayer();
        Location playerLocation = p.getLocation();

        for (Shop shop : ShopUtils.getShops()) {

            if (shop.getHologram() != null) {

                Location shopLocation = shop.getLocation();

                if (playerLocation.getWorld().equals(shopLocation.getWorld())) {

                    if (playerLocation.distance(shop.getHologram().getLocation()) <= Config.maximal_distance) {

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

}
