package de.epiceric.shopchest.shop.hologram.v1_8_R3;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.shop.hologram.IHologramLine;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;

public class HologramLine implements IHologramLine {
    private PacketPlayOutSpawnEntity spawnPacket;
    private DataWatcher dataWatcher;

    private int id;
    private Location location;
    private String text;

    public HologramLine(Location location, String text) {
        this.id = HologramUtil.getFreeEntityId();
        this.location = location.clone();
        this.text = text == null ? "" : text;

        this.spawnPacket = new PacketPlayOutSpawnEntity();
        this.dataWatcher = createDataWatcher();

        HologramUtil.updateSpawnPacket(id, 78, spawnPacket, location);
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
        HologramUtil.updateSpawnPacket(id, 78, spawnPacket, location);

        int x = MathHelper.floor(location.getX() * 32d);
        int y = MathHelper.floor((location.getY() + 1.975) * 32d);
        int z = MathHelper.floor(location.getZ() * 32d);

        Packet<?> teleportPacket = new PacketPlayOutEntityTeleport(id, x, y, z, (byte) 0, (byte) 0, true);
        location.getWorld().getPlayers().forEach(player -> HologramUtil.sendPackets(player, teleportPacket));
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