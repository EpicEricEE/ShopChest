package de.epiceric.shopchest.shop.hologram;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class HologramLine implements IHologramLine {
    private PacketPlayOutSpawnEntity spawnPacket;
    private DataWatcher dataWatcher;

    private int id;
    private Location location;
    private String text;

    public HologramLine(Location location, String text) {
        this.id = 5;
        this.location = location.clone();
        this.text = text == null ? "" : text;

        this.spawnPacket = new PacketPlayOutSpawnEntity();
        this.dataWatcher = createDataWatcher();

        updatePacket();
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        updatePacket();

        int x = MathHelper.floor(location.getX() * 32d);
        int y = MathHelper.floor((location.getY() + 1.975) * 32d);
        int z = MathHelper.floor(location.getZ() * 32d);

        Packet<?> teleportPacket = new PacketPlayOutEntityTeleport(id, x, y, z, (byte) 0, (byte) 0, true);
        location.getWorld().getPlayers().forEach(player -> sendPackets(player, teleportPacket));
    }

    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    @Override
    public void setText(String text) {
        this.text = text == null ? "" : text;

        dataWatcher.a(2, this.text); // custom name
        dataWatcher.a(3, (byte) (this.text.isEmpty() ? 0 : 1)); // name visible

        Packet<?> metadataPacket = new PacketPlayOutEntityMetadata(id, dataWatcher, true);
        location.getWorld().getPlayers().forEach(player -> sendPackets(player, metadataPacket));
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void showPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        sendPackets(player, spawnPacket, new PacketPlayOutEntityMetadata(id, dataWatcher, true));
    }

    @Override
    public void hidePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        sendPackets(player, new PacketPlayOutEntityDestroy(id));
    }

    private void sendPackets(Player player, Packet<?>... packets) {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        Arrays.stream(packets).forEach(packet -> con.sendPacket(packet));
    }

    private void updatePacket() {
        PacketDataSerializer s = new PacketDataSerializer(Unpooled.buffer());
        s.b(id); // id
        s.writeByte(78); // entity type
        s.writeInt(MathHelper.floor(location.getX() * 32d)); // x
        s.writeInt(MathHelper.floor((location.getY() + 1.975) * 32d)); // y
        s.writeInt(MathHelper.floor(location.getZ() * 32d)); // z
        s.writeByte(0); // pitch
        s.writeByte(0); // yaw
        s.writeInt(0); // has motion (?)

        try {
            spawnPacket.a(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            s.release();
        }
    }

    private DataWatcher createDataWatcher() {
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(0, 0b100000); // entity flags
        dataWatcher.a(1, (short) 300); // air ticks
        dataWatcher.a(2, text); // custom name
        dataWatcher.a(3, (byte) (text.isEmpty() ? 0 : 1)); // name visible
        dataWatcher.a(10, (byte) 0b10000); // armor stand flags
        return dataWatcher;
    }
}