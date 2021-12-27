package de.epiceric.shopchest.event;

import java.util.Collection;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.shop.Shop;

/**
 * Called when shops have been loaded and added to the server
 */
public class ShopsLoadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Collection<Shop> shops;

    public ShopsLoadedEvent(Collection<Shop> shops) {
        this.shops = shops;
    }

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
