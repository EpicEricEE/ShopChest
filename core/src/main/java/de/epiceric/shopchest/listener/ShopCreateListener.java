package de.epiceric.shopchest.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopCreateEvent;
import de.epiceric.shopchest.api.event.ShopPreCreateEvent;
import de.epiceric.shopchest.api.event.ShopSelectItemEvent;
import de.epiceric.shopchest.api.flag.CreateFlag;
import de.epiceric.shopchest.api.flag.SelectFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.shop.ShopProductImpl;
import de.epiceric.shopchest.util.Logger;

public class ShopCreateListener implements Listener {
    private final ShopChest plugin;
    
    public ShopCreateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    private boolean isInt(double d) {
        return d % 1 == 0;
    }

    private boolean validateItem(ShopPlayer player, Material type, double buyPrice, double sellPrice) {
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
    public void beforeCommand(ShopPreCreateEvent e) {
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

        if (e.isItemSelected()) {
            // Check if item is in blacklist, and if minimum/maximum prices are followed
            Material type = e.getItemStack().getType();
            if (!validateItem(player, type, buyPrice, sellPrice)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(ShopPreCreateEvent e) {
        ShopPlayer player = e.getPlayer();

        if (!e.isItemSelected()) {
            if (!(player.getFlag().orElse(null) instanceof SelectFlag)) {
                // Set flag only if player doesn't already have SelectFlag
                SelectFlag.Type type = e.isAdminShop() ? SelectFlag.Type.ADMIN : SelectFlag.Type.NORMAL;
                SelectFlag flag = new SelectFlag(e.getAmount(),
                        e.getBuyPrice(), e.getSellPrice(), type,
                        player.getBukkitPlayer().getGameMode());

                player.setFlag(flag);
                player.getBukkitPlayer().setGameMode(GameMode.CREATIVE);
                player.sendMessage("§aOpen your inventory and select the item you want to sell or buy."); // TODO: 18n
            }
        } else {
            ShopProduct product = new ShopProductImpl(e.getItemStack(), e.getAmount());
            player.setFlag(new CreateFlag(plugin, product, e.getBuyPrice(), e.getSellPrice(), e.isAdminShop()));
            player.sendMessage("§aClick a chest within 15 seconds to create a shop."); // TODO: 18n
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeSelect(ShopSelectItemEvent e) {
        if (!validateItem(e.getPlayer(), e.getItem().getType(), e.getBuyPrice(), e.getSellPrice())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSelect(ShopSelectItemEvent e) {
        ShopProduct product = new ShopProductImpl(e.getItem(), e.getAmount());
        ShopPlayer player = e.getPlayer();

        if (!e.isEditingShop()) {
            player.setFlag(new CreateFlag(plugin, product, e.getBuyPrice(), e.getSellPrice(), e.isAdminShop()));
            player.sendMessage("§aItem has been selected: §e{0}", product.getLocalizedName()); // TODO: 18n
            player.sendMessage("§aClick a chest within 15 seconds to create a shop."); // TODO: 18n
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAction(ShopCreateEvent e) {
        if (e.isCancelled() && !e.getPlayer().hasPermission("shopchest.create.protected")) {
            e.getPlayer().sendMessage("§cYou don't have permission to create a shop here."); // TODO: i18n
            return;
        }

        Shop shop = e.getShop();
        if (shop.isAdminShop()) {
            plugin.getShopManager()
                .addAdminShop(shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice())
                .thenAccept(newShop -> e.getPlayer().sendMessage("§aAdmin shop has been added with ID {0}.", newShop.getId())) // TODO: i18n
                .exceptionally(ex -> {
                    Logger.severe("Failed to add admin shop");
                    Logger.severe(ex);
                    e.getPlayer().sendMessage("§cFailed to add admin shop: {0}", ex.getMessage()); // TODO: i18n
                    return null;
                });
        } else {
            plugin.getShopManager()
                .addShop(shop.getVendor().get(), shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice())
                .thenAccept(newShop -> e.getPlayer().sendMessage("§aShop has been added with ID {0}.", newShop.getId())) // TODO: i18n
                .exceptionally(ex -> {
                    Logger.severe("Failed to add shop");
                    Logger.severe(ex);
                    e.getPlayer().sendMessage("§cFailed to add shop: {0}", ex.getMessage()); // TODO: i18n
                    return null;
                });
        }
    }
}