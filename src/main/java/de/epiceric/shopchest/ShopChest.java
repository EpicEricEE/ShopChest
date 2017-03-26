package de.epiceric.shopchest;

import com.intellectualcrafters.plot.IPlotMain;
import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
import de.epiceric.shopchest.utils.*;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import de.epiceric.shopchest.worldguard.ShopFlag;
import fr.xephi.authme.AuthMe;
import net.milkbowl.vault.economy.Economy;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ShopChest extends JavaPlugin {

    private static ShopChest instance;

    private Config config = null;
    private ShopCommand shopCommand;
    private Economy econ = null;
    private Database database;
    private boolean isUpdateNeeded = false;
    private String latestVersion = "";
    private String downloadLink = "";
    private ShopUtils shopUtils;
    private FileWriter fw;
    private WorldGuardPlugin worldGuard;
    private Towny towny;
    private AuthMe authMe;
    private ShopUpdater updater;

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

    @Override
    public void onLoad() {
        instance = this;

        config = new Config(this);

        if (config.enable_debug_log) {
            File debugLogFile = new File(getDataFolder(), "debug.txt");

            try {
                if (!debugLogFile.exists()) {
                    debugLogFile.createNewFile();
                }

                new PrintWriter(debugLogFile).close();

                fw = new FileWriter(debugLogFile, true);
            } catch (IOException e) {
                getLogger().info("Failed to instantiate FileWriter");
                e.printStackTrace();
            }
        }

        debug("Loading ShopChest version " + getDescription().getVersion());

        Plugin worldGuardPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin instanceof WorldGuardPlugin) {
            worldGuard = (WorldGuardPlugin) worldGuardPlugin;
            ShopFlag.init(this, true);
        }
    }

    @Override
    public void onEnable() {
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

        switch (Utils.getServerVersion()) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_10_R1":
            case "v1_11_R1":
                break;
            default:
                debug("Server version not officially supported: " + Utils.getServerVersion() + "!");
                debug("Plugin may still work, but more errors are expected!");
                getLogger().warning("Server version not officially supported: " + Utils.getServerVersion() + "!");
                getLogger().warning("Plugin may still work, but more errors are expected!");
        }

        if (worldGuard != null && !ShopFlag.isLoaded()) {
            ShopFlag.init(this, false);

            try {
                // Reload WorldGuard regions, so that custom flags are applied
                for (World world : getServer().getWorlds()) {
                    worldGuard.getRegionManager(world).load();
                }
            } catch (Exception e) {
                getLogger().severe("Failed to reload WorldGuard region manager. WorldGuard support will probably not work!");
                debug("Failed to load WorldGuard region manager");
                debug(e);
            }
        }

        Plugin townyPlugin = Bukkit.getServer().getPluginManager().getPlugin("Towny");
        if (townyPlugin instanceof Towny) {
            towny = (Towny) townyPlugin;
        }

        Plugin authMePlugin = Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
        if (authMePlugin instanceof AuthMe) {
            authMe = (AuthMe) authMePlugin;
        }

        debug("Loading utils and extras...");

        LanguageUtils.load();
        saveResource("item_names.txt", true);

        shopUtils = new ShopUtils(this);

        debug("Initializing Metrics...");
        Metrics metrics = new Metrics(this);

        metrics.addCustomChart(new Metrics.AdvancedPie("shop_type") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hashMap) {
                int normal = 0;
                int admin = 0;

                for (Shop shop : shopUtils.getShops()) {
                    if (shop.getShopType() == ShopType.NORMAL) normal++;
                    else if (shop.getShopType() == ShopType.ADMIN) admin++;
                }

                hashMap.put("Admin", admin);
                hashMap.put("Normal", normal);

                return hashMap;
            }
        });

        metrics.addCustomChart(new Metrics.SimplePie("database_type") {
            @Override
            public String getValue() {
                return config.database_type.toString();
            }
        });

        if (config.database_type == Database.DatabaseType.SQLite) {
            debug("Using database type: SQLite");
            getLogger().info("Using SQLite");
            database = new SQLite(this);
        } else {
            debug("Using database type: MySQL");
            getLogger().info("Using MySQL");
            database = new MySQL(this);
            if (config.database_mysql_ping_interval > 0) {
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    @Override
                    public void run() {
                        if (database instanceof MySQL) {
                            ((MySQL) database).ping();
                        }
                    }
                }, config.database_mysql_ping_interval * 20L, config.database_mysql_ping_interval * 20L);
            }
        }

        if (config.auto_reload_time > 0) {
           Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    debug("Auto reloading shops...");

                    ShopReloadEvent event = new ShopReloadEvent(Bukkit.getConsoleSender());
                    Bukkit.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) shopUtils.reloadShops(true, false, null);
                }
            }, config.auto_reload_time * 20, config.auto_reload_time * 20);
        }

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
                        if (p.hasPermission(Permissions.UPDATE_NOTIFICATION)) {
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

        shopCommand = new ShopCommand(this);

        debug("Registering listeners...");
        getServer().getPluginManager().registerEvents(new ShopUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new NotifyPlayerOnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestProtectListener(this, worldGuard), this);

        if (!Utils.getServerVersion().equals("v1_8_R1"))
            getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);

        if (hasWorldGuard())
            getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);

        initializeShops();

        updater = new ShopUpdater(this);
        updater.start();
    }

    @Override
    public void onDisable() {
        debug("Disabling ShopChest...", true);

        if (updater != null) {
            debug("Stopping updater", true);
            updater.cancel();
        }

        if (database != null) {
            for (Shop shop : shopUtils.getShops()) {
                shopUtils.removeShop(shop, false, true);
                debug("Removed shop (#" + shop.getID() + ")", true);
            }

            database.disconnect();
        }

        if (fw != null && config.enable_debug_log) {
            try {
                fw.close();
            } catch (IOException e) {
                getLogger().severe("Failed to close FileWriter");
                e.printStackTrace();
            }
        }
    }

    /**
     * Print a message to the <i>/plugins/ShopChest/debug.txt</i> file
     * @param message Message to print
     * @param useCurrentThread Whether the current thread should be used. If set to false, a new synchronized task will be created.
     */
    public void debug(final String message, boolean useCurrentThread) {
        if (config.enable_debug_log && fw != null) {
            if (useCurrentThread) {
                debug(message);
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        debug(message);
                    }
                }.runTask(this);
            }
        }
    }

    /**
     * Print a message to the <i>/plugins/ShopChest/debug.txt</i> file
     * @param message Message to print
     */
    public void debug(String message) {
        if (config.enable_debug_log && fw != null) {
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
     * @param useCurrentThread Whether the current thread should be used. If set to false, a new synchronized task will be created.
     */
    public void debug(final Throwable throwable, boolean useCurrentThread) {
        if (config.enable_debug_log && fw != null) {
            if (useCurrentThread) {
                debug(throwable);
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PrintWriter pw = new PrintWriter(fw);
                        throwable.printStackTrace(pw);
                        pw.flush();
                    }
                }.runTask(this);
            }
        }
    }

    /**
     * Print a {@link Throwable}'s stacktrace to the <i>/plugins/ShopChest/debug.txt</i> file
     * @param throwable {@link Throwable} whose stacktrace will be printed
     */
    public void debug(Throwable throwable) {
        if (config.enable_debug_log && fw != null) {
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
        shopUtils.reloadShops(false, false, new Callback(this) {
            @Override
            public void onResult(Object result) {
                if (result instanceof Integer) {
                    int count = (int) result;
                    getLogger().info("Initialized " + count + " Shops");
                    debug("Initialized " + count + " Shops");
                }
            }
        });

    }

    /**
     * @return The {@link ShopCommand}
     */
    public ShopCommand getShopCommand() {
        return shopCommand;
    }

    /**
     * @return The {@link ShopUpdater} that schedules hologram and item updates
     */
    public ShopUpdater getUpdater() {
        return updater;
    }

    /**
     * Set the {@link ShopUpdater} that schedules hologram and item updates
     */
    public void setUpdater(ShopUpdater updater) {
        this.updater = updater;
    }

    /**
     * @return Whether the plugin 'AuthMe' is enabled
     */
    public boolean hasAuthMe() {
        return authMe != null;
    }

    /**
     * @return Whether the plugin 'PlotSquared' is enabled
     */
    public boolean hasPlotSquared() {
        return getServer().getPluginManager().getPlugin("PlotSquared") != null;
    }

    /**
     * @return An instance of {@link AuthMe} or {@code null} if AuthMe is not enabled
     */
    public AuthMe getAuthMe() {
        return authMe;
    }

    /**
     * @return Whether the plugin 'Towny' is enabled
     */
    public boolean hasTowny() {
        return towny != null;
    }

    /**
     * @return An instance of {@link Towny} or {@code null} if Towny is not enabled
     */
    public Towny getTowny() {
        return towny;
    }

    /**
     * @return Whether the plugin 'WorldGuard' is enabled
     */
    public boolean hasWorldGuard() {
        return worldGuard != null;
    }

    /**
     * @return An instance of {@link WorldGuardPlugin} or {@code null} if WorldGuard is not enabled
     */
    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
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
     * @return ShopChest's shop database
     */
    public Database getShopDatabase() {
        return database;
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
