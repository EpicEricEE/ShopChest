package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

public class ShopItemListener implements Listener {

    private ShopUtils shopUtils;
    private ShopChest plugin;

    public ShopItemListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {

        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()) {
            return;
        }

        updateShopVisibility(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        updateShopVisibility(e.getPlayer(), true, e.getTo());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        updateShopVisibility(e.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        for (Shop shop : plugin.getShopUtils().getShops()) {
            shop.getItem().setVisible(e.getPlayer(), false);
        }
    }

    private void updateShopVisibility(Player p, boolean hideIfAway) {
        updateShopVisibility(p, hideIfAway, p.getLocation());
    }

    private void updateShopVisibility(Player p, boolean hideIfAway, Location playerLocation) {

        double itemDistanceSquared = plugin.getShopChestConfig().maximal_item_distance;
        itemDistanceSquared *= itemDistanceSquared;
        World w = p.getWorld();

        for (Shop shop : shopUtils.getShops()) {
            Location shopLocation = shop.getLocation();
            if (w.equals(shopLocation.getWorld()) && shopLocation.distanceSquared(playerLocation) <= itemDistanceSquared) {
                shop.getItem().setVisible(p, true);
            } else if (hideIfAway) {
                shop.getItem().setVisible(p, false);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced();
        Block below = b.getRelative(BlockFace.DOWN);

        if (shopUtils.isShop(below.getLocation())) {
            shopUtils.getShop(below.getLocation()).getItem().resetForPlayer(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        for (BlockState blockState : e.getReplacedBlockStates()) {
            Block below = blockState.getBlock().getRelative(BlockFace.DOWN);

            if (shopUtils.isShop(below.getLocation())) {
                shopUtils.getShop(below.getLocation()).getItem().resetForPlayer(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        // If the piston would only move itself
        Block airAfterPiston = e.getBlock().getRelative(e.getDirection());
        Block belowAir = airAfterPiston.getRelative(BlockFace.DOWN);
        if (shopUtils.isShop(belowAir.getLocation())) {
            e.setCancelled(true);
            return;
        }

        for (Block b : e.getBlocks()) {
            Block newBlock = b.getRelative(e.getDirection());
            Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
            if (shopUtils.isShop(belowNewBlock.getLocation())) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            Block newBlock = b.getRelative(e.getDirection());
            Block belowNewBlock = newBlock.getRelative(BlockFace.DOWN);
            if (shopUtils.isShop(belowNewBlock.getLocation())) {
                e.setCancelled(true);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    shopUtils.getShop(belowNewBlock.getLocation()).getItem().resetForPlayer(p);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLiquidFlow(BlockFromToEvent e) {
        Block b = e.getToBlock();
        Block below = b.getRelative(BlockFace.DOWN);

        if (shopUtils.isShop(below.getLocation())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Block clicked = e.getBlockClicked();
        Block underWater = clicked.getRelative(BlockFace.DOWN).getRelative(e.getBlockFace());

        if (shopUtils.isShop(clicked.getLocation())) {
            if (e.getBucket() == Material.LAVA_BUCKET) {
                shopUtils.getShop(clicked.getLocation()).getItem().resetForPlayer(e.getPlayer());
            }
        } else if (shopUtils.isShop(underWater.getLocation())) {
            if (e.getBucket() == Material.LAVA_BUCKET) {
                shopUtils.getShop(underWater.getLocation()).getItem().resetForPlayer(e.getPlayer());
            }
        } else {
            return;
        }

        e.setCancelled(true);
    }

}
