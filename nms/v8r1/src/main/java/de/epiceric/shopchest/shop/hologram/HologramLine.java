package de.epiceric.shopchest.shop.hologram;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R1.DataWatcher;
import net.minecraft.server.v1_8_R1.MathHelper;
import net.minecraft.server.v1_8_R1.Packet;
import net.minecraft.server.v1_8_R1.PacketDataSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R1.PlayerConnection;

public class HologramLine implements IHologramLine {
    private PacketPlayOutSpawnEntity packet;
    private DataWatcher dataWatcher;

    private int id;
    private Location location;
    private String text;

    public HologramLine(Location location, String text) {
        this.id = 5;
        this.location = location.clone();
        this.text = text == null ? "" : text;

        this.packet = new PacketPlayOutSpawnEntity();
        this.dataWatcher = createDataWatcher();

        updatePacket();
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        updatePacket();

        int x = MathHelper.floor(location.getX() * 32d);
        int y = MathHelper.floor(location.getY() * 32d);
        int z = MathHelper.floor(location.getZ() * 32d);

        Packet packet = new PacketPlayOutEntityTeleport(id, x, y, z, (byte) 0, (byte) 0, true);
        location.getWorld().getPlayers().forEach(player -> sendPackets(player, packet));
    }

    @Override
    public void setText(String text) {
        this.text = text == null ? "" : text;

        dataWatcher.a(2, this.text); // custom name
        dataWatcher.a(3, (byte) (this.text.isEmpty() ? 0 : 1)); // name visible

        location.getWorld().getPlayers()
                .forEach(player -> sendPackets(player, new PacketPlayOutEntityMetadata(id, dataWatcher, true)));
    }

    @Override
    public void showPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        sendPackets(player, packet, new PacketPlayOutEntityMetadata(id, dataWatcher, true));
    }

    @Override
    public void hidePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        sendPackets(player, new PacketPlayOutEntityDestroy(id));
    }

    @Override
    public void destroy() {
         location.getWorld().getPlayers().forEach(player -> hidePlayer(player));
    }

    private void sendPackets(Player player, Packet... packets) {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        Arrays.stream(packets).forEach(packet -> con.sendPacket(packet));
    }

    private void updatePacket() {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.b(id); // id
        serializer.writeByte(78); // entity type
        serializer.writeInt(MathHelper.floor(location.getX() * 32d)); // x
        serializer.writeInt(MathHelper.floor(location.getY() * 32d)); // y
        serializer.writeInt(MathHelper.floor(location.getZ() * 32d)); // z
        serializer.writeByte(0); // pitch
        serializer.writeByte(0); // yaw
        serializer.writeInt(0); // has motion (?)
        this.packet.a(serializer);
        serializer.release();
    }

    private DataWatcher createDataWatcher() {
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(0, 0b100000); // entity flags
        dataWatcher.a(1, (short) 300); // air ticks
        dataWatcher.a(2, text); // custom name
        dataWatcher.a(3, (byte) (text.isEmpty() ? 0 : 1)); // name visible
        dataWatcher.a(10, (byte) 0b1100); // armor stand flags
        return dataWatcher;
    }
}