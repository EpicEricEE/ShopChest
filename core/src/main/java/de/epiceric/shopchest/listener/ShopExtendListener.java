package de.epiceric.shopchest.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopExtendEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.util.Logger;

public class ShopExtendListener implements Listener {
    private final ShopChest plugin;

    private final Set<UUID> hasSentPermisionMessage = new HashSet<>();

    public ShopExtendListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeAction(ShopExtendEvent e) {
        ShopPlayer player = e.getPlayer();
        UUID uuid = player.getBukkitPlayer().getUniqueId();
        if (e.getShop().isAdminShop()) {
            if (!player.hasPermission("shopchest.extend.admin")) {
                player.sendMessage("§cYou don't have permission to extend admin shops."); // TODO: i18n
                hasSentPermisionMessage.add(uuid);
                e.setCancelled(true);
            }
        } else if (!player.isVendor(e.getShop())) {
            if (!player.hasPermission("shopchest.extend.other")) {
                player.sendMessage("§cYou don't have permission to extend this shop."); // TODO: i18n
                hasSentPermisionMessage.add(uuid);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAction(ShopExtendEvent e) {
        UUID uuid = e.getPlayer().getBukkitPlayer().getUniqueId();
        if (e.isCancelled() && !e.getPlayer().hasPermission("shopchest.create.protected")) {
            if (!hasSentPermisionMessage.contains(uuid)) {
                e.getPlayer().sendMessage("§cYou don't have permission to extend this shop here."); // TODO: i18n
            }
            hasSentPermisionMessage.remove(uuid);
            return;
        }

        Shop shop = e.getShop();
        plugin.getShopManager().removeShop(shop)
            .thenCompose((v) -> {
                if (shop.isAdminShop()) {
                    return plugin.getShopManager().addAdminShop(shop.getProduct(),
                            shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice());
                } else {
                    return plugin.getShopManager().addShop(shop.getVendor().get(), shop.getProduct(),
                            shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice());
                }
            })
            .exceptionally(ex -> {
                Logger.severe("Failed to extend shop");
                Logger.severe(ex);
                e.getPlayer().sendMessage("§cFailed to extend shop: {0}", ex.getMessage());
                return null;
            });
    }
}