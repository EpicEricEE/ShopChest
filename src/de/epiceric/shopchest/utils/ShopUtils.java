package de.epiceric.shopchest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.shop.Shop;

public class ShopUtils {

	private static HashMap<Location, Shop> shopLocation = new HashMap<>();
	
	public static Shop getShop(Location location) {
		
		Location newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
		
		if (shopLocation.containsKey(newLocation)) {
			return shopLocation.get(newLocation);
		} else {
			return null;
		}
		
	}
	
	public static boolean isShop(Location location) {
		
		Location newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
		
		return shopLocation.containsKey(newLocation);
		
	}
	
	public static Shop[] getShops() {
		
		ArrayList<Shop> shops = new ArrayList<>();
		
		for (Shop shop : shopLocation.values()) {
			shops.add(shop);
		}
		
		return shops.toArray(new Shop[shops.size()]);
		
	}
	
	public static void addShop(Shop shop) {
		
		Location loc = shop.getLocation();
		Block b = loc.getBlock();
		if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
			Chest c = (Chest) b.getState();
			InventoryHolder ih = c.getInventory().getHolder();
			if (ih instanceof DoubleChest) {
				DoubleChest dc = (DoubleChest) ih;
				Chest r = (Chest) dc.getRightSide();
				Chest l = (Chest) dc.getLeftSide();
				
				shopLocation.put(r.getLocation(), shop);
				shopLocation.put(l.getLocation(), shop);
				return;
				
			}
		}
		
		shopLocation.put(shop.getLocation(), shop);
		
	}
	
	public static void removeShop(Shop shop) {
		
		Location loc = shop.getLocation();
		Block b = loc.getBlock();
		if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
			Chest c = (Chest) b.getState();
			InventoryHolder ih = c.getInventory().getHolder();
			if (ih instanceof DoubleChest) {
				DoubleChest dc = (DoubleChest) ih;
				Chest r = (Chest) dc.getRightSide();
				Chest l = (Chest) dc.getLeftSide();
				
				if (shop.hasItem()) shop.getItem().remove();
				shop.removeHologram();
				shopLocation.remove(r.getLocation());
				shopLocation.remove(l.getLocation());
				return;
				
			}
		}
		
		shopLocation.remove(shop.getLocation());
		
	}
	
	public static String getConfigTitle(Location location) {
		World w = location.getWorld();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		
		return w.getName() + "_" + String.valueOf(x) + "_" + String.valueOf(y) +  "_" + String.valueOf(z);
	}
	

	public static int getShopLimit(Player p) {
		int limit = Config.default_limit();
		
		if (ShopChest.perm.hasGroupSupport()) {	
			List<String> groups = new ArrayList<String>();

			for (String key : Config.shopLimits_group()) {
				for (int i = 0; i < ShopChest.perm.getGroups().length; i++) {
					if (ShopChest.perm.getGroups()[i].equals(key)) {
						if (ShopChest.perm.playerInGroup(p, key)) {
							groups.add(key);
						}
					}
				}
			}
			
			if (groups.size() != 0) {
				List<Integer> limits = new ArrayList<>();
				for (String group : groups) {
					int gLimit = ShopChest.getInstance().getConfig().getInt("shop-limits.group." + group);
					limits.add(gLimit);
				}
				
				int highestLimit = 0;
				for (int l : limits) {
					if (l > highestLimit) {
						highestLimit = l;
					} else if (l == -1) {
						highestLimit = -1;
						break;
					}
				}
				
				limit = highestLimit;
			}
						
		}				
		
		for (String key : Config.shopLimits_player()) {
			int pLimit = ShopChest.getInstance().getConfig().getInt("shop-limits.player." + key);
			if (Utils.isUUID(key)) {
				if (p.getUniqueId().equals(UUID.fromString(key))) {
					limit = pLimit;
				}
			} else {
				if (p.getName().equals(key)) {
					limit = pLimit;
				}
			}
		}
		
		return limit;
	}
	
	public static int getShopAmount(OfflinePlayer p) {
		int shopCount = 0;
		
		for (Shop shop : ShopUtils.getShops()) {
			if (shop.getVendor().equals(p)) shopCount++;
		}
		
		return shopCount;
	}
	
	
}
