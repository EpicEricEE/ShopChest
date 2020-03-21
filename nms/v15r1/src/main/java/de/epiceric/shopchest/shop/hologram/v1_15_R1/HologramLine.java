package de.epiceric.shopchest.shop.hologram.v1_15_R1;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.shop.hologram.IHologramLine;
import net.minecraft.server.v1_15_R1.DataWatcher;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;

public class HologramLine implements IHologramLine {
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

        HologramUtil.updateSpawnPacket(id, EntityTypes.ARMOR_STAND, spawnPacket, location);
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        HologramUtil.updateSpawnPacket(id, EntityTypes.ARMOR_STAND, spawnPacket, location);

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
        dataWatcher.register(customName, Optional.ofNullable(ChatSerializer.b(text)));

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
            Field fEntityFlags = Entity.class.getDeclaredField("T");
            Field fAirTicks = Entity.class.getDeclaredField("AIR_TICKS");
            Field fNameVisible = Entity.class.getDeclaredField("aA");
            Field fCustomName = Entity.class.getDeclaredField("az");
            Field fNoGravity = Entity.class.getDeclaredField("aC");

            setAccessible(fEntityFlags, fAirTicks, fNameVisible, fCustomName, fNoGravity);

            nameVisible = (DataWatcherObject<Boolean>) fNameVisible.get(null);
            customName = (DataWatcherObject<Optional<IChatBaseComponent>>) fCustomName.get(null);
            DataWatcherObject<Byte> entityFlags = (DataWatcherObject<Byte>) fEntityFlags.get(null);
            DataWatcherObject<Integer> airTicks = (DataWatcherObject<Integer>) fAirTicks.get(null);
            DataWatcherObject<Boolean> noGravity = (DataWatcherObject<Boolean>) fNoGravity.get(null);
            DataWatcherObject<Byte> armorStandFlags = EntityArmorStand.b;

            DataWatcher dataWatcher = new DataWatcher(null);
            dataWatcher.register(entityFlags, (byte) 0b100000);
            dataWatcher.register(airTicks, 300);
            dataWatcher.register(nameVisible, !text.isEmpty());
            dataWatcher.register(customName, Optional.ofNullable(ChatSerializer.b(text)));
            dataWatcher.register(noGravity, true);
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