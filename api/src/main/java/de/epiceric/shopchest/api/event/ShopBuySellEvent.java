package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player buys or sells something from or to a shop
 */
public class ShopBuySellEvent extends ShopEvent implements Cancellable {
    private Type type;
    private int amount;
    private double price;
    private boolean cancelled;

    public ShopBuySellEvent(Player player, Shop shop, Type type, int amount, double price) {
        super(player, shop);
        this.type = type;
        this.amount = amount;
        this.price = price;
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
    public int getAmount() {
        return amount;
    }

    /**
     * @return The price which might be modified because of automatic item amount calculation
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
