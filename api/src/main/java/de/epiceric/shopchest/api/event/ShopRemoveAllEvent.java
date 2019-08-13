package de.epiceric.shopchest.api.event;

import de.epiceric.shopchest.api.shop.Shop;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Called when a player enters the command to remove all shops of a player
 * 
 * @since 1.13
 */
public class ShopRemoveAllEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private CommandSender sender;
    private OfflinePlayer vendor;
    private List<Shop> shops;
    private boolean cancelled;

    public ShopRemoveAllEvent(CommandSender sender, OfflinePlayer vendor, List<Shop> shops) {
        this.sender = sender;
        this.vendor = vendor;
        this.shops = shops;
    }

    /**
     * Gets the sender that entered the reload command
     * 
     * @return the sender
     * @since 1.13
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the player whose shops will be removed
     * 
     * @return the vendor
     * @since 1.13
     */
    public OfflinePlayer getVendor() {
        return vendor;
    }

    /**
     * Gets the shops that will be removed
     * <p>
     * This list can be modified to include or exclude certain shops.
     * 
     * @return a modifiable list of shops
     * @since 1.13
     */
    public List<Shop> getShops() {
        return shops;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
