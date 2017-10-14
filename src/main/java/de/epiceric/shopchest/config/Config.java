package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.ShopUpdater;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    /**
     * The quality of hologram and item updating (performance saving, or better quality)
     **/
    public static ShopUpdater.UpdateQuality updateQuality;

    /**
     * The item with which a player can click a shop to retrieve information
     **/
    public static ItemStack shopInfoItem;

    /**
     * The default value for the custom WorldGuard flag 'create-shop'
     **/
    public static boolean wgAllowCreateShopDefault;

    /**
     * The default value for the custom WorldGuard flag 'use-admin-shop'
     **/
    public static boolean wgAllowUseAdminShopDefault;

    /**
     * The default value for the custom WorldGuard flag 'use-shop'
     **/
    public static boolean wgAllowUseShopDefault;

    /**
     * The types of town plots residents are allowed to create shops in
     **/
    public static List<String> townyShopPlotsResidents;

    /**
     * The types of town plots the mayor is allowed to create shops in
     **/
    public static List<String> townyShopPlotsMayor;

    /**
     * The types of town plots the king is allowed to create shops in
     **/
    public static List<String> townyShopPlotsKing;

    /**
     * The events of AreaShop when shops in that region should be removed
     **/
    public static List<String> areashopRemoveShopEvents;

    /**
     * The hostname used in ShopChest's MySQL database
     **/
    public static String databaseMySqlHost;

    /**
     * The port used for ShopChest's MySQL database
     **/
    public static int databaseMySqlPort;

    /**
     * The database used for ShopChest's MySQL database
     **/
    public static String databaseMySqlDatabase;

    /**
     * The username used in ShopChest's MySQL database
     **/
    public static String databaseMySqlUsername;

    /**
     * The password used in ShopChest's MySQL database
     **/
    public static String databaseMySqlPassword;

    /**
     * The database type used for ShopChest.
     **/
    public static Database.DatabaseType databaseType;

    /**
     * The interval in seconds, a ping is sent to the MySQL server
     **/
    public static int databaseMySqlPingInterval;

    /**
     * <p>The minimum prices for certain items</p>
     * This returns a key set, which contains e.g "STONE", "STONE:1", of the <i>minimum-prices</i> section in ShopChest's config.
     * To actually retrieve the minimum price for an item, you have to get the double {@code minimum-prices.<key>}.
     **/
    public static Set<String> minimumPrices;

    /**
     * <p>The maximum prices for certain items</p>
     * This returns a key set, which contains e.g "STONE", "STONE:1", of the {@code maximum-prices} section in ShopChest's config.
     * To actually retrieve the maximum price for an item, you have to get the double {@code maximum-prices.<key>}.
     **/
    public static Set<String> maximumPrices;

    /**
     * <p>List containing items, of which players can't create a shop</p>
     * If this list contains an item (e.g "STONE", "STONE:1"), it's in the blacklist.
     **/
    public static List<String> blacklist;

    /**
     * Whether prices may contain decimals
     **/
    public static boolean allowDecimalsInPrice;

    /**
     * Whether the buy price of a shop must be greater than or equal the sell price
     **/
    public static boolean buyGreaterOrEqualSell;

    /**
     * Whether shops should be protected by hoppers
     **/
    public static boolean hopperProtection;

    /**
     * Whether shops should be protected by explosions
     **/
    public static boolean explosionProtection;

    /**
     * Whether buys and sells must be confirmed
     **/
    public static boolean confirmShopping;

    /**
     * Whether quality mode should be enabled
     **/
    public static boolean enableQualityMode;

    /**
     * Whether hologram interaction should be enabled
     **/
    public static boolean enableHologramInteraction;

    /**
     * Whether the debug log file should be created
     **/
    public static boolean enableDebugLog;

    /**
     * Whether buys and sells should be logged in the database
     **/
    public static boolean enableEcomomyLog;

    /**
     * Whether WorldGuard integration should be enabled
     **/
    public static boolean enableWorldGuardIntegration;

    /**
     * Set whether the economy-log should be cleaned up on server start
     **/
    public static boolean cleanupEconomyLog;

    /**
     * Sets the time limit for cleaning up the economy log in days
     **/
    public static int cleanupEconomyLogDays;

    /**
     * Whether Towny integration should be enabled
     **/
    public static boolean enableTownyIntegration;

    /**
     * Whether AuthMe integration should be enabled
     **/
    public static boolean enableAuthMeIntegration;

    /**
     * Whether PlotSquared integration should be enabled
     **/
    public static boolean enablePlotsquaredIntegration;

    /**
     * Whether uSkyBlock integration should be enabled
     **/
    public static boolean enableUSkyblockIntegration;

    /**
     * Whether ASkyBlock integration should be enabled
     **/
    public static boolean enableASkyblockIntegration;

    /**
     * Whether IslandWorld integration should be enabled
     **/
    public static boolean enableIslandWorldIntegration;

    /**
     * Whether GriefPrevention integration should be enabled
     **/
    public static boolean enableGriefPreventionIntegration;

    /**
     * Whether AreaShop integration should be enabled
     **/
    public static boolean enableAreaShopIntegration;

    /**
     * Whether the vendor of the shop should get messages about buys and sells
     **/
    public static boolean enableVendorMessages;

    /**
     * Whether admin shops should be excluded of the shop limits
     **/
    public static boolean excludeAdminShops;

    /**
     * Whether the extension of a potion or tipped arrow (if available) should be appended to the item name.
     **/
    public static boolean appendPotionLevelToItemName;

    /**
     * Whether the shop items should be shown
     **/
    public static boolean showShopItems;

    /**
     * Whether players are allowed to sell/buy broken items
     **/
    public static boolean allowBrokenItems;

    /**
     * Whether only the shops a player has in sight should be shown to him
     **/
    public static boolean onlyShowShopsInSight;

    /**
     * Whether only the shop a player is looking at should be shown to him
     **/
    public static boolean onlyShowFirstShopInSight;

    /**
     * <p>Whether shops should automatically be removed from the database if an error occurred while loading</p>
     * (e.g. when no chest is found at a shop's location)
     */
    public static boolean removeShopOnError;

    /**
     * Whether the item amount should be calculated to fit the available money or inventory space
     **/
    public static boolean autoCalculateItemAmount;

    /**
     * <p>Whether the mouse buttons are inverted</p>
     * <b>Default:</b><br>
     * Right-Click: Buy<br>
     * Left-Click: Sell
     **/
    public static boolean invertMouseButtons;

    /**
     * Whether the hologram's location should be fixed at the bottom
     **/
    public static boolean hologramFixedBottom;

    /**
     * Amount every hologram should be lifted
     **/
    public static double hologramLift;

    /**
     * The maximum distance between a player and a shop to see the hologram
     **/
    public static double maximalDistance;

    /**
     * The maximum distance between a player and a shop to see the shop item
     **/
    public static double maximalItemDistance;

    /**
     * The price a player has to pay in order to create a normal shop
     **/
    public static double shopCreationPriceNormal;

    /**
     * The price a player has to pay in order to create an admin shop
     **/
    public static double shopCreationPriceAdmin;

    /**
     * The default shop limit for players whose limit is not set via a permission
     **/
    public static int defaultLimit;

    /**
     * The main command of ShopChest <i>(default: shop)</i>
     **/
    public static String mainCommandName;

    /**
     * The language file to use (e.g <i>en_US</i>, <i>de_DE</i>)
     **/
    public static String languageFile;

    /**
     * The language configuration of the currently selected language file
     */
    public static LanguageConfiguration langConfig;

    private ShopChest plugin;

    public Config(ShopChest plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

        reload(true, true, true);
    }

    /**
     * <p>Set a configuration value</p>
     * <i>Config is automatically reloaded</i>
     *
     * @param property Property to change
     * @param value    Value to set
     */
    public void set(String property, String value) {
        boolean langChange = (property.equalsIgnoreCase("language-file"));
        try {
            int intValue = Integer.parseInt(value);
            plugin.getConfig().set(property, intValue);

            plugin.saveConfig();
            reload(false, langChange, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            plugin.getConfig().set(property, doubleValue);

            plugin.saveConfig();
            reload(false, langChange, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            plugin.getConfig().set(property, boolValue);
        } else {
            plugin.getConfig().set(property, value);
        }

        plugin.saveConfig();

        reload(false, langChange, false);
    }

    /**
     * Add a value to a list in the config.yml.
     * If the list does not exist, a new list with the given value will be created
     *
     * @param property Location of the list
     * @param value    Value to add
     */
    public void add(String property, String value) {
        List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.add(intValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.add(doubleValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.add(boolValue);
        } else {
            list.add(value);
        }

        plugin.saveConfig();

        reload(false, false, false);
    }

    public void remove(String property, String value) {
        List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.remove(intValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.remove(doubleValue);

            plugin.saveConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not a double */ }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            boolean boolValue = Boolean.parseBoolean(value);
            list.remove(boolValue);
        } else {
            list.remove(value);
        }

        plugin.saveConfig();

        reload(false, false, false);
    }

    /**
     * Reload the configuration values from config.yml
     */
    public void reload(boolean firstLoad, boolean langReload, boolean showMessages) {
        plugin.reloadConfig();

        updateQuality = ShopUpdater.UpdateQuality.valueOf(plugin.getConfig().getString("update-quality"));
        shopInfoItem = ItemUtils.getItemStack(plugin.getConfig().getString("shop-info-item"));
        wgAllowCreateShopDefault = plugin.getConfig().getBoolean("worldguard-default-flag-values.create-shop");
        wgAllowUseAdminShopDefault = plugin.getConfig().getBoolean("worldguard-default-flag-values.use-admin-shop");
        wgAllowUseShopDefault = plugin.getConfig().getBoolean("worldguard-default-flag-values.use-shop");
        townyShopPlotsResidents = plugin.getConfig().getStringList("towny-shop-plots.residents");
        townyShopPlotsMayor = plugin.getConfig().getStringList("towny-shop-plots.mayor");
        townyShopPlotsKing = plugin.getConfig().getStringList("towny-shop-plots.king");
        areashopRemoveShopEvents = plugin.getConfig().getStringList("areashop-remove-shops");
        databaseMySqlPingInterval = plugin.getConfig().getInt("database.mysql.ping-interval");
        databaseMySqlHost = plugin.getConfig().getString("database.mysql.hostname");
        databaseMySqlPort = plugin.getConfig().getInt("database.mysql.port");
        databaseMySqlDatabase = plugin.getConfig().getString("database.mysql.database");
        databaseMySqlUsername = plugin.getConfig().getString("database.mysql.username");
        databaseMySqlPassword = plugin.getConfig().getString("database.mysql.password");
        databaseType = Database.DatabaseType.valueOf(plugin.getConfig().getString("database.type"));
        minimumPrices = (plugin.getConfig().getConfigurationSection("minimum-prices") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("minimum-prices").getKeys(true);
        maximumPrices = (plugin.getConfig().getConfigurationSection("maximum-prices") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("maximum-prices").getKeys(true);
        allowDecimalsInPrice = plugin.getConfig().getBoolean("allow-decimals-in-price");
        allowBrokenItems = plugin.getConfig().getBoolean("allow-broken-items");
        autoCalculateItemAmount = (allowDecimalsInPrice && plugin.getConfig().getBoolean("auto-calculate-item-amount"));
        blacklist = (plugin.getConfig().getStringList("blacklist") == null) ? new ArrayList<String>() : plugin.getConfig().getStringList("blacklist");
        buyGreaterOrEqualSell = plugin.getConfig().getBoolean("buy-greater-or-equal-sell");
        hopperProtection = plugin.getConfig().getBoolean("hopper-protection");
        explosionProtection = plugin.getConfig().getBoolean("explosion-protection");
        confirmShopping = plugin.getConfig().getBoolean("confirm-shopping");
        enableQualityMode = plugin.getConfig().getBoolean("enable-quality-mode");
        enableHologramInteraction = plugin.getConfig().getBoolean("enable-hologram-interaction");
        enableDebugLog = plugin.getConfig().getBoolean("enable-debug-log");
        enableEcomomyLog = plugin.getConfig().getBoolean("enable-economy-log");
        cleanupEcomomyLog = plugin.getConfig().getBoolean("cleanup-ecomomy-log");
        cleanupEcomomyLogDays = plugin.getConfig().getInt("cleanup-ecomomy-log-days");
        enableWorldGuardIntegration = plugin.getConfig().getBoolean("enable-worldguard-integration");
        enableTownyIntegration = plugin.getConfig().getBoolean("enable-towny-integration");
        enableAuthMeIntegration = plugin.getConfig().getBoolean("enable-authme-integration");
        enablePlotsquaredIntegration = plugin.getConfig().getBoolean("enable-plotsquared-integration");
        enableUSkyblockIntegration = plugin.getConfig().getBoolean("enable-uskyblock-integration");
        enableASkyblockIntegration = plugin.getConfig().getBoolean("enable-askyblock-integration");
        enableIslandWorldIntegration = plugin.getConfig().getBoolean("enable-islandworld-integration");
        enableGriefPreventionIntegration = plugin.getConfig().getBoolean("enable-griefprevention-integration");
        enableAreaShopIntegration = plugin.getConfig().getBoolean("enable-areashop-integration");
        enableVendorMessages = plugin.getConfig().getBoolean("enable-vendor-messages");
        onlyShowShopsInSight = plugin.getConfig().getBoolean("only-show-shops-in-sight");
        onlyShowFirstShopInSight = plugin.getConfig().getBoolean("only-show-first-shop-in-sight");
        excludeAdminShops = plugin.getConfig().getBoolean("shop-limits.exclude-admin-shops");
        appendPotionLevelToItemName = plugin.getConfig().getBoolean("append-potion-level-to-item-name");
        showShopItems = plugin.getConfig().getBoolean("show-shop-items");
        removeShopOnError = plugin.getConfig().getBoolean("remove-shop-on-error");
        invertMouseButtons = plugin.getConfig().getBoolean("invert-mouse-buttons");
        hologramFixedBottom = plugin.getConfig().getBoolean("hologram-fixed-bottom");
        hologramLift = plugin.getConfig().getDouble("hologram-lift");
        maximalDistance = plugin.getConfig().getDouble("maximal-distance");
        maximalItemDistance = plugin.getConfig().getDouble("maximal-item-distance");
        shopCreationPriceNormal = plugin.getConfig().getDouble("shop-creation-price.normal");
        shopCreationPriceAdmin = plugin.getConfig().getDouble("shop-creation-price.admin");
        defaultLimit = plugin.getConfig().getInt("shop-limits.default");
        mainCommandName = plugin.getConfig().getString("main-command-name");
        languageFile = plugin.getConfig().getString("language-file");

        if (firstLoad || langReload) loadLanguageConfig(showMessages);
        if (!firstLoad && langReload) LanguageUtils.load();
    }

    /**
     * @return ShopChest's {@link LanguageConfiguration}
     */
    public LanguageConfiguration getLanguageConfig() {
        return langConfig;
    }

    private Reader getTextResource(String file, boolean showMessages) {
        try {
            return (Reader) plugin.getClass().getDeclaredMethod("getTextResource", String.class).invoke(plugin, file);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            if (showMessages) plugin.getLogger().severe("Failed to get file from jar: " + file);
            plugin.debug("Failed to get file from jar: " + file);
            plugin.debug(e);
        }

        return null;
    }

    private void loadLanguageConfig(boolean showMessages) {
        langConfig = new LanguageConfiguration(plugin, showMessages);
        File langFolder = new File(plugin.getDataFolder(), "lang");

        if (!(new File(langFolder, "en_US.lang")).exists())
            plugin.saveResource("lang/en_US.lang", false);

        if (!(new File(langFolder, "de_DE.lang")).exists())
            plugin.saveResource("lang/de_DE.lang", false);

        File langConfigFile = new File(langFolder, languageFile + ".lang");
        File langDefaultFile = new File(langFolder, "en_US.lang");

        if (!langConfigFile.exists()) {
            if (!langDefaultFile.exists()) {
                try {
                    Reader r = getTextResource("lang/" + langConfigFile.getName(), showMessages);

                    if (r == null) {
                        r = getTextResource("lang/en_US.lang", showMessages);
                        if (showMessages) plugin.getLogger().info("Using locale \"en_US\" (Streamed from jar file)");
                    } else {
                        if (showMessages)
                            plugin.getLogger().info("Using locale \"" + langConfigFile.getName().substring(0, langConfigFile.getName().length() - 5) + "\" (Streamed from jar file)");
                    }

                    if (r == null) {
                        if (showMessages) plugin.getLogger().warning("Using default language values");
                        plugin.debug("Using default language values (#1)");
                    }

                    BufferedReader br = new BufferedReader(r);

                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = br.readLine();
                    }

                    langConfig.loadFromString(sb.toString());
                } catch (IOException | InvalidConfigurationException e) {
                    if (showMessages) {
                        plugin.getLogger().warning("Using default language values");
                    }

                    plugin.debug("Using default language values (#2)");
                    plugin.debug(e);
                }
            } else {
                try {
                    langConfig.load(langDefaultFile);
                    if (showMessages) plugin.getLogger().info("Using locale \"en_US\"");
                } catch (IOException | InvalidConfigurationException e) {
                    if (showMessages) {
                        plugin.getLogger().warning("Using default language values");
                    }

                    plugin.debug("Using default language values (#3)");
                    plugin.debug(e);
                }
            }
        } else {
            try {
                if (showMessages)
                    plugin.getLogger().info("Using locale \"" + langConfigFile.getName().substring(0, langConfigFile.getName().length() - 5) + "\"");
                langConfig.load(langConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                if (showMessages) {
                    plugin.getLogger().warning("Using default language values");
                }

                plugin.debug("Using default language values (#4)");
                plugin.debug(e);
            }
        }
    }

}
