package de.epiceric.shopchest.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopEditEvent;
import de.epiceric.shopchest.api.event.ShopPreEditEvent;
import de.epiceric.shopchest.api.event.ShopSelectItemEvent;
import de.epiceric.shopchest.api.flag.EditFlag;
import de.epiceric.shopchest.api.flag.SelectFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.shop.ShopImpl;
import de.epiceric.shopchest.shop.ShopProductImpl;
import de.epiceric.shopchest.util.Logger;

public class ShopEditListener implements Listener {
    private final ShopChest plugin;
    
    public ShopEditListener(ShopChest plugin) {
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
    public void beforeCommand(ShopPreEditEvent e) {
        ShopPlayer player = e.getPlayer();
        double buyPrice = e.getBuyPrice();
        double sellPrice = e.getSellPrice();

        // Check permission normal shop
        if (!player.hasPermission("shopchest.edit")) {
            e.setCancelled(true);
            player.sendMessage("§cYou don't have permission to edit a shop."); // TODO: i18n
            return;
        }

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
            if (buyHigherSell && e.getBuyPrice() > 0 && e.getBuyPrice() < e.getSellPrice()) {
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

        if (e.hasItemStack()) {
            // Check if item is in blacklist, and if minimum/maximum prices are followed
            Material type = e.getItemStack().getType();
            if (!validateItem(player, type, buyPrice, sellPrice)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(ShopPreEditEvent e) {
        ShopPlayer player = e.getPlayer();

        if (!e.hasItemStack() && e.willEditItem()) {
            if (!(player.getFlag().orElse(null) instanceof SelectFlag)) {
                // Set flag only if player doesn't already have SelectFlag
                SelectFlag flag = new SelectFlag(e.getAmount(),
                        e.getBuyPrice(), e.getSellPrice(), SelectFlag.Type.EDIT,
                        player.getBukkitPlayer().getGameMode());

                player.setFlag(flag);
                player.getBukkitPlayer().setGameMode(GameMode.CREATIVE);
                player.sendMessage("§aOpen your inventory and select the item you want to sell or buy."); // TODO: 18n
            }
        } else {
            player.setFlag(new EditFlag(plugin, e.getItemStack(), e.getAmount(), e.getBuyPrice(), e.getSellPrice()));
            player.sendMessage("§aClick a chest within 15 seconds to make the edit."); // TODO: 18n
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

        if (e.isEditingShop()) {
            player.setFlag(new EditFlag(plugin, e.getItem(), e.getAmount(), e.getBuyPrice(), e.getSellPrice()));
            player.sendMessage("§aItem has been selected: §e{0}", product.getLocalizedName()); // TODO: 18n
            player.sendMessage("§aClick a chest within 15 seconds to make the edit."); // TODO: 18n
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeAction(ShopEditEvent e) {
        Shop shop = e.getShop();
        ShopPlayer player = e.getPlayer();

        // Check if either buying or selling is enabled
        if (e.getBuyPrice() == 0 && e.getSellPrice() == 0) {
            player.sendMessage("§cYou cannot have both prices set to zero."); // TODO: i18n
            e.setCancelled(true);
            return;
        }

        // Check if buy price is higher than sell price
        boolean buyHigherSell = Config.FEATURES_VENDOR_MONEY_PROTECTION.get();
        if (buyHigherSell && e.getBuyPrice() > 0 && e.getBuyPrice() < e.getSellPrice()) {
            e.setCancelled(true);
            player.sendMessage("§cThe buy price must at least be as high as the sell price to prevent players from stealing your money."); // TODO: i18n
            return;
        }

        // Check permissions
        if (shop.isAdminShop() && !player.hasPermission("shopchest.edit.admin")) {
            player.sendMessage("§cYou don't have permission to edit an admin shop."); // TODO: i18n
            e.setCancelled(true);
            return;
        }

        if (!player.isVendor(shop) && !player.hasPermission("shopchest.edit.other")) {
            player.sendMessage("§cYou don't have permission to edit this shop."); // TODO: i18n
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAction(ShopEditEvent e) {
        ShopImpl shop = (ShopImpl) e.getShop();
        shop.setBuyPrice(e.getBuyPrice());
        shop.setSellPrice(e.getSellPrice());
        shop.setProduct(new ShopProductImpl(e.getItemStack(), e.getAmount()));

        ((ShopChestImpl) plugin).getDatabase()
            .updateShop(shop)
            .thenRun(() -> e.getPlayer().sendMessage("§aShop has been edited.")) // TODO: i18n
            .exceptionally(ex -> {
                Logger.severe("Failed to save shop edit");
                Logger.severe(ex);
                e.getPlayer().sendMessage("§cFailed to save edit: {0}", ex.getMessage());
                return null;
            });
    }
}