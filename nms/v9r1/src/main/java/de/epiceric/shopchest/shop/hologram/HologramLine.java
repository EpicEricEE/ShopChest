package de.epiceric.shopchest.shop.hologram;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_9_R1.DataWatcher;
import net.minecraft.server.v1_9_R1.DataWatcherObject;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.Packet;
import net.minecraft.server.v1_9_R1.PacketDataSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_9_R1.PlayerConnection;

public class HologramLine implements IHologramLine {
    private PacketPlayOutSpawnEntity spawnPacket;
    private DataWatcher dataWatcher;

    private DataWatcherObject<Boolean> nameVisible;
    private DataWatcherObject<Optional<IChatBaseComponent>> customName;

    private int id;
    private Location location;
    private String text;

    public HologramLine(Location location, String text) {
        this.id = 5;
        this.location = location.clone();
        this.text = text;

        this.spawnPacket = new PacketPlayOutSpawnEntity();
        this.dataWatcher = createDataWatcher();

        updatePacket();
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        updatePacket();

        PacketPlayOutEntityTeleport packet = createTeleportPacket();
        location.getWorld().getPlayers().forEach(player -> sendPackets(player, packet));
    }

    @Override
    public void setText(String text) {
        this.text = text;

        dataWatcher.register(nameVisible, !text.isEmpty());
        dataWatcher.register(customName, text);

        Packet<?> metadataPacket = new PacketPlayOutEntityMetadata(id, dataWatcher, true);
        location.getWorld().getPlayers().forEach(player -> sendPackets(player, metadataPacket));
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

    @Override
    public void destroy() {
        location.getWorld().getPlayers().forEach(player -> hidePlayer(player));
    }

    private void sendPackets(Player player, Packet<?>... packets) {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        Arrays.stream(packets).forEach(packet -> con.sendPacket(packet));
    }

    private PacketPlayOutEntityTeleport createTeleportPacket() {
        PacketDataSerializer s = new PacketDataSerializer(Unpooled.buffer());
        s.d(id); // id
        s.writeDouble(location.getX()); // x
        s.writeDouble(location.getY() + 1.975); // y
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

    private void updatePacket() {
        PacketDataSerializer s = new PacketDataSerializer(Unpooled.buffer());
        s.d(id); // id
        s.a(UUID.randomUUID()); // uuid
        s.writeByte(78); // type
        s.writeDouble(location.getX()); // x
        s.writeDouble(location.getY() + 1.975); // y
        s.writeDouble(location.getZ()); // z
        s.writeByte(0); // pitch
        s.writeByte(0); // yaw
        s.writeInt(0); // ?
        s.writeShort(0); // mot x
        s.writeShort(0); // mot y
        s.writeShort(0); // mot z

        try {
            spawnPacket.a(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            s.release();
        }
    }

    @SuppressWarnings("unchecked")
    private DataWatcher createDataWatcher() {
        try {
            Field fEntityFlags = Entity.class.getDeclaredField("ax");
            Field fAirTicks = Entity.class.getDeclaredField("ay");
            Field fNameVisible = Entity.class.getDeclaredField("aA");
            Field fCustomName = Entity.class.getDeclaredField("az");

            setAccessible(fEntityFlags, fAirTicks, fNameVisible, fCustomName);

            nameVisible = (DataWatcherObject<Boolean>) fNameVisible.get(null);
            customName = (DataWatcherObject<Optional<IChatBaseComponent>>) fCustomName.get(null);
            DataWatcherObject<Byte> entityFlags = (DataWatcherObject<Byte>) fEntityFlags.get(null);
            DataWatcherObject<Integer> airTicks = (DataWatcherObject<Integer>) fAirTicks.get(null);
            DataWatcherObject<Byte> armorStandFlags = EntityArmorStand.a;

            DataWatcher dataWatcher = new DataWatcher(null);
            dataWatcher.register(entityFlags, (byte) 0b100000);
            dataWatcher.register(airTicks, 300);
            dataWatcher.register(nameVisible, !text.isEmpty());
            dataWatcher.register(customName, text);
            dataWatcher.register(armorStandFlags, (byte) 0b10000);

            return dataWatcher;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setAccessible(AccessibleObject... args) {
        Arrays.stream(args).forEach(arg -> arg.setAccessible(true));
    }
}