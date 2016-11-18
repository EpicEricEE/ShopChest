package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;

public class ChestProtectListener implements Listener {

    private ShopChest plugin;
    private ShopUtils shopUtils;

    public ChestProtectListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        final Block b = e.getBlock();

        if (shopUtils.isShop(b.getLocation())) {
            final Shop shop = shopUtils.getShop(e.getBlock().getLocation());
            Player p = e.getPlayer();

            if (p.isSneaking()) {
                plugin.debug(String.format("%s tries to break %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));

                if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || p.hasPermission(Permissions.REMOVE_OTHER)) {
                    shopUtils.removeShop(shop, true);

                    if (shop.getInventoryHolder() instanceof DoubleChest) {
                        DoubleChest dc = (DoubleChest) shop.getInventoryHolder();
                        final Chest l = (Chest) dc.getLeftSide();
                        final Chest r = (Chest) dc.getRightSide();

                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                            @Override
                            public void run() {
                                Shop newShop = null;

                                if (b.getLocation().equals(l.getLocation()))
                                    newShop = new Shop(shop.getID(), plugin, shop.getVendor(), shop.getProduct(), r.getLocation(), shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());
                                else if (b.getLocation().equals(r.getLocation()))
                                    newShop = new Shop(shop.getID(), plugin, shop.getVendor(), shop.getProduct(), l.getLocation(), shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());

                                shopUtils.addShop(newShop, true);
                            }
                        }, 1L);
                        return;
                    }

                    plugin.debug(String.format("%s broke %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));
                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_REMOVED));
                    return;
                }
            }

            if (shop.getItem() != null) {
                shop.getItem().resetForPlayer(p);
            }

            e.setCancelled(true);
            e.getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CANNOT_BREAK_SHOP));
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (plugin.getShopChestConfig().explosion_protection) {
            ArrayList<Block> bl = new ArrayList<>(e.blockList());
            for (Block b : bl) {
                if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                    if (shopUtils.isShop(b.getLocation())) e.blockList().remove(b);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlockPlaced();
        if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {

            Chest c = (Chest) b.getState();
            InventoryHolder ih = c.getInventory().getHolder();

            if (ih instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) ih;
                Chest r = (Chest) dc.getRightSide();
                Chest l = (Chest) dc.getLeftSide();

                if (shopUtils.isShop(r.getLocation()) || shopUtils.isShop(l.getLocation())) {
                    Shop shop;

                    if (b.getLocation().equals(r.getLocation())) {
                        shop = shopUtils.getShop(l.getLocation());
                    } else if (b.getLocation().equals(l.getLocation())) {
                        shop = shopUtils.getShop(r.getLocation());
                    } else {
                        return;
                    }

                    plugin.debug(String.format("%s tries to extend %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));

                    if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || p.hasPermission(Permissions.EXTEND_OTHER)) {
                        if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                            shopUtils.removeShop(shop, true);
                            Shop newShop = new Shop(shop.getID(), ShopChest.getInstance(), shop.getVendor(), shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());
                            shopUtils.addShop(newShop, true);
                            plugin.debug(String.format("%s extended %s's shop (#%d)", p.getName(), shop.getVendor().getName(), shop.getID()));
                        } else {
                            e.setCancelled(true);
                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_BLOCKED));
                        }
                    } else {
                        e.setCancelled(true);
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_EXTEND_OTHERS));
                    }
                }

            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemMove(InventoryMoveItemEvent e) {
        if (plugin.getShopChestConfig().hopper_protection) {
            if ((e.getSource().getType().equals(InventoryType.CHEST)) && (!e.getInitiator().getType().equals(InventoryType.PLAYER))) {

                if (e.getSource().getHolder() instanceof DoubleChest) {
                    DoubleChest dc = (DoubleChest) e.getSource().getHolder();
                    Chest r = (Chest) dc.getRightSide();
                    Chest l = (Chest) dc.getLeftSide();

                    if (shopUtils.isShop(r.getLocation()) || shopUtils.isShop(l.getLocation())) e.setCancelled(true);

                } else if (e.getSource().getHolder() instanceof Chest) {
                    Chest c = (Chest) e.getSource().getHolder();

                    if (shopUtils.isShop(c.getLocation())) e.setCancelled(true);
                }

            }
        }
    }

}
