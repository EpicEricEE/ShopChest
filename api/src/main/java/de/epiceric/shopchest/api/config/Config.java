package de.epiceric.shopchest.api.config;

import org.bukkit.Material;

import de.epiceric.shopchest.api.database.DatabaseType;

/**
 * Class to access configuration properties
 * 
 * @since 2.0
 */
public class Config {
    private Config() {}

    public static final Property<String> CORE_MAIN_COMMAND_NAME = new Property<>(String.class, "core", "main_command_name", "shop");
    public static final Property<String> CORE_LANGUAGE_FILE = new Property<>(String.class, "core", "language_file", "en_US");
    public static final Property<Boolean> CORE_ENABLE_UPDATE_CHECKER = new Property<>(Boolean.class, "core", "enable_update_checker", true);
    public static final Property<Boolean> CORE_REMOVE_SHOP_ON_ERROR = new Property<>(Boolean.class, "core", "remove_shop_on_error", true);
    public static final Property<Boolean> CORE_INVERT_MOUSE_BUTTONS = new Property<>(Boolean.class, "core", "invert_mouse_buttons", false);
    public static final Property<Integer> CORE_DEFAULT_SHOP_LIMIT = new Property<>(Integer.class, "core", "default_shop_limit", 5);
    public static final Property<Material> CORE_SHOP_INFO_ITEM = new Property<>(Material.class, "core", "shop_info_item", Material.STICK);

    public static final Property<Double> SHOP_CREATION_PRICE = new Property<>(Double.class, "shop_creation", "price", 5d);
    public static final Property<Boolean> SHOP_CREATION_ALLOW_DECIMAL_PRICES = new Property<>(Boolean.class, "shop_creation", "allow_decimal_prices", true);
    public static final Property<Boolean> SHOP_CREATION_ALLOW_BROKEN_ITEMS = new Property<>(Boolean.class, "shop_creation", "allow_broken_items", false);
    public static final Property<MaterialList> SHOP_CREATION_BLACKLIST = new Property<>(MaterialList.class, "shop_creation", "blacklist", new MaterialList());

    public static final Property<MaterialDoubleMap> SHOP_CREATION_MINIMUM_PRICES = new Property<>(MaterialDoubleMap.class, "shop_creation", "minimum_prices", new MaterialDoubleMap());
    public static final Property<MaterialDoubleMap> SHOP_CREATION_MAXIMUM_PRICES = new Property<>(MaterialDoubleMap.class, "shop_creation", "maximum_prices", new MaterialDoubleMap());

    public static final Property<Boolean> FEATURES_CONFIRM_SHOPPING = new Property<>(Boolean.class, "features", "confirm_shopping", false);
    public static final Property<Boolean> FEATURES_CREATIVE_ITEM_SELECTION = new Property<>(Boolean.class, "features", "creative_item_selection", true);
    public static final Property<Boolean> FEATURES_REFUND_SHOP_CREATION = new Property<>(Boolean.class, "features", "refund_shop_creation", false);
    public static final Property<Boolean> FEATURES_VENDOR_MESSAGES = new Property<>(Boolean.class, "features", "vendor_messages", true);
    public static final Property<Boolean> FEATURES_VENDOR_MONEY_PROTECTION = new Property<>(Boolean.class, "features", "vendor_money_protection", true);
    public static final Property<Boolean> FEATURES_AUTO_ADJUST_ITEM_AMOUNT = new Property<>(Boolean.class, "features", "auto_adjust_item_amount", false);

    public static final Property<Double> HOLOGRAM_MAXIMUM_DISTANCE = new Property<>(Double.class, "hologram", "maximum_distance", 2.5);
    public static final Property<Double> HOLOGRAM_ADDITIONAL_LIFT = new Property<>(Double.class, "hologram", "additional_lift", 0d);

    public static final Property<Boolean> ECONOMY_LOG_ENABLE = new Property<>(Boolean.class, "economy_log", "enable", true);
    public static final Property<Boolean> ECONOMY_LOG_CLEANUP = new Property<>(Boolean.class, "economy_log", "cleanup", true);
    public static final Property<Integer> ECONOMY_LOG_CLEANUP_DAYS = new Property<>(Integer.class, "economy_log", "cleanup_days", 30);

    public static final Property<DatabaseType> DATABASE_TYPE = new Property<>(DatabaseType.class, "database", "type", DatabaseType.SQLITE);
    public static final Property<String> DATABASE_TABLE_PREFIX = new Property<>(String.class, "database", "table_prefix", "shopchest_");
    public static final Property<String> DATABASE_MYSQL_HOSTNAME = new Property<>(String.class, "database.mysql", "hostname", "");
    public static final Property<Integer> DATABASE_MYSQL_PORT = new Property<>(Integer.class, "database.mysql", "port", 3306);
    public static final Property<String> DATABASE_MYSQL_DATABASE = new Property<>(String.class, "database.mysql", "database", "");
    public static final Property<String> DATABASE_MYSQL_USERNAME = new Property<>(String.class, "database.mysql", "username", "");
    public static final Property<String> DATABASE_MYSQL_PASSWORD = new Property<>(String.class, "database.mysql", "password", "");
    public static final Property<Integer> DATABASE_MYSQL_PING_INTERVAL = new Property<>(Integer.class, "database.mysql", "ping_interval", 3600);
}