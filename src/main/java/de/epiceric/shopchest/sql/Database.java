package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopBuySellEvent;
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

public abstract class Database {

    private static Set<String> notFoundWorlds = new HashSet<>();

    ShopChest plugin;
    Connection connection;

    Database(ShopChest plugin) {
        this.plugin = plugin;
    }

    /**
     * @return New connection to the database
     */
    public abstract Connection getConnection();

    /**
     * (Re-)Connects to the the database and initializes it. <br>
     * Creates the table (if doesn't exist) and tests the connection
     * @param callback Callback that - if succeeded - returns the amount of shops that were found (as {@code int})
     */
    public void connect(final Callback<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    disconnect();

                    plugin.debug("Connecting to database...");
                    connection = getConnection();

                    String queryCreateTableShopList =
                            "CREATE TABLE IF NOT EXISTS shops (" +
                                    "id INTEGER PRIMARY KEY " + (Database.this instanceof SQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                                    "vendor TINYTEXT NOT NULL," +
                                    "product TEXT NOT NULL," +
                                    "world TINYTEXT NOT NULL," +
                                    "x INTEGER NOT NULL," +
                                    "y INTEGER NOT NULL," +
                                    "z INTEGER NOT NULL," +
                                    "buyprice FLOAT NOT NULL," +
                                    "sellprice FLOAT NOT NULL," +
                                    "shoptype TINYTEXT NOT NULL" +
                                    ")";

                    String queryCreateTableShopLog =
                            "CREATE TABLE IF NOT EXISTS `shop_log` (" +
                                    "id INTEGER PRIMARY KEY " + (Database.this instanceof SQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                                    "timestamp TINYTEXT NOT NULL," +
                                    "executor TINYTEXT NOT NULL," +
                                    "product TINYTEXT NOT NULL," +
                                    "vendor TINYTEXT NOT NULL," +
                                    "world TINYTEXT NOT NULL," +
                                    "x INTEGER NOT NULL," +
                                    "y INTEGER NOT NULL," +
                                    "z INTEGER NOT NULL," +
                                    "price FLOAT NOT NULL," +
                                    "type TINYTEXT NOT NULL" +
                                    ")";

                    String queryCreateTablePlayerLogout =
                            "CREATE TABLE IF NOT EXISTS player_logout (" +
                                    "player VARCHAR(36) PRIMARY KEY NOT NULL," +
                                    "time LONG NOT NULL" +
                                    ")";

                    String queryCheckIfTableExists =
                            (Database.this instanceof SQLite ?
                                    "SELECT name FROM sqlite_master WHERE type='table' AND name='shop_list'" :
                                    "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='shop_list'");

                    String queryCheckIfBackupTableExists =
                            (Database.this instanceof SQLite ?
                                    "SELECT name FROM sqlite_master WHERE type='table' AND name='shop_list_old'" :
                                    "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='shop_list_old'");

                    String queryCopyTableShopList = "INSERT INTO shops (vendor,product,world,x,y,z,buyprice,sellprice,shoptype) SELECT vendor,product,world,x,y,z,buyprice,sellprice,shoptype FROM shop_list";
                    String queryRenameTableShopList = "ALTER TABLE shop_list RENAME TO shop_list_old";

                    // Create table "shops"
                    Statement s = connection.createStatement();
                    s.executeUpdate(queryCreateTableShopList);
                    s.close();

                    // Check if old table "shop_list" exists
                    Statement s2 = connection.createStatement();
                    ResultSet rs = s2.executeQuery(queryCheckIfTableExists);

                    if (rs.next()) {
                        plugin.debug("Table 'shop_list' exists");

                        // Check if backup table "shop_list_old" already exists
                        Statement s3 = connection.createStatement();
                        ResultSet rs2 = s3.executeQuery(queryCheckIfBackupTableExists);

                        if (rs2.next()) {
                            plugin.debug("Backup table 'shop_list_old' already exists: Cannot backup current table");
                        } else {
                            plugin.debug("Backing up old table");

                            // Table exists: Copy contents to new table
                            PreparedStatement ps = connection.prepareStatement(queryCopyTableShopList);
                            ps.executeUpdate();
                            ps.close();

                            plugin.debug("Renaming table");
                            // Rename/Backup old table
                            PreparedStatement ps2 = connection.prepareStatement(queryRenameTableShopList);
                            ps2.executeUpdate();
                            ps2.close();
                        }

                        close(s3, rs2);
                    }

                    close(s2, rs);

                    // Create table "shop_log"
                    Statement s3 = connection.createStatement();
                    s3.executeUpdate(queryCreateTableShopLog);
                    s3.close();

                    // Create table "player_logout"
                    Statement s4 = connection.createStatement();
                    s4.executeUpdate(queryCreateTablePlayerLogout);
                    s4.close();

                    // Clean up economy log
                    if (Config.cleanupEconomyLogDays > 0) {
                        cleanUpEconomy(false);
                    }

                    // Count entries in table "shops"
                    PreparedStatement ps = connection.prepareStatement("SELECT * FROM shops");
                    ResultSet rs2 = ps.executeQuery();

                    int count = 0;
                    while (rs2.next()) {
                        if (rs2.getString("vendor") != null) count++;
                    }
                    plugin.debug("Initialized database with " + count + " entries");

                    close(ps, rs2);

                    if (callback != null) callback.callSyncResult(count);
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to initialize database");
                    plugin.debug("Failed to initialize database");
                    plugin.debug(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Remove a shop from the database
     *
     * @param shop Shop to remove
     * @param callback Callback that - if succeeded - returns {@code null}
     */
    public void removeShop(final Shop shop, final Callback<Void> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;

                try {
                    ps = connection.prepareStatement("DELETE FROM shops WHERE id = ?");
                    ps.setInt(1, shop.getID());
                    ps.executeUpdate();

                    plugin.debug("Removing shop from database (#" + shop.getID() + ")");
                    if (callback != null) callback.callSyncResult(null);
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to remove shop from database (#" + shop.getID() + ")");
                    plugin.debug(ex);
                } finally {
                    close(ps, null);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * @param id ID of the shop
     * @param callback Callback that - if succeeded - returns whether a shop with the given ID exists (as {@code boolean})
     */
    public void isShop(final int id, final Callback<Boolean> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    ps = connection.prepareStatement("SELECT * FROM shops WHERE id = ?");
                    ps.setInt(1, id);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (rs.getInt("id") == id) {
                            if (callback != null) callback.callSyncResult(true);
                            return;
                        }
                    }

                    if (callback != null) callback.callSyncResult(false);
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to check if shop with ID exists (#" + id + ")");
                    plugin.debug(ex);
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get all shops from the database
     * @param callback Callback that - if succeeded - returns a read-only collection of all shops (as {@code Collection<Shop>})
     * @param showConsoleMessages Whether console messages (errors or warnings) should be shown
     */
    public void getShops(final boolean showConsoleMessages, final Callback<Collection<Shop>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;

                ArrayList<Shop> shops = new ArrayList<>();

                try {
                    ps = connection.prepareStatement("SELECT * FROM shops");
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");

                        plugin.debug("Getting Shop... (#" + id + ")");

                        String worldName = rs.getString("world");
                        World world = Bukkit.getWorld(worldName);
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");

                        if (world == null) {
                            WorldNotFoundException ex = new WorldNotFoundException("Could not find world with name \"" + worldName + "\"");
                            if (showConsoleMessages && !notFoundWorlds.contains(worldName)) plugin.getLogger().warning(ex.getMessage());
                            notFoundWorlds.add(worldName);
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

                    if (callback != null) callback.callSyncResult(Collections.unmodifiableCollection(shops));
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to get shops");
                    plugin.debug(ex);
                } finally {
                    close(ps, rs);
                }


            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Adds a shop to the database
     * @param shop Shop to add
     * @param callback Callback that - if succeeded - returns the ID the shop was given (as {@code int})
     */
    public void addShop(final Shop shop, final Callback<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    if (!shop.hasId()) {
                        ps = connection.prepareStatement("REPLACE INTO shops (vendor,product,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                        ps.setString(1, shop.getVendor().getUniqueId().toString());
                        ps.setString(2, Utils.encode(shop.getProduct()));
                        ps.setString(3, shop.getLocation().getWorld().getName());
                        ps.setInt(4, shop.getLocation().getBlockX());
                        ps.setInt(5, shop.getLocation().getBlockY());
                        ps.setInt(6, shop.getLocation().getBlockZ());
                        ps.setDouble(7, shop.getBuyPrice());
                        ps.setDouble(8, shop.getSellPrice());
                        ps.setString(9, shop.getShopType().toString());
                        ps.executeUpdate();

                        int shopId = -1;
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            shopId = rs.getInt(1);
                        }

                        shop.setId(shopId);
                    } else {
                        ps = connection.prepareStatement("REPLACE INTO shops (id,vendor,product,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)");

                        ps.setInt(1, shop.getID());
                        ps.setString(2, shop.getVendor().getUniqueId().toString());
                        ps.setString(3, Utils.encode(shop.getProduct()));
                        ps.setString(4, shop.getLocation().getWorld().getName());
                        ps.setInt(5, shop.getLocation().getBlockX());
                        ps.setInt(6, shop.getLocation().getBlockY());
                        ps.setInt(7, shop.getLocation().getBlockZ());
                        ps.setDouble(8, shop.getBuyPrice());
                        ps.setDouble(9, shop.getSellPrice());
                        ps.setString(10, shop.getShopType().toString());
                        ps.executeUpdate();
                    }

                    if (callback != null) callback.callSyncResult(shop.getID());
                    plugin.debug("Adding shop to database (#" + shop.getID() + ")");
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to add shop to database (#" + shop.getID() + ")");
                    plugin.debug(ex);
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Log an economy transaction to the database
     * @param executor Player who bought/sold something
     * @param product ItemStack that was bought/sold
     * @param vendor Vendor of the shop
     * @param location Location of the shop
     * @param price Price (buyprice or sellprice, depends on {@code type})
     * @param type Whether the player bought or sold something
     * @param callback Callback that - if succeeded - returns {@code null}
     */
    public void logEconomy(final Player executor, final ItemStack product, final OfflinePlayer vendor, final ShopType shopType, final Location location, final double price, final ShopBuySellEvent.Type type, final Callback<Void> callback) {
        if (Config.enableEconomyLog) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PreparedStatement ps = null;

                    try {
                        ps = connection.prepareStatement("INSERT INTO shop_log (timestamp,executor,product,vendor,world,x,y,z,price,type) VALUES(?,?,?,?,?,?,?,?,?,?)");

                        ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                        ps.setString(2, executor.getUniqueId().toString() + " (" + executor.getName() + ")");
                        ps.setString(3, product.getAmount() + " x " + LanguageUtils.getItemName(product));
                        ps.setString(4, vendor.getUniqueId().toString() + " (" + vendor.getName() + ")" + (shopType == ShopType.ADMIN ? " (ADMIN)" : ""));
                        ps.setString(5, location.getWorld().getName());
                        ps.setInt(6, location.getBlockX());
                        ps.setInt(7, location.getBlockY());
                        ps.setInt(8, location.getBlockZ());
                        ps.setDouble(9, price);
                        ps.setString(10, type.toString());
                        ps.executeUpdate();

                        if (callback != null) callback.callSyncResult(null);
                        plugin.debug("Logged economy transaction to database");
                    } catch (final SQLException ex) {
                        if (callback != null) callback.callSyncError(ex);
                        plugin.getLogger().severe("Failed to access database");
                        plugin.debug("Failed to log economy transaction to database");
                        plugin.debug(ex);
                    } finally {
                        close(ps, null);
                    }
                }
            }.runTaskAsynchronously(plugin);
        } else {
            if (callback != null) callback.callSyncResult(null);
        }
    }

    /**
     * Cleans up the economy log to reduce file size
     * @param async Whether the call should be executed asynchronously
     */
    public void cleanUpEconomy(boolean async) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Statement s = null;
                Statement s2 = null;

                Calendar cal = Calendar.getInstance();
                long time = System.currentTimeMillis();
                cal.add(Calendar.DATE, -Config.cleanupEconomyLogDays);
                time -= Config.cleanupEconomyLogDays * 86400000L;
                String logPurgeLimit = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
                String queryCleanUpLog = "DELETE FROM shop_log WHERE timestamp < '" + logPurgeLimit + "'";
                String queryCleanUpPlayers = "DELETE FROM player_logout WHERE time < " + String.valueOf(time);

                try {
                    s = connection.createStatement();
                    s.executeUpdate(queryCleanUpLog);

                    s2 = connection.createStatement();
                    s2.executeUpdate(queryCleanUpPlayers);

                    plugin.getLogger().info("Cleaned up economy log");
                    plugin.debug("Cleaned up economy log");
                } catch (final SQLException ex) {
                    plugin.getLogger().severe("Failed to clean up economy log");
                    plugin.debug("Failed to clean up economy log");
                    plugin.debug(ex);
                } finally {
                    close(s, null);
                    close(s2, null);
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
     * @param player Player whose revenue to get
     * @param logoutTime Time in milliseconds when he logged out the last time
     * @param callback Callback that - if succeeded - returns the revenue the player made while offline (as {@code double})
     */
    public void getRevenue(final Player player, final long logoutTime, final Callback<Double> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;

                String vendor = String.format("%s (%s)", player.getUniqueId().toString(), player.getName());

                double revenue = 0;

                try {
                    ps = connection.prepareStatement("SELECT * FROM shop_log WHERE vendor = ?;");
                    ps.setString(1, vendor);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (rs.getString("vendor").equals(vendor)) {
                            double singleRevenue = rs.getDouble("price");
                            ShopBuySellEvent.Type type = ShopBuySellEvent.Type.valueOf(rs.getString("type"));

                            if (type == ShopBuySellEvent.Type.SELL) singleRevenue = -singleRevenue;

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

                    if (callback != null) callback.callSyncResult(revenue);
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to get revenue from player \"" + player.getUniqueId().toString() + "\"");
                    plugin.debug(ex);
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Log a logout to the database
     * @param player Player who logged out
     * @param timestamp Time in milliseconds when the player logged out
     * @param callback Callback that - if succeeded - returns {@code null}
     */
    public void logLogout(final Player player, final long timestamp, final Callback<Void> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;

                try {
                    ps = connection.prepareStatement("REPLACE INTO player_logout (player,time) VALUES(?,?)");

                    ps.setString(1, player.getUniqueId().toString());
                    ps.setLong(2, timestamp);
                    ps.executeUpdate();

                    if (callback != null) callback.callSyncResult(null);
                    plugin.debug("Logged logout to database");
                } catch (final SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to log logout to database");
                    plugin.debug(ex);
                } finally {
                    close(ps, null);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get the last logout of a player
     * @param player Player who logged out
     * @param callback Callback that - if succeeded - returns the time in milliseconds the player logged out (as {@code long})
     */
    public void getLastLogout(final Player player, final Callback<Long> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;
                ResultSet rs = null;

                String playerUuid = player.getUniqueId().toString();

                try {
                    ps = connection.prepareStatement("SELECT * FROM player_logout WHERE player=?;");
                    ps.setString(1, playerUuid);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        if (rs.getString("player").equals(playerUuid)) {
                            if (callback != null) callback.callSyncResult(rs.getLong("time"));
                            return;
                        }
                    }

                    if (callback != null) callback.callSyncResult(-1L);
                } catch (SQLException ex) {
                    if (callback != null) callback.callSyncError(ex);
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to get last logout from player \"" + playerUuid + "\"");
                    plugin.debug(ex);
                } finally {
                    close(ps, rs);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Closes a {@link Statement} and a {@link ResultSet}
     * @param s {@link Statement} to close
     * @param rs {@link ResultSet} to close
     */
    void close(Statement s, ResultSet rs) {
        try {
            if (s != null)
                s.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            plugin.debug("Failed to close PreparedStatement/ResultSet");
            plugin.debug(ex);
        }
    }

    /**
     * Closes the connection to the database
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                plugin.debug("Disconnecting from database...");
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to disconnect from database");
            plugin.debug("Failed to disconnect from database");
            plugin.debug(e);
        }
    }

    public enum DatabaseType {
        SQLite,
        MySQL
    }
}