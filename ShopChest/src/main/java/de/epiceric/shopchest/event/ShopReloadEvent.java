package de.epiceric.shopchest.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player reloads the shops
 */
public class ShopReloadEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private CommandSender sender;
    private boolean cancelled;

    public ShopReloadEvent(CommandSender sender) {
        this.sender = sender;
    }

    /**
     * @return Player who is involved in this event
     */
    public CommandSender getSender() {
        return sender;
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
