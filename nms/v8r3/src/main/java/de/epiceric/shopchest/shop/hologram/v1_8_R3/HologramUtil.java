package de.epiceric.shopchest.shop.hologram.v1_8_R3;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PlayerConnection;

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
        serializer.writeByte(entityType); // entity type
        serializer.writeInt(MathHelper.floor(location.getX() * 32d)); // x
        serializer.writeInt(MathHelper.floor((location.getY() + yLift) * 32d)); // y
        serializer.writeInt(MathHelper.floor(location.getZ() * 32d)); // z
        serializer.writeByte(0); // pitch
        serializer.writeByte(0); // yaw
        serializer.writeInt(0); // has motion (?)

        try {
            spawnPacket.a(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serializer.release();
        }
    }
}