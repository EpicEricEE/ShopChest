package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.event.ShopBuySellEvent.Type;
import de.epiceric.shopchest.exceptions.WorldNotFoundException;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.zaxxer.hikari.HikariDataSource;

public abstract class Database {

    private static Set<String> notFoundWorlds = new HashSet<>();

    private String tableShops;
    private String tableLogs;
    private String tableLogouts;

    ShopChest plugin;
    HikariDataSource dataSource;

    protected Database(ShopChest plugin) {
        this.plugin = plugin;
    }

    abstract HikariDataSource getDataSource();

    /**
     * (Re-)Connects to the the database and initializes it. <br>
     * Creates the table (if doesn't exist) and tests the connection
     * 
     * @param callback Callback that - if succeeded - returns the amount of shops
     *                 that were found (as {@code int})
     */
    public void connect(final Callback<Integer> callback) {
        if (!Config.databaseTablePrefix.matches("^([a-zA-Z0-9\\-\\_]+)?$")) {
            // Only letters, numbers dashes and underscores are allowed
            plugin.getLogger().severe("Database table prefix contains illegal letters, using 'shopchest_' prefix.");
            Config.databaseTablePrefix = "shopchest_";
        }

        this.tableShops = Config.databaseTablePrefix + "shops";
        this.tableLogs = Config.databaseTablePrefix + "economy_logs";
        this.tableLogouts = Config.databaseTablePrefix + "player_logouts";

        new BukkitRunnable() {
            @Override
            public void run() {
                disconnect();

                dataSource = getDataSource();
                
                String autoIncrement = Database.this instanceof SQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT";

                String queryCreateTableShopList = 
                        "CREATE TABLE IF NOT EXISTS " + tableShops + " ("
                        + "id INTEGER PRIMARY KEY " + autoIncrement + ","
                        + "vendor TINYTEXT NOT NULL,"
                        + "product TEXT NOT NULL,"
                        + "amount INTEGER NOT NULL,"
                        + "world TINYTEXT NOT NULL,"
                        + "x INTEGER NOT NULL,"
                        + "y INTEGER NOT NULL,"
                        + "z INTEGER NOT NULL,"
                        + "buyprice FLOAT NOT NULL,"
                        + "sellprice FLOAT NOT NULL,"
                        + "shoptype TINYTEXT NOT NULL)";

                String queryCreateTableShopLog = 
                        "CREATE TABLE IF NOT EXISTS " + tableLogs + " ("
                        + "id INTEGER PRIMARY KEY " + autoIncrement + ","
                        + "shop_id INTEGER NOT NULL,"
                        + "timestamp TINYTEXT NOT NULL,"
                        + "time LONG NOT NULL,"
                        + "player_name TINYTEXT NOT NULL,"
                        + "player_uuid TINYTEXT NOT NULL,"
                        + "product_name TINYTEXT NOT NULL,"
                        + "product TEXT NOT NULL,"
                        + "amount INTEGER NOT NULL,"
                        + "vendor_name TINYTEXT NOT NULL,"
                        + "vendor_uuid TINYTEXT NOT NULL,"
                        + "admin BIT NOT NULL,"
                        + "world TINYTEXT NOT NULL,"
                        + "x INTEGER NOT NULL,"
                        + "y INTEGER NOT NULL,"
                        + "z INTEGER NOT NULL,"
                        + "price FLOAT NOT NULL,"
                        + "type TINYTEXT NOT NULL)";

                String queryCreateTablePlayerLogout = 
                        "CREATE TABLE IF NOT EXISTS " + tableLogouts + " ("
                        + "player VARCHAR(36) PRIMARY KEY NOT NULL,"
                        + "time LONG NOT NULL)";

                String queryCheckIfOldFormat =
                        Database.this instanceof SQLite ?
                                "SELECT name FROM sqlite_master WHERE type='table' AND name='shop_log'" :
                                "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='shop_log'";

                String queryRenameTableLogouts = "ALTER TABLE player_logout RENAME TO " + tableLogouts;
                String queryRenameTableLogs = "ALTER TABLE shop_log RENAME TO backup_shop_log"; // for backup
                String queryRenameTableShops = "ALTER TABLE shops RENAME TO backup_shops"; // for backup
                        
                try (Connection con = dataSource.getConnection()) {
                    // Check if database is in old format
                    try (Statement s = con.createStatement()) {
                        ResultSet rs = s.executeQuery(queryCheckIfOldFormat);
                        if (rs.next()) {
                            plugin.getLogger().warning("Database is using old format and will be converted.");

                            try (Statement s2 = con.createStatement()) {
                                s.executeUpdate(queryRenameTableShops);

                                // Create new shops table
                                try (Statement s3 = con.createStatement()) {
                                    s3.executeUpdate(queryCreateTableShopList);
                                }

                                // Create new log table
                                try (Statement s4 = con.createStatement()) {
                                    s4.executeUpdate(queryCreateTableShopLog);
                                }

                                // Convert shop table
                                try (Statement s3 = con.createStatement()) {
                                    ResultSet rs2 = s3.executeQuery("SELECT id,product FROM backup_shops");
                                    while (rs2.next()) {
                                        ItemStack is = Utils.decode(rs2.getString("product"));
                                        int amount = is.getAmount();
                                        is.setAmount(1);
                                        String product = Utils.encode(is);
                                        
                                        String insertQuery = "INSERT INTO " + tableShops + " SELECT id,vendor,?,?,world,x,y,z,buyprice,sellprice,shoptype FROM backup_shops WHERE id = ?";
                                        try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                                            ps.setString(1, product);
                                            ps.setInt(2, amount);
                                            ps.setInt(3, rs2.getInt("id"));
                                            ps.executeUpdate();
                                        }
                                    }
                                }

                                // Convert log table
                                try (Statement s3 = con.createStatement()) {
                                    ResultSet rs2 = s3.executeQuery("SELECT id,timestamp,executor,product,vendor FROM shop_log");
                                    while (rs2.next()) {
                                        String timestamp = rs2.getString("timestamp");
                                        long time = 0L;

                                        try {
                                            time = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(timestamp).getTime();
                                        } catch (ParseException e) {
                                            plugin.debug("Failed to parse timestamp '" + timestamp + "': Time is set to 0");
                                            plugin.debug(e);
                                        }

                                        String player = rs2.getString("executor");
                                        String playerUuid = player.substring(0, 36);
                                        String playerName = player.substring(38, player.length() - 1);

                                        String oldProduct = rs2.getString("product");
                                        String product = oldProduct.split(" x ")[1];
                                        int amount = Integer.valueOf(oldProduct.split(" x ")[0]);

                                        String vendor = rs2.getString("vendor");
                                        String vendorUuid = vendor.substring(0, 36);
                                        String vendorName = vendor.substring(38).replaceAll("\\)( \\(ADMIN\\))?", "");
                                        boolean admin = vendor.endsWith("(ADMIN)");
                                        
                                        String insertQuery = "INSERT INTO " + tableLogs + " SELECT id,-1,timestamp,?,?,?,?,'Unknown',?,?,?,?,world,x,y,z,price,type FROM shop_log WHERE id = ?";
                                        try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                                            ps.setLong(1, time);
                                            ps.setString(2, playerName);
                                            ps.setString(3, playerUuid);
                                            ps.setString(4, product);
                                            ps.setInt(5, amount);
                                            ps.setString(6, vendorName);
                                            ps.setString(7, vendorUuid);
                                            ps.setBoolean(8, admin);
                                            ps.setInt(9, rs2.getInt("id"));
                                            ps.executeUpdate();
                                        }
                                    }
                                }

                                // Rename log table
                                try (Statement s3 = con.createStatement()) {
                                    s3.executeUpdate(queryRenameTableLogs);
                                }

                                // Rename logout table
                                try (Statement s3 = con.createStatement()) {
                                    s3.executeUpdate(queryRenameTableLogouts);
                                }
                            }
                        }
                    }
                    

                    // Create shop table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(queryCreateTableShopList);
                    }

                    // Create log table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(queryCreateTableShopLog);
                    }

