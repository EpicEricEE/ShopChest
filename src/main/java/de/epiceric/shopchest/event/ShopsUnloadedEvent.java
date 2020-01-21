package de.epiceric.shopchest.event;

import java.util.Collection;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.shop.Shop;

/**
 * Called when shops have been unloaded and removed from the server
 */
public class ShopsUnloadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Collection<Shop> shops;

    public ShopsUnloadedEvent(Collection<Shop> shops) {
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
