package de.epiceric.shopchest.shop.hologram.v1_9_R1;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.Packet;
import net.minecraft.server.v1_9_R1.PacketDataSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_9_R1.PlayerConnection;

public class HologramUtil {
    private HologramUtil() {
    }

    /**
     * Gets an unused entity ID to be used for hologram lines and items
     * 
     * @return an unused entity ID or -1 if an error occurred
     */
    public static int getFreeEntityId() {
        try {
            Field entityCountField = Entity.class.getDeclaredField("entityCount");
            entityCountField.setAccessible(true);
            int id = entityCountField.getInt(null);
            entityCountField.setInt(null, id + 1);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Sends the given packets to the given player
     * 
     * @param player the player to send the packets to
     * @param packets the packets to send
     */
    public static void sendPackets(Player player, Packet<?>... packets) {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        Arrays.stream(packets).forEach(con::sendPacket);
    }

    /**
     * Updates the PacketPlayOutSpawnEntity data to include the correct location
     * 
     * @param id the entity ID
     * @param entityType the entity type
     * @param spawnPacket the packet
     * @param location the entity's location
     */
    public static void updateSpawnPacket(int id, int entityType, PacketPlayOutSpawnEntity spawnPacket, Location location) {
        double yLift = entityType == 78 ? 1.975 : 0;

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.b(id); // id
        serializer.a(UUID.randomUUID()); // uuid
        serializer.writeByte(entityType); // entity type
        serializer.writeDouble(location.getX()); // x
        serializer.writeDouble(location.getY() + yLift); // y
        serializer.writeDouble(location.getZ()); // z
        serializer.writeByte(0); // pitch
        serializer.writeByte(0); // yaw
        serializer.writeInt(0); // has motion
        serializer.writeShort(0); // mot x
        serializer.writeShort(0); // mot y
        serializer.writeShort(0); // mot z

        try {
            spawnPacket.a(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serializer.release();
        }
    }

    /**
     * Creates a PacketPlayOutEntityTeleport
     * 
     * @param id the entity ID
     * @param location the new entity location
     * @param lift whether the entity will be lifted by 1.975
     * @return the packet
     */
    public static PacketPlayOutEntityTeleport createTeleportPacket(int id, Location location, boolean lift) {
        PacketDataSerializer s = new PacketDataSerializer(Unpooled.buffer());
        s.d(id); // id
        s.writeDouble(location.getX()); // x
        s.writeDouble(location.getY() + (lift ? 1.975 : 0)); // y
        s.writeDouble(location.getZ()); // z
        s.writeByte(0); // yaw
        s.writeByte(0); // pitch
        s.writeBoolean(true); // on ground

        try {
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
            packet.a(s);
            return packet;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            s.release();
        }

        return null;
    }
}