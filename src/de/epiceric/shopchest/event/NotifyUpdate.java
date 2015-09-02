package de.epiceric.shopchest.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.utils.JsonBuilder;
import de.epiceric.shopchest.utils.JsonBuilder.ClickAction;
import de.epiceric.shopchest.utils.JsonBuilder.HoverAction;
import net.milkbowl.vault.permission.Permission;

public class NotifyUpdate implements Listener {

	private Permission perm = ShopChest.perm;
	
	public NotifyUpdate() {}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		Player p = e.getPlayer();
		
		if (ShopChest.isUpdateNeeded) {
			if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
				String version = ShopChest.latestVersion;
				String link = ShopChest.downloadLink;
				JsonBuilder jb = new JsonBuilder(Config.update_available(version)).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, link);
				jb.sendJson(p);
			}
		}
		
	}
	
}
