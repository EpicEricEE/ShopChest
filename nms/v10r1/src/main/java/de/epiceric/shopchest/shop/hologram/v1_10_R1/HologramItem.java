package de.epiceric.shopchest.shop.hologram.v1_10_R1;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;

import com.google.common.base.Optional;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.shop.hologram.IHologramItem;
import net.minecraft.server.v1_10_R1.DataWatcher;
import net.minecraft.server.v1_10_R1.DataWatcherObject;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityItem;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_10_R1.PacketPlayOutSpawnEntity;

public class HologramItem implements IHologramItem {
    private PacketPlayOutSpawnEntity spawnPacket;
    private DataWatcher dataWatcher;

    private DataWatcherObject<Optional<net.minecraft.server.v1_10_R1.ItemStack>> item;

    private int id;
    private Location location;
    private ItemStack itemStack;

    public HologramItem(Location location, ItemStack itemStack) {
        this.id = HologramUtil.getFreeEntityId();
        this.location = location.clone();
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);

        this.spawnPacket = new PacketPlayOutSpawnEntity();
        this.dataWatcher = createDataWatcher();

        HologramUtil.updateSpawnPacket(id, 2, spawnPacket, location);
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        HologramUtil.updateSpawnPacket(id, 2, spawnPacket, location);

        Packet<?> teleportPacket = HologramUtil.createTeleportPacket(id, location, false);
        location.getWorld().getPlayers().forEach(player -> HologramUtil.sendPackets(player, teleportPacket));
    }

    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);

        dataWatcher.register(item, Optional.of(getNmsItemStack())); // item stack

        Packet<?> metadataPacket = new PacketPlayOutEntityMetadata(id, dataWatcher, true);
        location.getWorld().getPlayers().forEach(player -> HologramUtil.sendPackets(player, metadataPacket));
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack.clone();
    }

    @Override
    public void showPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Packet<?> metadataPacket = new PacketPlayOutEntityMetadata(id, dataWatcher, true);
        Packet<?> velocityPacket = new PacketPlayOutEntityVelocity(id, 0, 0, 0);
        HologramUtil.sendPackets(player, spawnPacket, metadataPacket, velocityPacket);
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
            Field fAirTicks = Entity.class.getDeclaredField("az");
            Field fNoGravity = Entity.class.getDeclaredField("aD");
            Field fItem = EntityItem.class.getDeclaredField("c");

            setAccessible(fAirTicks, fNoGravity, fItem);

            item = (DataWatcherObject<Optional<net.minecraft.server.v1_10_R1.ItemStack>>) fItem.get(null);
            DataWatcherObject<Integer> airTicks = (DataWatcherObject<Integer>) fAirTicks.get(null);
            DataWatcherObject<Boolean> noGravity = (DataWatcherObject<Boolean>) fNoGravity.get(null);

            DataWatcher dataWatcher = new DataWatcher(null);
            dataWatcher.register(airTicks, 300);
            dataWatcher.register(noGravity, true);
            dataWatcher.register(item, Optional.of(getNmsItemStack()));

            return dataWatcher;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private net.minecraft.server.v1_10_R1.ItemStack getNmsItemStack() {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    private void setAccessible(AccessibleObject... args) {
        Arrays.stream(args).forEach(arg -> arg.setAccessible(true));
    }
}