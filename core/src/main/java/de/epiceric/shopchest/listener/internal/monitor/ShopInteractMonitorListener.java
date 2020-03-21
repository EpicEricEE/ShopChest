package de.epiceric.shopchest.listener.internal.monitor;

import java.text.MessageFormat;

import com.google.gson.JsonPrimitive;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopBuySellEvent;
import de.epiceric.shopchest.api.event.ShopCreateEvent;
import de.epiceric.shopchest.api.event.ShopEditEvent;
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
import de.epiceric.shopchest.shop.ShopImpl;
import de.epiceric.shopchest.shop.ShopProductImpl;
import de.epiceric.shopchest.util.ItemUtil;
import de.epiceric.shopchest.util.Logger;
import de.epiceric.shopchest.util.NmsUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class ShopInteractMonitorListener implements Listener {
    private final ShopChest plugin;

    public ShopInteractMonitorListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopCreate(ShopCreateEvent e) {
        Shop shop = e.getShop();
        if (shop.isAdminShop()) {
            plugin.getShopManager().addAdminShop(shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice())
                    .thenAccept(newShop -> e.getPlayer().sendMessage("§aAdmin shop has been added with ID {0}.", newShop.getId()))
                    .exceptionally(ex -> { // TODO: i18n
                        Logger.severe("Failed to add admin shop");
                        Logger.severe(ex);
                        e.getPlayer().sendMessage("§cFailed to add admin shop: {0}", ex.getMessage());
                        return null;
                    });
        } else {
            plugin.getShopManager().addShop(shop.getVendor().get(), shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice())
                    .thenAccept(newShop -> e.getPlayer().sendMessage("§aShop has been added with ID {0}.", newShop.getId()))
                    .exceptionally(ex -> { // TODO: i18n
                        Logger.severe("Failed to add shop");
                        Logger.severe(ex);
                        e.getPlayer().sendMessage("§cFailed to add shop: {0}", ex.getMessage());
                        return null;
                    });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopEdit(ShopEditEvent e) {
        ShopImpl shop = (ShopImpl) e.getShop();
        shop.setBuyPrice(e.getBuyPrice());
        shop.setSellPrice(e.getSellPrice());
        shop.setProduct(new ShopProductImpl(e.getItemStack(), e.getAmount()));

        ((ShopChestImpl) plugin).getDatabase().updateShop(shop)
                .thenRun(() -> e.getPlayer().sendMessage("§aShop has been edited.")) // TODO: i18n
                .exceptionally(ex -> {
                    Logger.severe("Failed to save shop edit");
                    Logger.severe(ex);
                    e.getPlayer().sendMessage("§cFailed to save edit: {0}", ex.getMessage());
                    return null;
                });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopRemove(ShopRemoveEvent e) {
        plugin.getShopManager().removeShop(e.getShop())
                .thenRun(() -> e.getPlayer().sendMessage("§aShop has been removed.")) // TODO: i18n
                .exceptionally(ex -> {
                    Logger.severe("Failed to remove shop");
                    Logger.severe(ex);
                    e.getPlayer().sendMessage("§cFailed to remove shop: {0}", ex.getMessage());
                    return null;
                });
    }

    private String getProductJson(ShopProduct product) {
        try {
            Class<?> craftItemStackClass = NmsUtil.getCraftClass("inventory.CraftItemStack");
            Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, product.getItemStack());
            Class<?> nbtTagCompoundClass = NmsUtil.getNmsClass("NBTTagCompound");
            Object nbtTagCompound = nbtTagCompoundClass.getConstructor().newInstance();
            nmsStack.getClass().getMethod("save", nbtTagCompoundClass).invoke(nmsStack, nbtTagCompound);
            String itemJson = new JsonPrimitive(nbtTagCompound.toString()).toString();

             // TODO: i18n
            return "[{" + 
                    "\"text\":\"Product: \"," +
                    "\"color\":\"gold\"" +
                "},{" +
                    "\"text\":\"" + product.getAmount() + " x \"," +
                    "\"color\":\"white\"" +
                "},{" +
                    "\"text\":\"" + product.getLocalizedName() + "\"," +
                    "\"color\":\"white\"," +
                    "\"underlined\":true," +
                    "\"hoverEvent\":{" +
                        "\"action\":\"show_item\"," +
                        "\"value\":" + itemJson +
                    "}" +
                "}]";
        } catch (Exception e) {
            Logger.severe("Failed to create JSON for item. Product preview will not be available.");
            Logger.severe(e);
            return MessageFormat.format("§6Product: §f{0} x {1}", product.getAmount(), product.getLocalizedName()); // TODO: i18n
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopInfo(ShopInfoEvent e) {
        Shop shop = e.getShop();
        ShopProduct product = shop.getProduct();
        ShopPlayer player = e.getPlayer();
        String productJson = getProductJson(product);

        // TODO: i18n
        player.sendMessage("§e--------- §fShop Info §e-----------------------------");
        player.sendMessage("§7Hover over the underlined product for more details");
        player.sendMessage("§6Vendor: §f{0}", shop.getVendor().map(OfflinePlayer::getName).orElse("Admin"));

        if (productJson.startsWith("[{")) {
            NmsUtil.sendJsonMessage(player.getBukkitPlayer(), getProductJson(product));
        } else {
            // JSON creation failed -> productJson is a "normal" string
            player.sendMessage(productJson);
        }

        if (!shop.isAdminShop()) {
            int inStock = 0;
            int freeSpace = 0;
    
            try {
                for (ItemStack content : shop.getInventory().getStorageContents()) {
                    if (ItemUtil.isEqual(content, product.getItemStack())) {
                        freeSpace += content.getMaxStackSize() - content.getAmount();
                        inStock += content.getAmount();
                    } else if (content == null || content.getType() == Material.AIR) {
                        freeSpace += product.getItemStack().getMaxStackSize();
                    }
                }
            } catch (ChestNotFoundException ignored) {
                // Should not be possible since chest has to be clicked
                // for this event to be fired.
            }

            // TODO: i18n
            if (shop.canPlayerBuy()) player.sendMessage("§6In stock: §f{0}", inStock);
            if (shop.canPlayerSell()) player.sendMessage("§6Free space: §f{0}", freeSpace);
        }
        
        if (shop.canPlayerBuy()) player.sendMessage("§6Buy for: §f{0}", plugin.formatEconomy(shop.getBuyPrice()));
        if (shop.canPlayerSell()) player.sendMessage("§6Sell for: §f{0}", plugin.formatEconomy(shop.getSellPrice()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopOpen(ShopOpenEvent e) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopExtend(ShopExtendEvent e) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopRemoveAll(ShopRemoveAllEvent e) {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopBuySellTransaction(ShopBuySellEvent e) {
        // Money transaction in HIGHEST priority
        Economy economy = ((ShopChestImpl) plugin).getEconomy();
        Player bukkitPlayer = e.getPlayer().getBukkitPlayer();
        String worldName = e.getShop().getWorld().getName();

        if (e.getType() == Type.BUY) {
            EconomyResponse r = economy.withdrawPlayer(bukkitPlayer, worldName, e.getPrice());
            if (!r.transactionSuccess()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cFailed to withdraw money: {0}", r.errorMessage); // TODO: i18n
                return;
            }

            e.getShop().getVendor().ifPresent(vendor -> {
                EconomyResponse rVendor = economy.depositPlayer(vendor, worldName, e.getPrice());
                if (!rVendor.transactionSuccess()) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cFailed to deposit money to vendor: {0}", r.errorMessage); // TODO: i18n

                    EconomyResponse rBack = economy.depositPlayer(bukkitPlayer, worldName, e.getPrice());
                    if (!rBack.transactionSuccess()) {
                        e.getPlayer().sendMessage("§cFailed to reverse your withdrawal: {0}", r.errorMessage); // TODO: i18n
                    }
                    return;
                }
            });
        } else {
            EconomyResponse r = economy.depositPlayer(bukkitPlayer, worldName, e.getPrice());
            if (!r.transactionSuccess()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cFailed to deposit money: {0}", r.errorMessage); // TODO: i18n
                return;
            }

            e.getShop().getVendor().ifPresent(vendor -> {
                EconomyResponse rVendor = economy.withdrawPlayer(vendor, worldName, e.getPrice());
                if (!rVendor.transactionSuccess()) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage("§cFailed to withdraw money from vendor: {0}", r.errorMessage); // TODO: i18n
                    
                    EconomyResponse rBack = economy.depositPlayer(bukkitPlayer, worldName, e.getPrice());
                    if (!rBack.transactionSuccess()) {
                        e.getPlayer().sendMessage("§cFailed to reverse your deposit: {0}", r.errorMessage); // TODO: i18n
                    }
                    return;
                }
            });
                
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopBuySell(ShopBuySellEvent e) {
        ShopPlayer player = e.getPlayer();
        Player bukkitPlayer = player.getBukkitPlayer();
        String itemName = e.getShop().getProduct().getLocalizedName();
        String price = plugin.formatEconomy(e.getPrice());
        boolean isBuy = e.getType() == Type.BUY;
        Shop shop = e.getShop();
        ShopProduct product = shop.getProduct();
        boolean isAdmin = shop.isAdminShop();

        try {
            if (isBuy) {
                for (int i = 0; i < e.getAmount(); i++) {
                    bukkitPlayer.getInventory().addItem(product.getItemStack());
                    if (!isAdmin) {
                        shop.getInventory().removeItem(product.getItemStack());
                    }
                }

                // TODO: i18n
                if (isAdmin) {
                    player.sendMessage("§aYou bought §e{0} x {1} §afor §e{2}§a.", e.getAmount(), itemName, price);
                } else {
                    player.sendMessage("§aYou bought §e{0} x {1} §afor §e{2} §afrom §e{3}§a.", e.getAmount(), itemName,
                            price, shop.getVendor().get().getName());
                }
            } else {
                for (int i = 0; i < e.getAmount(); i++) {
                    bukkitPlayer.getInventory().removeItem(product.getItemStack());
                    if (!isAdmin) {
                        shop.getInventory().addItem(product.getItemStack());
                    }
                }
                
                // TODO: i18n
                if (isAdmin) {
                    player.sendMessage("§aYou sold §e{0} x {1} §afor §e{2}§a.", e.getAmount(), itemName, price);
                } else {
                    player.sendMessage("§aYou sold §e{0} x {1} §afor §e{2} §ato §e{3}§a.", e.getAmount(), itemName,
                            price, shop.getVendor().get().getName());
                }
            }
        } catch (ChestNotFoundException ignored) {
            // Should not be possible since chest has to be clicked
            // for this event to be fired.
        }
    }
    
}