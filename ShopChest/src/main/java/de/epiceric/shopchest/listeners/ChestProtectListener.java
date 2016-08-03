package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
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
        if (shopUtils.isShop(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CANNOT_BREAK_SHOP));
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
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

        Block b = e.getBlockPlaced();
        if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {

            Chest c = (Chest) b.getState();
            InventoryHolder ih = c.getInventory().getHolder();

            if (ih instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) ih;
                Chest r = (Chest) dc.getRightSide();
                Chest l = (Chest) dc.getLeftSide();

                if (shopUtils.isShop(r.getLocation()) || shopUtils.isShop(l.getLocation())) {
                    plugin.debug(e.getPlayer().getName() + " tried to extend a shop to a double chest");

                    if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        Shop shop;

                        if (b.getLocation().equals(r.getLocation())) {
                            shop = shopUtils.getShop(l.getLocation());
                        } else if (b.getLocation().equals(l.getLocation())) {
                            shop = shopUtils.getShop(r.getLocation());
                        } else {
                            return;
                        }

                        shopUtils.removeShop(shop, true);

                        Shop newShop = new Shop(shop.getID(), ShopChest.getInstance(), shop.getVendor(), shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());
                        shopUtils.addShop(newShop, true);
                    } else {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_BLOCKED));
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
