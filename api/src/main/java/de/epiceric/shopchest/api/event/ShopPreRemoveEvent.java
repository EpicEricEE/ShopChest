package de.epiceric.shopchest.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player enters the command to remove a shop
 * 
 * @since 1.13
 */
public class ShopPreRemoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private boolean cancelled;

    public ShopPreRemoveEvent(Player player) {
        this.player = player;
    }

    /**
     * Gets the player who is involved in this event
     * 
     * @return the player
     * @since 1.13
     */
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
