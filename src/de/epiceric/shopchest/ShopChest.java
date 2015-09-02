package de.epiceric.shopchest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.InteractShop;
import de.epiceric.shopchest.event.NotifyUpdate;
import de.epiceric.shopchest.event.ProtectChest;
import de.epiceric.shopchest.event.RegenerateShopItem;
import de.epiceric.shopchest.event.UpdateHolograms;
import de.epiceric.shopchest.shop.Hologram;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.JsonBuilder;
import de.epiceric.shopchest.utils.JsonBuilder.ClickAction;
import de.epiceric.shopchest.utils.JsonBuilder.HoverAction;
import de.epiceric.shopchest.utils.Metrics;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_8_R3.EntityArmorStand;

public class ShopChest extends JavaPlugin{

	private static ShopChest instance;
	
	public File shopChestsFile;
	public YamlConfiguration shopChests;
	
	public static Logger logger = Logger.getLogger("Minecraft");
	public static Economy econ = null;
	public static Permission perm = null;
	
	public static boolean isUpdateNeeded = false;
	public static String latestVersion = "";
	public static String downloadLink = "";
	
	public static ShopChest getInstance() {
		return instance;
	}
	
	
	private boolean setupEconomy() {

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perm = rsp.getProvider();
        return perm != null;
    }
	
	
	@Override
	public void onLoad() {
		getLogger().info(Utils.getVersion());
	}
	
	@Override
	public void onEnable() {
		
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("[ShopChest] Could not find plugin 'Vault'!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
		if (!setupEconomy() ) {
            logger.severe("[ShopChest] Could not find any Vault dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        logger.severe("[ShopChest] [PluginMetrics] Could not submit stats.");
	    }
		
		setupPermissions();
		
		instance = this;
		reloadConfig();
		saveDefaultConfig();
		
		UpdateChecker uc = new UpdateChecker(this, getDescription().getWebsite());
		logger.info("[ShopChest] Checking for Updates");
		if(uc.updateNeeded()) {
			latestVersion = uc.getVersion();
			downloadLink = uc.getLink();
			isUpdateNeeded = true;
			Bukkit.getConsoleSender().sendMessage("[ShopChest] " + ChatColor.GOLD + "New version available: " + ChatColor.RED + latestVersion);
		} else {
			logger.info("[ShopChest] No new version available");
			isUpdateNeeded = false;
		}
		
		if (isUpdateNeeded) {
			for (Player p : getServer().getOnlinePlayers()) {
				if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
					JsonBuilder jb = new JsonBuilder(Config.update_available(latestVersion)).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, downloadLink);
					jb.sendJson(p);
				
				}
			}
		}
		
		
		File shopChestDataFolder = new File(getDataFolder(), "data");
		shopChestDataFolder.mkdirs();
		
		shopChestsFile = new File(getDataFolder(), "/data/shops.yml");	
		if (!shopChestsFile.exists())
			try {shopChestsFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
		
		File itemNamesFile = new File(getDataFolder(), "item_names.txt");
		
		if (!itemNamesFile.exists())
			try {itemNamesFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
		
		copy(getResource("item_names"), itemNamesFile);
			
		shopChests = YamlConfiguration.loadConfiguration(shopChestsFile);
		
		
		try {
			Commands.registerCommand(new Commands(this, Config.main_command_name(), "Manage Shops.", "", new ArrayList<String>()), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		initializeShops();
		
		getServer().getPluginManager().registerEvents(new UpdateHolograms(), this);
		getServer().getPluginManager().registerEvents(new RegenerateShopItem(), this);
		getServer().getPluginManager().registerEvents(new InteractShop(this), this);
		getServer().getPluginManager().registerEvents(new NotifyUpdate(), this);
		getServer().getPluginManager().registerEvents(new ProtectChest(), this);

		
	}

	
	@Override
	public void onDisable() {
		
		for (Shop shop : ShopUtils.getShops()) {
			Hologram hologram = shop.getHologram();
			
			for (Player p : getServer().getOnlinePlayers()) {
				hologram.hidePlayer(p);
			}
			
			for (Object o : hologram.getEntities()) {
				EntityArmorStand e = (EntityArmorStand) o;
				e.getWorld().removeEntity(e);
			}

			
			shop.getItem().remove();
		}
		
	}
	
	private void initializeShops() {
		
		Bukkit.getConsoleSender().sendMessage("[ShopChest] Found " + String.valueOf(shopChests.getKeys(false).size()) + " Shops");
		
		for (String key : shopChests.getKeys(false)) {
			
			OfflinePlayer vendor = shopChests.getOfflinePlayer(key + ".vendor");
			int locationX = shopChests.getInt(key + ".location.x");
			int locationY = shopChests.getInt(key + ".location.y");
			int locationZ = shopChests.getInt(key + ".location.z");
			World locationWorld = getServer().getWorld(shopChests.getString(key + ".location.world"));
			Location location = new Location(locationWorld, locationX, locationY, locationZ);
			ItemStack product = shopChests.getItemStack(key + ".product");
			double buyPrice = shopChests.getDouble(key + ".price.buy");
			double sellPrice = shopChests.getDouble(key + ".price.sell");
			boolean infinite = shopChests.getBoolean(key + ".infinite");
			
			ShopUtils.addShop(new Shop(this, vendor, product, location, buyPrice, sellPrice, infinite));
			
		}
		
	}
	
	public static void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
}
