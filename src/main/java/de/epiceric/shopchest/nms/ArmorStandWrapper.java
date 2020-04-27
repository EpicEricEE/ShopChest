package de.epiceric.shopchest.nms;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;

public class ArmorStandWrapper {
    
    private final Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private final Class<?> packetPlayOutEntityMetadataClass = Utils.getNMSClass("PacketPlayOutEntityMetadata");
    private final Class<?> packetPlayOutEntityTeleportClass = Utils.getNMSClass("PacketPlayOutEntityTeleport");
    private final Class<?> dataWatcherClass = Utils.getNMSClass("DataWatcher");

    private final UUID uuid = UUID.randomUUID();
    private final int entityId;

    private ShopChest plugin;
    private Object entity;
    private Location location;
    private String customName;

    public ArmorStandWrapper(ShopChest plugin, Location location, String customName, boolean interactable) {
        this.plugin = plugin;
        this.location = location;
        this.customName = customName;
        this.entityId = Utils.getFreeEntityId();
    }

    public void setVisible(Player player, boolean visible) {
        try {
            if (visible) {
                Object dataWatcher = Utils.createDataWatcher(customName, null);
                Utils.sendPacket(plugin, Utils.createPacketSpawnEntity(plugin, entityId, uuid, location, EntityType.ARMOR_STAND), player);
                Utils.sendPacket(plugin, packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class)
                        .newInstance(entityId, dataWatcher, true), player);
            } else if (entityId != -1) {
                Utils.sendPacket(plugin, packetPlayOutEntityDestroyClass.getConstructor(int[].class).newInstance((Object) new int[]{entityId}), player);
            }
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Could not change hologram visibility");
            plugin.debug("Could not change armor stand visibility");
            plugin.debug(e);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
        try {
            Object packet = packetPlayOutEntityTeleportClass.getConstructor().newInstance();
            Field[] fields = packetPlayOutEntityTeleportClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }

            boolean isPre9 = Utils.getMajorVersion() < 9;
            fields[0].set(packet, entityId);

            double y = location.getY() + (Utils.getServerVersion().equals("v1_8_R1") ? 0 : 1.975);
            if (isPre9) {
                fields[1].set(packet, (int)(location.getX() * 32));
                fields[2].set(packet, (int)(y * 32));
                fields[3].set(packet, (int)(location.getZ() * 32));
            } else {
                fields[1].set(packet, location.getX());
                fields[2].set(packet, y);
                fields[3].set(packet, location.getZ());
            }
            fields[4].set(packet, (byte) 0);
            fields[5].set(packet, (byte) 0);
            fields[6].set(packet, true);

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
        Object dataWatcher = Utils.createDataWatcher(customName, null);
        try {
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
        for (Player player : location.getWorld().getPlayers()) {
            setVisible(player, false);
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
