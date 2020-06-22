package de.epiceric.shopchest.listeners;

import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResidenceRentListener implements Listener {
    private ShopChest plugin;
    private ShopUtils shopUtils;
    private List<ResidenceRentEvent.RentEventType> types;

    public ResidenceRentListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
        this.types = populateTypes();
    }

    @EventHandler
    public void onResidenceUnrent(ResidenceRentEvent event) {
        if (!Config.enableResidenceIntegration) { return; }

        // https://github.com/Zrips/Residence/issues/487
        // Note -- this works now but the issue remains as I feel the UNRENT name *should* be associated with /res market unrent
        if (types.stream().anyMatch(t -> t == event.getCause())) {
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

    private List<ResidenceRentEvent.RentEventType> populateTypes() {
        final List<ResidenceRentEvent.RentEventType> types = new ArrayList<>();
        try {
            for (String type : Config.residenceRemoveShopEventTypes) {
                types.add(ResidenceRentEvent.RentEventType.valueOf(type));
            }
        } catch (IllegalArgumentException exception) {
            plugin.debug("Failed to populate residence enum list, using only the defaults...");
            types.clear();
            types.add(ResidenceRentEvent.RentEventType.RENT_EXPIRE);
            types.add(ResidenceRentEvent.RentEventType.UNRENT);
            types.add(ResidenceRentEvent.RentEventType.UNRENTABLE);
        }

        return types;
    }
}
