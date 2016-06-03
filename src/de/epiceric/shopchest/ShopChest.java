package de.epiceric.shopchest;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.*;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.interfaces.jsonbuilder.*;
import de.epiceric.shopchest.interfaces.utils.*;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.sql.MySQL;
import de.epiceric.shopchest.sql.SQLite;
import de.epiceric.shopchest.utils.Metrics;
import de.epiceric.shopchest.utils.Metrics.Graph;
import de.epiceric.shopchest.utils.Metrics.Plotter;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ShopChest extends JavaPlugin {

    public static Logger logger;
    public static Economy econ = null;
    public static Permission perm = null;
    public static boolean lockette = false;
    public static boolean lwc = false;
    public static Database database;
    public static boolean isUpdateNeeded = false;
    public static String latestVersion = "";
    public static String downloadLink = "";
    public static String[] broadcast = null;
    public static Utils utils;
    private static ShopChest instance;

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
        instance = this;

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Could not find plugin 'Vault'!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            logger.severe("Could not find any Vault dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Metrics metrics = new Metrics(this);
            Graph shopType = metrics.createGraph("Shop Type");
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

            Graph databaseType = metrics.createGraph("Database Type");
            databaseType.addPlotter(new Plotter("SQLite") {

                @Override
                public int getValue() {
                    if (Config.database_type() == Database.DatabaseType.SQLite)
                        return 1;

                    return 0;
                }

            });

            databaseType.addPlotter(new Plotter("MySQL") {

                @Override
                public int getValue() {
                    if (Config.database_type() == Database.DatabaseType.MySQL)
                        return 1;

                    return 0;
                }

            });

            metrics.start();
        } catch (IOException e) {
            logger.severe("Could not submit stats.");
        }

        saveResource("item_names.txt", true);
        reloadConfig();
        saveDefaultConfig();

        if (Config.database_type() == Database.DatabaseType.SQLite){
            logger.info("Using SQLite");
            database = new SQLite(this);
        } else {
            logger.info("Using MySQL");
            database = new MySQL(this);
        }

        switch (Utils.getVersion(getServer())) {

            case "v1_8_R1":
                utils = new Utils_1_8_R1();
                break;
            case "v1_8_R2":
                utils = new Utils_1_8_R2();
                break;
            case "v1_8_R3":
                utils = new Utils_1_8_R3();
                break;
            case "v1_9_R1":
                utils = new Utils_1_9_R1();
                break;
            case "v1_9_R2":
                utils = new Utils_1_9_R2();
                break;
            default:
                logger.severe("Incompatible Server Version: " + Utils.getVersion(getServer()) + "!");
                getServer().getPluginManager().disablePlugin(this);
                return;
        }

        lockette = getServer().getPluginManager().getPlugin("Lockette") != null;
        lwc = getServer().getPluginManager().getPlugin("LWC") != null;

        setupPermissions();

        UpdateChecker uc = new UpdateChecker(this, getDescription().getWebsite());
        UpdateCheckerResult result = uc.updateNeeded();

        if (Config.enable_broadcast()) broadcast = uc.getBroadcast();

        Bukkit.getConsoleSender().sendMessage("[ShopChest] " + Config.checking_update());
        if (result == UpdateCheckerResult.TRUE) {
            latestVersion = uc.getVersion();
            downloadLink = uc.getLink();
            isUpdateNeeded = true;
            Bukkit.getConsoleSender().sendMessage("[ShopChest] " + ChatColor.translateAlternateColorCodes('&', Config.update_available(latestVersion)));

            for (Player p : getServer().getOnlinePlayers()) {
                if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
                    JsonBuilder jb;
                    switch (Utils.getVersion(getServer())) {
                        case "v1_8_R1":
                            jb = new JsonBuilder_1_8_R1(Config.update_available(latestVersion));
                            break;
                        case "v1_8_R2":
                            jb = new JsonBuilder_1_8_R2(Config.update_available(latestVersion));
                            break;
                        case "v1_8_R3":
                            jb = new JsonBuilder_1_8_R3(Config.update_available(latestVersion));
                            break;
                        case "v1_9_R1":
                            jb = new JsonBuilder_1_9_R1(Config.update_available(latestVersion));
                            break;
                        case "v1_9_R2":
                            jb = new JsonBuilder_1_9_R2(Config.update_available(latestVersion));
                            break;
                        default:
                            return;
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

        if (getServer().getPluginManager().getPlugin("ClearLag") != null)
            getServer().getPluginManager().registerEvents(new RegenerateShopItemAfterRemove(), this);

        if (getServer().getPluginManager().getPlugin("LWC") != null)
            new LWCMagnetListener().initializeListener();
    }

    @Override
    public void onDisable() {
        utils.removeShops();
    }

    private void initializeShops() {
        int count = 0;

        for (int id = 1; id < database.getHighestID() + 1; id++) {

            try {
                Shop shop = (Shop) database.get(id, Database.ShopInfo.SHOP);
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


}
