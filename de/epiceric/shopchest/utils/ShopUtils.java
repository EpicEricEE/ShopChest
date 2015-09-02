package de.epiceric.shopchest.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

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
	
}
