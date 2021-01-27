package de.epiceric.shopchest.listeners;

import java.util.ArrayList;

import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import de.epiceric.shopchest.ShopChest;

public class BlockExplodeListener implements Listener {

    private ShopChest plugin;

    public BlockExplodeListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        ArrayList<Block> bl = new ArrayList<>(e.blockList());
        for (Block b : bl) {
            if (ShopUtils.isShopMaterial(b.getType())) {
                if (plugin.getShopUtils().isShop(b.getLocation())) e.blockList().remove(b);
            }
        }
    }

}
