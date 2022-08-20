package de.epiceric.shopchest.listener;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopInfoEvent;
import de.epiceric.shopchest.api.event.ShopPreInfoEvent;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.api.flag.InfoFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.util.ItemUtil;

public class ShopInfoListener implements Listener {
    private final ShopChest plugin;

    public ShopInfoListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    // TODO: Move to nms modules or get rid of nms references
    /* private BaseComponent[] getProductMessage(ShopProduct product) {
        try {
            TextComponent title = new TextComponent("Product: ");
            title.setColor(ChatColor.GOLD);

            TextComponent amount = new TextComponent(product.getAmount() + " x ");
            amount.setColor(ChatColor.WHITE);

            NBTTagCompound nbt = CraftItemStack.asNMSCopy(product.getItemStack()).save(new NBTTagCompound());
            String itemJson = new JsonPrimitive(nbt.toString()).toString();

            TextComponent item = new TextComponent(product.getLocalizedName());
            item.setColor(ChatColor.WHITE);
            item.setUnderlined(true);
            item.setHoverEvent(new HoverEvent(Action.SHOW_ITEM, new BaseComponent[] { new TextComponent(itemJson) }));

            return new BaseComponent[] { title, amount, item };
        } catch (Exception e) {
            Logger.severe("Failed to create JSON for item. Product preview will not be available.");
            Logger.severe(e);
            return TextComponent.fromLegacyText(MessageFormat.format("§6Product: §f{0} x {1}",
                    product.getAmount(), product.getLocalizedName())); // TODO: i18n
        }
    } */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(ShopPreInfoEvent e) {
        ShopPlayer player = e.getPlayer();
        player.setFlag(new InfoFlag(plugin));
        player.sendMessage("§aClick a shop within 15 seconds to retrieve information about it."); // TODO: 18n
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAction(ShopInfoEvent e) {
        Shop shop = e.getShop();
        ShopProduct product = shop.getProduct();
        ShopPlayer player = e.getPlayer();

        // TODO: i18n
        player.sendMessage("§e--------- §fShop Info §e-----------------------------");
        player.sendMessage("§7Hover over the underlined product for more details");
        player.sendMessage("§6Vendor: §f{0}", shop.getVendor().map(OfflinePlayer::getName).orElse("Admin"));
        // player.getBukkitPlayer().spigot().sendMessage(getProductMessage(product)); // TODO

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
}