package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.ShopProduct;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player wants to create a shop (enters the command)
 */
public class ShopPreCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private ShopProduct product;
    private double buyPrice;
    private double sellPrice;
    private boolean admin;
    private boolean cancelled;

    public ShopPreCreateEvent(Player player, ShopProduct product, double buyPrice, double sellPrice, boolean admin) {
        this.player = player;
        this.product = product;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.admin = admin;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the product
     */
    public ShopProduct getProduct() {
        return product;
    }

    /**
     * @return the buyPrice
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * @return the sellPrice
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * @return the admin
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
