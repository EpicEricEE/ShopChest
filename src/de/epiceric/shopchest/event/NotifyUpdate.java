package de.epiceric.shopchest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.interfaces.jsonbuilder.*;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import net.milkbowl.vault.permission.Permission;

public class NotifyUpdate implements Listener {

	private Permission perm = ShopChest.perm;
	
	public NotifyUpdate() {}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		Player p = e.getPlayer();
		
		if (ShopChest.isUpdateNeeded) {
			if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
				JsonBuilder jb;
				
				switch (Utils.getVersion(ShopChest.getInstance().getServer())) {
					case "v1_8_R1": jb = new JsonBuilder_1_8_R1(Config.update_available(ShopChest.latestVersion)); break;
					case "v1_8_R2": jb = new JsonBuilder_1_8_R2(Config.update_available(ShopChest.latestVersion)); break;
					case "v1_8_R3": jb = new JsonBuilder_1_8_R3(Config.update_available(ShopChest.latestVersion)); break;
					case "v1_9_R1": jb = new JsonBuilder_1_9_R1(Config.update_available(ShopChest.latestVersion)); break;
					case "v1_9_R2": jb = new JsonBuilder_1_9_R2(Config.update_available(ShopChest.latestVersion)); break;
					default: return;
				}		
				jb.sendJson(p);
			}
		}
		
		if (perm.has(p, "shopchest.broadcast")) {
			if (ShopChest.broadcast != null) {
				for (String message : ShopChest.broadcast) {
					p.sendMessage(message);
				}
			}
		}
		
	}
	
}
