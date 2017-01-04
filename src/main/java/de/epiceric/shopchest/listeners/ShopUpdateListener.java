package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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

}
