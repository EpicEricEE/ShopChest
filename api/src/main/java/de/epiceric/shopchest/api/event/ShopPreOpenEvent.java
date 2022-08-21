package de.epiceric.shopchest.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Called when a player enters the command to open a shop
 * 
 * @since 2.0
 */
public class ShopPreOpenEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private ShopPlayer player;
    private boolean cancelled;

    public ShopPreOpenEvent(ShopPlayer player) {
        this.player = player;
    }

    /**
     * Get the player who is involved in this event
     * 
     * @return the player
     * @since 2.0
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