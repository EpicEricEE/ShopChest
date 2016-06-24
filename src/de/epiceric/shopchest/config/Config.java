package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.sql.Database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    private static ShopChest plugin = ShopChest.getInstance();

    public static String database_mysql_host = plugin.getConfig().getString("database.mysql.hostname");

    public static int database_mysql_port = plugin.getConfig().getInt("database.mysql.port");

    public static String database_mysql_database = plugin.getConfig().getString("database.mysql.database");

    public static String database_mysql_username = plugin.getConfig().getString("database.mysql.username");

    public static String database_mysql_password = plugin.getConfig().getString("database.mysql.password");

    public static Database.DatabaseType database_type = Database.DatabaseType.valueOf(plugin.getConfig().getString("database.type"));

    public static Set<String> minimum_prices = (plugin.getConfig().getConfigurationSection("minimum-prices") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("minimum-prices").getKeys(true);

    public static Set<String> shopLimits_group = (plugin.getConfig().getConfigurationSection("shop-limits.group") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("shop-limits.group").getKeys(true);

    public static Set<String> shopLimits_player = (plugin.getConfig().getConfigurationSection("shop-limits.player") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("shop-limits.player").getKeys(true);

    public static List<String> blacklist = (plugin.getConfig().getStringList("blacklist") == null) ? new ArrayList<String>() : plugin.getConfig().getStringList("blacklist");

    public static boolean buy_greater_or_equal_sell = plugin.getConfig().getBoolean("buy-greater-or-equal-sell");

    public static boolean hopper_protection = plugin.getConfig().getBoolean("hopper-protection");

    public static boolean explosion_protection = plugin.getConfig().getBoolean("explosion-protection");

    public static boolean enable_broadcast = plugin.getConfig().getBoolean("enable-broadcast");

    public static boolean exclude_admin_shops = plugin.getConfig().getBoolean("shop-limits.exclude-admin-shops");

    public static double maximal_distance = plugin.getConfig().getDouble("maximal-distance");

    public static double shop_creation_price_normal = plugin.getConfig().getDouble("shop-creation-price.normal");

    public static double shop_creation_price_admin = plugin.getConfig().getDouble("shop-creation-price.admin");

    public static int default_limit = plugin.getConfig().getInt("shop-limits.default");

    public static String main_command_name = plugin.getConfig().getString("main-command-name");

    public static String language_file = plugin.getConfig().getString("language-file");

}
