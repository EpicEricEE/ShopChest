package de.epiceric.shopchest.api.flag;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Represents a flag a player can have
 * 
 * @since 1.13
 */
public interface Flag {

    /**
     * Called when this flag is assigned to a player
     * 
     * @param player the player
     * @since 1.13
     */
    default void onAssign(ShopPlayer player) {};
    
    /**
     * Called when this flag will be removed from a player
     * <p>
     * The flag will be removed after this method is called.
     * 
     * @param player the player
     * @since 1.13
     */
    default void onRemove(ShopPlayer player) {};
}