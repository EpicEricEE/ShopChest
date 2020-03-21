package de.epiceric.shopchest.database;

import org.bukkit.Chunk;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.zaxxer.hikari.HikariDataSource;

public abstract class Database {
    private final Set<String> notFoundWorlds = new HashSet<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean initialized;

    String tableShops;
    String tableLogs;
    String tableLogouts;
    String tableFields;

    ShopChestImpl plugin;
    HikariDataSource dataSource;

    protected Database(ShopChestImpl plugin) {
        this.plugin = plugin;
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
     * Connects or reconnects to the the database and initializes it
     * <p>
     * All tables are created if necessary. If the database structure has to be updated,
     * that is done as well.
     * 
     * @return a completable future that returns the amount of shops found in the database
     */
    public CompletableFuture<Integer> connect() {
        CompletableFuture<Integer> result = new CompletableFuture<>();

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
                result.completeExceptionally(e);
                return;
            }

            if (dataSource == null) {
                Exception e = new IllegalStateException("Data source is null");
                Logger.severe("Failed to get data source: {0}", e.getMessage());
                result.completeExceptionally(e);
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
                        initialized = true;
                        result.complete(count);
                    } else {
                        throw new SQLException("Count result set has no entries");
                    }
                }
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to initialize or connect to database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Removes the given shop from the database
     *
     * @param shop the shop to remove
     * @return a completable future returning nothing
     */
    public CompletableFuture<Void> removeShop(Shop shop) {
        CompletableFuture<Void> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableShops + " WHERE id = ?")) {
                ps.setInt(1, shop.getId());
                ps.executeUpdate();
                result.complete(null);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to remove shop from database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Gets shop amounts for each player
     * 
     * @return a completable future returning a map of each player's shop amounts
     */
    public CompletableFuture<Map<UUID, Integer>> getShopAmounts() {
        CompletableFuture<Map<UUID, Integer>> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    Statement s = con.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT vendor, COUNT(*) AS count FROM " + tableShops + " WHERE shoptype = 'NORMAL' GROUP BY vendor");

                Map<UUID, Integer> shopAmounts = new HashMap<>();
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("vendor"));
                    shopAmounts.put(uuid, rs.getInt("count"));
                }

                result.complete(shopAmounts);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to get shop amounts from database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Gets the shops in the given chunks from the database
     * 
     * @param chunks the chunks whose shops will be retrieved
     * @return a completable future returning the shops
     */
    public CompletableFuture<Collection<Shop>> getShops(Chunk[] chunks) {
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

        CompletableFuture<Collection<Shop>> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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
                } catch (SQLException e) {
                    result.completeExceptionally(e);
                    Logger.severe("Failed to get shops from database");
                    Logger.severe(e);

                    return;
                }
            }

            result.complete(shops);
        });

        return result;
    }

    /**
     * Adds the given shop to the database
     * 
     * @param shop the shop to add
     * @return a completable future returning the added shop's ID
     */
    public CompletableFuture<Integer> addShop(Shop shop) {
        final String queryNoId = "REPLACE INTO " + tableShops + " (vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)";
        final String queryWithId = "REPLACE INTO " + tableShops + " (id,vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        CompletableFuture<Integer> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = shop.getId() != -1 ? queryWithId : queryNoId;

            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                int i = 0;
                if (shop.getId() != -1) {
                    i = 1;
                    ps.setInt(1, shop.getId());
                }

                ps.setString(i+1, shop.getVendor().map(vendor -> vendor.getUniqueId().toString()).orElse("admin"));
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

                result.complete(shop.getId());
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to add shop to database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Updates the given shop in the database
     * 
     * @param shop shop to update
     * @return a completable future that returns nothing
     */
    public CompletableFuture<Void> updateShop(Shop shop) {
        final String query = "REPLACE INTO " + tableShops + " (id,vendor,product,amount,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        CompletableFuture<Void> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, shop.getId());
                ps.setString(2, shop.getVendor().map(vendor -> vendor.getUniqueId().toString()).orElse("admin"));
                ps.setString(3, encodeItemStack(shop.getProduct().getItemStack()));
                ps.setInt(4, shop.getProduct().getAmount());
                ps.setString(5, shop.getLocation().getWorld().getName());
                ps.setInt(6, shop.getLocation().getBlockX());
                ps.setInt(7, shop.getLocation().getBlockY());
                ps.setInt(8, shop.getLocation().getBlockZ());
                ps.setDouble(9, shop.getBuyPrice());
                ps.setDouble(10, shop.getSellPrice());
                ps.setString(11, shop.isAdminShop() ? "ADMIN" : "NORMAL");
                ps.executeUpdate();

                result.complete(null);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to update shop in database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Logs an economy transaction to the database
     * 
     * @param executor the player who bought/sold something
     * @param shop the shop the player bought from or sold to
     * @param product the item that was bought/sold
     * @param price the price the product was bought or sold for
     * @param type whether the executor bought or sold
     * @return a completable future returning nothing
     */
    public CompletableFuture<Void> logEconomy(Player executor, Shop shop, ShopProduct product, double price, Type type) {
        if (!Config.ECONOMY_LOG_ENABLE.get()) {
            return CompletableFuture.completedFuture(null);
        }

        final String query = "INSERT INTO " + tableLogs + " (shop_id,timestamp,time,player_name,player_uuid,product_name,product,amount,"
                + "vendor_name,vendor_uuid,admin,world,x,y,z,price,type) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        CompletableFuture<Void> result = new CompletableFuture<>();

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
                ps.setString(9, shop.getVendor().map(OfflinePlayer::getName).orElse(""));
                ps.setString(10, shop.getVendor().map(vendor -> vendor.getUniqueId().toString()).orElse(""));
                ps.setBoolean(11, shop.isAdminShop());
                ps.setString(12, shop.getLocation().getWorld().getName());
                ps.setInt(13, shop.getLocation().getBlockX());
                ps.setInt(14, shop.getLocation().getBlockY());
                ps.setInt(15, shop.getLocation().getBlockZ());
                ps.setDouble(16, price);
                ps.setString(17, type.toString());
                ps.executeUpdate();

                result.complete(null);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to log economy transaction to database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Cleans up the economy log to reduce file size
     * 
     * @return a completable future returning nothing
     */
    public CompletableFuture<Void> cleanUpEconomy() {
        if (!Config.ECONOMY_LOG_CLEANUP.get()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> result = new CompletableFuture<>();

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
                result.complete(null);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to clean up economy log");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Gets the revenue a player got while he was offline
     * 
     * @param player the player whose revenue to get
     * @param logoutTime the system time in milliseconds when he logged out the last time
     * @return a completable future returning the revenue
     */
    public CompletableFuture<Double> getRevenue(Player player, long logoutTime) {
        CompletableFuture<Double> result = new CompletableFuture<>();

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

                result.complete(revenue);
            } catch (SQLException e) {
                result.complete(null);
                Logger.severe("Failed to get revenue of {0} from database", player.getName());
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Logs the given player's logout to the database
     * 
     * @param player the player who logged out
     * @return a completable future returning nothing
     */
    public CompletableFuture<Void> logLogout(Player player) {
        CompletableFuture<Void> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("REPLACE INTO " + tableLogouts + " (player,time) VALUES(?,?)")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setLong(2, System.currentTimeMillis());
                ps.executeUpdate();
                result.complete(null);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to log last logout to database");
                Logger.severe(e);
            }
        });

        return result;
    }

    /**
     * Gets the system time in milliseconds when the given player logged out last
     * 
     * @param player the player who logged out
     * @return a completable future returning the time or -1 if no recent logout has been found
     */
    public CompletableFuture<Long> getLastLogout(Player player) {
        CompletableFuture<Long> result = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection con = dataSource.getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableLogouts + " WHERE player = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                result.complete(rs.next() ? rs.getLong("time") : -1L);
            } catch (SQLException e) {
                result.completeExceptionally(e);
                Logger.severe("Failed to get last logout from database");
                Logger.severe(e);
            }
        });

        return result;
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
     * Gets whether the database has been fully initialized
     * 
     * @return whether the database is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}