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
     * @return Sender, who caused the reload ({@link org.bukkit.entity.Player} or {@link org.bukkit.command.ConsoleCommandSender})
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
