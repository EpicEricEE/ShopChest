package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player opens a shop (clicks on a chest)
 */
public class ShopOpenEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;

    public ShopOpenEvent(Player player, Shop shop) {
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
