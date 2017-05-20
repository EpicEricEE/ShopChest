package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private static List<Hologram> holograms = new ArrayList<>();

    private boolean exists = false;
    private List<Object> nmsArmorStands = new ArrayList<>();
    private List<ArmorStand> armorStands = new ArrayList<>();
    private ArmorStand interactArmorStand;
    private Location location;
    private List<Player> visible = new ArrayList<>();
    private ShopChest plugin;

    private Class<?> entityArmorStandClass = Utils.getNMSClass("EntityArmorStand");
    private Class<?> packetPlayOutSpawnEntityLivingClass = Utils.getNMSClass("PacketPlayOutSpawnEntityLiving");
    private Class<?> packetPlayOutEntityDestroyClass = Utils.getNMSClass("PacketPlayOutEntityDestroy");
    private Class<?> entityLivingClass = Utils.getNMSClass("EntityLiving");

    public Hologram(ShopChest plugin, String[] lines, Location location) {
        this.plugin = plugin;
        this.location = location;

        Class[] requiredClasses = new Class[] {
                entityArmorStandClass, entityLivingClass, packetPlayOutSpawnEntityLivingClass,
                packetPlayOutEntityDestroyClass,
        };

        for (Class c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to create hologram: Could not find all required classes");
                return;
            }
        }

        create(lines);
    }

    public void addLine(int line, String text) {
        if (text == null || text.isEmpty()) return;

        text = ChatColor.translateAlternateColorCodes('&', text);

        for (int i = line; i < armorStands.size(); i++) {
            ArmorStand stand = armorStands.get(i);
            stand.teleport(stand.getLocation().subtract(0, 0.25, 0));
        }

        if (line >= armorStands.size()) {
            line = armorStands.size();
        }

        Location location = this.location.clone().subtract(0, line * 0.25, 0);

        try {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setCustomName(text);
            armorStand.setCustomNameVisible(true);

            Object craftArmorStand = armorStand.getClass().cast(armorStand);
            Object nmsArmorStand = craftArmorStand.getClass().getMethod("getHandle").invoke(craftArmorStand);

            nmsArmorStands.add(line, nmsArmorStand);
            armorStands.add(line, armorStand);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().severe("Could not create Hologram with reflection");
            plugin.debug("Could not create Hologram with reflection");
            plugin.debug(e);
        }
    }

    public void setLine(int line, String text) {
        if (text == null ||text.isEmpty()) {
            removeLine(line);
            return;
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        if (armorStands.size() <= line) {
            addLine(line, text);
            return;
        }

        armorStands.get(line).setCustomName(text);
    }

    public void removeLine(int line) {
        for (int i = line + 1; i < armorStands.size(); i++) {
            ArmorStand stand = armorStands.get(i);
            stand.teleport(stand.getLocation().add(0, 0.25, 0));
        }

        if (armorStands.size() > line) {
            armorStands.get(line).remove();
            armorStands.remove(line);
            nmsArmorStands.remove(line);
        }
    }

    public String[] getLines() {
        List<String> lines = new ArrayList<>();
        for (ArmorStand armorStand : armorStands) {
            lines.add(armorStand.getCustomName());
        }

        return lines.toArray(new String[lines.size()]);
    }

    private void create(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            addLine(i, lines[i]);
        }

        if (plugin.getShopChestConfig().enable_hologram_interaction) {
            Location loc = location.clone().add(0, 0.4, 0);

            try {
                ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setGravity(false);
                armorStand.setVisible(false);

                Object craftArmorStand = armorStand.getClass().cast(armorStand);
                Object nmsArmorStand = craftArmorStand.getClass().getMethod("getHandle").invoke(craftArmorStand);

                nmsArmorStands.add(nmsArmorStand);
                interactArmorStand = armorStand;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
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
        return location.clone();
    }

    /**
     * @param p Player to which the hologram should be shown
     */
    public void showPlayer(final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Object o : nmsArmorStands) {
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
            }
        }.runTaskAsynchronously(plugin);

        visible.add(p);
    }

    /**
     * @param p Player from which the hologram should be hidden
     */
    public void hidePlayer(final Player p, boolean useCurrentThread) {
        if (useCurrentThread) {
            sendDestroyPackets(p);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendDestroyPackets(p);
                }
            }.runTaskAsynchronously(plugin);
        }

        visible.remove(p);
    }

    private void sendDestroyPackets(Player p) {
        for (Object o : nmsArmorStands) {
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
    }

    /**
     * @param p Player from which the hologram should be hidden
     */
    public void hidePlayer(final Player p) {
        hidePlayer(p, false);
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
        return armorStands.contains(armorStand);
    }

    /** Returns the ArmorStands of this hologram */
    public List<ArmorStand> getArmorStands() {
        return armorStands;
    }

    /** Returns the ArmorStand of this hologram that is responsible for interaction */
    public ArmorStand getInteractArmorStand() {
        return interactArmorStand;
    }

    /**
     * Removes the hologram. <br>
     * Hologram will be hidden from all players and will be killed
     */
    public void remove() {
        for (ArmorStand armorStand : armorStands) {
            armorStand.remove();
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
