package de.epiceric.shopchest.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Called when a player enters the command to retrieve information about a shop
 * 
 * @since 1.13
 */
public class ShopPreInfoEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ShopPlayer player;
    private boolean cancelled;

    public ShopPreInfoEvent(ShopPlayer player) {
        this.player = player;
    }

    /**
     * Gets the player who is involved in this event
     * 
     * @return the player
     * @since 1.13
     */
    public ShopPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