                    // Create logout table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(queryCreateTablePlayerLogout);
                    }

                    // Clean up economy log
                    if (Config.cleanupEconomyLogDays > 0) {
                        cleanUpEconomy(false);
                    }

                    // Count shops entries in database
                    try (Statement s = con.createStatement();) {
                        ResultSet rs = s.executeQuery("SELECT COUNT(id) FROM " + tableShops);
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            
                            plugin.debug("Initialized database with " + count + " entries");

                            if (callback != null) {
                                callback.callSyncResult(count);
                            }
                        } else {
                            throw new SQLException("Count result set has no entries");
                        }
                    }
                } catch (SQLException e) {
                    if (callback != null) {
                        callback.callSyncError(e);
                    }
                    
                    plugin.getLogger().severe("Failed to initialize database");
                    plugin.debug("Failed to initialize database");
                    plugin.debug(e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Remove a shop from the database
     *
     * @param shop     Shop to remove
     * @param callback Callback that - if succeeded - returns {@code null}
     */
    public void removeShop(final Shop shop, final Callback<Void> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableShops + " WHERE id = ?")) {
                    ps.setInt(1, shop.getID());
                    ps.executeUpdate();

                    plugin.debug("Removing shop from database (#" + shop.getID() + ")");

                    if (callback != null) {
                        callback.callSyncResult(null);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to remove shop from database");
                    plugin.debug("Failed to remove shop from database (#" + shop.getID() + ")");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get all shops from the database
     * 
     * @param showConsoleMessages Whether console messages (errors or warnings)
     *                            should be shown
     * @param callback            Callback that - if succeeded - returns a read-only
     *                            collection of all shops (as
     *                            {@code Collection<Shop>})
     */
    public void getShops(final boolean showConsoleMessages, final Callback<Collection<Shop>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<Shop> shops = new ArrayList<>();

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableShops + "")) {
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");

                        plugin.debug("Getting Shop... (#" + id + ")");

                        String worldName = rs.getString("world");
                        World world = Bukkit.getWorld(worldName);

                        if (world == null) {
                            WorldNotFoundException ex = new WorldNotFoundException(worldName);
                            if (showConsoleMessages && !notFoundWorlds.contains(worldName)) {
                                plugin.getLogger().warning(ex.getMessage());
                                notFoundWorlds.add(worldName);
                            }
                            plugin.debug("Failed to get shop (#" + id + ")");
                            plugin.debug(ex);
                            continue;
                        }

                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        Location location = new Location(world, x, y, z);
                        OfflinePlayer vendor = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("vendor")));
                        ItemStack itemStack = Utils.decode(rs.getString("product"));
                        int amount = rs.getInt("amount");
                        ShopProduct product = new ShopProduct(itemStack, amount);
                        double buyPrice = rs.getDouble("buyprice");
                        double sellPrice = rs.getDouble("sellprice");
                        ShopType shopType = ShopType.valueOf(rs.getString("shoptype"));

                        plugin.debug("Initializing new shop... (#" + id + ")");

                        shops.add(new Shop(id, plugin, vendor, product, location, buyPrice, sellPrice, shopType));
                    }

                    if (callback != null) {
                        callback.callSyncResult(Collections.unmodifiableCollection(shops));
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to get shops from database");
                    plugin.debug("Failed to get shops");
                    plugin.debug(ex);
                }

            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Adds a shop to the database
     * 
     * @param shop     Shop to add
     * @param callback Callback that - if succeeded - returns the ID the shop was
     *                 given (as {@code int})
     */
    public void addShop(final Shop shop, final Callback<Integer> callback) {
        final String queryNoId = "REPLACE INTO " + tableShops + " (vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)";
        final String queryWithId = "REPLACE INTO " + tableShops + " (id,vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        new BukkitRunnable() {
            @Override
            public void run() {
                String query = shop.hasId() ? queryWithId : queryNoId;

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    int i = 0;
                    if (shop.hasId()) {
                        i = 1;
                        ps.setInt(1, shop.getID());
                    }

                    ps.setString(i+1, shop.getVendor().getUniqueId().toString());
                    ps.setString(i+2, Utils.encode(shop.getProduct().getItemStack()));
                    ps.setInt(i+3, shop.getProduct().getAmount());
                    ps.setString(i+4, shop.getLocation().getWorld().getName());
                    ps.setInt(i+5, shop.getLocation().getBlockX());
                    ps.setInt(i+6, shop.getLocation().getBlockY());
                    ps.setInt(i+7, shop.getLocation().getBlockZ());
                    ps.setDouble(i+8, shop.getBuyPrice());
                    ps.setDouble(i+9, shop.getSellPrice());
                    ps.setString(i+10, shop.getShopType().toString());
                    ps.executeUpdate();

                    if (!shop.hasId()) {
                        int shopId = -1;
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            shopId = rs.getInt(1);
                        }

                        shop.setId(shopId);
                    }

                    if (callback != null) {
                        callback.callSyncResult(shop.getID());
                    }

                    plugin.debug("Adding shop to database (#" + shop.getID() + ")");
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to add shop to database");
                    plugin.debug("Failed to add shop to database (#" + shop.getID() + ")");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Log an economy transaction to the database
     * 
     * @param executor Player who bought/sold something
     * @param shop The {@link Shop} the player bought from or sold to
     * @param product The {@link ItemStack} that was bought/sold
     * @param price The price the product was bought or sold for
     * @param type Whether the executor bought or sold
     * @param callback Callback that - if succeeded - returns {@code null}
     */
    public void logEconomy(final Player executor, Shop shop, ShopProduct product, double price, Type type, final Callback<Void> callback) {
        final String query = "INSERT INTO " + tableLogs + " (shop_id,timestamp,time,player_name,player_uuid,product_name,product,amount,"
                + "vendor_name,vendor_uuid,admin,world,x,y,z,price,type) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        if (Config.enableEconomyLog) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try (Connection con = dataSource.getConnection();
                            PreparedStatement ps = con.prepareStatement(query)) {

                        long millis = System.currentTimeMillis();

                        ps.setInt(1, shop.getID());
                        ps.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(millis));
                        ps.setLong(3, millis);
                        ps.setString(4, executor.getName());
                        ps.setString(5, executor.getUniqueId().toString());
                        ps.setString(6, LanguageUtils.getItemName(product.getItemStack()));
                        ps.setString(7, Utils.encode(product.getItemStack()));
                        ps.setInt(8, product.getAmount());
                        ps.setString(9, shop.getVendor().getName());
                        ps.setString(10, shop.getVendor().getUniqueId().toString());
                        ps.setBoolean(11, shop.getShopType() == ShopType.ADMIN);
                        ps.setString(12, shop.getLocation().getWorld().getName());
                        ps.setInt(13, shop.getLocation().getBlockX());
                        ps.setInt(14, shop.getLocation().getBlockY());
                        ps.setInt(15, shop.getLocation().getBlockZ());
                        ps.setDouble(16, price);
                        ps.setString(17, type.toString());
                        ps.executeUpdate();

                        if (callback != null) {
                            callback.callSyncResult(null);
                        }

                        plugin.debug("Logged economy transaction to database");
                    } catch (SQLException ex) {
                        if (callback != null) {
                            callback.callSyncError(ex);
                        }

                        plugin.getLogger().severe("Failed to log economy transaction to database");
                        plugin.debug("Failed to log economy transaction to database");
                        plugin.debug(ex);
                    }
                }
            }.runTaskAsynchronously(plugin);
        } else {
            if (callback != null) {
                callback.callSyncResult(null);
            }
        }
    }

    /**
     * Cleans up the economy log to reduce file size
     * 
     * @param async Whether the call should be executed asynchronously
     */
    public void cleanUpEconomy(boolean async) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis() - Config.cleanupEconomyLogDays * 86400000L;
                String queryCleanUpLog = "DELETE FROM " + tableLogs + " WHERE time < " + time;
                String queryCleanUpPlayers = "DELETE FROM " + tableLogouts + " WHERE time < " + time;

                try (Connection con = dataSource.getConnection();
                        Statement s = con.createStatement();
                        Statement s2 = con.createStatement()) {
                    s.executeUpdate(queryCleanUpLog);
                    s2.executeUpdate(queryCleanUpPlayers);

                    plugin.getLogger().info("Cleaned up economy log");
                    plugin.debug("Cleaned up economy log");
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Failed to clean up economy log");
                    plugin.debug("Failed to clean up economy log");
                    plugin.debug(ex);
                }
            }
        };

        if (async) {
            runnable.runTaskAsynchronously(plugin);
        } else {
            runnable.run();
        }
    }

    /**
     * Get the revenue a player got while he was offline
     * 
     * @param player     Player whose revenue to get
     * @param logoutTime Time in milliseconds when he logged out the last time
     * @param callback   Callback that - if succeeded - returns the revenue the
     *                   player made while offline (as {@code double})
     */
    public void getRevenue(final Player player, final long logoutTime, final Callback<Double> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                double revenue = 0;

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableLogs + " WHERE vendor_uuid = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        long timestamp = rs.getLong("time");
                        double singleRevenue = rs.getDouble("price");
                        ShopBuySellEvent.Type type = ShopBuySellEvent.Type.valueOf(rs.getString("type"));

                        if (type == ShopBuySellEvent.Type.SELL) {
                            singleRevenue = -singleRevenue;
                        }

                        if (timestamp > logoutTime) {
                            revenue += singleRevenue;
                        }
                    }

                    if (callback != null) {
                        callback.callSyncResult(revenue);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to get revenue from database");
                    plugin.debug("Failed to get revenue from player \"" + player.getUniqueId().toString() + "\"");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Log a logout to the database
     * 
     * @param player    Player who logged out
     * @param callback  Callback that - if succeeded - returns {@code null}
     */
    public void logLogout(final Player player, final Callback<Void> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("REPLACE INTO " + tableLogouts + " (player,time) VALUES(?,?)")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setLong(2, System.currentTimeMillis());
                    ps.executeUpdate();

                    if (callback != null) {
                        callback.callSyncResult(null);
                    }

                    plugin.debug("Logged logout to database");
                } catch (final SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to log last logout to database");
                    plugin.debug("Failed to log logout to database");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get the last logout of a player
     * 
     * @param player   Player who logged out
     * @param callback Callback that - if succeeded - returns the time in
     *                 milliseconds the player logged out (as {@code long})
     *                 or {@code -1} if the player has not logged out yet.
     */
    public void getLastLogout(final Player player, final Callback<Long> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableLogouts + " WHERE player = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        if (callback != null) {
                            callback.callSyncResult(rs.getLong("time"));
                        }
                    }

                    if (callback != null) {
                        callback.callSyncResult(-1L);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to get last logout from database");
                    plugin.debug("Failed to get last logout from player \"" + player.getName() + "\"");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Closes the data source
     */
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public enum DatabaseType {
        SQLite, MySQL
    }
}