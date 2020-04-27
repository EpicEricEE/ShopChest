package de.epiceric.shopchest.nms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;

public class Hologram {
    // concurrent since update task is in async thread
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private final List<ArmorStandWrapper> wrappers = new ArrayList<>();
    private final Location location;
    private final ShopChest plugin;

    private boolean exists;

    public Hologram(ShopChest plugin, String[] lines, Location location) {
        this.plugin = plugin;
        this.location = location;

        for (int i = 0; i < lines.length; i++) {
            addLine(i, lines[i]);
        }

        this.exists = true;
    }

    /**
     * @return Location of the hologram
     */
    public Location getLocation() {
        return location.clone();
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
            if (armorStand.getUniqueId().equals(wrapper.getUuid())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return A list of {@link ArmorStandWrapper}s of this hologram
     */
    public List<ArmorStandWrapper> getArmorStandWrappers() {
        return wrappers;
    }

    /**
     * @param p Player to check
     * @return Whether the hologram is visible to the player
     */
    public boolean isVisible(Player p) {
        return viewers.contains(p.getUniqueId());
    }

    /**
     * @param p Player to which the hologram should be shown
     */
    public void showPlayer(Player p) {
        showPlayer(p, false);
    }

    /**
     * @param p Player to which the hologram should be shown
     * @param force Whether to force showing the hologram
     */
    public void showPlayer(Player p, boolean force) {
        if (viewers.add(p.getUniqueId()) || force) {
            togglePlayer(p, true);
        }
    }

    /**
     * @param p Player from which the hologram should be hidden
     */
    public void hidePlayer(Player p) {
        hidePlayer(p, false);
    }

    /**
     * @param p Player from which the hologram should be hidden
     * @param force Whether to force hiding the hologram
     */
    public void hidePlayer(Player p, boolean force) {
        if (viewers.remove(p.getUniqueId()) || force) {
            togglePlayer(p, false);
        }
    }

    /**
     * <p>Removes the hologram.</p>
     * 
     * Hologram will be hidden from all players and all
     * ArmorStand entities will be killed.
     */
    public void remove() {
        viewers.clear();

        for (ArmorStandWrapper wrapper : wrappers) {
            wrapper.remove();
        }
        wrappers.clear();

        exists = false;
    }

    /**
     * Remove the player from the list of viewers. The hologram is
     * then counted as hidden, but no packets are sent to the player.
     * @param p Player whose visibility status will be reset
     */
    public void resetVisible(Player p) {
        viewers.remove(p.getUniqueId());
    }

    private void togglePlayer(Player p, boolean visible) {
        for (ArmorStandWrapper wrapper : wrappers) {
            wrapper.setVisible(p, visible);
        }
    }

    /**
     * Get all hologram lines
     *
     * @return Hologram lines
     */
    public String[] getLines() {
        List<String> lines = new ArrayList<>();
        for (ArmorStandWrapper wrapper : wrappers) {
            lines.add(wrapper.getCustomName());
        }

        return lines.toArray(new String[lines.size()]);
    }

    /**
     * Add a line
     *
     * @param line where to insert
     * @param text text to display
     */
    public void addLine(int line, String text) {
        addLine(line, text, false);
    }

    private void addLine(int line, String text, boolean forceUpdateLine) {
        if (text == null || text.isEmpty()) return;

        if (line >= wrappers.size()) {
            line = wrappers.size();
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        if (Config.hologramFixedBottom) {
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

        if (!Config.hologramFixedBottom) {
            loc.subtract(0, line * 0.25, 0);
        }

        ArmorStandWrapper wrapper = new ArmorStandWrapper(plugin, loc, text, false);
        wrappers.add(line, wrapper);

        if (forceUpdateLine) {
            for (Player player : location.getWorld().getPlayers()) {
                if (viewers.contains(player.getUniqueId())) {
                    wrapper.setVisible(player, true);
                }
            }
        }
    }

    /**
     * Set a line
     *
     * @param line index to change
     * @param text text to display
     */
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

    /**
     * Remove a line
     *
     * @param line index to remove
     */
    public void removeLine(int line) {
        if (line < wrappers.size()) {
            if (Config.hologramFixedBottom) {
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
}
