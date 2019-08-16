package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Called when a player extends a shop's chest to a double chest
 * 
 * @since 1.13
 */
public class ShopExtendEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;
    private Location newChestLocation;

    public ShopExtendEvent(Player player, Shop shop, Location newChest) {
        super(player, shop);
        this.newChestLocation = newChest;
    }

    /**
     * Gets the location of the placed chest
     * 
     * @return the location of the placed chest
     * @since 1.13
     */
    public Location getNewChestLocation() {
        return newChestLocation;
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