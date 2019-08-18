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
     * Gets the amount which might be modified because of automatic item amount
     * calculation
     * 
     * @return the amount
     * @since 1.13
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of items the player will buy or sell
     * <p>
     * This might not be equal to the amount specified in {@link Shop#getProduct()}.
     * 
     * @param amount the amount
     * @since 1.13
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the price the player and the vendor of the shop will pay or receive
     * <p>
     * This might not be equal to {@link Shop#getBuyPrice()} or
     * {@link Shop#getSellPrice()}.
     * 
     * @return the price
     * @since 1.13
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the amount of money the player and the vendor of the shop will pay or
     * receive
     * 
     * @param price the price
     * @since 1.13
     */
    public void setPrice(double price) {
        this.price = price;
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
        BUY, SELL;
    }
}
