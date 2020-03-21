package de.epiceric.shopchest.util;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NmsUtil {
    private NmsUtil() {
    }

    /**
     * Gets the current server version with revision number
     * <p>
     * (e.g. v1_9_R2, v1_10_R1)
     * 
     * @return the server version
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * Gets the revision of the current server version
     * <p>
     * (e.g. 2 for v1_9_R2 or 1 for v1_10_R1)
     * 
     * @return the revision
     */
    public static int getRevision() {
        String version = getServerVersion();
        return Integer.parseInt(version.substring(version.length() - 1));
    }

    /**
     * Gets the major version of the server
     * <p>
     * (e.g. 9 for v1_9_R2, 10 for v1_10_R1)
     * 
     * @return the major version
     */
    public static int getMajorVersion() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

    /**
     * Gets the class with the given name in the {@code net.minecraft.server} package
     * 
     * @param className the name of the class
     * @return the class or {@code null} if it was not found
     */
    public static Class<?> getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + getServerVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets the class with the given name in the {@code org.bukkit.craftbukkit} package
     * 
     * @param className the name of the class
     * @return the class or {@code null} if it was not found
     */
    public static Class<?> getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getServerVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Get a free entity ID (used for holograms and items)
     * 
     * @return The id or {@code -1} if a free entity ID could not be retrieved.
     */
    public static int getFreeEntityId() {
        try {
            Class<?> entityClass = getNmsClass("Entity");
            Field entityCountField = entityClass.getDeclaredField("entityCount");
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
     * Sends a packet to a player
     * 
     * @param packet the packet to send
     * @param player the player to whom the packet should be sent
     * @return whether the packet has successfully been sent
     */
    public static boolean sendPacket(Object packet, Player player) {
        try {
            if (packet == null) {
                Logger.severe("Failed to send packet to {0}: Packet is null", player.getName());
                return false;
            }

            Class<?> packetClass = getNmsClass("Packet");
            if (packetClass == null) {
                Logger.severe("Failed to send packet to {0}: Could not find Packet class", player.getName());
                return false;
            }

            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);

            return true;
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to send packet {0} to {1}", packet.getClass().getName(), player.getName());
            Logger.severe(e);
            return false;
        }
    }

    /**
     * Sends a raw JSON message to the player
     * 
     * @param player the player
     * @param jsonMessage the JSON message
     */
    public static void sendJsonMessage(Player player, String jsonMessage) {
        try {
            Class<?> chatSerializerClass = getServerVersion().equals("v1_8_R1")
                    ? getNmsClass("ChatSerializer")
                    : getNmsClass("IChatBaseComponent$ChatSerializer");

            Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, jsonMessage);
            Object packetPlayOutChat = getNmsClass("PacketPlayOutChat")
                    .getConstructor(getNmsClass("IChatBaseComponent"))
                    .newInstance(iChatBaseComponent);
            
            sendPacket(packetPlayOutChat, player);
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to create chat packet");
            Logger.severe(e);
        }
    }
}