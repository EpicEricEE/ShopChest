package de.epiceric.shopchest.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @deprecated Use {@link ShopsLoadedEvent} instead since shops are loaded
 *             dynamically based on chunk loading
 */
@Deprecated
public class ShopInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private int amount;

    public ShopInitializedEvent(int amount) {
        this.amount = amount;
    }

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
