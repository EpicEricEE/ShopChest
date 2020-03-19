package de.epiceric.shopchest.util;

import org.bukkit.inventory.ItemStack;

public class ItemUtil {
    private ItemUtil() {
    }

    /**
     * Gets whether the given item stacks are equal not considering their amount
     * 
     * @param itemStack1 the item stack
     * @param itemStack2 the other item stack
     * @return whether the item stacks are equal
     */
    public static boolean isEqual(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        // TODO: book generation stuff

        return itemStack1.isSimilar(itemStack2);
    }
}