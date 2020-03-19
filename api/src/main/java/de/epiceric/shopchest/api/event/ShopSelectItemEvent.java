package de.epiceric.shopchest.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.flag.SelectFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Called when a player has selected an item
 * 
 * @since 1.13
 */
public class ShopSelectItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ShopPlayer player;
    private ItemStack item;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private SelectFlag.Type type;
    private boolean cancelled;

    public ShopSelectItemEvent(ShopPlayer player, ItemStack item, int amount, double buyPrice, double sellPrice, SelectFlag.Type type) {
        this.player = player;
        this.item = item == null ? null : item.clone();
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.type = type;
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
     * @see #isNormalShop()
     * @see #isEditingShop()
     * @since 1.13
     */
    public boolean isAdminShop() {
        return type == SelectFlag.Type.ADMIN;
    }

    /**
     * Gets whether the shop will be a normal shop
     * 
     * @return whether the shop will be a normal shop
     * @see #isAdminShop()
     * @see #isEditingShop()
     * @since 1.13
     */
    public boolean isNormalShop() {
        return type == SelectFlag.Type.NORMAL;
    }

    /**
     * Gets whether the shop is being edited
     * 
     * @return whether the shop is being edited
     * @see #isAdminShop()
     * @see #isNormalShop()
     * @since 1.13
     */
    public boolean isEditingShop() {
        return type == SelectFlag.Type.EDIT;
    }

    /**
     * Gets either the shop type of the shop being created, or whether the shop
     * is being edited
     * 
     * @return the shop type or {@link Type#EDIT}
     */
    public SelectFlag.Type getType() {
        return type;
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
