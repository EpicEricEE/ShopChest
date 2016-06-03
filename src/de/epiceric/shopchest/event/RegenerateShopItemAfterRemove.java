package de.epiceric.shopchest.event;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RegenerateShopItemAfterRemove implements Listener {


    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityRemove(me.minebuilders.clearlag.events.EntityRemoveEvent e) {
        boolean containsShopItem = false;
        for (Entity entity : e.getEntityList()) {
            if (entity.hasMetadata("shopItem")) {
                containsShopItem = true;
                break;
            }
        }

        if (containsShopItem) ShopUtils.reloadShops(null);
    }


}
