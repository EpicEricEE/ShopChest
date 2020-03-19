package de.epiceric.shopchest.shop.hologram;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IHologramLine {

    /**
     * Sets the location of this hologram line and sends the update to
     * all players in the hologram's world
     * 
     * @param location the location to set
     */
    void setLocation(Location location);

    /**
     * Gets the current location
     * 
     * @return the location
     */
    Location getLocation();

    /**
     * Sets the text of this hologram line and sends the update to all players
     * in the hologram's world
     * <p>
     * The text must not be JSON text, but regular text using Bukkit's {@link ChatColor}.
     * 
     * @param text the text
     */
    void setText(String text);

    /**
     * Gets the current text
     * 
     * @return the text
     */
    String getText();

    /**
     * Displays the hologram line to the given player
     * 
     * @param player the player to show the line to
     */
    void showPlayer(Player player);

    /**
     * Hides the hologram line from the given player
     * 
     * @param player the player to hide the line from
     */
    void hidePlayer(Player player);

    /**
     * Hides the hologram line from all players in the hologram's world
     */
    default void destroy() {
        getLocation().getWorld().getPlayers().forEach(this::hidePlayer);
    }

}