package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.event.Cancellable;

/**
 * Called when a player clicks a shop to open it
 * 
 * @since 2.0
 */
public class ShopOpenEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;

    public ShopOpenEvent(ShopPlayer player, Shop shop) {
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