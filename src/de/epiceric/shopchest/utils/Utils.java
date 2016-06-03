package de.epiceric.shopchest.utils;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

    public static int getAmount(Inventory inventory, ItemStack itemStack) {
        int amount = 0;

        ArrayList<ItemStack> inventoryItems = new ArrayList<>();

        if (inventory instanceof PlayerInventory) {
            if (getVersion(Bukkit.getServer()).contains("1_9")) {
                inventoryItems.add(inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.add(inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.add(inventory.getItem(i));
            }
        }

        for (ItemStack item : inventoryItems) {
            if (item != null) {
                if (item.isSimilar(itemStack)) {
                    amount += item.getAmount();
                }
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
