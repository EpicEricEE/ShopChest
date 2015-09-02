package de.epiceric.shopchest.event;


import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class RegenerateShopItem implements Listener {

	public RegenerateShopItem() {}
	
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onItemDespawn(ItemDespawnEvent e) {
		Item item = e.getEntity();		
		if (item.hasMetadata("shopItem")) e.setCancelled(true);		
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPickUpItem(PlayerPickupItemEvent e) {
		if (e.getItem().hasMetadata("shopItem")) e.setCancelled(true);
	}

	
}
