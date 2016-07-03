package de.epiceric.shopchest.utils;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;

public class Utils {

    /**
     * Gets the amount of items in an inventory
     *
     * @param inventory The inventory, in which the items are counted
     * @param itemStack The items to count
     * @return Amount of given items in the given inventory
     */
    public static int getAmount(Inventory inventory, ItemStack itemStack) {
        int amount = 0;

        ArrayList<ItemStack> inventoryItems = new ArrayList<>();

        if (inventory instanceof PlayerInventory) {
            if (getMajorVersion() >= 9) {
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

    /**
     * @return The current server version with revision number (e.g. 1_9_R2, 1_10_R1)
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * @return The major version of the server (e.g. <i>9</i> for 1.9.2, <i>10</i> for 1.10)
     */
    public static int getMajorVersion() {
        return Integer.valueOf(getServerVersion().split("_")[1]);
    }

    /**
     * Checks if a given String is a UUID
     * @param string String to check
     * @return Whether the string is a UUID
     */
    public static boolean isUUID(String string) {
        return string.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    }

    /**
     * Encodes an {@link ItemStack} in a Base64 String
     * @param itemStack {@link ItemStack} to encode
     * @return Base64 encoded String
     */
    public static String encode(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return new String(Base64.encodeBase64(config.saveToString().getBytes()));
    }

    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
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
