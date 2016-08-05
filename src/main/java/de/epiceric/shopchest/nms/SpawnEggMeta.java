package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class SpawnEggMeta {

    private static String getNBTEntityID(ShopChest plugin, ItemStack stack) {
        try {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + Utils.getServerVersion() + ".inventory.CraftItemStack");

            Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);

            Object nbtTagCompound = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
            if (nbtTagCompound == null) return null;

            Object entityTagCompound = nbtTagCompound.getClass().getMethod("getCompound", String.class).invoke(nbtTagCompound, "EntityTag");
            if (entityTagCompound == null) return null;

            Object id = entityTagCompound.getClass().getMethod("getString", String.class).invoke(entityTagCompound, "id");
            if (id instanceof String) return (String) id;

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            plugin.debug("Could not get NBTEntityID with reflection");
            plugin.debug(e);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param stack {@link ItemStack} (Spawn Egg) of which the Entity should be gotten
     * @return The {@link EntityType} the Spawn Egg will spawn or <b>null</b> if <i>nbtEntityID</i> is null
     */
    public static EntityType getEntityTypeFromItemStack(ShopChest plugin, ItemStack stack) {
        if (Utils.getMajorVersion() == 8) {
            for (EntityType entityType : EntityType.values()) {
                if (entityType.getTypeId() == stack.getDurability()) {
                    return entityType;
                }
            }
        }

        String nbtEntityID = getNBTEntityID(plugin, stack);

        if (nbtEntityID == null) return null;

        switch (nbtEntityID) {
            case "PigZombie":
                return EntityType.PIG_ZOMBIE;
            case "CaveSpider":
                return EntityType.CAVE_SPIDER;
            case "LavaSlime":
                return EntityType.MAGMA_CUBE;
            case "MushroomCow":
                return EntityType.MUSHROOM_COW;
            case "EntityHorse":
                return EntityType.HORSE;
            case "PolarBear":
                return EntityType.POLAR_BEAR;
            default:
                return EntityType.valueOf(nbtEntityID.toUpperCase());

        }
    }

}
