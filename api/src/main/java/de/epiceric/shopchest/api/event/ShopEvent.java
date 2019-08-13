package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents a shop related event
 * 
 * @since 1.13
 */
public abstract class ShopEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private Shop shop;
    private Player player;

    public ShopEvent(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    /**
     * Get the shop which is involved in this event
     * 
     * @return the shop
     * @since 1.13
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Gets the player who is involved in this event
     * 
     * @return the player
     * @since 1.13
     */
    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
