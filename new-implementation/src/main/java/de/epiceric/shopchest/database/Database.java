package de.epiceric.shopchest.database;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.event.ShopBuySellEvent.Type;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.shop.ShopImpl;
import de.epiceric.shopchest.shop.ShopProductImpl;
import de.epiceric.shopchest.util.Logger;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.zaxxer.hikari.HikariDataSource;

public abstract class Database {
    private final Set<String> notFoundWorlds = new HashSet<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String tableShops;
    String tableLogs;
    String tableLogouts;
    String tableFields;

    ShopChestImpl plugin;
    HikariDataSource dataSource;

    protected Database(ShopChestImpl plugin) {
        this.plugin = plugin;
    }

    protected <T> void callSyncResult(Consumer<T> callback, T result) {
        if (callback == null) return;

        if (plugin.getServer().isPrimaryThread()) {
            callback.accept(result);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(result));
        }
    }

    protected void callSyncError(Consumer<Throwable> callback, Throwable error) {
        if (callback == null) return;

        if (plugin.getServer().isPrimaryThread()) {
            callback.accept(error);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(error));
        }
    }

    abstract HikariDataSource getDataSource();

    abstract String getQueryCreateTableShops();

    abstract String getQueryCreateTableLog();

    abstract String getQueryCreateTableLogout();

    abstract String getQueryCreateTableFields();

    abstract String getQueryGetTable();

    private ItemStack decodeItemStack(String encoded) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            Logger.severe("Failed to decode ItemStack");
            Logger.severe(e);
            return null;
        }
        return config.getItemStack("i", null);
    }

    private String encodeItemStack(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return Base64.getEncoder().encodeToString(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    private int getDatabaseVersion() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            try (Statement s = con.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT value FROM " + tableFields + " WHERE field='version'");
                if (rs.next()) {
                    return rs.getInt("value");
                }
            }
        }
        return 0;
    }

    private void setDatabaseVersion(int version) throws SQLException {
        String queryUpdateVersion = "REPLACE INTO " + tableFields + " VALUES ('version', ?)";
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(queryUpdateVersion)) {
                ps.setInt(1, version);
                ps.executeUpdate();
            }
        }
    }

    private boolean update() throws SQLException {
        String queryGetTable = getQueryGetTable();

        try (Connection con = dataSource.getConnection()) {
            boolean needsUpdate1 = false; // update "shop_log" to "economy_logs" and update "shops" with prefixes
            boolean needsUpdate2 = false; // create field table and set database version

            try (PreparedStatement ps = con.prepareStatement(queryGetTable)) {
                ps.setString(1, "shop_log");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    needsUpdate1 = true;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(queryGetTable)) {
                ps.setString(1, tableFields);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    needsUpdate2 = true;
                }
            }

            if (needsUpdate1) {
                String queryRenameTableLogouts = "ALTER TABLE player_logout RENAME TO " + tableLogouts;
                String queryRenameTableLogs = "ALTER TABLE shop_log RENAME TO backup_shop_log"; // for backup
                String queryRenameTableShops = "ALTER TABLE shops RENAME TO backup_shops"; // for backup

                Logger.info("Updating database... (#1)");

                // Rename logout table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(queryRenameTableLogouts);
                }

                // Backup shops table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(queryRenameTableShops);
                }

                // Backup log table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(queryRenameTableLogs);
                }

                // Create new shops table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableShops());
                }

                // Create new log table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableLog());
                }

                // Convert shop table
                try (Statement s = con.createStatement()) {
                    ResultSet rs = s.executeQuery("SELECT id,product FROM backup_shops");
                    while (rs.next()) {
                        ItemStack is = decodeItemStack(rs.getString("product"));
                        int amount = is.getAmount();
                        is.setAmount(1);
                        String product = encodeItemStack(is);
                        
                        String insertQuery = "INSERT INTO " + tableShops + " SELECT id,vendor,?,?,world,x,y,z,buyprice,sellprice,shoptype FROM backup_shops WHERE id = ?";
                        try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                            ps.setString(1, product);
                            ps.setInt(2, amount);
                            ps.setInt(3, rs.getInt("id"));
                            ps.executeUpdate();
                        }
                    }
                }

                // Convert log table
                try (Statement s = con.createStatement()) {
                    ResultSet rs = s.executeQuery("SELECT id,timestamp,executor,product,vendor FROM backup_shop_log");
                    while (rs.next()) {
                        String timestamp = rs.getString("timestamp");
                        long time = 0L;

                        try {
                            time = dateFormat.parse(timestamp).getTime();
                        } catch (ParseException e) {
                            Logger.warning("Failed to parse timestamp \"{0}\": Time is set to 0", timestamp);
                        }

                        String player = rs.getString("executor");
                        String playerUuid = player.substring(0, 36);
                        String playerName = player.substring(38, player.length() - 1);

                        String oldProduct = rs.getString("product");
                        String product = oldProduct.split(" x ")[1];
                        int amount = Integer.valueOf(oldProduct.split(" x ")[0]);

                        String vendor = rs.getString("vendor");
                        String vendorUuid = vendor.substring(0, 36);
                        String vendorName = vendor.substring(38).replaceAll("\\)( \\(ADMIN\\))?", "");
                        boolean admin = vendor.endsWith("(ADMIN)");
                        
                        String insertQuery = "INSERT INTO " + tableLogs + " SELECT id,-1,timestamp,?,?,?,?,'Unknown',?,?,?,?,world,x,y,z,price,type FROM backup_shop_log WHERE id = ?";
                        try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                            ps.setLong(1, time);
                            ps.setString(2, playerName);
                            ps.setString(3, playerUuid);
                            ps.setString(4, product);
                            ps.setInt(5, amount);
                            ps.setString(6, vendorName);
                            ps.setString(7, vendorUuid);
                            ps.setBoolean(8, admin);
                            ps.setInt(9, rs.getInt("id"));
                            ps.executeUpdate();
                        }
                    }
                }
            }

            if (needsUpdate2) {
                Logger.info("Updating database... (#2)");

                // Create fields table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableFields());
                }

                setDatabaseVersion(2);
            }
            
            int databaseVersion = getDatabaseVersion();

            if (databaseVersion < 3) {
                // Logger.info("Updating database... (#3)");

                // Update database structure...

                // setDatabaseVersion(3);
            }

            int newDatabaseVersion = getDatabaseVersion();
            return needsUpdate1 || needsUpdate2 || newDatabaseVersion > databaseVersion;
        }
    }

    /**
     * <p>(Re-)Connects to the the database and initializes it.</p>
     * 
     * All tables are created if necessary and if the database
     * structure has to be updated, that is done as well.
     * 
     * @param callback Callback that - if succeeded - returns the amount of shops
     *                 that were found (as {@code int})
     */
    public void connect(Consumer<Integer> callback, Consumer<Throwable> errorCallback) {
        if (!Config.DATABASE_TABLE_PREFIX.get().matches("^([a-zA-Z0-9\\-\\_]+)?$")) {
            // Only letters, numbers dashes and underscores are allowed
            Logger.severe("Database table prefix contains illegal letters, using 'shopchest_' prefix.");
            Config.DATABASE_TABLE_PREFIX.set("shopchest_");
        }

        String tablePrefix = Config.DATABASE_TABLE_PREFIX.get();

        this.tableShops = tablePrefix + "shops";
        this.tableLogs = tablePrefix + "economy_logs";
        this.tableLogouts = tablePrefix + "player_logouts";
        this.tableFields = tablePrefix + "fields";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            disconnect();

            try {
                dataSource = getDataSource();
            } catch (Exception e) {
                callSyncError(errorCallback, e);
                return;
            }

            if (dataSource == null) {
                Exception e = new IllegalStateException("Data source is null");
                Logger.severe("Failed to get data source: {0}", e.getMessage());
                callSyncError(errorCallback, e);
                return;
            }

            try (Connection con = dataSource.getConnection()) {
                // Update database structure if necessary
                if (update()) {
                    Logger.info("Updating database finished");
                }

                // Create shop table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableShops());
                }

                // Create log table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableLog());
                }

                // Create logout table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableLogout());
                }

                // Create fields table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableFields());
                }

                // Clean up economy log
                if (Config.ECONOMY_LOG_CLEANUP.get()) {
                    cleanUpEconomy();
                }

                // Count shops entries in database
                try (Statement s = con.createStatement()) {
                    ResultSet rs = s.executeQuery("SELECT COUNT(id) FROM " + tableShops);
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        callSyncResult(callback, count);
                    } else {
                        throw new SQLException("Count result set has no entries");
                    }
                }
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to initialize or connect to database");
                Logger.severe(e);
            }
        });
    }

    /**
     * Remove a shop from the database
     *
     * @param shop     Shop to remove
     * @param callback Callback that - if succeeded - returns {@code null}
     */
    public void removeShop(Shop shop, Consumer<Void> callback, Consumer<Throwable> errorCallback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableShops + " WHERE id = ?")) {
                ps.setInt(1, shop.getId());
                ps.executeUpdate();
                callSyncResult(callback, null);
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to remove shop from database");
                Logger.severe(e);
            }
        });
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
    public void getShops(Consumer<Collection<Shop>> callback, Consumer<Throwable> errorCallback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Shop> shops = new ArrayList<>();

            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableShops + "")) {
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");

                    String worldName = rs.getString("world");
                    World world = plugin.getServer().getWorld(worldName);

                    if (world == null) {
                        if (!notFoundWorlds.contains(worldName)) {
                            Logger.warning("Could not find world with name \"{0}\"", worldName);
                            notFoundWorlds.add(worldName);
                        }
                        continue;
                    }

                    boolean admin = rs.getString("shoptype").equalsIgnoreCase("ADMIN");
                    OfflinePlayer vendor = admin ? null
                            : plugin.getServer().getOfflinePlayer(UUID.fromString(rs.getString("vendor")));

                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    Location location = new Location(world, x, y, z);

                    ItemStack itemStack = decodeItemStack(rs.getString("product"));
                    int amount = rs.getInt("amount");
                    ShopProduct product = new ShopProductImpl(itemStack, amount);

                    double buyPrice = rs.getDouble("buyprice");
                    double sellPrice = rs.getDouble("sellprice");

                    shops.add(new ShopImpl(id, vendor, product, location, buyPrice, sellPrice));
                }

                callSyncResult(callback, Collections.unmodifiableCollection(shops));
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to get shops from database");
                Logger.severe(e);
            }

        });
    }

    /**
     * Adds a shop to the database
     * 
     * @param shop     Shop to add
     * @param callback Callback that - if succeeded - returns the ID the shop was
     *                 given (as {@code int})
     */
    public void addShop(Shop shop, Consumer<Integer> callback, Consumer<Throwable> errorCallback) {
        final String queryNoId = "REPLACE INTO " + tableShops + " (vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)";
        final String queryWithId = "REPLACE INTO " + tableShops + " (id,vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = shop.getId() != -1 ? queryWithId : queryNoId;

            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                int i = 0;
                if (shop.getId() != -1) {
                    i = 1;
                    ps.setInt(1, shop.getId());
                }

                ps.setString(i+1, shop.isAdminShop() ? "admin" : shop.getVendor().getUniqueId().toString());
                ps.setString(i+2, encodeItemStack(shop.getProduct().getItemStack()));
                ps.setInt(i+3, shop.getProduct().getAmount());
                ps.setString(i+4, shop.getLocation().getWorld().getName());
                ps.setInt(i+5, shop.getLocation().getBlockX());
                ps.setInt(i+6, shop.getLocation().getBlockY());
                ps.setInt(i+7, shop.getLocation().getBlockZ());
                ps.setDouble(i+8, shop.getBuyPrice());
                ps.setDouble(i+9, shop.getSellPrice());
                ps.setString(i+10, shop.isAdminShop() ? "ADMIN" : "NORMAL");
                ps.executeUpdate();

                if (shop.getId() == -1) {
                    int shopId = -1;
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        shopId = rs.getInt(1);
                    }
                    ((ShopImpl) shop).setId(shopId);
                }

                callSyncResult(callback, shop.getId());
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to add shop to database");
                Logger.severe(e);
            }
        });
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
    public void logEconomy(Player executor, Shop shop, ShopProduct product, double price, Type type, Consumer<Void> callback, Consumer<Throwable> errorCallback) {
        if (!Config.ECONOMY_LOG_ENABLE.get()) {
            callSyncResult(callback, null);
            return;
        }

        final String query = "INSERT INTO " + tableLogs + " (shop_id,timestamp,time,player_name,player_uuid,product_name,product,amount,"
                + "vendor_name,vendor_uuid,admin,world,x,y,z,price,type) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement(query)) {

                long millis = System.currentTimeMillis();

                ps.setInt(1, shop.getId());
                ps.setString(2, dateFormat.format(millis));
                ps.setLong(3, millis);
                ps.setString(4, executor.getName());
                ps.setString(5, executor.getUniqueId().toString());
                ps.setString(6, product.getLocalizedName());
                ps.setString(7, encodeItemStack(product.getItemStack()));
                ps.setInt(8, product.getAmount());
                ps.setString(9, shop.getVendor().getName());
                ps.setString(10, shop.getVendor().getUniqueId().toString());
                ps.setBoolean(11, shop.isAdminShop());
                ps.setString(12, shop.getLocation().getWorld().getName());
                ps.setInt(13, shop.getLocation().getBlockX());
                ps.setInt(14, shop.getLocation().getBlockY());
                ps.setInt(15, shop.getLocation().getBlockZ());
                ps.setDouble(16, price);
                ps.setString(17, type.toString());
                ps.executeUpdate();

                callSyncResult(callback, null);
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to log economy transaction to database");
                Logger.severe(e);
            }
        });
    }

    /**
     * Cleans up the economy log to reduce file size
     */
    public void cleanUpEconomy() {
        if (!Config.ECONOMY_LOG_CLEANUP.get()) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int cleanupDays = Config.ECONOMY_LOG_CLEANUP_DAYS.get();
            long time = System.currentTimeMillis() - cleanupDays * 86400000L;
            String queryCleanUpLog = "DELETE FROM " + tableLogs + " WHERE time < " + time;
            String queryCleanUpPlayers = "DELETE FROM " + tableLogouts + " WHERE time < " + time;

            try (Connection con = dataSource.getConnection();
                    Statement s = con.createStatement();
                    Statement s2 = con.createStatement()) {
                s.executeUpdate(queryCleanUpLog);
                s2.executeUpdate(queryCleanUpPlayers);
                Logger.info("Cleaned up economy log entries older than {0} days", cleanupDays);
            } catch (SQLException e) {
                Logger.severe("Failed to clean up economy log");
                Logger.severe(e);
            }
        });
    }

    /**
     * Get the revenue a player got while he was offline
     * 
     * @param player     Player whose revenue to get
     * @param logoutTime Time in milliseconds when he logged out the last time
     * @param callback   Callback that - if succeeded - returns the revenue the
     *                   player made while offline (as {@code double})
     */
    public void getRevenue(Player player, long logoutTime, Consumer<Double> callback, Consumer<Throwable> errorCallback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            double revenue = 0;

            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableLogs + " WHERE vendor_uuid = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    long timestamp = rs.getLong("time");
                    double singleRevenue = rs.getDouble("price");
                    Type type = Type.valueOf(rs.getString("type"));

                    if (type == Type.SELL) {
                        singleRevenue = -singleRevenue;
                    }

                    if (timestamp > logoutTime) {
                        revenue += singleRevenue;
                    }
                }

                callSyncResult(callback, revenue);
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to get revenue of {0} from database", player.getName());
                Logger.severe(e);
            }
        });
    }

    /**
     * Log a logout to the database
     * 
     * @param player    Player who logged out
     * @param callback  Callback that - if succeeded - returns {@code null}
     */
    public void logLogout(Player player, Consumer<Void> callback, Consumer<Throwable> errorCallback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("REPLACE INTO " + tableLogouts + " (player,time) VALUES(?,?)")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setLong(2, System.currentTimeMillis());
                ps.executeUpdate();
                callSyncResult(callback, null);
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to log last logout to database");
                Logger.severe(e);
            }
        });
    }

    /**
     * Get the last logout of a player
     * 
     * @param player   Player who logged out
     * @param callback Callback that - if succeeded - returns the time in
     *                 milliseconds the player logged out (as {@code long})
     *                 or {@code -1} if the player has not logged out yet.
     */
    public void getLastLogout(Player player, Consumer<Long> callback, Consumer<Throwable> errorCallback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableLogouts + " WHERE player = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                callSyncResult(callback, rs.next() ? rs.getLong("time") : -1L);
            } catch (SQLException e) {
                callSyncError(errorCallback, e);
                Logger.severe("Failed to get last logout from database");
                Logger.severe(e);
            }
        });
    }

    /**
     * Closes the data source
     */
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}