package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.event.ShopBuySellEvent.Type;
import de.epiceric.shopchest.exceptions.WorldNotFoundException;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.shop.Shop;
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

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.zaxxer.hikari.HikariDataSource;

public abstract class Database {

    private static Set<String> notFoundWorlds = new HashSet<>();

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
        new BukkitRunnable() {
            @Override
            public void run() {
                disconnect();

                dataSource = getDataSource();

                String queryCreateTableShopList = 
                        "CREATE TABLE IF NOT EXISTS shops ("
                        + "id INTEGER PRIMARY KEY " + (Database.this instanceof SQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ","
                        + "vendor TINYTEXT NOT NULL,"
                        + "product TEXT NOT NULL,"
                        + "world TINYTEXT NOT NULL,"
                        + "x INTEGER NOT NULL,"
                        + "y INTEGER NOT NULL,"
                        + "z INTEGER NOT NULL,"
                        + "buyprice FLOAT NOT NULL,"
                        + "sellprice FLOAT NOT NULL,"
                        + "shoptype TINYTEXT NOT NULL)";

                String queryCreateTableShopLog = 
                        "CREATE TABLE IF NOT EXISTS `shop_log` ("
                        + "id INTEGER PRIMARY KEY " + (Database.this instanceof SQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ","
                        + "timestamp TINYTEXT NOT NULL,"
                        + "executor TINYTEXT NOT NULL,"
                        + "product TINYTEXT NOT NULL,"
                        + "vendor TINYTEXT NOT NULL,"
                        + "world TINYTEXT NOT NULL,"
                        + "x INTEGER NOT NULL,"
                        + "y INTEGER NOT NULL,"
                        + "z INTEGER NOT NULL,"
                        + "price FLOAT NOT NULL,"
                        + "type TINYTEXT NOT NULL)";

                String queryCreateTablePlayerLogout = 
                        "CREATE TABLE IF NOT EXISTS player_logout ("
                        + "player VARCHAR(36) PRIMARY KEY NOT NULL,"
                        + "time LONG NOT NULL)";

                try {
                    // Create table "shops"
                    try (Connection con = dataSource.getConnection(); Statement s = con.createStatement()) {
                        s.executeUpdate(queryCreateTableShopList);
                    }

                    // Create table "shop_log"
                    try (Connection con = dataSource.getConnection(); Statement s = con.createStatement()) {
                        s.executeUpdate(queryCreateTableShopLog);
                    }

                    // Create table "player_logout"
                    try (Connection con = dataSource.getConnection(); Statement s = con.createStatement()) {
                        s.executeUpdate(queryCreateTablePlayerLogout);
                    }

                    // Clean up economy log
                    if (Config.cleanupEconomyLogDays > 0) {
                        cleanUpEconomy(false);
                    }

                    // Count entries in table "shops"
                    try (Connection con = dataSource.getConnection(); Statement s = con.createStatement();) {
                        ResultSet rs = s.executeQuery("SELECT id FROM shops");

                        int count = 0;
                        while (rs.next()) {
                            count++;
                        }

                        plugin.debug("Initialized database with " + count + " entries");

                        if (callback != null) {
                            callback.callSyncResult(count);
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
                        PreparedStatement ps = con.prepareStatement("DELETE FROM shops WHERE id = ?")) {
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

                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to remove shop from database (#" + shop.getID() + ")");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * @param id       ID of the shop
     * @param callback Callback that - if succeeded - returns whether a shop with
     *                 the given ID exists (as {@code boolean})
     */
    public void isShop(final int id, final Callback<Boolean> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM shops WHERE id = ?")) {
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        if (rs.getInt("id") == id) {
                            if (callback != null) {
                                callback.callSyncResult(true);
                            }
                            return;
                        }
                    }

                    if (callback != null) {
                        callback.callSyncResult(false);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to check if shop with ID exists (#" + id + ")");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get all shops from the database
     * 
     * @param callback            Callback that - if succeeded - returns a read-only
     *                            collection of all shops (as
     *                            {@code Collection<Shop>})
     * @param showConsoleMessages Whether console messages (errors or warnings)
     *                            should be shown
     */
    public void getShops(final boolean showConsoleMessages, final Callback<Collection<Shop>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<Shop> shops = new ArrayList<>();

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM shops")) {
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");

                        plugin.debug("Getting Shop... (#" + id + ")");

                        String worldName = rs.getString("world");
                        World world = Bukkit.getWorld(worldName);
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");

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

                        Location location = new Location(world, x, y, z);

                        plugin.debug("Initializing new shop... (#" + id + ")");

                        OfflinePlayer vendor = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("vendor")));
                        ItemStack product = Utils.decode(rs.getString("product"));
                        double buyPrice = rs.getDouble("buyprice");
                        double sellPrice = rs.getDouble("sellprice");
                        ShopType shopType = ShopType.valueOf(rs.getString("shoptype"));

                        shops.add(new Shop(id, plugin, vendor, product, location, buyPrice, sellPrice, shopType));
                    }

                    if (callback != null) {
                        callback.callSyncResult(Collections.unmodifiableCollection(shops));
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to access database");
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
        final String queryNoId = "REPLACE INTO shops (vendor,product,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?)";
        final String queryWithId = "REPLACE INTO shops (id,vendor,product,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)";

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
                    ps.setString(i+2, Utils.encode(shop.getProduct()));
                    ps.setString(i+3, shop.getLocation().getWorld().getName());
                    ps.setInt(i+4, shop.getLocation().getBlockX());
                    ps.setInt(i+5, shop.getLocation().getBlockY());
                    ps.setInt(i+6, shop.getLocation().getBlockZ());
                    ps.setDouble(i+7, shop.getBuyPrice());
                    ps.setDouble(i+8, shop.getSellPrice());
                    ps.setString(i+9, shop.getShopType().toString());
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

                    plugin.getLogger().severe("Failed to access database");
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
    public void logEconomy(final Player executor, Shop shop, ItemStack product, double price, Type type, final Callback<Void> callback) {
        final String query = "INSERT INTO shop_log (timestamp,executor,product,vendor,world,x,y,z,price,type) VALUES(?,?,?,?,?,?,?,?,?,?)";
        if (Config.enableEconomyLog) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try (Connection con = dataSource.getConnection();
                            PreparedStatement ps = con.prepareStatement(query)) {

                        ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                        ps.setString(2, executor.getUniqueId().toString() + " (" + executor.getName() + ")");
                        ps.setString(3, product.getAmount() + " x " + LanguageUtils.getItemName(product));
                        ps.setString(4, shop.getVendor().getUniqueId().toString() + " (" + shop.getVendor().getName() + ")" + (shop.getShopType() == ShopType.ADMIN ? " (ADMIN)" : ""));
                        ps.setString(5, shop.getLocation().getWorld().getName());
                        ps.setInt(6, shop.getLocation().getBlockX());
                        ps.setInt(7, shop.getLocation().getBlockY());
                        ps.setInt(8, shop.getLocation().getBlockZ());
                        ps.setDouble(9, price);
                        ps.setString(10, type.toString());
                        ps.executeUpdate();

                        if (callback != null) {
                            callback.callSyncResult(null);
                        }

                        plugin.debug("Logged economy transaction to database");
                    } catch (SQLException ex) {
                        if (callback != null) {
                            callback.callSyncError(ex);
                        }

                        plugin.getLogger().severe("Failed to access database");
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
                Calendar cal = Calendar.getInstance();
                long time = System.currentTimeMillis();
                cal.add(Calendar.DATE, -Config.cleanupEconomyLogDays);
                time -= Config.cleanupEconomyLogDays * 86400000L;
                String logPurgeLimit = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
                String queryCleanUpLog = "DELETE FROM shop_log WHERE timestamp < '" + logPurgeLimit + "'";
                String queryCleanUpPlayers = "DELETE FROM player_logout WHERE time < " + String.valueOf(time);

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
                String vendor = String.format("%s (%s)", player.getUniqueId().toString(), player.getName());
                double revenue = 0;

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM shop_log WHERE vendor = ?")) {
                    ps.setString(1, vendor);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        if (rs.getString("vendor").equals(vendor)) {
                            double singleRevenue = rs.getDouble("price");
                            ShopBuySellEvent.Type type = ShopBuySellEvent.Type.valueOf(rs.getString("type"));

                            if (type == ShopBuySellEvent.Type.SELL) {
                                singleRevenue = -singleRevenue;
                            }

                            long timestamp;

                            try {
                                timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("timestamp")).getTime();
                            } catch (ParseException ex) {
                                plugin.debug("Failed to get revenue from player \"" + player.getUniqueId().toString() + "\"");
                                plugin.debug(ex);
                                continue;
                            }

                            if (timestamp > logoutTime) {
                                revenue += singleRevenue;
                            }
                        }
                    }

                    if (callback != null) {
                        callback.callSyncResult(revenue);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to access database");
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
     * @param timestamp Time in milliseconds when the player logged out
     * @param callback  Callback that - if succeeded - returns {@code null}
     */
    public void logLogout(final Player player, final long timestamp, final Callback<Void> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("REPLACE INTO player_logout (player,time) VALUES(?,?)")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setLong(2, timestamp);
                    ps.executeUpdate();

                    if (callback != null) {
                        callback.callSyncResult(null);
                    }

                    plugin.debug("Logged logout to database");
                } catch (final SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to access database");
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
                String uuid = player.getUniqueId().toString();

                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM player_logout WHERE player = ?")) {
                    ps.setString(1, uuid);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        if (rs.getString("player").equals(uuid)) {
                            if (callback != null) {
                                callback.callSyncResult(rs.getLong("time"));
                            }
                            return;
                        }
                    }

                    if (callback != null) {
                        callback.callSyncResult(-1L);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to access database");
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