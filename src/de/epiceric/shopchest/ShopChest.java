package de.epiceric.shopchest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.InteractShop;
import de.epiceric.shopchest.event.ItemCustomNameListener;
import de.epiceric.shopchest.event.NotifyUpdate;
import de.epiceric.shopchest.event.ProtectChest;
import de.epiceric.shopchest.event.RegenerateShopItem;
import de.epiceric.shopchest.event.RegenerateShopItemAfterRemove;
import de.epiceric.shopchest.event.UpdateHolograms;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.interfaces.utils.*;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.SQLite;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import de.epiceric.shopchest.interfaces.jsonbuilder.*;
import de.epiceric.shopchest.utils.Metrics;
import de.epiceric.shopchest.utils.Metrics.Graph;
import de.epiceric.shopchest.utils.Metrics.Plotter;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class ShopChest extends JavaPlugin{

	private static ShopChest instance;
	
	public static Statement statement;
	public static Logger logger;
	public static Economy econ = null;
	public static Permission perm = null;
	public static LWC lwc = null;
	public static boolean lockette = false;
	public static SQLite sqlite;
	
	public static boolean isUpdateNeeded = false;
	public static String latestVersion = "";
	public static String downloadLink = "";
	public static String[] broadcast = null;
	
	public static Utils utils;
	
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
	public void onEnable() {
		logger = getLogger();
		
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Could not find plugin 'Vault'!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
		if (!setupEconomy() ) {
            logger.severe("Could not find any Vault dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		try {
	        Metrics metrics = new Metrics(this);
	        Graph shopType = metrics.createGraph("Shop Type");
	        
	        shopType.addPlotter(new Plotter("Infinite") {
				
				@Override
				public int getValue() {					
					int value = 0;
					
					for (Shop shop : ShopUtils.getShops()) {
						if (shop.getShopType() == ShopType.INFINITE) value++;
					}
					
					return value;
				}
			});
	        
	        shopType.addPlotter(new Plotter("Normal") {
				
				@Override
				public int getValue() {
					int value = 0;
					
					for (Shop shop : ShopUtils.getShops()) {
						if (shop.getShopType() == ShopType.NORMAL) value++;
					}
					
					return value;
				}
				
			});
	        
	        shopType.addPlotter(new Plotter("Admin") {

				@Override
				public int getValue() {
					int value = 0;
					
					for (Shop shop : ShopUtils.getShops()) {
						if (shop.getShopType() == ShopType.ADMIN) value++;
					}
					
					return value;
				}        	
	        	
	        });
	        
	        metrics.start();
	    } catch (IOException e) {
	        logger.severe("Could not submit stats.");
	    }

		reloadConfig();
		saveDefaultConfig();
		
		sqlite =  new SQLite(this);	
		sqlite.load();
		
		switch (Utils.getVersion(getServer())) {
		
		case "v1_8_R1": utils = new Utils_1_8_R1(); break;
		case "v1_8_R2": utils = new Utils_1_8_R2(); break;
		case "v1_8_R3": utils = new Utils_1_8_R3(); break;
		case "v1_9_R1": utils = new Utils_1_9_R1(); break;
		default:
			logger.severe("Incompatible Server Version: " + Utils.getVersion(getServer()) + "!");
			getServer().getPluginManager().disablePlugin(this);
			return;	
		}
		
		if (getServer().getPluginManager().getPlugin("LWC") != null) {
			Plugin lwcp = getServer().getPluginManager().getPlugin("LWC");
			lwc = ((LWCPlugin) lwcp).getLWC();
		} else {
			lwc = null;
		}
		
		if (getServer().getPluginManager().getPlugin("Lockette") != null) {
			lockette = true;
		} else {
			lockette = false;
		}
		
		setupPermissions();
		
		instance = this;

		UpdateChecker uc = new UpdateChecker(this, getDescription().getWebsite());
		UpdateCheckerResult result = uc.updateNeeded();
		
		if (Config.enable_broadcast()) broadcast = uc.getBroadcast();
				
		Bukkit.getConsoleSender().sendMessage("[ShopChest] " + Config.checking_update());
		if(result == UpdateCheckerResult.TRUE) {
			latestVersion = uc.getVersion();
			downloadLink = uc.getLink();
			isUpdateNeeded = true;
			Bukkit.getConsoleSender().sendMessage("[ShopChest] " + Config.update_available(latestVersion));
			
			for (Player p : getServer().getOnlinePlayers()) {
				if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
					JsonBuilder jb;
					switch (Utils.getVersion(getServer())) {
						case "v1_8_R1": jb = new JsonBuilder_1_8_R1(Config.update_available(latestVersion)); break;
						case "v1_8_R2": jb = new JsonBuilder_1_8_R2(Config.update_available(latestVersion)); break;
						case "v1_8_R3": jb = new JsonBuilder_1_8_R3(Config.update_available(latestVersion)); break;
						case "v1_9_R1": jb = new JsonBuilder_1_9_R1(Config.update_available(latestVersion)); break;
						default: return;
					}
					jb.sendJson(p);
				}
			}
			
		} else if (result == UpdateCheckerResult.FALSE) {
			latestVersion = "";
			downloadLink = "";
			isUpdateNeeded = false;
			Bukkit.getConsoleSender().sendMessage("[ShopChest] " + Config.no_new_update());
		} else {
			latestVersion = "";
			downloadLink = "";
			isUpdateNeeded = false;
			Bukkit.getConsoleSender().sendMessage("[ShopChest] " + Config.update_check_error());
		}
		
		for (Player p : getServer().getOnlinePlayers()) {
			if (perm.has(p, "shopchest.broadcast")) {
				if (broadcast != null) {
					for (String message : broadcast) {
						p.sendMessage(message);
					}
				}
			}
		}
		
		if (broadcast != null) {
			for (String message : broadcast) {
				Bukkit.getConsoleSender().sendMessage("[ShopChest] " + message);
			}
		}

		File itemNamesFile = new File(getDataFolder(), "item_names.txt");
		
		if (!itemNamesFile.exists())
			try {itemNamesFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
		
		copy(getResource("item_names.txt"), itemNamesFile);
					
		
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
		getServer().getPluginManager().registerEvents(new ItemCustomNameListener(), this);
		
		if (getServer().getPluginManager().getPlugin("ClearLag") != null) getServer().getPluginManager().registerEvents(new RegenerateShopItemAfterRemove(), this);
	}

	
	@Override
	public void onDisable() {
		utils.removeShops();
	}
	
	private void initializeShops() {
		int count = 0;
		
		for (int id = 1; id < sqlite.getHighestID() + 1; id++) {
			
			try {
				Shop shop = sqlite.getShop(id);
				shop.createHologram();
				shop.createItem();			
				ShopUtils.addShop(shop);
			} catch (NullPointerException e) {
				continue;
			}
			
			count++;
			
		}
		
		logger.info("Initialized " + String.valueOf(count) + " Shops");
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
