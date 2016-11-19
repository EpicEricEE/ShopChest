package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hologram {

    private static List<Hologram> holograms = new ArrayList<>();

    private boolean exists = false;
    private List<Object> entityList = new ArrayList<>();
    private List<UUID> entityUuidList = new ArrayList<>();
    private String[] text;
    private Location location;
    private List<Player> visible = new ArrayList<>();
    private ShopChest plugin;

    private Class<?> entityArmorStandClass = Utils.getNMSClass("EntityArmorStand");
    private Class<?> nmsWorldClass = Utils.getNMSClass("World");
    private Class<?> packetPlayOutSpawnEntityLivingClass = Utils.getNMSClass("PacketPlayOutSpawnEntityLiving");
    private Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private Class<?> entityClass = Utils.getNMSClass("Entity");
    private Class<?> entityLivingClass = Utils.getNMSClass("EntityLiving");

    public Hologram(ShopChest plugin, String[] text, Location location) {
        this.plugin = plugin;
        this.text = text;
        this.location = location;

        Class[] requiredClasses = new Class[] {
                nmsWorldClass, entityArmorStandClass, entityLivingClass, entityClass,
                packetPlayOutSpawnEntityLivingClass, packetPlayOutEntityDestroyClass
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
        Location loc = location.clone();

        for (int i = 0; i <= text.length; i++) {
            String text = null;

            if (i != this.text.length) {
                text = this.text[i];
                if (text == null) continue;
            } else {
                if (plugin.getShopChestConfig().enable_hologram_interaction) {
                    loc = location.clone();
                    loc.add(0, 1, 0);
                } else {
                    continue;
                }
            }

            try {
                Object craftWorld = loc.getWorld().getClass().cast(loc.getWorld());
                Object nmsWorldServer = craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);

                Constructor entityArmorStandConstructor = entityArmorStandClass.getConstructor(nmsWorldClass, double.class, double.class, double.class);
                Object entityArmorStand = entityArmorStandConstructor.newInstance(nmsWorldServer, loc.getX(), loc.getY(),loc.getZ());

                if (text != null) {
                    entityArmorStandClass.getMethod("setCustomName", String.class).invoke(entityArmorStand, text);
                    entityArmorStandClass.getMethod("setCustomNameVisible", boolean.class).invoke(entityArmorStand, true);
                }

                entityArmorStandClass.getMethod("setInvisible", boolean.class).invoke(entityArmorStand, true);

                if (Utils.getMajorVersion() < 10) {
                    entityArmorStandClass.getMethod("setGravity", boolean.class).invoke(entityArmorStand, false);
                } else {
                    entityArmorStandClass.getMethod("setNoGravity", boolean.class).invoke(entityArmorStand, true);
                }

                // Probably like an addEntity() method...
                Method b = nmsWorldServer.getClass().getDeclaredMethod("b", entityClass);
                b.setAccessible(true);
                b.invoke(nmsWorldServer, entityArmorStand);

                Object uuid = entityClass.getMethod("getUniqueID").invoke(entityArmorStand);

                entityUuidList.add((UUID) uuid);
                entityList.add(entityArmorStand);

                loc.subtract(0, 0.25, 0);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                plugin.getLogger().severe("Could not create Hologram with reflection");
                plugin.debug("Could not create Hologram with reflection");
                plugin.debug(e);
            }


        }

        holograms.add(this);
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
                plugin.getLogger().severe("Could not show Hologram to player with reflection");
                plugin.debug("Could not show Hologram to player with reflection");
                plugin.debug(e);
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
                plugin.getLogger().severe("Could not hide Hologram from player with reflection");
                plugin.debug("Could not hide Hologram from player with reflection");
                plugin.debug(e);
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
     * @param armorStand Armor stand to check
     * @return Whether the given armor stand is part of the hologram
     */
    public boolean contains(ArmorStand armorStand) {
        return entityUuidList.contains(armorStand.getUniqueId());
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
                plugin.getLogger().severe("Could not remove Hologram with reflection");
                plugin.debug("Could not remove Hologram with reflection");
                plugin.debug(e);
            }
        }
        exists = false;
        holograms.remove(this);
    }

    /**
     * @param armorStand Armor stand that's part of a hologram
     * @return Hologram, the armor stand is part of
     */
    public static Hologram getHologram(ArmorStand armorStand) {
        for (Hologram hologram : holograms) {
            if (hologram.contains(armorStand)) return hologram;
        }

        return null;
    }

    /**
     * @param armorStand Armor stand to check
     * @return Whether the armor stand is part of a hologram
     */
    public static boolean isPartOfHologram(ArmorStand armorStand) {
        for (Hologram hologram : holograms) {
            if (hologram.contains(armorStand)) {
                return true;
            }
        }
        return false;
    }

}
