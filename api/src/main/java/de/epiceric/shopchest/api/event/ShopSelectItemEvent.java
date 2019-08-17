package de.epiceric.shopchest.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a player has selected an item
 * 
 * @since 1.13
 */
public class ShopSelectItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private ItemStack item;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private boolean admin;
    private boolean cancelled;

    public ShopSelectItemEvent(Player player, ItemStack item, int amount, double buyPrice, double sellPrice, boolean admin) {
        this.player = player;
        this.item = item == null ? null : item.clone();
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.admin = admin;
    }

    /**
     * Gets the player who is involved in this event
     * 
     * @return the player
     * @since 1.13
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item the player has selected
     * 
     * @return the item
     * @since 1.13
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Sets the item
     * <p>
     * If {@code item} is {@code null}, the event will be cancelled.
     * 
     * @param item the item
     * @since 1.13
     */
    public void setItem(ItemStack item) {
        if (item == null) {
            setCancelled(true);
            this.item = null;
        } else {
            this.item = item.clone();
        }
    }

    /**
     * Gets the amount of items the player wants to sell or buy at the shop
     * 
     * @return the amount
     * @since 1.13
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the price for which players will be able to buy from the shop
     * 
     * @return the buy price
     * @since 1.13
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Gets the price for which players will be able to sell to the shop
     * 
     * @return the sell price
     * @since 1.13
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Gets whether the shop will be an admin shop
     * 
     * @return whether the shop will be an admin shop
     * @since 1.13
     */
    public boolean isAdminShop() {
        return admin;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
