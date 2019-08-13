package de.epiceric.shopchest.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The plugin's main entry point
 * 
 * @since 1.13
 */
public abstract class ShopChest extends JavaPlugin {
    
    /**
     * Gets an instance of the shop manager
     * 
     * @return the shop manager
     */
    public abstract ShopManager getShopManager();

    /**
     * Gets the amount of shops the given player is allowed to have
     * <p>
     * If the player has no shop limit {@link Integer#MAX_VALUE} is returned.
     * 
     * @param player the player
     * @return the shop limit
     */
    public abstract int getShopLimit(OfflinePlayer player);
    
}