package de.epiceric.shopchest.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import de.epiceric.shopchest.shop.Shop;

/**
 * Called when a player extends a shop (making a chest a double chest)
 */
public class ShopExtendEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;
    private Location newChestLocation;

    public ShopExtendEvent(Player player, Shop shop, Location newChest) {
        super(player, shop);
        this.newChestLocation = newChest;
    }

    /**
     * @return Location of the placed chest
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
