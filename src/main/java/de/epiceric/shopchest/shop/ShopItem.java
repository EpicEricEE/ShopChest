package de.epiceric.shopchest.shop;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;

public class ShopItem {
    private final ShopChest plugin;

    // concurrent since update task is in async thread
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private final ItemStack itemStack;
    private final Location location;
    private final UUID uuid = UUID.randomUUID();
    private final int entityId;

    private final Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private final Class<?> packetPlayOutEntityVelocityClass = Utils.getNMSClass("PacketPlayOutEntityVelocity");
    private final Class<?> packetPlayOutEntityMetadataClass = Utils.getNMSClass("PacketPlayOutEntityMetadata");
    private final Class<?> dataWatcherClass = Utils.getNMSClass("DataWatcher");
    private final Class<?> vec3dClass = Utils.getNMSClass("Vec3D");
    private final Class<?> craftItemStackClass = Utils.getCraftClass("inventory.CraftItemStack");
    private final Class<?> nmsItemStackClass = Utils.getNMSClass("ItemStack");

    public ShopItem(ShopChest plugin, ItemStack itemStack, Location location) {
        this.plugin = plugin;
        this.itemStack = itemStack;
        this.location = location;
        this.entityId = Utils.getFreeEntityId();

        Class<?> entityClass = Utils.getNMSClass("Entity");

        Class<?>[] requiredClasses = new Class<?>[] {
                nmsItemStackClass, craftItemStackClass, packetPlayOutEntityMetadataClass, dataWatcherClass,
                packetPlayOutEntityDestroyClass, entityClass, packetPlayOutEntityVelocityClass,
        };

        for (Class<?> c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to create shop item: Could not find all required classes");
                return;
            }
        }
    }

    /**
     * @return Clone of the location, where the shop item should be (it could have been moved by something, even though it shouldn't)
     */
    public Location getLocation() {
        return location.clone();
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
            try {
                Object nmsItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
                Object dataWatcher = Utils.createDataWatcher(null, nmsItemStack);
                Utils.sendPacket(plugin, Utils.createPacketSpawnEntity(plugin, entityId, uuid, location, EntityType.DROPPED_ITEM), p);
                Utils.sendPacket(plugin, packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class).newInstance(entityId, dataWatcher, true), p);
                if (Utils.getMajorVersion() < 14) {
                    Utils.sendPacket(plugin, packetPlayOutEntityVelocityClass.getConstructor(int.class, double.class, double.class, double.class).newInstance(entityId, 0D, 0D, 0D), p);
                } else {
                    Object vec3d = vec3dClass.getConstructor(double.class, double.class, double.class).newInstance(0D, 0D, 0D);
                    Utils.sendPacket(plugin, packetPlayOutEntityVelocityClass.getConstructor(int.class, vec3dClass).newInstance(entityId, vec3d), p);
                }
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | InstantiationException e) {
                plugin.getLogger().severe("Failed to create item!");
                plugin.debug("Failed to create item!");
                plugin.debug(e);
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
