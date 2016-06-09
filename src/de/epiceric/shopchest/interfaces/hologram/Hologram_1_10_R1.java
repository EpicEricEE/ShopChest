package de.epiceric.shopchest.interfaces.hologram;

import de.epiceric.shopchest.interfaces.Hologram;
import net.minecraft.server.v1_10_R1.EntityArmorStand;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_10_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hologram_1_10_R1 implements Hologram {

    private boolean exists = false;
    private int count;
    private List<EntityArmorStand> entitylist = new ArrayList<EntityArmorStand>();
    private String[] text;
    private Location location;
    private double DISTANCE = 0.25D;
    private HashMap<OfflinePlayer, Boolean> visible = new HashMap<OfflinePlayer, Boolean>();

    public Hologram_1_10_R1(String[] text, Location location) {
        this.text = text;
        this.location = location;
        create();
    }

    public Location getLocation() {
        return location;
    }

    public List<EntityArmorStand> getEntities() {
        return entitylist;
    }

    public void showPlayer(OfflinePlayer p) {
        for (Object o : entitylist) {
            EntityArmorStand armor = (EntityArmorStand) o;
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armor);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
        visible.put(p, true);
    }

    public void hidePlayer(OfflinePlayer p) {
        for (Object o : entitylist) {
            EntityArmorStand armor = (EntityArmorStand) o;
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armor.getId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
        visible.put(p, false);
    }

    public boolean isVisible(OfflinePlayer p) {
        if (visible.containsKey(p)) return visible.get(p);
        else return false;
    }

    private void create() {
        for (String text : this.text) {
            EntityArmorStand entity = new EntityArmorStand(((CraftWorld) this.location.getWorld()).getHandle(), this.location.getX(), this.location.getY(), this.location.getZ());
            entity.setCustomName(text);
            entity.setCustomNameVisible(true);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entitylist.add(entity);
            this.location.subtract(0, this.DISTANCE, 0);
            count++;
        }

        for (int i = 0; i < count; i++) {
            this.location.add(0, this.DISTANCE, 0);
        }

        count = 0;
        exists = true;
    }

    public boolean exists() {
        return exists;
    }

    public void remove() {
        for (EntityArmorStand e : entitylist) {
            e.die();
        }
        exists = false;
    }

}
