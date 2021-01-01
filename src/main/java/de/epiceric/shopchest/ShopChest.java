package de.epiceric.shopchest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.palmergames.bukkit.towny.Towny;
import com.plotsquared.core.PlotSquared;
import com.wasteofplastic.askyblock.ASkyBlock;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import de.epiceric.shopchest.command.ShopCommand;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.HologramFormat;
import de.epiceric.shopchest.event.ShopInitializedEvent;
import de.epiceric.shopchest.external.BentoBoxShopFlag;
import de.epiceric.shopchest.external.PlotSquaredOldShopFlag;
import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.external.WorldGuardShopFlag;
import de.epiceric.shopchest.external.listeners.ASkyBlockListener;
import de.epiceric.shopchest.external.listeners.GriefPreventionListener;
import de.epiceric.shopchest.external.listeners.IslandWorldListener;
import de.epiceric.shopchest.external.listeners.PlotSquaredListener;
import de.epiceric.shopchest.external.listeners.TownyListener;
import de.epiceric.shopchest.external.listeners.USkyBlockListener;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.listeners.AreaShopListener;
import de.epiceric.shopchest.listeners.BentoBoxListener;
import de.epiceric.shopchest.listeners.BlockExplodeListener;
import de.epiceric.shopchest.listeners.ChestProtectListener;
import de.epiceric.shopchest.listeners.CreativeModeListener;
import de.epiceric.shopchest.listeners.NotifyPlayerOnJoinListener;
import de.epiceric.shopchest.listeners.ShopInteractListener;
import de.epiceric.shopchest.listeners.ShopItemListener;
import de.epiceric.shopchest.listeners.ShopUpdateListener;
import de.epiceric.shopchest.listeners.WorldGuardListener;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.sql.MySQL;
import de.epiceric.shopchest.sql.SQLite;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUpdater;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import de.epiceric.shopchest.utils.Utils;
import fr.xephi.authme.AuthMe;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.wiefferink.areashop.AreaShop;
import net.milkbowl.vault.economy.Economy;
import pl.islandworld.IslandWorld;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;
import world.bentobox.bentobox.BentoBox;

public class ShopChest extends JavaPlugin {

    private static ShopChest instance;

    private Config config;
    private HologramFormat hologramFormat;
    private ShopCommand shopCommand;
    private Economy econ = null;
    private Database database;
    private boolean isUpdateNeeded = false;
    private String latestVersion = "";
    private String downloadLink = "";
    private ShopUtils shopUtils;
    private FileWriter fw;
    private Plugin worldGuard;
    private Towny towny;
    private AuthMe authMe;
    private uSkyBlockAPI uSkyBlock;
    private ASkyBlock aSkyBlock;
    private IslandWorld islandWorld;
    private GriefPrevention griefPrevention;
    private AreaShop areaShop;
    private BentoBox bentoBox;
    private ShopUpdater updater;
    private ExecutorService shopCreationThreadPool;

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

        if (Config.enableDebugLog) {
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

        worldGuard = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuard != null) {
            WorldGuardShopFlag.register(this);
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
            case "v1_12_R1":
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_14_R1":
            case "v1_15_R1":
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
                break;
            default:
                debug("Server version not officially supported: " + Utils.getServerVersion() + "!");
                debug("Plugin may still work, but more errors are expected!");
                getLogger().warning("Server version not officially supported: " + Utils.getServerVersion() + "!");
                getLogger().warning("Plugin may still work, but more errors are expected!");
        }

        shopUtils = new ShopUtils(this);
        saveResource("item_names.txt", true);
        LanguageUtils.load();

        File hologramFormatFile = new File(getDataFolder(), "hologram-format.yml");
        if (!hologramFormatFile.exists()) {
            saveResource("hologram-format.yml", false);
        }

        hologramFormat = new HologramFormat(this);
        shopCommand = new ShopCommand(this);
        shopCreationThreadPool = new ThreadPoolExecutor(0, 8,
                5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        
        loadExternalPlugins();
        loadMetrics();
        initDatabase();
        checkForUpdates();
        registerListeners();
        registerExternalListeners();
        initializeShops();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        updater = new ShopUpdater(this);
        updater.start();
    }

