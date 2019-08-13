package de.epiceric.shopchest.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after all shops are initialized after the plugin is enabled
 * 
 * @since 1.13
 */
public class ShopInitializedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private int amount;

    public ShopInitializedEvent(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the amount of shops that were initialized
     * 
     * @return the amount of shops
     * @since 1.13
     */
    public int getAmount() {
        return amount;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
