package de.epiceric.shopchest.event;

import de.epiceric.shopchest.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player creates a shop (clicks on a chest)
 */
public class ShopCreateEvent extends ShopEvent implements Cancellable {
    private Player player;
    private Shop shop;
    private double creationPrice;
    private boolean cancelled;

    public ShopCreateEvent(Player player, Shop shop, double creationPrice) {
        this.player = player;
        this.shop = shop;
        this.creationPrice = creationPrice;
    }

    @Override
    public Shop getShop() {
        return shop;
    }

    @Override
    public Player getPlayer() {
        return player;
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
