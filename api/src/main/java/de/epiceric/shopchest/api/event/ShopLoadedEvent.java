package de.epiceric.shopchest.api.event;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.api.shop.Shop;

/**
 * Called after shops are loaded from the database due to a chunk load
 * 
 * @since 1.13
 */
public class ShopLoadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private Collection<Shop> shops;

    public ShopLoadedEvent(Collection<Shop> shops) {
        this.shops = Collections.unmodifiableCollection(shops);
    }

    /**
     * Gets the shops that have been loaded
     * 
     * @return the shops
     * @since 1.13
     */
    public Collection<Shop> getShops() {
        return shops;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
