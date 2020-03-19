package de.epiceric.shopchest.shop.hologram.v1_8_R3;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.shop.hologram.IHologramItem;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;

public class HologramItem implements IHologramItem {
    private PacketPlayOutSpawnEntity spawnPacket;
    private DataWatcher dataWatcher;

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

        int x = MathHelper.floor(location.getX() * 32d);
        int y = MathHelper.floor(location.getY() * 32d);
        int z = MathHelper.floor(location.getZ() * 32d);

        Packet<?> teleportPacket = new PacketPlayOutEntityTeleport(id, x, y, z, (byte) 0, (byte) 0, true);
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

        dataWatcher.a(10, getNmsItemStack());

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

    private DataWatcher createDataWatcher() {
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(1, (short) 300); // air ticks
        dataWatcher.a(10, getNmsItemStack()); // item stack
        return dataWatcher;
    }

    private net.minecraft.server.v1_8_R3.ItemStack getNmsItemStack() {
        return CraftItemStack.asNMSCopy(itemStack);
    }
}