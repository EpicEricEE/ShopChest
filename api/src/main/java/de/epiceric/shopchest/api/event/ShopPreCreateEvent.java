package de.epiceric.shopchest.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Called when a player enters the command to create a shop
 * <p>
 * The player may have to select an item first, but may also close the
 * creative inventory to cancel shop creation.
 * 
 * @see ShopSelectItemEvent
 * @since 1.13
 */
public class ShopPreCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ShopPlayer player;
    private ItemStack itemStack;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private boolean admin;
    private boolean cancelled;

    public ShopPreCreateEvent(ShopPlayer player, ItemStack itemStack, int amount, double buyPrice, double sellPrice, boolean admin) {
        this.player = player;
        this.itemStack = itemStack;
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
    public ShopPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the item stack the shop will sell or buy
     * 
     * @return the product or {@code null} if it has not been selected
     * @since 1.13
     */
    public ItemStack getItemStack() {
        return itemStack == null ? null : itemStack.clone();
    }

    /**
     * Gets the amount of items the shop will sell or buy
     * 
     * @return the amount
     * @since 1.13
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets whether the item has already been selected
     * 
     * @return whether the item has been selected
     * @since 1.13
     */
    public boolean isItemSelected() {
        return getItemStack() != null;
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
