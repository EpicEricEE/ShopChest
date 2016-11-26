package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.sql.Database;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    private ShopChest plugin;

    private LanguageConfiguration langConfig;

    /**
     * The hostname used in ShopChest's MySQL database
     **/
    public String database_mysql_host;

    /** The port used for ShopChest's MySQL database **/
    public int database_mysql_port;

    /** The database used for ShopChest's MySQL database **/
    public String database_mysql_database;

    /** The username used in ShopChest's MySQL database **/
    public String database_mysql_username;

    /** The password used in ShopChest's MySQL database **/
    public String database_mysql_password;

    /** The database type used for ShopChest. **/
    public Database.DatabaseType database_type;

    /** The interval in seconds, a ping is sent to the MySQL server **/
    public int database_mysql_ping_interval;

    /**
     * <p>The minimum prices for certain items</p>
     * This returns a key set, which contains e.g "STONE", "STONE:1", of the <i>minimum-prices</i> section in ShopChest's config.
     * To actually retrieve the price for an item, you have to get the Double <i>minimum-prices.<b>key</b></i>.
     **/
    public Set<String> minimum_prices;

    /**
     * <p>List containing items, of which players can't create a shop</p>
     * If this list contains an item (e.g "STONE", "STONE:1"), it's in the blacklist.
     **/
    public List<String> blacklist;

    /** Whether prices may contain decimals **/
    public boolean allow_decimals_in_price;

    /** Whether the buy price of a shop must be greater than or equal the sell price **/
    public boolean buy_greater_or_equal_sell;

    /** Whether shops should be protected by hoppers **/
    public boolean hopper_protection;

    /** Whether shops should be protected by explosions **/
    public boolean explosion_protection;

    /** Whether quality mode should be enabled **/
    public boolean enable_quality_mode;

    /** Whether hologram interaction should be enabled **/
    public boolean enable_hologram_interaction;

    /** Whether the debug log file should be created **/
    public boolean enable_debug_log;

    /** Whether WorldGuard integration should be enabled **/
    public boolean enable_worldguard_integration;

    /** Whether Towny integration should be enabled **/
    public boolean enable_towny_integration;

    /** Whether the vendor of the shop should get messages about buys and sells **/
    public boolean enable_vendor_messages;

    /** Whether admin shops should be excluded of the shop limits **/
    public boolean exclude_admin_shops;

    /** Whether the buy- and sell price should be arranged below each other **/
    public boolean two_line_prices;

    /** Whether the extension of a potion or tipped arrow (if available) should be appended to the item name. **/
    public boolean append_potion_level_to_item_name;

    /** Whether the shop items should be shown **/
    public boolean show_shop_items;

    /** Whether players are allowed to sell/buy broken items **/
    public boolean allow_broken_items;

    /**
     * <p>Whether shops should automatically be removed from the database if an error occurred while loading</p>
     * (e.g. when no chest is found at a shop's location)
     */
    public boolean remove_shop_on_error;

    /** Whether the item amount should be calculated to fit the available money or inventory space **/
    public boolean auto_calculate_item_amount;

    /** Amount the hologram should be lifted **/
    public double two_line_hologram_lift;

    /** The maximum distance between a player and a shop to see the hologram **/
    public double maximal_distance;

    /** The maximum distance between a player and a shop to see the shop item **/
    public double maximal_item_distance;

    /** The price a player has to pay in order to create a normal shop **/
    public double shop_creation_price_normal;

    /** The price a player has to pay in order to create an admin shop **/
    public double shop_creation_price_admin;

    /** The default shop limit for players whose limit is not set via a permission **/
    public int default_limit;

    /** The time between automatic shop reloads (if set to 0, the timer will be disabled) **/
    public int auto_reload_time;

    /** The main command of ShopChest <i>(default: shop)</i> **/
    public String main_command_name;

    /** The language file to use (e.g <i>en_US</i>, <i>de_DE</i>) **/
    public String language_file;


    public Config(ShopChest plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        reload(true, true, true);
    }

    /**
     * <p>Set a configuration value</p>
     * <i>Config is automatically reloaded</i>
     *
     * @param property Property to change
     * @param value Value to set
     */
    public void set(String property, String value) {
        boolean langChange = (property.equalsIgnoreCase("language-file"));
        try {
            int intValue = Integer.parseInt(value);
            plugin.getConfig().set(property, intValue);

            plugin.saveConfig();
            plugin.reloadConfig();
            reload(false, langChange, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            plugin.getConfig().set(property, doubleValue);

            plugin.saveConfig();
            plugin.reloadConfig();
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
        plugin.reloadConfig();

        reload(false, langChange, false);
    }

    /**
     * Add a value to a list in the config.yml.
     * If the list does not exist, a new list with the given value will be created
     * @param property Location of the list
     * @param value Value to add
     */
    public void add(String property, String value) {
        List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.add(intValue);

            plugin.saveConfig();
            plugin.reloadConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.add(doubleValue);

            plugin.saveConfig();
            plugin.reloadConfig();
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
        plugin.reloadConfig();

        reload(false, false, false);
    }

    public void remove(String property, String value) {
        List list = (plugin.getConfig().getList(property) == null) ? new ArrayList<>() : plugin.getConfig().getList(property);

        try {
            int intValue = Integer.parseInt(value);
            list.remove(intValue);

            plugin.saveConfig();
            plugin.reloadConfig();
            reload(false, false, false);

            return;
        } catch (NumberFormatException e) { /* Value not an integer */ }

        try {
            double doubleValue = Double.parseDouble(value);
            list.remove(doubleValue);

            plugin.saveConfig();
            plugin.reloadConfig();
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
        plugin.reloadConfig();

        reload(false, false, false);
    }

    /**
     * Reload the configuration values from config.yml
     */
    public void reload(boolean firstLoad, boolean langReload, boolean showMessages) {
        database_mysql_ping_interval = plugin.getConfig().getInt("database.mysql.ping-interval");
        database_mysql_host = plugin.getConfig().getString("database.mysql.hostname");
        database_mysql_port = plugin.getConfig().getInt("database.mysql.port");
        database_mysql_database = plugin.getConfig().getString("database.mysql.database");
        database_mysql_username = plugin.getConfig().getString("database.mysql.username");
        database_mysql_password = plugin.getConfig().getString("database.mysql.password");
        database_type = Database.DatabaseType.valueOf(plugin.getConfig().getString("database.type"));
        minimum_prices = (plugin.getConfig().getConfigurationSection("minimum-prices") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("minimum-prices").getKeys(true);
        allow_decimals_in_price = plugin.getConfig().getBoolean("allow-decimals-in-price");
        allow_broken_items = plugin.getConfig().getBoolean("allow-broken-items");
        auto_calculate_item_amount = (allow_decimals_in_price && plugin.getConfig().getBoolean("auto-calculate-item-amount"));
        blacklist = (plugin.getConfig().getStringList("blacklist") == null) ? new ArrayList<String>() : plugin.getConfig().getStringList("blacklist");
        buy_greater_or_equal_sell = plugin.getConfig().getBoolean("buy-greater-or-equal-sell");
        hopper_protection = plugin.getConfig().getBoolean("hopper-protection");
        two_line_prices = plugin.getConfig().getBoolean("two-line-prices");
        enable_quality_mode = plugin.getConfig().getBoolean("enable-quality-mode");
        enable_hologram_interaction = plugin.getConfig().getBoolean("enable-hologram-interaction");
        enable_debug_log = plugin.getConfig().getBoolean("enable-debug-log");
        enable_worldguard_integration = plugin.getConfig().getBoolean("enable-worldguard-integration");
        enable_towny_integration = plugin.getConfig().getBoolean("enable-towny-integration");
        enable_vendor_messages = plugin.getConfig().getBoolean("enable-vendor-messages");
        explosion_protection = plugin.getConfig().getBoolean("explosion-protection");
        exclude_admin_shops = plugin.getConfig().getBoolean("shop-limits.exclude-admin-shops");
        append_potion_level_to_item_name = plugin.getConfig().getBoolean("append-potion-level-to-item-name");
        show_shop_items = plugin.getConfig().getBoolean("show-shop-items");
        remove_shop_on_error = plugin.getConfig().getBoolean("remove-shop-on-error");
        two_line_hologram_lift = plugin.getConfig().getDouble("two-line-hologram-lift");
        maximal_distance = plugin.getConfig().getDouble("maximal-distance");
        maximal_item_distance = plugin.getConfig().getDouble("maximal-item-distance");
        shop_creation_price_normal = plugin.getConfig().getDouble("shop-creation-price.normal");
        shop_creation_price_admin = plugin.getConfig().getDouble("shop-creation-price.admin");
        default_limit = plugin.getConfig().getInt("shop-limits.default");
        auto_reload_time = plugin.getConfig().getInt("auto-reload-time");
        main_command_name = plugin.getConfig().getString("main-command-name");
        language_file = plugin.getConfig().getString("language-file");

        if (firstLoad || langReload) loadLanguageConfig(showMessages);
        if (!firstLoad && langReload) LanguageUtils.load();
    }

    /**
     * @return ShopChest's {@link LanguageConfiguration}
     */
    public LanguageConfiguration getLanguageConfig() {
        return langConfig;
    }

    private void loadLanguageConfig(boolean showMessages) {
        langConfig = new LanguageConfiguration(plugin, showMessages);
        File langFolder = new File(plugin.getDataFolder(), "lang");

        if (!(new File(langFolder, "en_US.lang")).exists())
            plugin.saveResource("lang/en_US.lang", false);

        if (!(new File(langFolder, "de_DE.lang")).exists())
            plugin.saveResource("lang/de_DE.lang", false);

        File langConfigFile = new File(langFolder, language_file + ".lang");
        File langDefaultFile = new File(langFolder, "en_US.lang");

        if (!langConfigFile.exists()) {
            if (!langDefaultFile.exists()) {
                try {
                    Reader r = plugin._getTextResource("lang/" + langConfigFile.getName());

                    if (r == null) {
                        r = plugin._getTextResource("lang/en_US.lang");
                        if (showMessages) plugin.getLogger().info("Using locale \"en_US\" (Streamed from jar file)");
                    } else {
                        if (showMessages) plugin.getLogger().info("Using locale \"" + langConfigFile.getName().substring(0, langConfigFile.getName().length() - 5) + "\" (Streamed from jar file)");
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

                    plugin.debug("Using default language values (#1)");
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

                    plugin.debug("Using default language values (#2)");
                    plugin.debug(e);
                }
            }
        } else {
            try {
                if (showMessages) plugin.getLogger().info("Using locale \"" + langConfigFile.getName().substring(0, langConfigFile.getName().length() - 5) + "\"");
                langConfig.load(langConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                if (showMessages) {
                    plugin.getLogger().warning("Using default language values");
                }

                plugin.debug("Using default language values (#3)");
                plugin.debug(e);
            }
        }
    }

}
