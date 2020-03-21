package de.epiceric.shopchest.shop.hologram.v1_9_R2;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.shop.hologram.IHologramLine;
import net.minecraft.server.v1_9_R2.DataWatcher;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R2.PacketPlayOutSpawnEntity;

public class HologramLine implements IHologramLine{
    private PacketPlayOutSpawnEntity spawnPacket;
    private DataWatcher dataWatcher;

    private DataWatcherObject<Boolean> nameVisible;
    private DataWatcherObject<Optional<IChatBaseComponent>> customName;

    private int id;
    private Location location;
    private String text;

    public HologramLine(Location location, String text) {
        this.id = HologramUtil.getFreeEntityId();
        this.location = location.clone();
        this.text = text;

        this.spawnPacket = new PacketPlayOutSpawnEntity();
        this.dataWatcher = createDataWatcher();

        HologramUtil.updateSpawnPacket(id, 78, spawnPacket, location);
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        HologramUtil.updateSpawnPacket(id, 78, spawnPacket, location);

        PacketPlayOutEntityTeleport packet = HologramUtil.createTeleportPacket(id, location, true);
        location.getWorld().getPlayers().forEach(player -> HologramUtil.sendPackets(player, packet));
    }

    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    @Override
    public void setText(String text) {
        this.text = text;

        dataWatcher.register(nameVisible, !text.isEmpty());
        dataWatcher.register(customName, text);

        Packet<?> metadataPacket = new PacketPlayOutEntityMetadata(id, dataWatcher, true);
        location.getWorld().getPlayers().forEach(player -> HologramUtil.sendPackets(player, metadataPacket));
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

        HologramUtil.sendPackets(player, spawnPacket, new PacketPlayOutEntityMetadata(id, dataWatcher, true));
    }

    @Override
    public void hidePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        HologramUtil.sendPackets(player, new PacketPlayOutEntityDestroy(id));
    }

    @SuppressWarnings("unchecked")
    private DataWatcher createDataWatcher() {
        try {
            Field fEntityFlags = Entity.class.getDeclaredField("ay");
            Field fAirTicks = Entity.class.getDeclaredField("az");
            Field fNameVisible = Entity.class.getDeclaredField("aB");
            Field fCustomName = Entity.class.getDeclaredField("aA");

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