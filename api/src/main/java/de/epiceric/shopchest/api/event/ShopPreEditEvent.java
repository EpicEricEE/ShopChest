package de.epiceric.shopchest.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Called when a player enters the command to edit a shop
 * <p>
 * The player may have to select an item first, but may also close the
 * creative inventory to cancel the shop edit.
 * 
 * @see ShopSelectItemEvent
 * @since 2.0
 */
public class ShopPreEditEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ShopPlayer player;
    private ItemStack itemStack;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private boolean editItem;
    private boolean cancelled;

    public ShopPreEditEvent(ShopPlayer player, ItemStack itemStack, int amount, double buyPrice, double sellPrice, boolean editItem) {
        this.player = player;
        this.itemStack = itemStack;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.editItem = editItem;
    }

    /**
     * Gets the player who is involved in this event
     * 
     * @return the player
     * @since 2.0
     */
    public ShopPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the item stack the shop will sell or buy
     * 
     * @return the product or null if it has not been selected (yet)
     * @since 2.0
     */
    public ItemStack getItemStack() {
        return itemStack == null ? null : itemStack.clone();
    }

    /**
     * Gets whether an item has been selected
     * 
     * @return whether an item has been selected
     */
    public boolean hasItemStack() {
        return itemStack != null;
    }

    /**
     * Gets the amount the player has set
     * 
     * @return the amount or -1 if no amount was set
     * @since 2.0
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets whether an amount has been set
     * 
     * @return whether an amount has been set
     */
    public boolean hasAmount() {
        return amount > -1;
    }

    /**
     * Gets the buy price the player has set
     * 
     * @return the buy price or -1 if no buy price was set
     * @since 2.0
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Gets whether a buy price has been set
     * 
     * @return whether a buy price has been set
     */
    public boolean hasBuyPrice() {
        return buyPrice > -1;
    }

    /**
     * Gets the sell price the player has set
     * 
     * @return the sell price or -1 if no sell price was set
     * @since 2.0
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Gets whether a sell price has been set
     * 
     * @return whether a sell price has been set
     */
    public boolean hasSellPrice() {
        return sellPrice > -1;
    }

    /**
     * Gets whether the player will edit the item
     * 
     * @return whether the player will edit the item
     */
    public boolean willEditItem() {
        return editItem;
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
