package de.epiceric.shopchest.event;

import de.epiceric.shopchest.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class ShopEditEvent extends ShopEvent implements Cancellable {
    private boolean cancelled;
    private final double newBuyPrice;
    private final double newSellPrice;

    public ShopEditEvent(Player player, Shop shop, double newBuyPrice, double newSellPrice) {
        super(player, shop);
        this.newBuyPrice = newBuyPrice;
        this.newSellPrice = newSellPrice;
    }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { cancelled = cancel; }
}
