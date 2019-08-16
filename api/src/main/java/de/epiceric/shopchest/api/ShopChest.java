package de.epiceric.shopchest.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.epiceric.shopchest.api.player.ShopPlayer;

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
     * Gets the wrapped {@link ShopPlayer} for the given player
     * 
     * @param player the player
     * @return the wrapped player
     */
    public abstract ShopPlayer wrapPlayer(Player player);
    
}