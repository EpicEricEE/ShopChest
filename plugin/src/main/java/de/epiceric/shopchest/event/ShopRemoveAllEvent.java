package de.epiceric.shopchest.event;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.epiceric.shopchest.shop.Shop;

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

    public CommandSender getSender() {
        return sender;
    }

    public OfflinePlayer getVendor() {
        return vendor;
    }

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
