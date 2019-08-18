package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;

import org.bukkit.event.Cancellable;

/**
 * Called when a player buys or sells something from or to a shop
 * 
 * @since 1.13
 */
public class ShopBuySellEvent extends ShopEvent implements Cancellable {
    private Type type;
    private int amount;
    private double price;
    private boolean cancelled;

    public ShopBuySellEvent(ShopPlayer player, Shop shop, Type type, int amount, double price) {
        super(player, shop);
        this.type = type;
        this.amount = amount;
        this.price = price;
    }

    /**
     * Gets whether the type of transaction is a buy or a sell
     * 
     * @return the type of transaction
     * @since 1.13
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the amount which might be modified because of automatic item amount calculation
     * 
     * @return the amount
     * @since 1.13
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the price which might be modified because of automatic item amount calculation
     * 
     * @return the price
     * @since 1.13
     */
    public double getPrice() {
        return price;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public enum Type {
        BUY,
        SELL;
    }
}
