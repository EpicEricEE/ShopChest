package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.sql.Database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    private static ShopChest plugin = ShopChest.getInstance();

    /**
     * The hostname used in ShopChest's MySQL database
     **/
    public static String database_mysql_host = plugin.getConfig().getString("database.mysql.hostname");

    /** The port used for ShopChest's MySQL database **/
    public static int database_mysql_port = plugin.getConfig().getInt("database.mysql.port");

    /** The database used for ShopChest's MySQL database **/
    public static String database_mysql_database = plugin.getConfig().getString("database.mysql.database");

    /** The username used in ShopChest's MySQL database **/
    public static String database_mysql_username = plugin.getConfig().getString("database.mysql.username");

    /** The password used in ShopChest's MySQL database **/
    public static String database_mysql_password = plugin.getConfig().getString("database.mysql.password");

    /** The database type used for ShopChest. **/
    public static Database.DatabaseType database_type = Database.DatabaseType.valueOf(plugin.getConfig().getString("database.type"));

    /**
     * The amount of attempts, ShopChest tries to reconnect to the database, when the connection is lost, until giving up
     **/
    public static int database_reconnect_attempts = plugin.getConfig().getInt("database.reconnect-attempts");

    /**
     * <p>The minimum prices for certain items</p>
     * This returns a key set, which contains e.g "STONE", "STONE:1", of the <i>minimum-prices</i> section in ShopChest's config.
     * To actually retrieve the price for an item, you have to get the Double <i>minimum-prices.<b>key</b></i>.
     **/
    public static Set<String> minimum_prices = (plugin.getConfig().getConfigurationSection("minimum-prices") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("minimum-prices").getKeys(true);

    /**
     * <p>The shop limits of certain groups</p>
     * This returns a key set, which contains the group names, of the <i>shop-limits.group</i> section in ShopChest's config.
     * To actually retrieve the limits for a group, you have to get the Integer <i>shop-limits.group.<b>key</b></i>.
     **/
    public static Set<String> shopLimits_group = (plugin.getConfig().getConfigurationSection("shop-limits.group") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("shop-limits.group").getKeys(true);

    /**
     * <p>The shop limits of certain players</p>
     * This returns a key set, which contains the player names, of the <i>shop-limits.player</i> section in ShopChest's config.
     * To actually retrieve the limits for a player, you have to get the Integer <i>shop-limits.player.<b>key</b></i>.
     **/
    public static Set<String> shopLimits_player = (plugin.getConfig().getConfigurationSection("shop-limits.player") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("shop-limits.player").getKeys(true);

    /**
     * <p>List containing items, of which players can't create a shop</p>
     * If this list contains an item (e.g "STONE", "STONE:1"), it's in the blacklist.
     **/
    public static List<String> blacklist = (plugin.getConfig().getStringList("blacklist") == null) ? new ArrayList<String>() : plugin.getConfig().getStringList("blacklist");

    /** Whether the buy price of a shop must be greater than or equal the sell price **/
    public static boolean buy_greater_or_equal_sell = plugin.getConfig().getBoolean("buy-greater-or-equal-sell");

    /** Whether shops should be protected by hoppers **/
    public static boolean hopper_protection = plugin.getConfig().getBoolean("hopper-protection");

    /** Whether shops should be protected by explosions **/
    public static boolean explosion_protection = plugin.getConfig().getBoolean("explosion-protection");

    /** Whether admin shops should be excluded of the shop limits **/
    public static boolean exclude_admin_shops = plugin.getConfig().getBoolean("shop-limits.exclude-admin-shops");

    /** The maximum distance between a player and a shop to see the hologram **/
    public static double maximal_distance = plugin.getConfig().getDouble("maximal-distance");

    /** The price a player has to pay in order to create a normal shop **/
    public static double shop_creation_price_normal = plugin.getConfig().getDouble("shop-creation-price.normal");

    /** The price a player has to pay in order to create an admin shop **/
    public static double shop_creation_price_admin = plugin.getConfig().getDouble("shop-creation-price.admin");

    /** The default shop limit for players and groups that are not listed in {@link #shopLimits_player} or in {@link #shopLimits_group} **/
    public static int default_limit = plugin.getConfig().getInt("shop-limits.default");

    /** The main command of ShopChest <i>(default: shop)</i> **/
    public static String main_command_name = plugin.getConfig().getString("main-command-name");

    /** The language file to use (e.g <i>en_US</i>, <i>de_DE</i>) **/
    public static String language_file = plugin.getConfig().getString("language-file");

}
