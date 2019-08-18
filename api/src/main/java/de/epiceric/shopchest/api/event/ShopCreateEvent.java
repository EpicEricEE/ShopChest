package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.event.Cancellable;

/**
 * Called when a player clicks a chest to create a shop
 * 
 * @since 1.13
 */
public class ShopCreateEvent extends ShopEvent implements Cancellable {
    private double creationPrice;
    private boolean cancelled;

    public ShopCreateEvent(ShopPlayer player, Shop shop, double creationPrice) {
        super(player, shop);
        this.creationPrice = creationPrice;
    }

    /**
     * Gets the price the player has to pay in order to create the shop
     * <p>
     * The price is only paid if the event is not cancelled.
     * 
     * @return the creation price
     * @since 1.13
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
