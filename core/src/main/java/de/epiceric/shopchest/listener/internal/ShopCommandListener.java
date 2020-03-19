package de.epiceric.shopchest.listener.internal;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopPreCreateEvent;
import de.epiceric.shopchest.api.event.ShopPreEditEvent;
import de.epiceric.shopchest.api.event.ShopSelectItemEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class ShopCommandListener implements Listener {
    private final ShopChest plugin;

    public ShopCommandListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    private boolean isInt(double d) {
        return d % 1 == 0;
    }

    private boolean isItemAllowed(ShopPlayer player, Material type, double buyPrice, double sellPrice) {
        // Check if item is blacklisted
        if (Config.SHOP_CREATION_BLACKLIST.get().getList().contains(type)) {
            player.sendMessage("§cYou cannot create a shop with this item."); // TODO: i18n
            return false;
        }

        // Check if minimum price is followed
        double minPrice = Config.SHOP_CREATION_MINIMUM_PRICES.get().getMap().getOrDefault(type, 0d);
        if (buyPrice > 0 && buyPrice < minPrice) {
            player.sendMessage("§cThe buy price must be higher than " + plugin.formatEconomy(minPrice) + "."); // TODO: i18n
            return false;
        } else if (sellPrice > 0 && sellPrice < minPrice) {
            player.sendMessage("§cThe sell price must be higher than " + plugin.formatEconomy(minPrice) + "."); // TODO: i18n
            return false;
        }

        // Check if maximum price is followed
        double maxPrice = Config.SHOP_CREATION_MAXIMUM_PRICES.get().getMap().getOrDefault(type, Double.MAX_VALUE);
        if (buyPrice > 0 && buyPrice > maxPrice) {
            player.sendMessage("§cThe buy price must be lower than " + plugin.formatEconomy(maxPrice) + "."); // TODO: i18n
            return false;
        } else if (sellPrice > 0 && sellPrice > maxPrice) {
            player.sendMessage("§cThe sell price must be lower than " + plugin.formatEconomy(maxPrice) + "."); // TODO: i18n
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSelectItem(ShopSelectItemEvent e) {
        if (!isItemAllowed(e.getPlayer(), e.getItem().getType(), e.getBuyPrice(), e.getSellPrice())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopPreCreate(ShopPreCreateEvent e) {
        ShopPlayer player = e.getPlayer();
        double buyPrice = e.getBuyPrice();
        double sellPrice = e.getSellPrice();

        // Check permission admin shop
        if (e.isAdminShop() && !player.hasPermission("shopchest.create.admin")) {
            e.setCancelled(true);
            player.sendMessage("§cYou don't have permission to create an admin shop."); // TODO: i18n
            return;
        }

        // TODO: buy/sell and item based permission

        // Check permission normal shop
        if (!e.isAdminShop() && !player.hasPermission("shopchest.create")) {
            e.setCancelled(true);
            player.sendMessage("§cYou don't have permission to create a shop."); // TODO: i18n
            return;
        }

        // Check shop limit
        if (player.getShopAmount() >= player.getShopLimit()) {
            e.setCancelled(true);
            player.sendMessage("§cYou don't have permission to create any more shops."); // TODO: i18n
            return;
        }

        // Check if either buying or selling is enabled
        if (buyPrice <= 0 && sellPrice <= 0) {
            e.setCancelled(true);
            player.sendMessage("§cYou cannot have both prices set to zero."); // TODO: i18n
            return;
        }

        // Check if prices are integers
        boolean allowDecimals = Config.SHOP_CREATION_ALLOW_DECIMAL_PRICES.get();
        if (!allowDecimals && (!isInt(buyPrice) || !isInt(buyPrice))) {
            e.setCancelled(true);
            player.sendMessage("§cThe prices must not contain decimals."); // TODO: i18n
            return;
        }

        // Check if buy price is higher than sell price
        boolean buyHigherSell = Config.FEATURES_VENDOR_MONEY_PROTECTION.get();
        if (buyHigherSell && buyPrice > 0 && buyPrice < sellPrice) {
            e.setCancelled(true);
            player.sendMessage("§cThe buy price must at least be as high as the sell price to prevent players from stealing your money."); // TODO: i18n
            return;
        }

        if (e.isItemSelected() && !isItemAllowed(player, e.getItemStack().getType(), buyPrice, sellPrice)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopPreEdit(ShopPreEditEvent e) {
        ShopPlayer player = e.getPlayer();

        // TODO: buy/sell and item based permission
        
        if (e.hasBuyPrice() && e.hasSellPrice()) {
            // Check if either buying or selling is enabled
            if (e.getBuyPrice() == 0 && e.getSellPrice() == 0) {
                e.setCancelled(true);
                player.sendMessage("§cYou cannot have both prices set to zero."); // TODO: i18n
                return;
            }

            // Check if buy price is higher than sell price
            boolean buyHigherSell = Config.FEATURES_VENDOR_MONEY_PROTECTION.get();
            if (buyHigherSell && e.getBuyPrice() < e.getSellPrice()) {
                e.setCancelled(true);
                player.sendMessage("§cThe buy price must at least be as high as the sell price to prevent players from stealing your money."); // TODO: i18n
                return;
            }
        }

        // Check if prices are integers
        if (!Config.SHOP_CREATION_ALLOW_DECIMAL_PRICES.get()) {
            boolean isBuyPriceInt = e.hasBuyPrice() && isInt(e.getBuyPrice());
            boolean isSellPriceInt = e.hasSellPrice() && isInt(e.getSellPrice());
            if (!isBuyPriceInt || !isSellPriceInt) {
                e.setCancelled(true);
                player.sendMessage("§cThe prices must not contain decimals."); // TODO: i18n
                return;
            }
        }

        // Check blacklist, minimum and maximum prices if item is set
        if (e.hasItemStack() && !isItemAllowed(player, e.getItemStack().getType(), e.getBuyPrice(), e.getSellPrice())) {
            e.setCancelled(true);
        }
    }
}