package de.epiceric.shopchest.interfaces;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Utils {

	public abstract void reload(Player p);
	
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
	
	public static String encode(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return new String(Base64.encodeBase64(config.saveToString().getBytes()));
    }
	
	public static String toString(ItemStack itemStack) {
		YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return config.saveToString();
	}
	
	public static ItemStack decode(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(Base64.decodeBase64(string.getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }


	
}
