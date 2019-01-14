package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class ArmorStandWrapper {

    private Class<?> worldClass = Utils.getNMSClass("World");
    private Class<?> worldServerClass = Utils.getNMSClass("WorldServer");
    private Class<?> dataWatcherClass = Utils.getNMSClass("DataWatcher");
    private Class<?> iChatBaseComponentClass = Utils.getNMSClass("IChatBaseComponent");
    private Class<?> chatMessageClass = Utils.getNMSClass("ChatMessage");
    private Class<?> entityClass = Utils.getNMSClass("Entity");
    private Class<?> entityArmorStandClass = Utils.getNMSClass("EntityArmorStand");
    private Class<?> entityLivingClass = Utils.getNMSClass("EntityLiving");
    private Class<?> packetPlayOutSpawnEntityLivingClass = Utils.getNMSClass("PacketPlayOutSpawnEntityLiving");
    private Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private Class<?> packetPlayOutEntityMetadataClass = Utils.getNMSClass("PacketPlayOutEntityMetadata");
    private Class<?> packetPlayOutEntityTeleportClass = Utils.getNMSClass("PacketPlayOutEntityTeleport");

    private ShopChest plugin;

    private Object nmsWorld;

    private Object entity;
    private Location location;
    private String customName;
    private UUID uuid;
    private int entityId;

    public ArmorStandWrapper(ShopChest plugin, Location location, String customName, boolean interactable) {
        this.plugin = plugin;
        this.location = location;
        this.customName = customName;

        try {
            Object craftWorld = location.getWorld().getClass().cast(location.getWorld());
            nmsWorld = craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);

            entity = entityArmorStandClass.getConstructor(worldClass, double.class, double.class,double.class)
                    .newInstance(nmsWorld, location.getX(), location.getY(), location.getZ());

            if (customName != null && !customName.trim().isEmpty()) {
                if (Utils.getMajorVersion() < 13) {
                    entityArmorStandClass.getMethod("setCustomName", String.class).invoke(entity, customName);
                } else {
                    Object chatMessage = chatMessageClass.getConstructor(String.class, Object[].class)
                            .newInstance(customName, new Object[0]);

                    entityArmorStandClass.getMethod("setCustomName", iChatBaseComponentClass).invoke(entity, chatMessage);
                }
                entityArmorStandClass.getMethod("setCustomNameVisible", boolean.class).invoke(entity, true);
            }

            if (Utils.getMajorVersion() < 10) {
                entityArmorStandClass.getMethod("setGravity", boolean.class).invoke(entity, false);
            } else {
                entityArmorStandClass.getMethod("setNoGravity", boolean.class).invoke(entity, true);
            }

            entityArmorStandClass.getMethod("setInvisible", boolean.class).invoke(entity, true);

            // Adds the entity to some lists so it can call interact events
            // It will also automatically load/unload it when far away
            if (interactable) {
                Method addEntityMethod = worldServerClass.getDeclaredMethod(Utils.getMajorVersion() == 8 ? "a" : "b", entityClass);
                addEntityMethod.setAccessible(true);
                addEntityMethod.invoke(worldServerClass.cast(nmsWorld), entity);
            }

            uuid = (UUID) entityClass.getMethod("getUniqueID").invoke(entity);
            entityId = (int) entityArmorStandClass.getMethod("getId").invoke(entity);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Failed to create line for hologram");
            plugin.debug("Failed to create armor stand");
            plugin.debug(e);
        }
    }

    public void setVisible(Player player, boolean visible) {
        try {
            Object entityLiving = entityLivingClass.cast(entity);
            Object packet;

            if (visible) {
                packet = packetPlayOutSpawnEntityLivingClass.getConstructor(entityLivingClass).newInstance(entityLiving);
            } else {
                packet = packetPlayOutEntityDestroyClass.getConstructor(int[].class).newInstance((Object) new int[]{entityId});
            }

            Utils.sendPacket(plugin, packet, player);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Could not change hologram visibility");
            plugin.debug("Could not change armor stand visibility");
            plugin.debug(e);
        }
    }

    public void setLocation(Location location) {
        this.location = location;

        try {
            entityClass.getMethod("setPosition", double.class, double.class, double.class).invoke(
                    entity, location.getX(), location.getY(), location.getZ());

            Object packet = packetPlayOutEntityTeleportClass.getConstructor(entityClass).newInstance(entity);

            for (Player player : location.getWorld().getPlayers()) {
                Utils.sendPacket(plugin, packet, player);
            }
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Could not set hologram location");
            plugin.debug("Could not set armor stand location");
            plugin.debug(e);
        }
    }

    public void setCustomName(String customName) {
        this.customName = customName;

        try {
            if (customName != null && !customName.isEmpty()) {
                if (Utils.getMajorVersion() < 13) {
                    entityClass.getMethod("setCustomName", String.class).invoke(entity, customName);
                } else {
                    Object chatMessage = chatMessageClass.getConstructor(String.class, Object[].class)
                            .newInstance(customName, new Object[0]);

                    entityClass.getMethod("setCustomName", iChatBaseComponentClass).invoke(entity, chatMessage);
                }
                entityClass.getMethod("setCustomNameVisible", boolean.class).invoke(entity, true);
            } else {
                if (Utils.getMajorVersion() < 13) {
                    entityClass.getMethod("setCustomName", String.class).invoke(entity, "");
                } else {
                    Object chatMessage = chatMessageClass.getConstructor(String.class, Object[].class)
                            .newInstance("", new Object[0]);

                    entityClass.getMethod("setCustomName", iChatBaseComponentClass).invoke(entity, chatMessage);
                }
                entityClass.getMethod("setCustomNameVisible", boolean.class).invoke(entity, false);
            }

            Object dataWatcher = entityClass.getMethod("getDataWatcher").invoke(entity);

            Object packet = packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class)
                    .newInstance(entityId, dataWatcher, true);

            for (Player player : location.getWorld().getPlayers()) {
                Utils.sendPacket(plugin, packet, player);
            }

        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Could not set hologram text");
            plugin.debug("Could not set armor stand custom name");
            plugin.debug(e);
        }
    }

    public void remove() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setVisible(player, false);
        }

        try {
            // Removes the entity from the lists it was added to for interaction
            Method addEntityMethod = worldServerClass.getDeclaredMethod(Utils.getMajorVersion() == 8 ? "b" : "c", entityClass);
            addEntityMethod.setAccessible(true);
            addEntityMethod.invoke(worldServerClass.cast(nmsWorld), entity);

            entityClass.getMethod("die").invoke(entity);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Could not remove hologram");
            plugin.debug("Could not remove armor stand from entity lists");
            plugin.debug(e);
        }
    }

    public int getEntityId() {
        return entityId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getCustomName() {
        return customName;
    }

    public Object getEntity() {
        return entity;
    }
}
