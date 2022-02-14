package de.epiceric.shopchest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.shop.Shop;

public abstract class ShopEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Shop shop;
    private Player player;

    public ShopEvent(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    /**
     * @return Shop which is involved in this event
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * @return Player who is involved in this event
     */
    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
