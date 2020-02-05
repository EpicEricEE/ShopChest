package de.epiceric.shopchest.event;

import de.epiceric.shopchest.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player buys or sells something from or to a shop
 */
public class ShopBuySellEvent extends ShopEvent implements Cancellable {
    private Type type;
    private int newAmount;
    private double newPrice;
    private double newTaxedPrice;
    private boolean cancelled;

    public ShopBuySellEvent(Player player, Shop shop, Type type, int newAmount, double newPrice, double newTaxedPrice) {
        super(player, shop);
        this.type = type;
        this.newAmount = newAmount;
        this.newPrice = newPrice;
        this.newTaxedPrice = newTaxedPrice;
    }

    /**
     * @return Whether the player buys or sells something
     */
    public Type getType() {
        return type;
    }

    /**
     * @return The amount which might be modified because of automatic item amount calculation
     */
    public int getNewAmount() {
        return newAmount;
    }

    /**
     * @return The price which might be modified because of automatic item amount calculation
     */
    public double getNewPrice() {
        return newPrice;
    }

    /**
     * @return The taxed price which might be modified because of automatic item amount calculation
     */
    public double getNewTaxedPrice() {
        return newTaxedPrice;
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
