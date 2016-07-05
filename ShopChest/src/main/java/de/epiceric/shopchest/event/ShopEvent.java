package de.epiceric.shopchest.event;

import de.epiceric.shopchest.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class ShopEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public abstract Shop getShop();

    public abstract Player getPlayer();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