    @Override
    public void onDisable() {
        debug("Disabling ShopChest...");

        if (shopUtils == null) {
            // Plugin has not been fully enabled (probably due to errors),
            // so only close file writer.
            if (fw != null && Config.enableDebugLog) {
                try {
                    fw.close();
                } catch (IOException e) {
                    getLogger().severe("Failed to close FileWriter");
                    e.printStackTrace();
                }
            }
            return;
        }

        if (getShopCommand() != null) {
            getShopCommand().unregister();
        }

        ClickType.clear();

        if (updater != null) {
            debug("Stopping updater");
            updater.stop();
        }

        if (shopCreationThreadPool != null) {
            shopCreationThreadPool.shutdown();
        }

        shopUtils.removeShops();
        debug("Removed shops");

        if (database != null && database.isInitialized()) {
            if (database instanceof SQLite) {
                ((SQLite) database).vacuum();
            }

            database.disconnect();
        }

        if (fw != null && Config.enableDebugLog) {
            try {
                fw.close();
            } catch (IOException e) {
                getLogger().severe("Failed to close FileWriter");
                e.printStackTrace();
            }
        }
    }

    private void loadExternalPlugins() {
        Plugin townyPlugin = Bukkit.getServer().getPluginManager().getPlugin("Towny");
        if (townyPlugin instanceof Towny) {
            towny = (Towny) townyPlugin;
        }

        Plugin authMePlugin = Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
        if (authMePlugin instanceof AuthMe) {
            authMe = (AuthMe) authMePlugin;
        }

        Plugin uSkyBlockPlugin = Bukkit.getServer().getPluginManager().getPlugin("uSkyBlock");
        if (uSkyBlockPlugin instanceof uSkyBlockAPI) {
            uSkyBlock = (uSkyBlockAPI) uSkyBlockPlugin;
        }

        Plugin aSkyBlockPlugin = Bukkit.getServer().getPluginManager().getPlugin("ASkyBlock");
        if (aSkyBlockPlugin instanceof ASkyBlock) {
            aSkyBlock = (ASkyBlock) aSkyBlockPlugin;
        }

        Plugin islandWorldPlugin = Bukkit.getServer().getPluginManager().getPlugin("IslandWorld");
        if (islandWorldPlugin instanceof IslandWorld) {
            islandWorld = (IslandWorld) islandWorldPlugin;
        }

        Plugin griefPreventionPlugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (griefPreventionPlugin instanceof GriefPrevention) {
            griefPrevention = (GriefPrevention) griefPreventionPlugin;
        }

        Plugin areaShopPlugin = Bukkit.getServer().getPluginManager().getPlugin("AreaShop");
        if (areaShopPlugin instanceof AreaShop) {
            areaShop = (AreaShop) areaShopPlugin;
        }

        Plugin bentoBoxPlugin = getServer().getPluginManager().getPlugin("BentoBox");
        if (bentoBoxPlugin instanceof BentoBox) {
            bentoBox = (BentoBox) bentoBoxPlugin;
        }

        if (hasWorldGuard()) {
            WorldGuardWrapper.getInstance().registerEvents(this);
        }

        if (hasPlotSquared()) {
            try {
                Class.forName("com.plotsquared.core.PlotSquared");
                PlotSquaredShopFlag.register(this);
            } catch (ClassNotFoundException ex) {
                PlotSquaredOldShopFlag.register(this);
            }
        }

        if (hasBentoBox()) {
            BentoBoxShopFlag.register(this);
        }
    }

    private void loadMetrics() {
        debug("Initializing Metrics...");

        Metrics metrics = new Metrics(this, 1726);
        metrics.addCustomChart(new Metrics.SimplePie("creative_setting", () -> Config.creativeSelectItem ? "Enabled" : "Disabled"));
        metrics.addCustomChart(new Metrics.SimplePie("database_type", () -> Config.databaseType.toString()));
        metrics.addCustomChart(new Metrics.AdvancedPie("shop_type", () -> {
                int normal = 0;
                int admin = 0;

                for (Shop shop : shopUtils.getShops()) {
                    if (shop.getShopType() == ShopType.NORMAL) normal++;
                    else if (shop.getShopType() == ShopType.ADMIN) admin++;
                }

                Map<String, Integer> result = new HashMap<>();

                result.put("Admin", admin);
                result.put("Normal", normal);

                return result;
        }));
    }

