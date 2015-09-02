package de.epiceric.shopchest.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import de.epiceric.shopchest.config.Config;
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
	
}
