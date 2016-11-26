package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public abstract class Database {

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
     */
    public void connect() {
        try {
            disconnect();

            plugin.debug("Connecting to database...");
            connection = getConnection();

            String queryCreateTableShopList =
                        "CREATE TABLE IF NOT EXISTS shop_list (" +
                        "`id` int(11) NOT NULL," +
                        "`vendor` tinytext NOT NULL," +
                        "`product` text NOT NULL," +
                        "`world` tinytext NOT NULL," +
                        "`x` int(11) NOT NULL," +
                        "`y` int(11) NOT NULL," +
                        "`z` int(11) NOT NULL," +
                        "`buyprice` float(32) NOT NULL," +
                        "`sellprice` float(32) NOT NULL," +
                        "`shoptype` tinytext NOT NULL," +
                        "PRIMARY KEY (`id`)" +
                        ");";

            String queryCreateTableShopLog =
                        "CREATE TABLE IF NOT EXISTS shop_log (" +
                        "`id` INTEGER PRIMARY KEY " + (this instanceof SQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT") + "," +
                        "`timestamp` TINYTEXT NOT NULL," +
                        "`executor` TINYTEXT NOT NULL," +
                        "`product` TINYTEXT NOT NULL," +
                        "`vendor` TINYTEXT NOT NULL," +
                        "`world` TINYTEXT NOT NULL," +
                        "`x` INTEGER NOT NULL," +
                        "`y` INTEGER NOT NULL," +
                        "`z` INTEGER NOT NULL," +
                        "`price` FLOAT NOT NULL," +
                        "`type` TINYTEXT NOT NULL" +
                        ");";

            // Create table "shop_list"
            Statement s = connection.createStatement();
            s.executeUpdate(queryCreateTableShopList);
            s.close();

            // Create table "shop_log"
            Statement s2 = connection.createStatement();
            s2.executeUpdate(queryCreateTableShopLog);
            s2.close();

            // Count entries in table "shop_list"
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM shop_list");
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                if (rs.getString("vendor") != null) count++;
            }
            plugin.debug("Initialized database with " + count + " entries");

            close(ps, rs);

        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to connect to database");
            plugin.debug("Failed to connect to database");
            plugin.debug(ex);
        }
    }

    /**
     * @return Lowest possible ID which is not used (> 0)
     */
    public int getNextFreeID() {
        int highestId = getHighestID();
        for (int i = 1; i <= highestId + 1; i++) {
            if (!isShop(i)) {
                plugin.debug("Next free id: " + i);
                return i;
            }
        }

        return 1;
    }

    /**
     * @return Highest ID which is used
     */
    public int getHighestID() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        int highestID = 0;

        try {
            ps = connection.prepareStatement("SELECT * FROM shop_list;");
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getInt("id") > highestID) {
                    highestID = rs.getInt("id");
                }
            }

            plugin.debug("Highest used ID: " + highestID);
            return highestID;

        } catch (SQLException ex) {
            plugin.debug("Failed to get highest used ID");
            plugin.getLogger().severe("Failed to access database");
        } finally {
            close(ps, rs);
        }

        return 0;
    }

    /**
     * Remove a shop from the database
     *
     * @param shop Shop to remove
     */
    public void removeShop(Shop shop) {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("DELETE FROM shop_list WHERE id = " + shop.getID() + ";");
            plugin.debug("Removing shop from database (#" + shop.getID() + ")");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to access database");
            plugin.debug("Failed to remove shop from database (#" + shop.getID() + ")");
            plugin.debug(ex);
        } finally {
            close(ps, null);
        }

    }

    /**
     * @param id ID of the shop
     * @return Whether a shop with the given ID exists
     */
    public boolean isShop(int id) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM shop_list WHERE id = " + id + ";");
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getInt("id") == id) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to access database");
            plugin.debug("Failed to check if shop with ID exists (#" + id + ")");
            plugin.debug(ex);
        } finally {
            close(ps, rs);
        }

        return false;
    }

    /**
     * @param id ID of the shop
     * @return Shop with the given ID
     */
    public Shop getShop(int id) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM shop_list WHERE id = " + id + ";");
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getInt("id") == id) {
                    plugin.debug("Getting Shop... (#" + id + ")");

                    World world = Bukkit.getWorld(rs.getString("world"));
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");

                    Location location = new Location(world, x, y, z);

                    Shop shop = plugin.getShopUtils().getShop(location);
                    if (shop != null) {
                        plugin.debug("Shop already exists, returning existing one (#" + id + ").");
                        return shop;
                    } else {
                        plugin.debug("Creating new shop... (#" + id + ")");

                        OfflinePlayer vendor = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("vendor")));
                        ItemStack product = Utils.decode(rs.getString("product"));
                        double buyPrice = rs.getDouble("buyprice");
                        double sellPrice = rs.getDouble("sellprice");
                        ShopType shopType = ShopType.valueOf(rs.getString("shoptype"));

                        return new Shop(id, plugin, vendor, product, location, buyPrice, sellPrice, shopType);
                    }
                }
            }

            plugin.debug("Shop with ID not found, returning null. (#" + id + ")");
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to access database");
            plugin.debug("Failed to get shop (#" + id + ")");
            plugin.debug(ex);
        } finally {
            close(ps, rs);
        }

        return null;
    }

    /**
     * Adds a shop to the database
     * @param shop Shop to add
     */
    public void addShop(Shop shop) {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("REPLACE INTO shop_list (id,vendor,product,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)");

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

            plugin.debug("Adding shop to database (#" + shop.getID() + ")");

            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to access database");
            plugin.debug("Failed to add shop to database (#" + shop.getID() + ")");
            plugin.debug(ex);
        } finally {
            close(ps, null);
        }
    }

    /**
     * Log an economy transaction to the database
     * @param executor Player who bought/sold something
     * @param product ItemStack that was bought/sold
     * @param vendor Vendor of the shop
     * @param location Location of the shop
     * @param price Price (buyprice or sellprice, depends on {@code type})
     * @param type Whether the player bought or sold something
     */
    public void logEconomy(final Player executor, final ItemStack product, final OfflinePlayer vendor, final ShopType shopType, final Location location, final double price, final ShopBuySellEvent.Type type) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {

                PreparedStatement ps = null;
                boolean debugLogEnabled = plugin.getShopChestConfig().enable_debug_log;

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

                    if (debugLogEnabled) {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.debug("Logged economy transaction to database");
                            }
                        });
                    }
                } catch (final SQLException ex) {
                    plugin.getLogger().severe("Failed to access database");
                    if (debugLogEnabled) {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                plugin.debug("Failed to log economy transaction to database");
                                plugin.debug(ex);
                            }
                        });
                    }
                } finally {
                    close(ps, null);
                }
            }
        });
    }

    /**
     * Closes a {@link PreparedStatement} and a {@link ResultSet}
     * @param ps {@link PreparedStatement} to close
     * @param rs {@link ResultSet} to close
     */
    private void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
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