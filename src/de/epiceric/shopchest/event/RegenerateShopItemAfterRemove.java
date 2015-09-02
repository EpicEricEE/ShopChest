package de.epiceric.shopchest.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Hologram;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;
import net.minecraft.server.v1_8_R3.EntityArmorStand;

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
		
		if (containsShopItem) reload();
	}
	
	private void reload() {
		
		for (Shop shop : ShopUtils.getShops()) {
			Hologram hologram = shop.getHologram();
			
			shop.getItem().remove();
			ShopUtils.removeShop(shop);
			
			for (Player p : ShopChest.getInstance().getServer().getOnlinePlayers()) {
				hologram.hidePlayer(p);
			}

			for (Object o : hologram.getEntities()) {
				EntityArmorStand e = (EntityArmorStand) o;
				e.getWorld().removeEntity(e);
			}			
						
			
		}
		
		for (String key : ShopChest.getInstance().shopChests.getKeys(false)) {
			
			OfflinePlayer vendor = ShopChest.getInstance().shopChests.getOfflinePlayer(key + ".vendor");
			int locationX = ShopChest.getInstance().shopChests.getInt(key + ".location.x");
			int locationY = ShopChest.getInstance().shopChests.getInt(key + ".location.y");
			int locationZ = ShopChest.getInstance().shopChests.getInt(key + ".location.z");
			World locationWorld = ShopChest.getInstance().getServer().getWorld(ShopChest.getInstance().shopChests.getString(key + ".location.world"));
			Location location = new Location(locationWorld, locationX, locationY, locationZ);
			ItemStack product = ShopChest.getInstance().shopChests.getItemStack(key + ".product");
			double buyPrice = ShopChest.getInstance().shopChests.getDouble(key + ".price.buy");
			double sellPrice = ShopChest.getInstance().shopChests.getDouble(key + ".price.sell");
			boolean infinite = ShopChest.getInstance().shopChests.getBoolean(key + ".infinite");
			
			ShopUtils.addShop(new Shop(ShopChest.getInstance(), vendor, product, location, buyPrice, sellPrice, infinite));
			
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
		}
		
	}

}
