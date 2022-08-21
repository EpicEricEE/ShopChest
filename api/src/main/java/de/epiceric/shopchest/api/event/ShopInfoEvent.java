package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.event.Cancellable;

/**
 * Called when a player clicks a shop to retrieve information about it
 * 
 * @since 2.0
 */
public class ShopInfoEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;

    public ShopInfoEvent(ShopPlayer player, Shop shop) {
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
