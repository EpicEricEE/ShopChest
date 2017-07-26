package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Hologram {

    private static List<Hologram> holograms = new ArrayList<>();

    private final Set<UUID> visibility = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private final List<ArmorStandWrapper> wrappers = new ArrayList<>();
    private final Location location;
    private final ShopChest plugin;
    private final Config config;

    private boolean exists = false;
    private ArmorStandWrapper interactArmorStandWrapper;

    public Hologram(ShopChest plugin, String[] lines, Location location) {
        this.plugin = plugin;
        this.config = plugin.getShopChestConfig();
        this.location = location;

        create(lines);
    }

    public void addLine(int line, String text) {
        addLine(line, text, false);
    }

    private void addLine(int line, String text, boolean forceUpdateLine) {
        if (text == null || text.isEmpty()) return;

        if (line >= wrappers.size()) {
            line = wrappers.size();
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        if (config.hologram_fixed_bottom) {
            for (int i = 0; i < line; i++) {
                ArmorStandWrapper wrapper = wrappers.get(i);
                wrapper.setLocation(wrapper.getLocation().add(0, 0.25, 0));
            }
        } else {
            for (int i = line; i < wrappers.size(); i++) {
                ArmorStandWrapper wrapper = wrappers.get(i);
                wrapper.setLocation(wrapper.getLocation().subtract(0, 0.25, 0));
            }
        }

        Location loc = getLocation();

        if (!config.hologram_fixed_bottom) {
            loc.subtract(0, line * 0.25, 0);
        }

        ArmorStandWrapper wrapper = new ArmorStandWrapper(plugin, loc, text);
        wrappers.add(line, wrapper);

        if (forceUpdateLine) {
            for (UUID uuid : visibility) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    wrapper.setVisible(player, true);
                }
            }
        }
    }

    public void setLine(int line, String text) {
        if (text == null ||text.isEmpty()) {
            removeLine(line);
            return;
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        if (line >= wrappers.size()) {
            addLine(line, text, true);
            return;
        }

        wrappers.get(line).setCustomName(text);
    }

    public void removeLine(int line) {
        if (line < wrappers.size()) {
            if (config.hologram_fixed_bottom) {
                for (int i = 0; i < line; i++) {
                    ArmorStandWrapper wrapper = wrappers.get(i);
                    wrapper.setLocation(wrapper.getLocation().subtract(0, 0.25, 0));
                }
            } else {
                for (int i = line + 1; i < wrappers.size(); i++) {
                    ArmorStandWrapper wrapper = wrappers.get(i);
                    wrapper.setLocation(wrapper.getLocation().add(0, 0.25, 0));
                }
            }

            wrappers.get(line).remove();
            wrappers.remove(line);
        }
    }

    public String[] getLines() {
        List<String> lines = new ArrayList<>();
        for (ArmorStandWrapper wrapper : wrappers) {
            lines.add(wrapper.getCustomName());
        }

        return lines.toArray(new String[lines.size()]);
    }

    private void create(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            addLine(i, lines[i]);
        }

        if (plugin.getShopChestConfig().enable_hologram_interaction) {
            double y = 0.6;
            if (config.hologram_fixed_bottom) y = 0.85;

            Location loc = getLocation().add(0, y, 0);
            interactArmorStandWrapper = new ArmorStandWrapper(plugin, loc, null);
        }

        for (Player player : location.getWorld().getPlayers()) {
            plugin.getShopUtils().updateShops(player, true);
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
        if (!isVisible(p)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (ArmorStandWrapper wrapper : wrappers) {
                        wrapper.setVisible(p, true);
                    }

                    if (interactArmorStandWrapper != null) {
                        interactArmorStandWrapper.setVisible(p, true);
                    }
                }
            }.runTaskAsynchronously(plugin);

            visibility.add(p.getUniqueId());
        }
    }

    /**
     * @param p Player from which the hologram should be hidden
     */
    public void hidePlayer(final Player p) {
        if (isVisible(p)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (ArmorStandWrapper wrapper : wrappers) {
                        wrapper.setVisible(p, false);
                    }

                    if (interactArmorStandWrapper != null) {
                        interactArmorStandWrapper.setVisible(p, false);
                    }
                }
            }.runTaskAsynchronously(plugin);

            visibility.remove(p.getUniqueId());
        }
    }

    /**
     * @param p Player to check
     * @return Whether the hologram is visible to the player
     */
    public boolean isVisible(Player p) {
        return visibility.contains(p.getUniqueId());
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
        for (ArmorStandWrapper wrapper : wrappers) {
            if (wrapper.getUuid().equals(armorStand.getUniqueId())) {
                return true;
            }
        }
        return interactArmorStandWrapper != null && interactArmorStandWrapper.getUuid().equals(armorStand.getUniqueId());
    }

    /** Returns the ArmorStandWrappers of this hologram */
    public List<ArmorStandWrapper> getArmorStandWrappers() {
        return wrappers;
    }

    /** Returns the ArmorStandWrapper of this hologram that is positioned higher to be used for interaction */
    public ArmorStandWrapper getInteractArmorStandWrapper() {
        return interactArmorStandWrapper;
    }

    /**
     * Removes the hologram. <br>
     * Hologram will be hidden from all players and will be killed
     */
    public void remove() {
        for (ArmorStandWrapper wrapper : wrappers) {
            wrapper.remove();
        }

        if (interactArmorStandWrapper != null) {
            interactArmorStandWrapper.remove();
        }

        wrappers.clear();

        exists = false;
        holograms.remove(this);
    }

    /**
     * @param armorStand Armor stand that's part of a hologram
     * @return Hologram, the armor stand is part of
     */
    public static Hologram getHologram(ArmorStand armorStand) {
        for (Hologram hologram : holograms) {
            if (hologram.contains(armorStand)) {
                return hologram;
            }
        }

        return null;
    }

    /**
     * @param armorStand Armor stand to check
     * @return Whether the armor stand is part of a hologram
     */
    public static boolean isPartOfHologram(ArmorStand armorStand) {
        return getHologram(armorStand) != null;
    }

}
