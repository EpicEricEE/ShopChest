package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Callback;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class ShopUpdateListener implements Listener {

    private ShopChest plugin;

    public ShopUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        final String worldName = e.getWorld().getName();

        plugin.getShopUtils().reloadShops(false, false, new Callback(plugin) {
            @Override
            public void onResult(Object result) {
                int amount = -1;
                if (result instanceof Integer) {
                    amount = (int) result;
                }
                plugin.getLogger().info(String.format("Reloaded %d shops because a new world '%s' was loaded", amount, worldName));
                plugin.debug(String.format("Reloaded %d shops because a new world '%s' was loaded", amount, worldName));
            }
        });
    }
}
