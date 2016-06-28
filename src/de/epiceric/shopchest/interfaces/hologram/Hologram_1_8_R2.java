package de.epiceric.shopchest.interfaces.hologram;

import de.epiceric.shopchest.interfaces.Hologram;
import net.minecraft.server.v1_8_R2.EntityArmorStand;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R2.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hologram_1_8_R2 implements Hologram {

    private boolean exists = false;
    private int count;
    private List<EntityArmorStand> entityList = new ArrayList<>();
    private String[] text;
    private Location location;
    private List<OfflinePlayer> visible = new ArrayList<>();

    public Hologram_1_8_R2(String[] text, Location location) {
        this.text = text;
        this.location = location;
        create();
    }

    public Location getLocation() {
        return location;
    }

    public void showPlayer(OfflinePlayer p) {
        for (Object o : entityList) {
            EntityArmorStand armor = (EntityArmorStand) o;
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armor);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
        visible.add(p);
    }

    public void hidePlayer(OfflinePlayer p) {
        for (Object o : entityList) {
            EntityArmorStand armor = (EntityArmorStand) o;
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armor.getId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
        visible.remove(p);
    }

    public boolean isVisible(OfflinePlayer p) {
        return visible.contains(p);
    }

    private void create() {
        for (String text : this.text) {
            EntityArmorStand entity = new EntityArmorStand(((CraftWorld) this.location.getWorld()).getHandle(), this.location.getX(), this.location.getY(), this.location.getZ());
            entity.setCustomName(text);
            entity.setCustomNameVisible(true);
            entity.setInvisible(true);
            entity.setGravity(false);
            entityList.add(entity);
            this.location.subtract(0, 0.25, 0);
            count++;
        }

        for (int i = 0; i < count; i++) {
            this.location.add(0, 0.25, 0);
        }

        count = 0;
        exists = true;
    }

    public boolean exists() {
        return exists;
    }

    public void remove() {
        for (EntityArmorStand e : entityList) {
            e.die();
        }
        exists = false;
    }

}
