package de.epiceric.shopchest.listeners;


import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.lang.reflect.InvocationTargetException;

public class ItemProtectListener implements Listener {

    private ShopUtils shopUtils;
    private ShopChest plugin;

    public ItemProtectListener(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDespawn(ItemDespawnEvent e) {
        Item item = e.getEntity();
        if (item.hasMetadata("shopItem")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickUpItem(PlayerPickupItemEvent e) {
        if (e.getItem().hasMetadata("shopItem")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(InventoryPickupItemEvent e) {
        if (e.getItem().hasMetadata("shopItem")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlockPlaced();
        Block below = b.getRelative(BlockFace.DOWN);

        if (shopUtils.isShop(below.getLocation())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        for (BlockState blockState : e.getReplacedBlockStates()) {
            Block below = blockState.getBlock().getRelative(BlockFace.DOWN);
            if (shopUtils.isShop(below.getLocation())) e.setCancelled(true);
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
            if (shopUtils.isShop(belowNewBlock.getLocation())) e.setCancelled(true);
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
        Block b = e.getBlockClicked();

        if (shopUtils.isShop(b.getLocation())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent e) {
        if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            if (e.getCaught() instanceof Item) {
                Item item = (Item) e.getCaught();
                if (item.hasMetadata("shopItem")) {
                    plugin.debug(e.getPlayer().getName() + " tried to fish a shop item");
                    e.setCancelled(true);

                    // Use some reflection to get the EntityFishingHook class so the hook can be removed...
                    try {
                        Class<?> craftFishClass = Class.forName("org.bukkit.craftbukkit." + Utils.getServerVersion() + ".entity.CraftFish");
                        Object craftFish = craftFishClass.cast(e.getHook());

                        Class<?> entityFishingHookClass = Class.forName("net.minecraft.server." + Utils.getServerVersion() + ".EntityFishingHook");
                        Object entityFishingHook = craftFishClass.getDeclaredMethod("getHandle").invoke(craftFish);

                        entityFishingHookClass.getDeclaredMethod("die").invoke(entityFishingHook);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
                        plugin.debug("Failed to kill fishing hook with reflection");
                        plugin.debug(ex);
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

}