    private void initDatabase() {
        if (Config.databaseType == Database.DatabaseType.SQLite) {
            debug("Using database type: SQLite");
            getLogger().info("Using SQLite");
            database = new SQLite(this);
        } else {
            debug("Using database type: MySQL");
            getLogger().info("Using MySQL");
            database = new MySQL(this);
            if (Config.databaseMySqlPingInterval > 0) {
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    @Override
                    public void run() {
                        if (database instanceof MySQL) {
                            ((MySQL) database).ping();
                        }
                    }
                }, Config.databaseMySqlPingInterval * 20L, Config.databaseMySqlPingInterval * 20L);
            }
        }
    }

    private void checkForUpdates() {
        if (!Config.enableUpdateChecker) {
            return;
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                UpdateChecker uc = new UpdateChecker(ShopChest.this);
                UpdateCheckerResult result = uc.check();

                switch (result) {
                    case TRUE:
                        latestVersion = uc.getVersion();
                        downloadLink = uc.getLink();
                        isUpdateNeeded = true;

                        getLogger().warning(String.format("Version %s is available! You are running version %s.",
                                latestVersion, getDescription().getVersion()));

                        for (Player p : getServer().getOnlinePlayers()) {
                            if (p.hasPermission(Permissions.UPDATE_NOTIFICATION)) {
                                Utils.sendUpdateMessage(ShopChest.this, p);
                            }
                        }
                        break;
                
                    case FALSE:
                        latestVersion = "";
                        downloadLink = "";
                        isUpdateNeeded = false;
                        break;

                    case ERROR:
                        latestVersion = "";
                        downloadLink = "";
                        isUpdateNeeded = false;
                        getLogger().severe("An error occurred while checking for updates.");
                        break;
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void registerListeners() {
        debug("Registering listeners...");
        getServer().getPluginManager().registerEvents(new ShopUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new NotifyPlayerOnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestProtectListener(this), this);
        getServer().getPluginManager().registerEvents(new CreativeModeListener(this), this);

        if (!Utils.getServerVersion().equals("v1_8_R1")) {
            getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
        }

        if (hasWorldGuard()) {
            getServer().getPluginManager().registerEvents(new WorldGuardListener(this), this);

            if (hasAreaShop()) {
                getServer().getPluginManager().registerEvents(new AreaShopListener(this), this);
            }
        }

        if (hasBentoBox()) {
            getServer().getPluginManager().registerEvents(new BentoBoxListener(this), this);
        }
    }

    private void registerExternalListeners() {
        if (hasASkyBlock())
            getServer().getPluginManager().registerEvents(new ASkyBlockListener(this), this);
        if (hasGriefPrevention())
            getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
        if (hasIslandWorld())
            getServer().getPluginManager().registerEvents(new IslandWorldListener(this), this);
        if (hasPlotSquared())
            getServer().getPluginManager().registerEvents(new PlotSquaredListener(this), this);
        if (hasTowny())
            getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        if (hasUSkyBlock())
            getServer().getPluginManager().registerEvents(new USkyBlockListener(this), this);
        if (hasWorldGuard())
            getServer().getPluginManager().registerEvents(new de.epiceric.shopchest.external.listeners.WorldGuardListener(this), this);
        if (hasBentoBox())
            getServer().getPluginManager().registerEvents(new de.epiceric.shopchest.external.listeners.BentoBoxListener(this), this);
    }

    /**
     * Initializes the shops
     */
    private void initializeShops() {
        getShopDatabase().connect(new Callback<Integer>(this) {
            @Override
            public void onResult(Integer result) {
                Chunk[] loadedChunks = getServer().getWorlds().stream().map(World::getLoadedChunks)
                        .flatMap(Stream::of).toArray(Chunk[]::new);

                shopUtils.loadShopAmounts(new Callback<Map<UUID,Integer>>(ShopChest.this) {
                    @Override
                    public void onResult(Map<UUID, Integer> result) {
                        getLogger().info("Loaded shop amounts");
                        debug("Loaded shop amounts");
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {
                        getLogger().severe("Failed to load shop amounts. Shop limits will not be working correctly!");
                        if (throwable != null) getLogger().severe(throwable.getMessage());
                    }
                });

                shopUtils.loadShops(loadedChunks, new Callback<Integer>(ShopChest.this) {
                    @Override
                    public void onResult(Integer result) {
                        getServer().getPluginManager().callEvent(new ShopInitializedEvent(result));
                        getLogger().info("Loaded " + result + " shops in already loaded chunks");
                        debug("Loaded " + result + " shops in already loaded chunks");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        getLogger().severe("Failed to load shops in already loaded chunks");
                        if (throwable != null) getLogger().severe(throwable.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                // Database connection probably failed => disable plugin to prevent more errors
                getLogger().severe("No database access. Disabling ShopChest");
                if (throwable != null) getLogger().severe(throwable.getMessage());
                getServer().getPluginManager().disablePlugin(ShopChest.this);
            }
        });
    }

    /**
     * Print a message to the <i>/plugins/ShopChest/debug.txt</i> file
     * @param message Message to print
     */
    public void debug(String message) {
        if (Config.enableDebugLog && fw != null) {
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
        if (Config.enableDebugLog && fw != null) {
            PrintWriter pw = new PrintWriter(fw);
            throwable.printStackTrace(pw);
            pw.flush();
        }
    }

    /**
     * @return A thread pool for executing shop creation tasks
     */
    public ExecutorService getShopCreationThreadPool() {
        return shopCreationThreadPool;
    }

    public HologramFormat getHologramFormat() {
        return hologramFormat;
    }

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
     * @return Whether the plugin 'AreaShop' is enabled
     */
    public boolean hasAreaShop() {
        return areaShop != null && areaShop.isEnabled();
    }

    /**
     * @return Whether the plugin 'GriefPrevention' is enabled
     */
    public boolean hasGriefPrevention() {
        return griefPrevention != null && griefPrevention.isEnabled();
    }

    /**
     * @return An instance of {@link GriefPrevention} or {@code null} if GriefPrevention is not enabled
     */
    public GriefPrevention getGriefPrevention() {
        return griefPrevention;
    }

    /**
     * @return Whether the plugin 'IslandWorld' is enabled
     */
    public boolean hasIslandWorld() {
        return islandWorld != null && islandWorld.isEnabled();
    }
    /**
     * @return Whether the plugin 'ASkyBlock' is enabled
     */
    public boolean hasASkyBlock() {
        return aSkyBlock != null && aSkyBlock.isEnabled();
    }

    /**
     * @return Whether the plugin 'uSkyBlock' is enabled
     */
    public boolean hasUSkyBlock() {
        return uSkyBlock != null && uSkyBlock.isEnabled();
    }

    /**
     * @return An instance of {@link uSkyBlockAPI} or {@code null} if uSkyBlock is not enabled
     */
    public uSkyBlockAPI getUSkyBlock() {
        return uSkyBlock;
    }

    /**
     * @return Whether the plugin 'PlotSquared' is enabled
     */
    public boolean hasPlotSquared() {
        if (Utils.getMajorVersion() < 13) {
            // Supported PlotSquared versions don't support versions below 1.13
            return false;
        }
        Plugin p = getServer().getPluginManager().getPlugin("PlotSquared");
        return p != null && p.isEnabled();
    }

    /**
     * @return Whether the plugin 'AuthMe' is enabled
     */
    public boolean hasAuthMe() {
        return authMe != null && authMe.isEnabled();
    }
    /**
     * @return Whether the plugin 'Towny' is enabled
     */
    public boolean hasTowny() {
        return towny != null && towny.isEnabled();
    }

    /**
     * @return Whether the plugin 'WorldGuard' is enabled
     */
    public boolean hasWorldGuard() {
        return worldGuard != null && worldGuard.isEnabled();
    }

    /**
     * @return Whether the plugin 'WorldGuard' is enabled
     */
    public boolean hasBentoBox() {
        return bentoBox != null && bentoBox.isEnabled();
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
     * @return The {@link Config} of ShopChest
     */
    public Config getShopChestConfig() {
        return config;
    }
}
