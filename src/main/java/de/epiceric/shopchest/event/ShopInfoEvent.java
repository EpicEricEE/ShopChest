package de.epiceric.shopchest.event;

import de.epiceric.shopchest.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player retrieves information about a shop (clicks on a chest)
 */
public class ShopInfoEvent extends ShopEvent implements Cancellable {
    private Player player;
    private Shop shop;
    private boolean cancelled;

    public ShopInfoEvent(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    @Override
    public Shop getShop() {
        return shop;
    }

    @Override
    public Player getPlayer() {
        return player;
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
