package de.epiceric.shopchest.shop.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IHologramItem {
    
    /**
     * Sets the location of this item and sends the update to
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
     * Sets the item shown in the hologram
     * <p>
     * The amount of the item stack will always be set to 1.
     * 
     * @param itemStack the item to set
     */
    void setItemStack(ItemStack itemStack);

    /**
     * Gets the current item of the hologram
     * 
     * @return the item
     */
    ItemStack getItemStack();

    /**
     * Displays the item to the given player
     * 
     * @param player the player to show the item to
     */
    void showPlayer(Player player);

    /**
     * Hides the item from the given player
     * 
     * @param player the player to hide the item from
     */
    void hidePlayer(Player player);

    /**
     * Hides the item from all players in the hologram's world
     */
    default void destroy() {
        getLocation().getWorld().getPlayers().forEach(this::hidePlayer);
    }
    
}