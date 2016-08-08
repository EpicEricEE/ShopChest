package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private boolean exists = false;
    private int count;
    private List<Object> entityList = new ArrayList<>();
    private String[] text;
    private Location location;
    private List<Player> visible = new ArrayList<>();
    private ShopChest plugin;

    private Class<?> entityArmorStandClass = Utils.getNMSClass("EntityArmorStand");
    private Class<?> nmsWorldClass = Utils.getNMSClass("World");
    private Class<?> packetPlayOutSpawnEntityLivingClass = Utils.getNMSClass("PacketPlayOutSpawnEntityLiving");
    private Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private Class<?> entityLivingClass = Utils.getNMSClass("EntityLiving");

    public Hologram(ShopChest plugin, String[] text, Location location) {
        this.plugin = plugin;
        this.text = text;
        this.location = location;

        Class[] requiredClasses = new Class[] {
                nmsWorldClass, entityArmorStandClass, entityLivingClass,
                packetPlayOutSpawnEntityLivingClass, packetPlayOutEntityDestroyClass,
        };

        for (Class c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to create hologram: Could not find all required classes");
                return;
            }
        }

        create();
    }

    private void create() {
        for (String text : this.text) {
            try {
                Object craftWorld = this.location.getWorld().getClass().cast(this.location.getWorld());
                Object nmsWorld = nmsWorldClass.cast(craftWorld.getClass().getMethod("getHandle").invoke(craftWorld));

                Constructor entityArmorStandConstructor = entityArmorStandClass.getConstructor(nmsWorldClass, double.class, double.class, double.class);
                Object entityArmorStand = entityArmorStandConstructor.newInstance(nmsWorld, this.location.getX(), this.location.getY(), this.getLocation().getZ());

                entityArmorStandClass.getMethod("setCustomName", String.class).invoke(entityArmorStand, text);
                entityArmorStandClass.getMethod("setCustomNameVisible", boolean.class).invoke(entityArmorStand, true);
                entityArmorStandClass.getMethod("setInvisible", boolean.class).invoke(entityArmorStand, true);

                if (Utils.getMajorVersion() < 10) {
                    entityArmorStandClass.getMethod("setGravity", boolean.class).invoke(entityArmorStand, false);
                } else {
                    entityArmorStandClass.getMethod("setNoGravity", boolean.class).invoke(entityArmorStand, true);
                }

                entityList.add(entityArmorStand);
                this.location.subtract(0, 0.25, 0);
                count++;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                plugin.debug("Could not create Hologram with reflection");
                plugin.debug(e);
                e.printStackTrace();
            }


        }

        for (int i = 0; i < count; i++) {
            this.location.add(0, 0.25, 0);
        }

        count = 0;
        exists = true;
    }

    /**
     * @return Location of the hologram
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param p Player to which the hologram should be shown
     */
    public void showPlayer(Player p) {
        for (Object o : entityList) {
            try {
                Object entityLiving = entityLivingClass.cast(o);
                Object packet = packetPlayOutSpawnEntityLivingClass.getConstructor(entityLivingClass).newInstance(entityLiving);

                Utils.sendPacket(plugin, packet, p);
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                plugin.debug("Could not show Hologram to player with reflection");
                plugin.debug(e);
                e.printStackTrace();
            }
        }
        visible.add(p);
    }

    /**
     * @param p Player from which the hologram should be hidden
     */
    public void hidePlayer(Player p) {
        for (Object o : entityList) {
            try {
                int id = (int) entityArmorStandClass.getMethod("getId").invoke(o);

                Object packet = packetPlayOutEntityDestroyClass.getConstructor(int[].class).newInstance((Object) new int[] {id});

                Utils.sendPacket(plugin, packet, p);
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                plugin.debug("Could not hide Hologram from player with reflection");
                plugin.debug(e);
                e.printStackTrace();
            }
        }
        visible.remove(p);
    }

    /**
     * @param p Player to check
     * @return Whether the hologram is visible to the player
     */
    public boolean isVisible(Player p) {
        return visible.contains(p);
    }

    /**
     * @return Whether the hologram exists and is not dead
     */
    public boolean exists() {
        return exists;
    }

    /**
     * Removes the hologram. <br>
     * IHologram will be hidden from all players and will be killed
     */
    public void remove() {
        for (Object o : entityList) {
            try {
                o.getClass().getMethod("die").invoke(o);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                plugin.debug("Could not remove Hologram with reflection");
                plugin.debug(e);
                e.printStackTrace();
            }
        }
        exists = false;
    }

}
