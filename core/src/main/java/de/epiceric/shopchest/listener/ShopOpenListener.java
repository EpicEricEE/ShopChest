package de.epiceric.shopchest.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopOpenEvent;
import de.epiceric.shopchest.api.event.ShopPreOpenEvent;
import de.epiceric.shopchest.api.flag.OpenFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class ShopOpenListener implements Listener {
    private final ShopChest plugin;

    public ShopOpenListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeCommand(ShopPreOpenEvent e) {
        if (!e.getPlayer().hasPermission("shopchest.open.other")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou don't have permission to open this shop."); // TODO: i18n
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(ShopPreOpenEvent e) {
        ShopPlayer player = e.getPlayer();
        player.setFlag(new OpenFlag(plugin));
        player.sendMessage("§aClick a shop within 15 seconds to open it."); // TODO: i18n
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeAction(ShopOpenEvent e) {
        if (e.getShop().isAdminShop()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou cannot open an admin shop."); // TODO: i18n
        }
    }
}