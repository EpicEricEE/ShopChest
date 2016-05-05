package de.epiceric.shopchest.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.epiceric.shopchest.ShopChest;
import net.md_5.bungee.api.ChatColor;

public class UpdateChecker {

	private ShopChest plugin;
	private String url;
	private String version;
	private String link;
	private String broadcast;
	
	public UpdateChecker(ShopChest plugin, String url) {
		this.plugin = plugin;
		this.url = url;
	}
	
	public boolean updateNeeded(CommandSender sender) {
		try {
			Connection con = Jsoup.connect("http://textuploader.com/all1l/raw");
			con.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
			
			Document doc = con.get();
						
			version = doc.text().split("\\|")[0];
			link = url + "download?version=" + doc.text().split("\\|")[1];
			
			if (doc.text().split("\\|").length == 3) {
				broadcast = doc.text().split("\\|")[2];
			}
			
			return !plugin.getDescription().getVersion().equals(version);
			
		} catch (Exception | Error e) {
			sender.sendMessage((sender instanceof ConsoleCommandSender ? "[ShopChest] " : "") + ChatColor.RED + "Error while checking for updates");
			return false;
		}
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getBroadcast() {
		return broadcast;
	}
	
}
