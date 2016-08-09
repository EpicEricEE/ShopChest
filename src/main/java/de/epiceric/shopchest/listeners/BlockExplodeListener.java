package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.ArrayList;

public class BlockExplodeListener implements Listener {

    private ShopChest plugin;

    public BlockExplodeListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        if (plugin.getShopChestConfig().explosion_protection) {
            ArrayList<Block> bl = new ArrayList<>(e.blockList());
            for (Block b : bl) {
                if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                    if (plugin.getShopUtils().isShop(b.getLocation())) e.blockList().remove(b);
                }
            }
        }
    }

}
