package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ShulkerBoxMeta {

    public static Map<Integer, ItemStack> getContents(ItemStack shulkerBox) {
        try {
            Class<?> craftItemStackClass = Utils.getCraftClass("inventory.CraftItemStack");

            if (craftItemStackClass == null) {
                ShopChest.getInstance().debug("Failed to get NBTContents: Could not find CraftItemStack class");
                return null;
            }

            Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, shulkerBox);
            if (nmsStack == null) {
                ShopChest.getInstance().debug("Failed to get NBTContents: asNMSCopy returned null");
                return null;
            }

            Object hasTag = nmsStack.getClass().getMethod("hasTag").invoke(nmsStack);
            if (hasTag == null || !(hasTag instanceof Boolean)) {
                ShopChest.getInstance().debug("Failed to get NBTContents: hasTag returned null");
                return null;
            }

            if (!(boolean) hasTag) {
                ShopChest.getInstance().debug("Failed to get NBTContents: ItemStack has no tag");
                return null;
            }

            Object nbtTagCompound = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
            if (nbtTagCompound == null) {
                ShopChest.getInstance().debug("Failed to get NBTContents: getTag returned null");
                return null;
            }

            Object blockEntityTagObject = nbtTagCompound.getClass().getMethod("getCompound", String.class).invoke(nbtTagCompound, "BlockEntityTag");
            if (blockEntityTagObject == null) {
                ShopChest.getInstance().debug("Failed to get NBTContents: getCompound returned null");
                return null;
            }

            Object itemsObject = blockEntityTagObject.getClass().getMethod("getList", String.class, int.class).invoke(blockEntityTagObject, "Items", 10);
            if (itemsObject == null) {
                ShopChest.getInstance().debug("Failed to get NBTContents: getList returned null");
                return null;
            }

            Object sizeObject = itemsObject.getClass().getMethod("size").invoke(itemsObject);
            if (sizeObject == null) {
                ShopChest.getInstance().debug("Failed to get NBTContents: size returned null or not an integer");
                return null;
            }

            int size = (Integer) sizeObject;

            Map<Integer, ItemStack> contentSlots = new HashMap<>();

            for (int i = 0; i < size; i++) {
                Object itemTag = itemsObject.getClass().getMethod("get", int.class).invoke(itemsObject, i);
                if (itemTag == null) {
                    ShopChest.getInstance().debug("Failed to get NBTContents: get returned null");
                    continue;
                }

                Object nmsStack2 = nmsStack.getClass().getConstructor(nbtTagCompound.getClass()).newInstance(itemTag);
                if (nmsStack2 == null) {
                    ShopChest.getInstance().debug("Failed to get NBTContents: Could not instantiate ItemStack with compound");
                    continue;
                }

                Object slotObject = itemTag.getClass().getMethod("getInt", String.class).invoke(itemTag, "Slot");
                if (slotObject == null || !(slotObject instanceof Integer)) {
                    ShopChest.getInstance().debug("Failed to get NBTContents: getInt returned null or not an integer");
                    continue;
                }

                Object itemStack = craftItemStackClass.getMethod("asBukkitCopy", nmsStack2.getClass()).invoke(null, nmsStack2);
                if (itemStack == null || !(itemStack instanceof ItemStack)) {
                    ShopChest.getInstance().debug("Failed to get NBTContents: asBukkitCopy returned null or not an ItemStack");
                    continue;
                }

                contentSlots.put((Integer) slotObject, (ItemStack) itemStack);
            }

            return contentSlots;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException  e) {
            ShopChest.getInstance().getLogger().severe("Failed to get NBTContents with reflection");
            ShopChest.getInstance().debug("Failed to get NBTContents with reflection");
            ShopChest.getInstance().debug(e);
        }

        return null;
    }

}
