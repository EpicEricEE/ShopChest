package de.epiceric.shopchest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player reloads the shops
 */
public class ShopReloadEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private boolean cancelled;

    public ShopReloadEvent(Player player) {
        this.player = player;
    }

    /**
     * @return Player who is involved in this event
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
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
