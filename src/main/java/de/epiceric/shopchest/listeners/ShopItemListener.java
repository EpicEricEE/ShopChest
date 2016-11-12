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
import org.bukkit.event.world.StructureGrowEvent;

public class ShopItemListener implements Listener {

    private ShopUtils shopUtils;
    private ShopChest plugin;

    public ShopItemListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()) {
            return;
        }

        updateShopItemVisibility(e.getPlayer(), true, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        if (e.getFrom().getWorld().equals(e.getTo().getWorld())) {
            if (e.getFrom().distanceSquared(e.getTo()) > 22500) {
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        updateShopItemVisibility(e.getPlayer(), true, true, e.getTo());
                    }
                }, 20L);
                return;
            }
            updateShopItemVisibility(e.getPlayer(), true, false, e.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        updateShopItemVisibility(e.getPlayer(), true, true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        updateShopItemVisibility(e.getPlayer(), false, false);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        for (Shop shop : plugin.getShopUtils().getShops()) {
            if (shop.getItem() != null) {
                shop.getItem().setVisible(e.getPlayer(), false);
            }
        }
    }

    private void updateShopItemVisibility(Player p, boolean hideIfAway, boolean reset) {
        updateShopItemVisibility(p, hideIfAway, reset, p.getLocation());
    }

    private void updateShopItemVisibility(Player p, boolean hideIfAway, boolean reset, Location playerLocation) {
        double itemDistanceSquared = plugin.getShopChestConfig().maximal_item_distance;
        itemDistanceSquared *= itemDistanceSquared;
        World w = playerLocation.getWorld();

        for (Shop shop : shopUtils.getShops()) {
            Location shopLocation = shop.getLocation();
            if (w.equals(shopLocation.getWorld()) && shopLocation.distanceSquared(playerLocation) <= itemDistanceSquared) {
                if (shop.getItem() != null) {
                    if (reset) shop.getItem().resetForPlayer(p);
                    else shop.getItem().setVisible(p, true);
                }
            } else if (hideIfAway) {
                if (shop.getItem() != null) shop.getItem().setVisible(p, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced();
        Block below = b.getRelative(BlockFace.DOWN);

        if (shopUtils.isShop(below.getLocation())) {
            Shop shop = shopUtils.getShop(below.getLocation());
            if (shop.getItem() != null) {
                shop.getItem().resetForPlayer(e.getPlayer());
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        for (BlockState blockState : e.getReplacedBlockStates()) {
            Block below = blockState.getBlock().getRelative(BlockFace.DOWN);

            if (shopUtils.isShop(below.getLocation())) {
                Shop shop = shopUtils.getShop(below.getLocation());
                if (shop.getItem() != null) {
                    shop.getItem().resetForPlayer(e.getPlayer());
                }
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
                    Shop shop = shopUtils.getShop(belowNewBlock.getLocation());
                    if (shop.getItem() != null) {
                        shop.getItem().resetForPlayer(p);
                    }
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
                Shop shop = shopUtils.getShop(clicked.getLocation());
                if (shop.getItem() != null) {
                    shop.getItem().resetForPlayer(e.getPlayer());
                }
            }
        } else if (shopUtils.isShop(underWater.getLocation())) {
            if (e.getBucket() == Material.LAVA_BUCKET) {
                Shop shop = shopUtils.getShop(underWater.getLocation());
                if (shop.getItem() != null) {
                    shop.getItem().resetForPlayer(e.getPlayer());
                }
            }
        } else {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureGrow(StructureGrowEvent e) {
        for (BlockState state : e.getBlocks()) {
            Block newBlock = state.getBlock();
            if (shopUtils.isShop(newBlock.getLocation()) || shopUtils.isShop(newBlock.getRelative(BlockFace.DOWN).getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockGrow(BlockGrowEvent e) {
        Block newBlock = e.getNewState().getBlock();
        if (shopUtils.isShop(newBlock.getLocation()) || shopUtils.isShop(newBlock.getRelative(BlockFace.DOWN).getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent e) {
        Block newBlock = e.getNewState().getBlock();
        if (shopUtils.isShop(newBlock.getLocation()) || shopUtils.isShop(newBlock.getRelative(BlockFace.DOWN).getLocation())) {
            e.setCancelled(true);
        }
    }

}
