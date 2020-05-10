package de.epiceric.shopchest.sql;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.event.ShopBuySellEvent.Type;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.Utils;

public abstract class Database {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean initialized;

    String tableShops;
    String tableLogs;
    String tableLogouts;
    String tableFields;

    ShopChest plugin;
    HikariDataSource dataSource;

    protected Database(ShopChest plugin) {
        this.plugin = plugin;
    }

    abstract HikariDataSource getDataSource();

    abstract String getQueryCreateTableShops();

    abstract String getQueryCreateTableLog();

    abstract String getQueryCreateTableLogout();

    abstract String getQueryCreateTableFields();

    abstract String getQueryGetTable();

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

                plugin.getLogger().info("Updating database... (#1)");

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
                        ItemStack is = Utils.decode(rs.getString("product"));
                        int amount = is.getAmount();
                        is.setAmount(1);
                        String product = Utils.encode(is);
                        
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
                            plugin.debug("Failed to parse timestamp '" + timestamp + "': Time is set to 0");
                            plugin.debug(e);
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
                plugin.getLogger().info("Updating database... (#2)");

                // Create fields table
                try (Statement s = con.createStatement()) {
                    s.executeUpdate(getQueryCreateTableFields());
                }

                setDatabaseVersion(2);
            }
            
            int databaseVersion = getDatabaseVersion();

            if (databaseVersion < 3) {
                // plugin.getLogger().info("Updating database... (#3)");

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
    public void connect(final Callback<Integer> callback) {
        if (!Config.databaseTablePrefix.matches("^([a-zA-Z0-9\\-\\_]+)?$")) {
            // Only letters, numbers dashes and underscores are allowed
            plugin.getLogger().severe("Database table prefix contains illegal letters, using 'shopchest_' prefix.");
            Config.databaseTablePrefix = "shopchest_";
        }

        this.tableShops = Config.databaseTablePrefix + "shops";
        this.tableLogs = Config.databaseTablePrefix + "economy_logs";
        this.tableLogouts = Config.databaseTablePrefix + "player_logouts";
        this.tableFields = Config.databaseTablePrefix + "fields";

        new BukkitRunnable() {
            @Override
            public void run() {
                disconnect();

                try {
                    dataSource = getDataSource();
                } catch (Exception e) {
                    callback.onError(e);
                    plugin.debug(e);
                    return;
                }

                if (dataSource == null) {
                    Exception e = new IllegalStateException("Data source is null");
                    callback.onError(e);
                    plugin.debug(e);
                    return;
                }

                try (Connection con = dataSource.getConnection()) {
                    // Update database structure if necessary
                    if (update()) {
                        plugin.getLogger().info("Updating database finished");
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
                    if (Config.cleanupEconomyLogDays > 0) {
                        cleanUpEconomy(false);
                    }

                    // Count shops entries in database
                    try (Statement s = con.createStatement()) {
                        ResultSet rs = s.executeQuery("SELECT COUNT(id) FROM " + tableShops);
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            initialized = true;
                            
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
                    
                    plugin.getLogger().severe("Failed to initialize or connect to database");
                    plugin.debug("Failed to initialize or connect to database");
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
     * Get shop amounts for each player
     * 
     * @param callback Callback that returns a map of each player's shop amount
     */
    public void getShopAmounts(final Callback<Map<UUID, Integer>> callback) {
        new BukkitRunnable(){
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        Statement s = con.createStatement()) {
                    ResultSet rs = s.executeQuery("SELECT vendor, COUNT(*) AS count FROM " + tableShops + " WHERE shoptype = 'NORMAL' GROUP BY vendor");

                    plugin.debug("Getting shop amounts from database");

                    Map<UUID, Integer> result = new HashMap<>();
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("vendor"));
                        result.put(uuid, rs.getInt("count"));
                    }

                    if (callback != null) {
                        callback.callSyncResult(result);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to get shop amounts from database");
                    plugin.debug("Failed to get shop amounts from database");
                    plugin.debug(ex);
                }        
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get all shops of a player, including admin shops
     * 
     * @param callback Callback that returns a set of shops of the given player
     */
    public void getShops(UUID playerUuid, final Callback<Collection<Shop>> callback) {
        new BukkitRunnable(){
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableShops + " WHERE vendor = ?")) {
                    ps.setString(1, playerUuid.toString());
                    ResultSet rs = ps.executeQuery();

                    plugin.debug("Getting a player's shops from database");

                    Set<Shop> result = new HashSet<>();
                    while (rs.next()) {
                        int id = rs.getInt("id");

                        plugin.debug("Getting Shop... (#" + id + ")");

                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");

                        World world = plugin.getServer().getWorld(rs.getString("world"));
                        Location location = new Location(world, x, y, z);
                        OfflinePlayer vendor = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("vendor")));
                        ItemStack itemStack = Utils.decode(rs.getString("product"));
                        int amount = rs.getInt("amount");
                        ShopProduct product = new ShopProduct(itemStack, amount);
                        double buyPrice = rs.getDouble("buyprice");
                        double sellPrice = rs.getDouble("sellprice");
                        ShopType shopType = ShopType.valueOf(rs.getString("shoptype"));

                        plugin.debug("Initializing new shop... (#" + id + ")");

                        result.add(new Shop(id, plugin, vendor, product, location, buyPrice, sellPrice, shopType));
                    }

                    if (callback != null) {
                        callback.callSyncResult(result);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    plugin.getLogger().severe("Failed to get player's shops from database");
                    plugin.debug("Failed to get player's shops from database");
                    plugin.debug(ex);
                }        
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Get all shops from the database that are located in the given chunks
     * 
     * @param chunks Shops in these chunks are retrieved
     * @param callback Callback that returns an immutable collection of shops if succeeded
     */
    public void getShopsInChunks(final Chunk[] chunks, final Callback<Collection<Shop>> callback) {
        // Split chunks into packages containing each {splitSize} chunks at max
        int splitSize = 80;
        int parts = (int) Math.ceil(chunks.length / (double) splitSize);
        Chunk[][] splitChunks = new Chunk[parts][];
        for (int i = 0; i < parts; i++) {
            int size = i < parts - 1 ? splitSize : chunks.length % splitSize;
            Chunk[] tmp = new Chunk[size];
            System.arraycopy(chunks, i * splitSize, tmp, 0, size);
            splitChunks[i] = tmp;
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                List<Shop> shops = new ArrayList<>();

                // Send a request for each chunk package
                for (Chunk[] newChunks : splitChunks) {

                    // Map chunks by world
                    Map<String, Set<Chunk>> chunksByWorld = new HashMap<>();
                    for (Chunk chunk : newChunks) {
                        String world = chunk.getWorld().getName();
                        Set<Chunk> chunksForWorld = chunksByWorld.getOrDefault(world, new HashSet<>());
                        chunksForWorld.add(chunk);
                        chunksByWorld.put(world, chunksForWorld);
                    }
    
                    // Create query dynamically
                    String query = "SELECT * FROM " + tableShops + " WHERE ";
                    for (String world : chunksByWorld.keySet()) {
                        query += "(world = ? AND (";
                        int chunkNum = chunksByWorld.get(world).size();
                        for (int i = 0; i < chunkNum; i++) {
                            query += "((x BETWEEN ? AND ?) AND (z BETWEEN ? AND ?)) OR ";
                        }
                        query += "1=0)) OR ";
                    }
                    query += "1=0";
    
                    try (Connection con = dataSource.getConnection();
                            PreparedStatement ps = con.prepareStatement(query)) {
                        int index = 0;
                        for (String world : chunksByWorld.keySet()) {
                            ps.setString(++index, world);
                            for (Chunk chunk : chunksByWorld.get(world)) {
                                int minX = chunk.getX() * 16;
                                int minZ = chunk.getZ() * 16;
                                ps.setInt(++index, minX);
                                ps.setInt(++index, minX + 15);
                                ps.setInt(++index, minZ);
                                ps.setInt(++index, minZ + 15);
                            }
                        }
    
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            int id = rs.getInt("id");
    
                            plugin.debug("Getting Shop... (#" + id + ")");
    
                            int x = rs.getInt("x");
                            int y = rs.getInt("y");
                            int z = rs.getInt("z");
    
                            World world = plugin.getServer().getWorld(rs.getString("world"));
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
                    } catch (SQLException ex) {
                        if (callback != null) {
                            callback.callSyncError(ex);
                        }
    
                        plugin.getLogger().severe("Failed to get shops from database");
                        plugin.debug("Failed to get shops");
                        plugin.debug(ex);

                        return;
                    }
                }
    
                if (callback != null) {
                    callback.callSyncResult(Collections.unmodifiableCollection(shops));
                }
            };
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
                        ps.setString(2, dateFormat.format(millis));
                        ps.setLong(3, millis);
                        ps.setString(4, executor.getName());
                        ps.setString(5, executor.getUniqueId().toString());
                        ps.setString(6, product.getLocalizedName());
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
            dataSource = null;
        }
    }

    /**
     * Returns whether a connection to the database has been established
     */
    public boolean isInitialized() {
        return initialized;
    }

    public enum DatabaseType {
        SQLite, MySQL
    }
}