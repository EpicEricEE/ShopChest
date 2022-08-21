package de.epiceric.shopchest.api.event;

import java.util.Optional;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;

/**
 * Called when a player clicks a chest to edit a shop
 * 
 * @see ShopSelectItemEvent
 * @since 2.0
 */
public class ShopEditEvent extends ShopEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ItemStack itemStack;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private boolean cancelled;

    public ShopEditEvent(ShopPlayer player, Shop shop, ItemStack itemStack, int amount, double buyPrice, double sellPrice) {
        super(player, shop);
        this.itemStack = itemStack;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    /**
     * Gets the new item stack the shop will sell or buy
     * 
     * @return the new product, or the old product if it has not been set
     * @since 2.0
     */
    public ItemStack getItemStack() {
        return Optional.ofNullable(itemStack).orElse(getShop().getProduct().getItemStack());
    }

    /**
     * Gets the new amount of items the shop will sell or buy
     * 
     * @return the new amount, or the old amount if it has not been set
     * @since 2.0
     */
    public int getAmount() {
        return amount == -1 ? getShop().getProduct().getAmount() : amount;
    }

    /**
     * Gets the new price for which players will be able to buy from the shop
     * 
     * @return the new buy price, or the old buy price if it has not been set
     * @since 2.0
     */
    public double getBuyPrice() {
        return buyPrice == -1 ? getShop().getBuyPrice() : buyPrice;
    }

    /**
     * Gets the new price for which players will be able to sell to the shop
     * 
     * @return the new sell price, or the old sell price if it has not been set
     * @since 2.0
     */
    public double getSellPrice() {
        return sellPrice == -1 ? getShop().getSellPrice() : sellPrice;
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
