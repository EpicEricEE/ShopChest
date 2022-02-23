package de.epiceric.shopchest.nms.reflection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionUtils {
    static NMSClassResolver nmsClassResolver = new NMSClassResolver();
    static Class<?> entityClass = nmsClassResolver.resolveSilent("world.entity.Entity");
    static Class<?> entityArmorStandClass = nmsClassResolver.resolveSilent("world.entity.decoration.EntityArmorStand");
    static Class<?> entityItemClass = nmsClassResolver.resolveSilent("world.entity.item.EntityItem");
    static Class<?> dataWatcherClass = nmsClassResolver.resolveSilent("network.syncher.DataWatcher");
    static Class<?> dataWatcherObjectClass = nmsClassResolver.resolveSilent("network.syncher.DataWatcherObject");
    static Class<?> chatSerializerClass = nmsClassResolver.resolveSilent("ChatSerializer", "network.chat.IChatBaseComponent$ChatSerializer");

    private ReflectionUtils() {}

    /**
     * Create a NMS data watcher object to send via a {@code PacketPlayOutEntityMetadata} packet.
     * Gravity will be disabled and the custom name will be displayed if available.
     * @param customName Custom Name of the entity or {@code null}
     * @param nmsItemStack NMS ItemStack or {@code null} if armor stand
     */
    public static Object createDataWatcher(ShopChestDebug debug, String customName, Object nmsItemStack) {
        String version = getServerVersion();
        int majorVersion = getMajorVersion();

        try {
            byte entityFlags = nmsItemStack == null ? (byte) 0b100000 : 0; // invisible if armor stand
            byte armorStandFlags = nmsItemStack == null ? (byte) 0b10000 : 0; // marker (since 1.8_R2)

            Object dataWatcher = dataWatcherClass.getConstructor(entityClass).newInstance((Object) null);
            if (majorVersion < 9) {
                if (getRevision() == 1) armorStandFlags = 0; // Marker not supported on 1.8_R1

                Method a = dataWatcherClass.getMethod("a", int.class, Object.class);
                a.invoke(dataWatcher, 0, entityFlags); // flags
                a.invoke(dataWatcher, 1, (short) 300); // air ticks (?)
                a.invoke(dataWatcher, 3, (byte) (customName != null ? 1 : 0)); // custom name visible
                a.invoke(dataWatcher, 2, customName != null ? customName : ""); // custom name
                a.invoke(dataWatcher, 4, (byte) 1); // silent
                a.invoke(dataWatcher, 10, nmsItemStack == null ? armorStandFlags : nmsItemStack); // item / armor stand flags
            } else {
                Method register = dataWatcherClass.getMethod("register", dataWatcherObjectClass, Object.class);
                String[] dataWatcherObjectFieldNames;

                if ("v1_9_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"ax", "ay", "aA", "az", "aB", null, "c", "a"};
                } else if ("v1_9_R2".equals(version)){
                    dataWatcherObjectFieldNames = new String[] {"ay", "az", "aB", "aA", "aC", null, "c", "a"};
                } else if ("v1_10_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"aa", "az", "aB", "aA", "aC", "aD", "c", "a"};
                } else if ("v1_11_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"Z", "az", "aB", "aA", "aC", "aD", "c", "a"};
                } else if ("v1_12_R1".equals(version) || "v1_12_R2".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"Z", "aA", "aC", "aB", "aD", "aE", "c", "a"};
                } else if ("v1_13_R1".equals(version) || "v1_13_R2".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"ac", "aD", "aF", "aE", "aG", "aH", "b", "a"};
                } else if ("v1_14_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"W", "AIR_TICKS", "aA", "az", "aB", "aC", "ITEM", "b"};
                } else if ("v1_15_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"T", "AIR_TICKS", "aA", "az", "aB", "aC", "ITEM", "b"};
                } else if ("v1_16_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"T", "AIR_TICKS", "ay", "ax", "az", "aA", "ITEM", "b"};
                } else if ("v1_16_R2".equals(version) || "v1_16_R3".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"S", "AIR_TICKS", "ar", "aq", "as", "at", "ITEM", "b"};
                } else {
                    return null;
                }

                Field fEntityFlags = entityClass.getDeclaredField(dataWatcherObjectFieldNames[0]);
                Field fAirTicks = entityClass.getDeclaredField(dataWatcherObjectFieldNames[1]);
                Field fNameVisible = entityClass.getDeclaredField(dataWatcherObjectFieldNames[2]);
                Field fCustomName = entityClass.getDeclaredField(dataWatcherObjectFieldNames[3]);
                Field fSilent = entityClass.getDeclaredField(dataWatcherObjectFieldNames[4]);
                Field fNoGravity = majorVersion >= 10 ? entityClass.getDeclaredField(dataWatcherObjectFieldNames[5]) : null;
                Field fItem = entityItemClass.getDeclaredField(dataWatcherObjectFieldNames[6]);
                Field fArmorStandFlags = entityArmorStandClass.getDeclaredField(dataWatcherObjectFieldNames[7]);

                fEntityFlags.setAccessible(true);
                fAirTicks.setAccessible(true);
                fNameVisible.setAccessible(true);
                fCustomName.setAccessible(true);
                fSilent.setAccessible(true);
                if (majorVersion >= 10) fNoGravity.setAccessible(true);
                fItem.setAccessible(true);
                fArmorStandFlags.setAccessible(true);
                
                register.invoke(dataWatcher, fEntityFlags.get(null), entityFlags);
                register.invoke(dataWatcher, fAirTicks.get(null), 300);
                register.invoke(dataWatcher, fNameVisible.get(null), customName != null);
                register.invoke(dataWatcher, fSilent.get(null), true);
                if (majorVersion < 13) register.invoke(dataWatcher, fCustomName.get(null), customName != null ? customName : "");
                
                if (nmsItemStack != null) {
                    register.invoke(dataWatcher, fItem.get(null), majorVersion < 11 ? com.google.common.base.Optional.of(nmsItemStack) : nmsItemStack);
                } else {
                    register.invoke(dataWatcher, fArmorStandFlags.get(null), armorStandFlags);
                }

                if (majorVersion >= 10) {
                    register.invoke(dataWatcher, fNoGravity.get(null), true);
                    if (majorVersion >= 13) {
                        if (customName != null) {
                            Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, JsonBuilder.parse(customName).toString());
                            register.invoke(dataWatcher, fCustomName.get(null), Optional.of(iChatBaseComponent));
                        } else {
                            register.invoke(dataWatcher, fCustomName.get(null), Optional.empty());
                        }
                    }
                }
            }
            return dataWatcher;
        } catch (InstantiationException | InvocationTargetException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            debug.getLogger().severe("Failed to create data watcher!");
            debug.debug("Failed to create data watcher");
            debug.debug(e);
        }
        return null;
    }

    /**
     * Get a free entity ID for use in {@link #createPacketSpawnEntity(ShopChestDebug, int, UUID, Location, EntityType)}
     * 
     * @return The id or {@code -1} if a free entity ID could not be retrieved.
     */
    public static int getFreeEntityId() {
        try {
            Field entityCountField = new FieldResolver(entityClass).resolve("entityCount", "b");
            entityCountField.setAccessible(true);
            if (entityCountField.getType() == int.class) {
                int id = entityCountField.getInt(null);
                entityCountField.setInt(null, id+1);
                return id;
            } else if (entityCountField.getType() == AtomicInteger.class) {
                return ((AtomicInteger) entityCountField.get(null)).incrementAndGet();
            }

            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Create a {@code PacketPlayOutSpawnEntity} object.
     * Only {@link EntityType#ARMOR_STAND} and {@link EntityType#DROPPED_ITEM} are supported! 
     */
    public static Object createPacketSpawnEntity(ShopChestDebug debug, int id, UUID uuid, Location loc, EntityType type) {
        try {
            Class<?> packetPlayOutSpawnEntityClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutSpawnEntity");
            Class<?> entityTypesClass = nmsClassResolver.resolveSilent("world.entity.EntityTypes");

            boolean isPre9 = getMajorVersion() < 9;
            boolean isPre14 = getMajorVersion() < 14;

            double y = loc.getY();
            if (type == EntityType.ARMOR_STAND && !getServerVersion().equals("v1_8_R1")) {
                // Marker armor stand => lift by normal armor stand height
                y += 1.975;
            }
            
            Object packet = packetPlayOutSpawnEntityClass.getConstructor().newInstance();

            Field[] fields = new Field[12];
            fields[0] = packetPlayOutSpawnEntityClass.getDeclaredField("a"); // ID
            fields[1] = packetPlayOutSpawnEntityClass.getDeclaredField("b"); // UUID (Only 1.9+)
            fields[2] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "b" : "c"); // Loc X
            fields[3] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "c" : "d"); // Loc Y
            fields[4] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "d" : "e"); // Loc Z
            fields[5] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "e" : "f"); // Mot X
            fields[6] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "f" : "g"); // Mot Y
            fields[7] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "g" : "h"); // Mot Z
            fields[8] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "h" : "i"); // Pitch
            fields[9] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "i" : "j"); // Yaw
            fields[10] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "j" : "k"); // Type
            fields[11] = packetPlayOutSpawnEntityClass.getDeclaredField(isPre9 ? "k" : "l"); // Data

            for (Field field : fields) {
                field.setAccessible(true);
            }

            Object entityType = null;
            if (!isPre14) {
                entityType = entityTypesClass.getField(type == EntityType.ARMOR_STAND ? "ARMOR_STAND" : "ITEM").get(null);
            }

            fields[0].set(packet, id);
            if (!isPre9) fields[1].set(packet, uuid);
            if (isPre9) {
                fields[2].set(packet, (int)(loc.getX() * 32));
                fields[3].set(packet, (int)(y * 32));
                fields[4].set(packet, (int)(loc.getZ() * 32));
            } else {
                fields[2].set(packet, loc.getX());
                fields[3].set(packet, y);
                fields[4].set(packet, loc.getZ());
            }
            fields[5].set(packet, 0);
            fields[6].set(packet, 0);
            fields[7].set(packet, 0);
            fields[8].set(packet, 0);
            fields[9].set(packet, 0);
            if (isPre14) fields[10].set(packet, type == EntityType.ARMOR_STAND ? 78 : 2);
            else fields[10].set(packet, entityType);
            fields[11].set(packet, 0);

            return packet;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            debug.getLogger().severe("Failed to create packet to spawn entity!");
            debug.debug("Failed to create packet to spawn entity!");
            debug.debug(e);
            return null;
        }
    }

    /**
     * Send a packet to a player
     * @param debug An instance of the {@link ShopChestDebug} debug instance
     * @param packet Packet to send
     * @param player Player to which the packet should be sent
     * @return {@code true} if the packet was sent, or {@code false} if an exception was thrown
     */
    public static boolean sendPacket(ShopChestDebug debug, Object packet, Player player) {
        try {
            if (packet == null) {
                debug.debug("Failed to send packet: Packet is null");
                return false;
            }

            Class<?> packetClass = nmsClassResolver.resolveSilent("network.protocol.Packet");
            if (packetClass == null) {
                debug.debug("Failed to send packet: Could not find Packet class");
                return false;
            }

            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Field fConnection = (new FieldResolver(nmsPlayer.getClass())).resolve("playerConnection", "b");
            Object playerConnection = fConnection.get(nmsPlayer);

            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);

            return true;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            debug.getLogger().severe("Failed to send packet " + packet.getClass().getName());
            debug.debug("Failed to send packet " + packet.getClass().getName());
            debug.debug(e);
            return false;
        }
    }

    /**
     * @return The current server version with revision number (e.g. v1_9_R2, v1_10_R1)
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * @return The revision of the current server version (e.g. <i>2</i> for v1_9_R2, <i>1</i> for v1_10_R1)
     */
    public static int getRevision() {
        return Integer.parseInt(getServerVersion().substring(getServerVersion().length() - 1));
    }

    /**
     * @return The major version of the server (e.g. <i>9</i> for 1.9.2, <i>10</i> for 1.10)
     */
    public static int getMajorVersion() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

}
