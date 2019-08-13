package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player retrieves information about a shop (clicks on a chest)
 */
public class ShopInfoEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;

    public ShopInfoEvent(Player player, Shop shop) {
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
