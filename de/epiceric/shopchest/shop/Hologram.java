package de.epiceric.shopchest.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

public class Hologram {
	 
    private List<EntityArmorStand> entitylist = new ArrayList<EntityArmorStand>();
    private String[] text;
    private Location location;
    private double DISTANCE = 0.25D;
    int count;
        
    private HashMap<OfflinePlayer, Boolean> visible = new HashMap<OfflinePlayer, Boolean>();

    public Hologram(String[] text, Location location) {
            this.text = text;
            this.location = location;
            create();
    }
    
    public Hologram(String text, Location location) {
        this.text = new String[] {text};
        this.location = location;
        create();
    }
    
    public String getText(int line) {
    	return text[line];
    }
    
    public Location getLocation() {
    	return location;
    }
    
    public List<EntityArmorStand> getEntities() {
    	return entitylist;
    }
   
    public void showPlayer(OfflinePlayer p) {
            for (EntityArmorStand armor : entitylist) {
                    PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armor);
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }
            visible.put(p, true);
    }

    public void hidePlayer(OfflinePlayer p) {
    	for (EntityArmorStand armor : entitylist) {
                    PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(armor.getId());
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

            }
            visible.put(p, false);
    }

    public boolean isVisible(OfflinePlayer p) {
    	if (visible.containsKey(p)) return visible.get(p); else return false;
    }
    
    private void create() {
            for (String text : this.text) {
                    EntityArmorStand entity = new EntityArmorStand(((CraftWorld) this.location.getWorld()).getHandle(),this.location.getX(), this.location.getY(),this.location.getZ());
                    entity.setCustomName(text);
                    entity.setCustomNameVisible(true);              
                    entity.setInvisible(true);
                    entity.setGravity(false);
                    entitylist.add(entity);
                    this.location.subtract(0, this.DISTANCE, 0);
                    count++;
            }

            for (int i = 0; i < count; i++) {
                    this.location.add(0, this.DISTANCE, 0);
            }
            this.count = 0;
    }

}
