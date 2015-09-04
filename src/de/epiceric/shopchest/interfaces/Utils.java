package de.epiceric.shopchest.interfaces;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Utils {

	public abstract void reload();
	
	public abstract void removeShops();
	
	public static int getAmount(Inventory inventory, Material type, short damage, ItemMeta itemMeta) {
	    ItemStack[] items = inventory.getContents();
	    int amount = 0;
	    for (ItemStack item : items) {
	        if ((item != null) && (item.getType().equals(type)) && (item.getDurability() == damage) && (item.getAmount() > 0) && (item.getItemMeta().equals(itemMeta))) {
	        	amount += item.getAmount();
	        }
	    }
	    return amount;
	}
	
	public static String getVersion(Server server) {
	    String packageName = server.getClass().getPackage().getName();
	    
	    return packageName.substring(packageName.lastIndexOf('.') + 1);
	}
	
	public static boolean isUUID(String string) {
		return string.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");		
	}
	
}
