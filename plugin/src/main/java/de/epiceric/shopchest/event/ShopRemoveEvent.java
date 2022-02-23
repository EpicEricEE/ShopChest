package de.epiceric.shopchest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import de.epiceric.shopchest.shop.Shop;

/**
 * Called when a player removes a shop (clicks on a chest)
 */
public class ShopRemoveEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;

    public ShopRemoveEvent(Player player, Shop shop) {
        super(player, shop);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
