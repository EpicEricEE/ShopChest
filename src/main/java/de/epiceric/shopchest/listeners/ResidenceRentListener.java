package de.epiceric.shopchest.listeners;

import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;

public class ResidenceRentListener implements Listener {
    private ShopChest plugin;
    private ShopUtils shopUtils;

    public ResidenceRentListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler
    public void onResidenceUnrent(ResidenceRentEvent event) {
        // there might be an issue with RentEventType.UNRENT which is why i've done it this way
        if (event.getCause() != ResidenceRentEvent.RentEventType.RENT) {
            Player player = event.getPlayer();
            plugin.getShopDatabase().getShops(player.getUniqueId(), new Callback<Collection<Shop>>(plugin) {
                @Override
                public void onResult(Collection<Shop> result) {
                    if (result.isEmpty()) { return; }

                    CuboidArea[] areas = event.getResidence().getAreaArray();
                    for (CuboidArea area : areas) {
                        // we only care about shop chests inside the residence
                        result.removeIf(s -> !area.containsLoc(s.getLocation()));
                    }

                    // remove the shops as they are inside the un-rented residence
                    for (Shop shop : result) {
                        shopUtils.removeShopById(shop.getID(), true);
                        shop.getInventoryHolder().getInventory().clear();
                    }
                }
            });
        }
    }
}
