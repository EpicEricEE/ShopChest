package de.epiceric.shopchest.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import de.epiceric.shopchest.ShopChest;

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
		
		if (containsShopItem) ShopChest.utils.reload();
	}


}
