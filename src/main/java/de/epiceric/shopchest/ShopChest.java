package de.epiceric.shopchest;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.event.ShopReloadEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.listeners.*;
import de.epiceric.shopchest.nms.JsonBuilder;
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
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ShopChest extends JavaPlugin {

    private static ShopChest instance;

    private Config config = null;
    private Economy econ = null;
    private Permission perm = null;
    private boolean lwc = false;
    private Database database;
    private boolean isUpdateNeeded = false;
    private String latestVersion = "";
    private String downloadLink = "";
    private ShopUtils shopUtils;
    private File debugLogFile;
    private FileWriter fw;

    /**
     * @return An instance of ShopChest
     */
    public static ShopChest getInstance() {
        return instance;
    }

    /**
     * Sets up the economy of Vault
     * @return Whether an economy plugin has been registered
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Sets up the permissions of Vault
     * @return Whether a permissions plugin has been registered
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perm = rsp.getProvider();
        return perm != null;
    }


    @Override
    public void onEnable() {
        instance = this;

        config = new Config(this);

        if (config.enable_debug_log) {
            debugLogFile = new File(getDataFolder(), "debug.txt");

            try {
                if (!debugLogFile.exists()) {
                    debugLogFile.createNewFile();
                }

                new PrintWriter(debugLogFile).close();

                fw = new FileWriter(debugLogFile, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        debug("Enabling ShopChest version " + getDescription().getVersion());

        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            debug("Could not find plugin \"Vault\"");
            getLogger().severe("Could not find plugin \"Vault\"");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            debug("Could not find any Vault economy dependency!");
            getLogger().severe("Could not find any Vault economy dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupPermissions()) {
            debug("Could not find any Vault permission dependency!");
            getLogger().severe("Could not find any Vault permission dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        switch (Utils.getServerVersion()) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_10_R1":
                break;
            default:
                debug("Server version not officially supported: " + Utils.getServerVersion() + "!");
                debug("Plugin may still work, but more errors are expected!");
                getLogger().warning("Server version not officially supported: " + Utils.getServerVersion() + "!");
                getLogger().warning("Plugin may still work, but more errors are expected!");
        }

        debug("Loading utils and extras...");

        LanguageUtils.load();
        saveResource("item_names.txt", true);

        shopUtils = new ShopUtils(this);

        try {
            debug("Initializing Metrics...");

            Metrics metrics = new Metrics(this);
            Graph shopType = metrics.createGraph("Shop Type");
            shopType.addPlotter(new Plotter("Normal") {

                @Override
                public int getValue() {
                    int value = 0;

                    for (Shop shop : shopUtils.getShops()) {
                        if (shop.getShopType() == ShopType.NORMAL) value++;
                    }

                    return value;
                }

            });

            shopType.addPlotter(new Plotter("Admin") {

                @Override
                public int getValue() {
                    int value = 0;

                    for (Shop shop : shopUtils.getShops()) {
                        if (shop.getShopType() == ShopType.ADMIN) value++;
                    }

                    return value;
                }

            });

            Graph databaseType = metrics.createGraph("Database Type");
            databaseType.addPlotter(new Plotter("SQLite") {

                @Override
                public int getValue() {
                    if (config.database_type == Database.DatabaseType.SQLite)
                        return 1;

                    return 0;
                }

            });

            databaseType.addPlotter(new Plotter("MySQL") {

                @Override
                public int getValue() {
                    if (config.database_type == Database.DatabaseType.MySQL)
                        return 1;

                    return 0;
                }

            });

            metrics.start();
        } catch (IOException e) {
            debug("Metrics: Failed to submit stats");
            getLogger().severe("Could not submit stats.");
        }

        if (config.database_type == Database.DatabaseType.SQLite) {
            debug("Using database type: SQLite");
            getLogger().info("Using SQLite");
            database = new SQLite(this);
        } else {
            debug("Using database type: MySQL");
            getLogger().info("Using MySQL");
            database = new MySQL(this);
        }

        if (config.auto_reload_time > 0) {
           Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    debug("Auto reloading shops...");

                    ShopReloadEvent event = new ShopReloadEvent(Bukkit.getConsoleSender());
                    Bukkit.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) getLogger().info("Successfully reloaded " + String.valueOf(shopUtils.reloadShops(true)) + " shops.");
                }
            }, config.auto_reload_time * 20, config.auto_reload_time * 20);
        }

        lwc = getServer().getPluginManager().isPluginEnabled("LWC");

        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                UpdateChecker uc = new UpdateChecker(ShopChest.this);
                UpdateCheckerResult result = uc.check();

                Bukkit.getConsoleSender().sendMessage("[ShopChest] " + LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CHECKING));
                if (result == UpdateCheckerResult.TRUE) {
                    latestVersion = uc.getVersion();
                    downloadLink = uc.getLink();
                    isUpdateNeeded = true;
                    Bukkit.getConsoleSender().sendMessage("[ShopChest] " + LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, latestVersion)));

                    for (Player p : getServer().getOnlinePlayers()) {
                        if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
                            JsonBuilder jb = new JsonBuilder(ShopChest.this, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, latestVersion)), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), downloadLink);
                            jb.sendJson(p);
                        }
                    }

                } else if (result == UpdateCheckerResult.FALSE) {
                    latestVersion = "";
                    downloadLink = "";
                    isUpdateNeeded = false;
                    Bukkit.getConsoleSender().sendMessage("[ShopChest] " + LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_NO_UPDATE));
                } else {
                    latestVersion = "";
                    downloadLink = "";
                    isUpdateNeeded = false;
                    Bukkit.getConsoleSender().sendMessage("[ShopChest] " + LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_ERROR));
                }
            }
        });

        try {
            debug("Trying to register command \"/" + config.main_command_name + "\"");
            ShopCommand.registerCommand(new ShopCommand(this, config.main_command_name, "Manage Shops.", "", new ArrayList<String>()), this);
        } catch (Exception e) {
            debug("Failed to register command");
            debug(e);
            e.printStackTrace();
        }

        initializeShops();

        debug("Registering listeners...");
        getServer().getPluginManager().registerEvents(new HologramUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemProtectListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new NotifyUpdateOnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestProtectListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemCustomNameListener(), this);

        if (getServer().getPluginManager().isPluginEnabled("ClearLag"))
            getServer().getPluginManager().registerEvents(new ClearLagListener(), this);

        if (lwc) new LWCMagnetListener(this).initializeListener();
    }

    @Override
    public void onDisable() {
        debug("Disabling ShopChest...");

        int highestId = database.getHighestID();

        for (int i = 1; i <= highestId; i++) {
            for (Shop shop : shopUtils.getShops()) {
                if (shop.getID() == i) {
                    shopUtils.removeShop(shop, false);
                    debug("Removed shop (#" + shop.getID() + ")");
                }
            }
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (item.hasMetadata("shopItem")) {
                        if (item.isValid()) {
                            debug("Removing not removed shop item (#" +
                                    (item.hasMetadata("shopId") ? item.getMetadata("shopId").get(0).asString() : "?") + ")");

                            item.remove();
                        }
                    }
                }
            }
        }

        if (config.enable_debug_log) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Item) {
                        Item item = (Item) entity;
                        if (item.hasMetadata("shopItem")) {
                            if (item.isValid()) {
                                debug("Shop item still valid (#" +
                                        (item.hasMetadata("shopId") ? item.getMetadata("shopId").get(0).asString() : "?") + ")");
                            }
                        }
                    }
                }
            }
        }

        database.disconnect();

        if (fw != null && config.enable_debug_log) {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Print a message to the <i>/plugins/ShopChest/debug.txt</i> file
     * @param message Message to print
     */
    public void debug(String message) {
        if (config.enable_debug_log) {
            try {
                Calendar c = Calendar.getInstance();
                String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(c.getTime());
                fw.write(String.format("[%s] %s\r\n", timestamp, message));
                fw.flush();
            } catch (IOException e) {
                getLogger().severe("Failed to print debug message.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Print a {@link Throwable}'s stacktrace to the <i>/plugins/ShopChest/debug.txt</i> file
     * @param throwable {@link Throwable} whose stacktrace will be printed
     */
    public void debug(Throwable throwable) {
        if (config.enable_debug_log) {
            PrintWriter pw = new PrintWriter(fw);
            throwable.printStackTrace(pw);
            pw.flush();
        }
    }


    /**
     * Initializes the shops
     */
    private void initializeShops() {
        debug("Initializing Shops...");
        int count = shopUtils.reloadShops(false);
        getLogger().info("Initialized " + count + " Shops");
        debug("Initialized " + count + " Shops");
    }

    /**
     * @return ShopChest's {@link ShopUtils} containing some important methods
     */
    public ShopUtils getShopUtils() {
        return shopUtils;
    }

    /**
     * @return Registered Economy of Vault
     */
    public Economy getEconomy() {
        return econ;
    }

    /**
     * @return Registered Permission of Vault
     */
    public Permission getPermission() {
        return perm;
    }

    /**
     * @return ShopChest's shop database
     */
    public Database getShopDatabase() {
        return database;
    }

    /**
     * @return Whether LWC is available
     */
    public boolean hasLWC() {
        return lwc;
    }

    /**
     * @return Whether an update is needed (will return false if not checked)
     */
    public boolean isUpdateNeeded() {
        return isUpdateNeeded;
    }

    /**
     * Set whether an update is needed
     * @param isUpdateNeeded Whether an update should be needed
     */
    public void setUpdateNeeded(boolean isUpdateNeeded) {
        this.isUpdateNeeded = isUpdateNeeded;
    }

    /**
     * @return The latest version of ShopChest (will return null if not checked or if no update is available)
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Set the latest version
     * @param latestVersion Version to set as latest version
     */
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    /**
     * @return The download link of the latest version (will return null if not checked or if no update is available)
     */
    public String getDownloadLink() {
        return downloadLink;
    }

    /**
     * Set the download Link of the latest version (will return null if not checked or if no update is available)
     * @param downloadLink Link to set as Download Link
     */
    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    /**
     * @return The {@link Config} of ShopChset
     */
    public Config getShopChestConfig() {
        return config;
    }

    /**
     * <p>Provides a reader for a text file located inside the jar.</p>
     * The returned reader will read text with the UTF-8 charset.
     * @param file the filename of the resource to load
     * @return null if {@link #getResource(String)} returns null
     * @throws IllegalArgumentException if file is null
     */
    public Reader _getTextResource(String file) throws IllegalArgumentException {
       return getTextResource(file);
    }
}
