package de.epiceric.shopchest.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.player.ShopPlayerImpl;
import de.epiceric.shopchest.shop.ShopImpl;

public class PlayerJoinQuitListener implements Listener {
    private ShopChest plugin;

    public PlayerJoinQuitListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        // TODO: Make dynamic
        plugin.getShopManager().getShops(e.getPlayer().getWorld())
                .forEach(shop -> ((ShopImpl) shop).getHologram().showPlayer(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        ShopPlayerImpl.unregister(e.getPlayer());
    }

}