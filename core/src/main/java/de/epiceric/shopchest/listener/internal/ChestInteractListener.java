package de.epiceric.shopchest.listener.internal;

import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopUseEvent;
import de.epiceric.shopchest.api.event.ShopCreateEvent;
import de.epiceric.shopchest.api.event.ShopEditEvent;
import de.epiceric.shopchest.api.event.ShopInfoEvent;
import de.epiceric.shopchest.api.event.ShopOpenEvent;
import de.epiceric.shopchest.api.event.ShopRemoveEvent;
import de.epiceric.shopchest.api.event.ShopUseEvent.Type;
import de.epiceric.shopchest.api.flag.CreateFlag;
import de.epiceric.shopchest.api.flag.EditFlag;
import de.epiceric.shopchest.api.flag.Flag;
import de.epiceric.shopchest.api.flag.InfoFlag;
import de.epiceric.shopchest.api.flag.OpenFlag;
import de.epiceric.shopchest.api.flag.RemoveFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.shop.ShopImpl;

public class ChestInteractListener implements Listener {
    private ShopChest plugin;

    public ChestInteractListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    private void handleShopUse(Shop shop, ShopPlayer player, Type type, Cancellable e) {
        if (player.isVendor(shop)) {
            return; // vendors cannot use their own shops
        }
        
        e.setCancelled(true);

        if (player.getBukkitPlayer().getGameMode() == GameMode.CREATIVE) {
            player.sendMessage("§cYou cannot use a shop in creative mode."); // TODO: i18n
            return;
        }

        if (Config.CORE_INVERT_MOUSE_BUTTONS.get()) {
            type = type == Type.BUY ? Type.SELL : Type.BUY;
        }

        double price = type == Type.BUY ? shop.getBuyPrice() : shop.getSellPrice();
        if (price <= 0) {
            String typeStr = type == Type.BUY ? "buy" : "sell";
            player.sendMessage("§cYou cannot {0} at this shop.", typeStr); // TODO: i18n
            return;
        }

        plugin.getServer().getPluginManager()
                .callEvent(new ShopUseEvent(player, shop, type, shop.getProduct().getAmount(), price));
    }

    private void handleFlags(ShopPlayer player, Shop shop, Cancellable e) {
        if (!player.getFlag().isPresent()) {
            return;
        }

        Flag flag = player.getFlag().get();
        player.removeFlag();

        if (flag instanceof InfoFlag) {
            plugin.getServer().getPluginManager().callEvent(new ShopInfoEvent(player, shop));
            e.setCancelled(true);
        } else if (flag instanceof RemoveFlag) {
            plugin.getServer().getPluginManager().callEvent(new ShopRemoveEvent(player, shop));
            e.setCancelled(true);
        } else if (flag instanceof OpenFlag) {
            ShopOpenEvent event = new ShopOpenEvent(player, shop);
            plugin.getServer().getPluginManager().callEvent(event);
            e.setCancelled(event.isCancelled());
        } else if (flag instanceof EditFlag) {
            EditFlag editFlag = (EditFlag) flag;
            plugin.getServer().getPluginManager().callEvent(new ShopEditEvent(player, shop, editFlag.getItemStack(),
                    editFlag.getAmount(), editFlag.getBuyPrice(), editFlag.getSellPrice()));
            e.setCancelled(true);
        } else if (flag instanceof CreateFlag) {
            e.setCancelled(true);
            player.sendMessage("§cThis chest already is a shop."); // TODO: i18n
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!(e.getClickedBlock().getState() instanceof Chest)) {
            return;
        }

        Location location = e.getClickedBlock().getLocation();
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        Optional<Shop> shopOpt = plugin.getShopManager().getShop(location);

        if (shopOpt.isPresent()) {
            Shop shop = shopOpt.get();
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                
                if (player.getFlag().isPresent()) {
                    // handle flags
                    handleFlags(player, shop, e);
                } else if (e.hasItem() && e.getItem().getType() == Config.CORE_SHOP_INFO_ITEM.get()) {
                    // handle shop interaction item
                    plugin.getServer().getPluginManager().callEvent(new ShopInfoEvent(player, shopOpt.get()));
                    e.setCancelled(true);
                    return;
                } else {
                    // handle buy
                    handleShopUse(shop, player, Type.BUY, e);
                }
            } else {
                // handle sell
                handleShopUse(shop, player, Type.SELL, e);
            }
        } else {
            // handle create
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                player.getFlag().filter(flag -> flag instanceof CreateFlag).ifPresent(f -> {
                    e.setCancelled(true);
                    CreateFlag flag = (CreateFlag) f;
                    player.removeFlag();
                    OfflinePlayer vendor = flag.isAdminShop() ? null : player.getBukkitPlayer();
                    plugin.getServer().getPluginManager().callEvent(new ShopCreateEvent(player,
                            new ShopImpl(vendor, flag.getProduct(), location, flag.getBuyPrice(), flag.getSellPrice()),
                            Config.SHOP_CREATION_PRICE.get()));
                });   
            }
        }
    }
}