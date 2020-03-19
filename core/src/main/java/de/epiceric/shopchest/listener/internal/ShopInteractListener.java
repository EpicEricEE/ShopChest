package de.epiceric.shopchest.listener.internal;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopBuySellEvent;
import de.epiceric.shopchest.api.event.ShopCreateEvent;
import de.epiceric.shopchest.api.event.ShopExtendEvent;
import de.epiceric.shopchest.api.event.ShopInfoEvent;
import de.epiceric.shopchest.api.event.ShopOpenEvent;
import de.epiceric.shopchest.api.event.ShopRemoveAllEvent;
import de.epiceric.shopchest.api.event.ShopRemoveEvent;
import de.epiceric.shopchest.api.event.ShopBuySellEvent.Type;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.util.ItemUtil;
import net.milkbowl.vault.economy.Economy;

public class ShopInteractListener implements Listener {
    private final ShopChest plugin;

    public ShopInteractListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopCreate(ShopCreateEvent e) {
        Chest chest = (Chest) e.getShop().getLocation().getBlock().getBlockData();

        if (e.getShop().getWorld().getTime() < 12000 && chest.getFacing() == BlockFace.WEST) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou cannot create a shop facing west before 12pm."); // TODO: Remove
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopRemove(ShopRemoveEvent e) {
        Shop shop = e.getShop();
        ShopPlayer player = e.getPlayer();

        if (shop.isAdminShop() && !player.hasPermission("shopchest.remove.admin")) {
            player.sendMessage("§cYou don't have permission to remove an admin shop."); // TODO: i18n
            e.setCancelled(true);
            return;
        }

        if (!player.ownsShop(shop) && !player.hasPermission("shop.remove.other")) {
            player.sendMessage("§cYou don't have permission to remove this shop."); // TODO: i18n
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopInfo(ShopInfoEvent e) {
        // No reason to deny a shop info request
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopOpen(ShopOpenEvent e) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopExtend(ShopExtendEvent e) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopRemoveAll(ShopRemoveAllEvent e) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShopBuySell(ShopBuySellEvent e) {
        Shop shop = e.getShop();
        ShopProduct product = shop.getProduct();
        ShopPlayer player = e.getPlayer();
        Player bukkitPlayer = player.getBukkitPlayer();
        Economy economy = ((ShopChestImpl) plugin).getEconomy();

        if (bukkitPlayer.isSneaking()) {
            // Shift click to buy/sell a stack
            int maxStackSize = product.getItemStack().getMaxStackSize();
            double newPrice = maxStackSize * e.getPrice() / e.getAmount();
            if (!Config.SHOP_CREATION_ALLOW_DECIMAL_PRICES.get()) {
                newPrice = e.getType() == Type.BUY ? Math.ceil(newPrice) : Math.floor(newPrice);
            }
            e.setPrice(newPrice);
            e.setAmount(maxStackSize);
        }

        int playerAmount = 0;
        int playerSpace = 0;
        for (ItemStack content : bukkitPlayer.getInventory().getStorageContents()) {
            if (ItemUtil.isEqual(content, product.getItemStack())) {
                playerAmount += content.getAmount();
                playerSpace += content.getMaxStackSize() - content.getAmount();
            } else if (content == null || content.getType() == Material.AIR) {
                playerSpace += product.getItemStack().getMaxStackSize();;
            }
        }

        // TODO: auto adjust item amount
        
        if (e.getType() == Type.BUY) {
            if (!economy.has(bukkitPlayer, shop.getWorld().getName(), e.getPrice())) {
                player.sendMessage("§cYou don't have enough money.");
                e.setCancelled(true);
                return;
            }
            if (playerSpace < e.getAmount()) {
                player.sendMessage("§cYou don't have enough space in your inventory.");
                e.setCancelled(true);
                return;
            }
        } else if (e.getType() == Type.SELL) {
            if (playerAmount < e.getAmount()) {
                player.sendMessage("§cYou don't have enough items to sell.");
                e.setCancelled(true);
                return;
            }
            if (!shop.isAdminShop() && !economy.has(shop.getVendor().get(), shop.getWorld().getName(), e.getPrice())) {
                player.sendMessage("§cThe vendor of this shop doesn't have enough money.");
                e.setCancelled(true);
                return;
            }
        }

        if (!shop.isAdminShop()) {
            int shopAmount = 0;
            int shopSpace = 0;

            try {
                for (ItemStack content : shop.getInventory().getStorageContents()) {
                    if (ItemUtil.isEqual(content, product.getItemStack())) {
                        shopAmount += content.getAmount();
                        shopSpace += content.getMaxStackSize() - content.getAmount();
                    } else if (content == null || content.getType() == Material.AIR) {
                        shopSpace += product.getItemStack().getMaxStackSize();;
                    }
                }
            } catch (ChestNotFoundException ignored) {
                // Should not be possible since chest has to be clicked
                // for this event to be fired.
            }
        
           boolean vendorMessages = Config.FEATURES_VENDOR_MESSAGES.get();
           Optional<ShopPlayer> vendor = shop.getVendor().filter(OfflinePlayer::isOnline)
                .map(offlinePlayer -> plugin.wrapPlayer(offlinePlayer.getPlayer()));
            
            if (e.getType() == Type.BUY) {
                if (shopAmount < e.getAmount()) {
                    player.sendMessage("§cThis shop is out of items to sell.");
                    if (vendorMessages && vendor.isPresent()) {
                        vendor.get().sendMessage("§cYour shop selling §e{0} x {1} §cis out of stock.", product.getAmount(),
                                product.getLocalizedName());
                    }
                    e.setCancelled(true);
                    return;
                }
            } else if (e.getType() == Type.SELL) {
                if (shopSpace < e.getAmount()) {
                    player.sendMessage("§cThis shop doesn't have enough space for your items.");
                    if (vendorMessages && vendor.isPresent()) {
                        vendor.get().sendMessage("§cYour shop buying §e{0} x {1} §cis full.", product.getAmount(),
                                product.getLocalizedName());
                    }
                    e.setCancelled(true);
                    return;
                }
            }
        }

    }
    
}