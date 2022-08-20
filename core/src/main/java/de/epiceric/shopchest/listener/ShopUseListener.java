package de.epiceric.shopchest.listener;

import java.text.MessageFormat;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopUseEvent;
import de.epiceric.shopchest.api.event.ShopUseEvent.Type;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.util.ItemUtil;
import de.epiceric.shopchest.util.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class ShopUseListener implements Listener {
    private static final class InventoryData {
        private final int amount;
        private final int freeSpace;

        private InventoryData(int amount, int freeSpace) {
            this.amount = amount;
            this.freeSpace = freeSpace;
        }
    }

    private final ShopChest plugin;

    public ShopUseListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    private void sendVendorMessage(Optional<OfflinePlayer> vendor, String message, Object... args) {
        if (Config.FEATURES_VENDOR_MESSAGES.get()) {
            vendor.filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .ifPresent(p -> p.sendMessage(MessageFormat.format(message, args)));
        }
    }

    private void checkShiftClick(Shop shop, Player player, ShopUseEvent e) {
        int amount = e.getAmount();
        double price = e.getPrice();

        if (player.isSneaking()) {
            int newAmount = shop.getProduct().getItemStack().getMaxStackSize();
            double newPrice = price * newAmount / amount;
            if (!Config.SHOP_CREATION_ALLOW_DECIMAL_PRICES.get()) {
                newPrice = e.getType() == Type.BUY ? Math.ceil(newPrice) : Math.floor(newPrice);
            }
            e.setPrice(newPrice);
            e.setAmount(newAmount);
        }
    }

    private InventoryData getInventoryData(Inventory inventory, ItemStack itemStack) {
        int amount = 0;
        int freeSpace = 0;
        for (ItemStack content : inventory.getStorageContents()) {
            if (ItemUtil.isEqual(content, itemStack)) {
                amount += content.getAmount();
                freeSpace += content.getMaxStackSize() - content.getAmount();
            } else if (content == null || content.getType() == Material.AIR) {
                freeSpace += itemStack.getMaxStackSize();;
            }
        }
        return new InventoryData(amount, freeSpace);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void beforeAction(ShopUseEvent e) {
        Shop shop = e.getShop();
        ShopProduct product = shop.getProduct();
        ShopPlayer player = e.getPlayer();
        Player bukkitPlayer = player.getBukkitPlayer();
        Economy economy = ((ShopChestImpl) plugin).getEconomy();

        if (!player.hasPermission("shopchest.use")) {
            player.sendMessage("§cYou don't have permission to use a shop.");
            e.setCancelled(true);
            return;
        }

        InventoryData playerData = getInventoryData(bukkitPlayer.getInventory(), product.getItemStack());
        InventoryData chestData;
        try {
            chestData = getInventoryData(shop.getInventory(), product.getItemStack());
        } catch (ChestNotFoundException ex) {
            Logger.severe("A non-existent chest has been clicked?!");
            Logger.severe(ex);
            player.sendMessage("§cThe shop chest you clicked does not seem to exist"); // TODO: i18n
            e.setCancelled(true);
            return;
        }

        checkShiftClick(shop, bukkitPlayer, e);

        // TODO: auto adjust item amount
        
        // Check money, space and available items
        if (e.getType() == Type.BUY) {
            if (playerData.freeSpace < e.getAmount()) {
                player.sendMessage("§cYou don't have enough space in your inventory."); // TODO: i18n
                e.setCancelled(true);
                return;
            }
            if (!economy.has(bukkitPlayer, shop.getWorld().getName(), e.getPrice())) {
                player.sendMessage("§cYou don't have enough money."); // TODO: i18n
                e.setCancelled(true);
                return;
            }
            if (!shop.isAdminShop()) {
                if (chestData.amount < e.getAmount()) {
                    player.sendMessage("§cThis shop is out of items to sell."); // TODO: i18n
                    sendVendorMessage(shop.getVendor(), "§cYour shop selling §e{0} x {1} §cis out of stick.", // TODO: i18n
                            product.getAmount(), product.getLocalizedName());
                    e.setCancelled(true);
                    return;
                }
            }
        } else if (e.getType() == Type.SELL) {
            if (playerData.amount < e.getAmount()) {
                player.sendMessage("§cYou don't have enough items to sell."); // TODO: i18n
                e.setCancelled(true);
                return;
            }
            if (!shop.isAdminShop()) {
                if (!economy.has(shop.getVendor().get(), shop.getWorld().getName(), e.getPrice())) {
                    player.sendMessage("§cThe vendor of this shop doesn't have enough money."); // TODO: i18n
                    e.setCancelled(true);
                    return;
                }
                if (chestData.freeSpace < e.getAmount()) {
                    player.sendMessage("§cThis shop doesn't have enough space for your items."); // TODO: i18n
                    sendVendorMessage(shop.getVendor(), "§cYour shop buying §e{0} x {1} §cis full.", // TODO: i18n
                            product.getAmount(), product.getLocalizedName());
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAction(ShopUseEvent e) {
        if (e.isCancelled() && !e.getPlayer().hasPermission("shopchest.use.protected")) {
            e.getPlayer().sendMessage("§cYou don't have permission to use this shop.");
            return;
        }

        // Money transaction in highest priority
        Shop shop = e.getShop();
        Economy economy = ((ShopChestImpl) plugin).getEconomy();
        Player bukkitPlayer = e.getPlayer().getBukkitPlayer();
        String worldName = e.getShop().getWorld().getName();

        if (e.getType() == Type.BUY) {
            EconomyResponse rPlayer = economy.withdrawPlayer(bukkitPlayer, worldName, e.getPrice());
            if (!rPlayer.transactionSuccess()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cFailed to withdraw money: {0}", rPlayer.errorMessage); // TODO: i18n
                return;
            }

            shop.getVendor().ifPresent(vendor -> {
                EconomyResponse rVendor = economy.depositPlayer(vendor, worldName, e.getPrice());
                if (!rVendor.transactionSuccess()) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cFailed to deposit money to vendor: {0}", rVendor.errorMessage); // TODO: i18n

                    EconomyResponse rBack = economy.depositPlayer(bukkitPlayer, worldName, e.getPrice());
                    if (!rBack.transactionSuccess()) {
                        e.getPlayer().sendMessage("§cFailed to reverse your withdrawal: {0}", rBack.errorMessage); // TODO: i18n
                    }
                    return;
                }
            });
        } else if (e.getType() == Type.SELL) {
            EconomyResponse rPlayer = economy.depositPlayer(bukkitPlayer, worldName, e.getPrice());
            if (!rPlayer.transactionSuccess()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cFailed to deposit money: {0}", rPlayer.errorMessage); // TODO: i18n
                return;
            }

            shop.getVendor().ifPresent(vendor -> {
                EconomyResponse rVendor = economy.withdrawPlayer(vendor, worldName, e.getPrice());
                if (!rVendor.transactionSuccess()) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cFailed to withdraw money from vendor: {0}", rVendor.errorMessage); // TODO: i18n

                    EconomyResponse rBack = economy.withdrawPlayer(bukkitPlayer, worldName, e.getPrice());
                    if (!rBack.transactionSuccess()) {
                        e.getPlayer().sendMessage("§cFailed to reverse your deposit: {0}", rBack.errorMessage); // TODO: i18n
                    }
                    return;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAction(ShopUseEvent e) {
        Shop shop = e.getShop();
        ShopPlayer player = e.getPlayer();
        ShopProduct product = shop.getProduct();
        Player bukkitPlayer = player.getBukkitPlayer();

        try {
            if (e.getType() == Type.BUY) {
                for (int i = 0; i < e.getAmount(); i++) {
                    bukkitPlayer.getInventory().addItem(product.getItemStack());
                    if (!shop.isAdminShop()) {
                        shop.getInventory().removeItem(product.getItemStack());
                    }
                }

                player.sendMessage("§aYou bought §e{0} x {1} §afor §e{2}.", e.getAmount(),
                        product.getLocalizedName(), plugin.formatEconomy(e.getPrice()));
            } else {
                for (int i = 0; i < e.getAmount(); i++) {
                    bukkitPlayer.getInventory().removeItem(product.getItemStack());
                    if (!shop.isAdminShop()) {
                        shop.getInventory().addItem(product.getItemStack());
                    }
                }
                
                player.sendMessage("§aYou sold §e{0} x {1} §afor §e{2}.", e.getAmount(),
                        product.getLocalizedName(), plugin.formatEconomy(e.getPrice()));
            }
        } catch (ChestNotFoundException ex) {
            Logger.severe("A non-existent chest has been clicked?!");
            Logger.severe(ex);
            player.sendMessage("§cThe shop chest you clicked does not seem to exist"); // TODO: i18n
        }
    }
}