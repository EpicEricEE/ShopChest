package de.epiceric.shopchest.listener;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopPreRemoveEvent;
import de.epiceric.shopchest.api.event.ShopRemoveAllEvent;
import de.epiceric.shopchest.api.event.ShopRemoveEvent;
import de.epiceric.shopchest.api.flag.RemoveFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.util.Logger;

public class ShopRemoveListener implements Listener {
    private final ShopChest plugin;

    public ShopRemoveListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(ShopPreRemoveEvent e) {
        ShopPlayer player = e.getPlayer();
        player.setFlag(new RemoveFlag(plugin));
        player.sendMessage("§aClick a shop within 15 seconds to remove it."); // TODO: 18n
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeAction(ShopRemoveEvent e) {
        ShopPlayer player = e.getPlayer();
        if (e.getShop().isAdminShop()) {
            if (!player.hasPermission("shopchest.remove.admin")) {
                player.sendMessage("§cYou don't have permission to remove admin shops."); // TODO: i18n
                e.setCancelled(true);
            }
        } else if (!player.isVendor(e.getShop())) {
            if (!player.hasPermission("shopchest.remove.other")) {
                player.sendMessage("§cYou don't have permission to remove this shop."); // TODO: i18n
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAction(ShopRemoveEvent e) {
        plugin.getShopManager()
            .removeShop(e.getShop())
            .thenRun(() -> e.getPlayer().sendMessage("§aShop has been removed.")) // TODO: i18n
            .exceptionally(ex -> {
                Logger.severe("Failed to remove shop");
                Logger.severe(ex);
                e.getPlayer().sendMessage("§cFailed to remove shop: {0}", ex.getMessage()); // TODO: i18n
                return null;
            });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeActionAll(ShopRemoveAllEvent e) {
        if (!e.getSender().hasPermission("shopchest.remove.all")) {
            e.setCancelled(true);
            e.getSender().sendMessage("§cYou don't have permission to remove those shops."); // TODO: i18n
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onActionAll(ShopRemoveAllEvent e) {        
        CompletableFuture<?>[] futures = e.getShops().stream()
            .map(shop -> plugin.getShopManager().removeShop(shop))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures)
            .thenRun(() -> {
                int amount = e.getShops().size();
                String shops = amount == 1 ? "shop" : "shops";
        
                e.getSender().sendMessage(MessageFormat.format("§aYou have removed §e{0} {1} §aowned by §e{2}§a.", // TODO: i18n
                        amount, shops, e.getVendor().getName()));
            })
            .exceptionally(ex -> {
                Logger.severe("Failed to remove all shops of {0}", e.getVendor().getName());
                Logger.severe(ex);
                e.getSender().sendMessage(MessageFormat.format("§cFailed to remove shops: {0}", ex.getMessage())); // TODO: i18n
                return null;
            });
    }
}