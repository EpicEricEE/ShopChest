package de.epiceric.shopchest.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;

public class ShopItemListener implements Listener {

    private ShopUtils shopUtils;
    private final ShopChest plugin;

    public ShopItemListener(ShopChest plugin) {
        this.shopUtils = plugin.getShopUtils();
        this.plugin = plugin;
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
    public void onShulkerClose(InventoryCloseEvent e) {
        if (e.getInventory().getType() != InventoryType.SHULKER_BOX) {
            return;
        }

        Block block = e.getPlayer().getTargetBlockExact(7);

        if (block == null || !(block.getState() instanceof ShulkerBox)) {
            return;
        }

        if (!shopUtils.isShop(block.getLocation())) {
            return;
        }

        Shop shop = shopUtils.getShop(block.getLocation());

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
          Bukkit.getOnlinePlayers().forEach(player -> shop.getItem().resetForPlayer(player));
        }, 15);
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
