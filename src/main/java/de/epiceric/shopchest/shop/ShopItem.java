package de.epiceric.shopchest.shop;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopItem {

    private final ShopChest plugin;

    // concurrent since update task is in async thread
    // since this is a fake entity, item is hidden per default
    private final Set<UUID> viewers = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private final ItemStack itemStack;
    private final Location location;

    private final Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private final Object[] creationPackets = new Object[3];
    private final Object entityItem;
    private final int entityId;

    public ShopItem(ShopChest plugin, ItemStack itemStack, Location location) {
        this.plugin = plugin;
        this.itemStack = itemStack;
        this.location = location;

        Class<?> packetPlayOutEntityVelocityClass = Utils.getNMSClass("PacketPlayOutEntityVelocity");
        Class<?> dataWatcherClass = Utils.getNMSClass("DataWatcher");
        Class<?> packetPlayOutEntityMetadataClass = Utils.getNMSClass("PacketPlayOutEntityMetadata");
        Class<?> packetPlayOutSpawnEntityClass = Utils.getNMSClass("PacketPlayOutSpawnEntity");
        Class<?> entityClass = Utils.getNMSClass("Entity");
        Class<?> entityItemClass = Utils.getNMSClass("EntityItem");
        Class<?> craftItemStackClass = Utils.getCraftClass("inventory.CraftItemStack");
        Class<?> nmsItemStackClass = Utils.getNMSClass("ItemStack");
        Class<?> craftWorldClass = Utils.getCraftClass("CraftWorld");
        Class<?> nmsWorldClass = Utils.getNMSClass("World");

        Class[] requiredClasses = new Class[] {
                nmsWorldClass, craftWorldClass, nmsItemStackClass, craftItemStackClass, entityItemClass,
                packetPlayOutSpawnEntityClass, packetPlayOutEntityMetadataClass, dataWatcherClass,
                packetPlayOutEntityDestroyClass, entityClass, packetPlayOutEntityVelocityClass,
        };

        for (Class c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to create shop item: Could not find all required classes");
                entityItem = null;
                entityId = -1;
                return;
            }
        }

        Object tmpEntityItem = null;
        int tmpEntityId = -1;

        try {
            Object craftWorld = craftWorldClass.cast(location.getWorld());
            Object nmsWorld = craftWorldClass.getMethod("getHandle").invoke(craftWorld);

            Object nmsItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);

            Constructor<?> entityItemConstructor = entityItemClass.getConstructor(nmsWorldClass);
            tmpEntityItem = entityItemConstructor.newInstance(nmsWorld);

            entityItemClass.getMethod("setPosition", double.class, double.class, double.class).invoke(tmpEntityItem, location.getX(), location.getY(), location.getZ());
            entityItemClass.getMethod("setItemStack", nmsItemStackClass).invoke(tmpEntityItem, nmsItemStack);
            if (Utils.getMajorVersion() >= 10) entityItemClass.getMethod("setNoGravity", boolean.class).invoke(tmpEntityItem, true);

            Field ageField = entityItemClass.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.setInt(tmpEntityItem, -32768);

            tmpEntityId = (int) entityItemClass.getMethod("getId").invoke(tmpEntityItem);
            Object dataWatcher = entityItemClass.getMethod("getDataWatcher").invoke(tmpEntityItem);

            creationPackets[0] = packetPlayOutSpawnEntityClass.getConstructor(entityClass, int.class).newInstance(tmpEntityItem, 2);
            creationPackets[1] = packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class).newInstance(tmpEntityId, dataWatcher, true);
            creationPackets[2] = packetPlayOutEntityVelocityClass.getConstructor(int.class, double.class, double.class, double.class).newInstance(tmpEntityId, 0D, 0D, 0D);
        } catch (NoSuchMethodException | NoSuchFieldException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().severe("Failed to create shop item");
            plugin.debug("Failed to create shop item with reflection");
            plugin.debug(e);
        }

        entityItem = tmpEntityItem;
        entityId = tmpEntityId;
    }

    /**
     * @return Clone of the location, where the shop item should be (it could have been moved by something, even though it shouldn't)
     *         To get the exact location, use reflection and extract the location of the {@code EntityItem}
     *         which you can get in {@link #getEntityItem()}.
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * @return {@code net.minecraft.server.[VERSION].EntityItem}
     */
    public Object getEntityItem() {
        return entityItem;
    }

    /**
     * @return A clone of this Item's {@link ItemStack}
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * @param p Player to check
     * @return Whether the item is visible to the player
     */
    public boolean isVisible(Player p) {
        return viewers.contains(p.getUniqueId());
    }

    /**
     * @param p Player to which the item should be shown
     */
    public void showPlayer(Player p) {
        showPlayer(p, false);
    }

    /**
     * @param p Player to which the item should be shown
     * @param force whether to force or not
     */
    public void showPlayer(Player p, boolean force) {
        if (viewers.add(p.getUniqueId()) || force) {
            for (Object packet : creationPackets) {
                Utils.sendPacket(plugin, packet, p);
            }
        }
    }

    /**
     * @param p Player from which the item should be hidden
     */
    public void hidePlayer(Player p) {
        hidePlayer(p, false);
    }

    /**
     * @param p Player from which the item should be hidden
     * @param force whether to force or not
     */
    public void hidePlayer(Player p, boolean force) {
        if (viewers.remove(p.getUniqueId()) || force) {
            try {
                if (p.isOnline()) {
                    Object packetPlayOutEntityDestroy = packetPlayOutEntityDestroyClass.getConstructor(int[].class).newInstance((Object) new int[]{entityId});
                    Utils.sendPacket(plugin, packetPlayOutEntityDestroy, p);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                plugin.getLogger().severe("Failed to destroy shop item");
                plugin.debug("Failed to destroy shop item with reflection");
                plugin.debug(e);
            }
        }
    }

    public void resetVisible(Player p) {
        viewers.remove(p.getUniqueId());
    }

    /**
     * Removes the item. <br>
     * Item will be hidden from all players
     */
    public void remove() {
        // Avoid ConcurrentModificationException
        for (UUID uuid : new ArrayList<>(viewers)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) hidePlayer(p);
        }
    }

    /**
     * Respawns the item at the set location for a player
     * @param p Player, for which the item should be reset
     */
    public void resetForPlayer(Player p) {
        hidePlayer(p);
        showPlayer(p);
    }

}
