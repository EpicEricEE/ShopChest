package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

// For versions below 1.9.4, since Bukkit's BookMeta
// didn't have generations in those versions

public class CustomBookMeta {

    public enum Generation {
        ORIGINAL,
        COPY_OF_ORIGINAL,
        COPY_OF_COPY,
        TATTERED
    }

    public static Generation getGeneration(ItemStack book) {
        try {
            Class<?> craftItemStackClass = Utils.getCraftClass("inventory.CraftItemStack");

            if (craftItemStackClass == null) {
                ShopChest.getInstance().debug("Failed to get NBTGeneration: Could not find CraftItemStack class");
                return null;
            }

            Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, book);

            Object nbtTagCompound = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
            if (nbtTagCompound == null) {
                ShopChest.getInstance().debug("Failed to get NBTGeneration: getTag returned null");
                return null;
            }

            Object generationObject = nbtTagCompound.getClass().getMethod("getInt", String.class).invoke(nbtTagCompound, "generation");
            if (generationObject == null) {
                ShopChest.getInstance().debug("Failed to get NBTGeneration: getInt returned null");
                return null;
            }

            if (generationObject instanceof Integer) {
                int generation = (Integer) generationObject;

                if (generation > 3) generation = 3;

                return Generation.values()[generation];
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            ShopChest.getInstance().getLogger().severe("Failed to get NBTEntityID with reflection");
            ShopChest.getInstance().debug("Failed to get NBTEntityID with reflection");
            ShopChest.getInstance().debug(e);
        }

        return null;
    }

}
