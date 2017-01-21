package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopUpdateEvent;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ShopUpdateListener implements Listener {

    private ShopChest plugin;

    public ShopUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopUpdate(ShopUpdateEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getShopUtils().updateShops(p, p.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (Shop shop : plugin.getShopUtils().getShops()) {
            shop.getHologram().hidePlayer(e.getPlayer());
            shop.getItem().setVisible(e.getPlayer(), false);
        }
    }

}
