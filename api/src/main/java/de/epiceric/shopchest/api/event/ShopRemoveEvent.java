package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player enters the command to remove a shop
 * 
 * @since 1.13
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
