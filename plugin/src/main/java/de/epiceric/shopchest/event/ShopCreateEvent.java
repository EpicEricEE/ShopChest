package de.epiceric.shopchest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import de.epiceric.shopchest.shop.Shop;

/**
 * Called when a player creates a shop (clicks on a chest)
 */
public class ShopCreateEvent extends ShopEvent implements Cancellable {
    private double creationPrice;
    private boolean cancelled;

    public ShopCreateEvent(Player player, Shop shop, double creationPrice) {
        super(player, shop);
        this.creationPrice = creationPrice;
    }
    /**
     * @return The price the player has to pay in order to create the shop (only if the event is not cancelled)
     */
    public double getCreationPrice() {
        return creationPrice;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
