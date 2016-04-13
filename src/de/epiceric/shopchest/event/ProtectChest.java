package de.epiceric.shopchest.event;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
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

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;

public class ProtectChest implements Listener {

	public ProtectChest() {}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (ShopUtils.isShop(e.getBlock().getLocation())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Config.cannot_break_shop());
		}
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e) {
		if (Config.explosion_protection()) {
			ArrayList<Block> bl = new ArrayList<>(e.blockList());
			for (Block b : bl) {
				if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
					if (ShopUtils.isShop(b.getLocation())) e.blockList().remove(b);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		if (Config.explosion_protection()) {
			ArrayList<Block> bl = new ArrayList<>(e.blockList());
			for (Block b : bl) {
				if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
					if (ShopUtils.isShop(b.getLocation())) e.blockList().remove(b);
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
				
				if (ShopUtils.isShop(r.getLocation()) || ShopUtils.isShop(l.getLocation())) {					
					Shop shop;
					
					if (b.getLocation().equals(r.getLocation())) {
						shop = ShopUtils.getShop(l.getLocation());
						ShopUtils.removeShop(shop);
						ShopChest.sqlite.removeShop(shop);
					} else if (b.getLocation().equals(l.getLocation())) {
						shop = ShopUtils.getShop(r.getLocation());
						ShopUtils.removeShop(shop);
						ShopChest.sqlite.removeShop(shop);
					} else {
						return;
					}
					
					if (shop.hasItem()) shop.getItem().remove();
					
					Shop newShop = new Shop(ShopChest.getInstance(), shop.getVendor(), shop.getProduct(), shop.getLocation(), shop.getBuyPrice(), shop.getSellPrice(), shop.getShopType());
					newShop.createHologram();
					newShop.createItem();
					ShopUtils.addShop(newShop);
					ShopChest.sqlite.addShop(newShop);
					
				}		
				
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onItemMove(InventoryMoveItemEvent e) {
		if (Config.hopper_protection()) {
			if ((e.getSource().getType().equals(InventoryType.CHEST)) && (!e.getInitiator().getType().equals(InventoryType.PLAYER))) {
				
				if (e.getSource().getHolder() instanceof DoubleChest) {				
					DoubleChest dc = (DoubleChest) e.getSource().getHolder();
					Chest r = (Chest) dc.getRightSide();
					Chest l = (Chest) dc.getLeftSide();
					
					if (ShopUtils.isShop(r.getLocation()) || ShopUtils.isShop(l.getLocation())) e.setCancelled(true);
					
				} else if (e.getSource().getHolder() instanceof Chest) {
					Chest c = (Chest) e.getSource().getHolder();
					
					if (ShopUtils.isShop(c.getLocation())) e.setCancelled(true);
				}
				
			}	
		}		
	}
	
}
