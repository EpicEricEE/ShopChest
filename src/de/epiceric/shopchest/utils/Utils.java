package de.epiceric.shopchest.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

	
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
	
	public static String getVersion() {
		
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		
	}
	
}
