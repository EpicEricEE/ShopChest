package de.epiceric.shopchest.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.epiceric.shopchest.api.command.ShopCommand;
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

    /**
     * Gets a formatted String for the given amount of money
     * 
     * @param amount the amount of money
     * @return the formatted amount
     */
    public abstract String formatEconomy(double amount);

    /**
     * Gets the main command of this plugin
     * 
     * @return the shop command
     */
    public abstract ShopCommand getShopCommand();
}