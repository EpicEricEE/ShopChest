package de.epiceric.shopchest.listener.internal.monitor;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopPreCreateEvent;
import de.epiceric.shopchest.api.event.ShopPreEditEvent;
import de.epiceric.shopchest.api.event.ShopPreInfoEvent;
import de.epiceric.shopchest.api.event.ShopPreOpenEvent;
import de.epiceric.shopchest.api.event.ShopPreRemoveEvent;
import de.epiceric.shopchest.api.event.ShopSelectItemEvent;
import de.epiceric.shopchest.api.flag.CreateFlag;
import de.epiceric.shopchest.api.flag.EditFlag;
import de.epiceric.shopchest.api.flag.Flag;
import de.epiceric.shopchest.api.flag.InfoFlag;
import de.epiceric.shopchest.api.flag.OpenFlag;
import de.epiceric.shopchest.api.flag.RemoveFlag;
import de.epiceric.shopchest.api.flag.SelectFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.shop.ShopProductImpl;

public class ShopCommandMonitorListener implements Listener {
    private final ShopChest plugin;

    public ShopCommandMonitorListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPreCreate(ShopPreCreateEvent e) {
        ShopPlayer player = e.getPlayer();

        if (!e.isItemSelected()) {
            if (!(player.getFlag().orElse(null) instanceof SelectFlag)) {
                // Set flag only if player doesn't already have SelectFlag
                SelectFlag.Type type = e.isAdminShop() ? SelectFlag.Type.ADMIN : SelectFlag.Type.NORMAL;
                Flag flag = new SelectFlag(e.getAmount(), e.getBuyPrice(), e.getSellPrice(), type,
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPreEdit(ShopPreEditEvent e) {
        ShopPlayer player = e.getPlayer();

        if (!e.hasItemStack() && e.willEditItem()) {
            if (!(player.getFlag().orElse(null) instanceof SelectFlag)) {
                // Set flag only if player doesn't already have SelectFlag
                Flag flag = new SelectFlag(e.getAmount(), e.getBuyPrice(), e.getSellPrice(), SelectFlag.Type.EDIT,
                        player.getBukkitPlayer().getGameMode());
                player.setFlag(flag);
                player.getBukkitPlayer().setGameMode(GameMode.CREATIVE);
                player.sendMessage("§aOpen your inventory and select the item you want to sell or buy."); // TODO: 18n
            }
        } else {
            player.setFlag(new EditFlag(plugin, e.getItemStack(), e.getAmount(), e.getBuyPrice(), e.getSellPrice()));
            player.sendMessage("§aClick a shop within 15 seconds to make the edit."); // TODO: 18n
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopSelectItem(ShopSelectItemEvent e) {
        ShopProduct product = new ShopProductImpl(e.getItem(), e.getAmount());
        ShopPlayer player = e.getPlayer();

        if (e.isEditingShop()) {
            player.setFlag(new EditFlag(plugin, e.getItem(), e.getAmount(), e.getBuyPrice(), e.getSellPrice()));
            player.sendMessage("§aItem has been selected: §e{0}", product.getLocalizedName()); // TODO: 18n
            player.sendMessage("§aClick a chest within 15 seconds to make the edit."); // TODO: 18n
        } else {
            player.setFlag(new CreateFlag(plugin, product, e.getBuyPrice(), e.getSellPrice(), e.isAdminShop()));
            player.sendMessage("§aItem has been selected: §e{0}", product.getLocalizedName()); // TODO: 18n
            player.sendMessage("§aClick a chest within 15 seconds to create a shop."); // TODO: 18n
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPreRemove(ShopPreRemoveEvent e) {
        ShopPlayer player = e.getPlayer();
        player.setFlag(new RemoveFlag(plugin));
        player.sendMessage("§aClick a shop within 15 seconds to remove it."); // TODO: 18n
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPreInfo(ShopPreInfoEvent e) {
        ShopPlayer player = e.getPlayer();
        player.setFlag(new InfoFlag(plugin));
        player.sendMessage("§aClick a shop within 15 seconds to retrieve information about it."); // TODO: 18n
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShopPreOpen(ShopPreOpenEvent e) {
        ShopPlayer player = e.getPlayer();
        player.setFlag(new OpenFlag(plugin));
        player.sendMessage("§aClick a shop within 15 seconds to open it."); // TODO: 18n
    }
    
}