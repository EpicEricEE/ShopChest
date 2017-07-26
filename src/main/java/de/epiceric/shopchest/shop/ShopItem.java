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
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopItem {

    private final ShopChest plugin;
    private final Set<UUID> visibility = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private final ItemStack itemStack;
    private final Location location;

    private Object entityItem;
    private int entityId;
    private Object[] creationPackets = new Object[3];

    private Class<?> nmsWorldClass = Utils.getNMSClass("World");
    private Class<?> craftWorldClass = Utils.getCraftClass("CraftWorld");
    private Class<?> nmsItemStackClass = Utils.getNMSClass("ItemStack");
    private Class<?> craftItemStackClass = Utils.getCraftClass("inventory.CraftItemStack");
    private Class<?> entityItemClass = Utils.getNMSClass("EntityItem");
    private Class<?> entityClass = Utils.getNMSClass("Entity");
    private Class<?> packetPlayOutSpawnEntityClass = Utils.getNMSClass("PacketPlayOutSpawnEntity");
    private Class<?> packetPlayOutEntityMetadataClass = Utils.getNMSClass("PacketPlayOutEntityMetadata");
    private Class<?> dataWatcherClass = Utils.getNMSClass("DataWatcher");
    private Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private Class<?> packetPlayOutEntityVelocityClass = Utils.getNMSClass("PacketPlayOutEntityVelocity");

    public ShopItem(ShopChest plugin, ItemStack itemStack, Location location) {
        this.plugin = plugin;
        this.itemStack = itemStack;
        this.location = location;

        Class[] requiredClasses = new Class[] {
                nmsWorldClass, craftWorldClass, nmsItemStackClass, craftItemStackClass, entityItemClass,
                packetPlayOutSpawnEntityClass, packetPlayOutEntityMetadataClass, dataWatcherClass,
                packetPlayOutEntityDestroyClass, entityClass, packetPlayOutEntityVelocityClass,
        };

        for (Class c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to create shop item: Could not find all required classes");
                return;
            }
        }

        create();
    }

    private void create() {
        try {
            Object craftWorld = craftWorldClass.cast(location.getWorld());
            Object nmsWorld = craftWorldClass.getMethod("getHandle").invoke(craftWorld);

            Object nmsItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);

            Constructor<?> entityItemConstructor = entityItemClass.getConstructor(nmsWorldClass);
            entityItem = entityItemConstructor.newInstance(nmsWorld);

            entityItemClass.getMethod("setPosition", double.class, double.class, double.class).invoke(entityItem, location.getX(), location.getY(), location.getZ());
            entityItemClass.getMethod("setItemStack", nmsItemStackClass).invoke(entityItem, nmsItemStack);
            if (Utils.getMajorVersion() >= 10) entityItemClass.getMethod("setNoGravity", boolean.class).invoke(entityItem, true);

            Field ageField = entityItemClass.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.setInt(entityItem, -32768);

            entityId = (int) entityItemClass.getMethod("getId").invoke(entityItem);
            Object dataWatcher = entityItemClass.getMethod("getDataWatcher").invoke(entityItem);

            creationPackets[0] = packetPlayOutSpawnEntityClass.getConstructor(entityClass, int.class).newInstance(entityItem, 2);
            creationPackets[1] = packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class).newInstance(entityId, dataWatcher, true);
            creationPackets[2] = packetPlayOutEntityVelocityClass.getConstructor(int.class, double.class, double.class, double.class).newInstance(entityId, 0D, 0D, 0D);
        } catch (NoSuchMethodException | NoSuchFieldException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().severe("Failed to create shop item");
            plugin.debug("Failed to create shop item with reflection");
            plugin.debug(e);
        }
    }

    public void remove() {
        for (UUID uuid : visibility) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) setVisible(p, false);
        }
    }

    /**
     * Respawns the item at the set location for a player
     * @param p Player, for which the item should be reset
     */
    public void resetForPlayer(Player p) {
        setVisible(p, false);
        setVisible(p, true);
    }

    public boolean isVisible(Player p) {
        return visibility.contains(p.getUniqueId());
    }

    public void setVisible(final Player p, boolean visible) {
        if (isVisible(p) == visible)
            return;

        if (visible) {
            for (Object packet : this.creationPackets) {
                Utils.sendPacket(plugin, packet, p);
            }
            visibility.add(p.getUniqueId());
        } else {
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
            visibility.remove(p.getUniqueId());
        }
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
}
