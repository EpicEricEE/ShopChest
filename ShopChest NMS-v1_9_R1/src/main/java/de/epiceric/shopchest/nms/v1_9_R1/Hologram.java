package de.epiceric.shopchest.nms.v1_9_R1;

import de.epiceric.shopchest.nms.IHologram;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;

public class Hologram implements IHologram {

    private boolean exists = false;
    private int count;
    private List<EntityArmorStand> entityList = new ArrayList<>();
    private String[] text;
    private Location location;
    private List<OfflinePlayer> visible = new ArrayList<>();

    public Hologram(String[] text, Location location) {
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
