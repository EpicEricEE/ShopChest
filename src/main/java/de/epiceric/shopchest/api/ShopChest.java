package de.epiceric.shopchest.api;

import org.bukkit.OfflinePlayer;

/**
 * The plugin's main entry point
 * 
 * @since 1.13
 */
public interface ShopChest {

    /**
     * Gets an instance of the shop manager
     * 
     * @return the shop manager
     */
    ShopManager getShopManager();

    /**
     * Gets the amount of shops the given player is allowed to have
     * <p>
     * If the player has no shop limit {@link Integer#MAX_VALUE} is returned.
     * 
     * @param player the player
     * @return the shop limit
     */
    int getShopLimit(OfflinePlayer player);
    
}