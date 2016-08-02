package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;

public abstract class Database {

    public ShopChest plugin;
    public Connection connection;

    public Database(ShopChest plugin) {
        this.plugin = plugin;
        initialize();
    }

    /**
     * @return Connection to the database
     */
    public abstract Connection getConnection();

    /**
     * Initializes the database. <br>
     * Creates the table (if doesn't exist) and tests the connection
     */
    private void initialize() {
        connection = getConnection();

        try {
            String queryCreateTable = "CREATE TABLE IF NOT EXISTS shop_list (" +
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

            Statement s = connection.createStatement();
            s.executeUpdate(queryCreateTable);
            s.close();

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM shop_list");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return Lowest possible ID which is not used (> 0)
     */
    public int getNextFreeID() {
        for (int i = 1; i < getHighestID() + 1; i++) {
            if (get(i, ShopInfo.X) == null) {
                return i;
            } else {
                if (i == getHighestID()) {
                    return i + 1;
                }
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

        int highestID = 1;

        try {
            ps = connection.prepareStatement("SELECT * FROM shop_list;");
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getInt("id") > highestID) {
                    highestID = rs.getInt("id");
                }
            }

            return highestID;

        } catch (SQLException ex) {
            ex.printStackTrace();
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
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(ps, null);
        }

    }

    /**
     * @param id ID of the shop
     * @param shopInfo What to get
     * @return Value you wanted to get. This needs to be casted to the right type!
     */
    public Object get(int id, ShopInfo shopInfo) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM shop_list WHERE id = " + id + ";");
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getInt("id") == id) {

                    switch (shopInfo) {
                        case SHOP:
                            Shop shop = plugin.getShopUtils().getShop((Location) get(id, ShopInfo.LOCATION));
                            if (shop != null)
                                return shop;
                            else {
                                return new Shop(id, plugin,
                                        (OfflinePlayer) get(id, ShopInfo.VENDOR),
                                        (ItemStack) get(id, ShopInfo.PRODUCT),
                                        (Location) get(id, ShopInfo.LOCATION),
                                        (double) get(id, ShopInfo.BUYPRICE),
                                        (double) get(id, ShopInfo.SELLPRICE),
                                        (ShopType) get(id, ShopInfo.SHOPTYPE));
                            }
                        case VENDOR:
                            return Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("vendor")));
                        case PRODUCT:
                            return Utils.decode(rs.getString("product"));
                        case WORLD:
                            return Bukkit.getWorld(rs.getString("world"));
                        case X:
                            return rs.getInt("x");
                        case Y:
                            return rs.getInt("y");
                        case Z:
                            return rs.getInt("z");
                        case LOCATION:
                            return new Location((World) get(id, ShopInfo.WORLD), (int) get(id, ShopInfo.X), (int) get(id, ShopInfo.Y), (int) get(id, ShopInfo.Z));
                        case BUYPRICE:
                            return rs.getDouble("buyprice");
                        case SELLPRICE:
                            return rs.getDouble("sellprice");
                        case SHOPTYPE:
                            String shoptype = rs.getString("shoptype");

                            if (shoptype.equals("INFINITE")) {

                                Shop newShop = new Shop(id, plugin,
                                        (OfflinePlayer) get(id, ShopInfo.VENDOR),
                                        (ItemStack) get(id, ShopInfo.PRODUCT),
                                        (Location) get(id, ShopInfo.LOCATION),
                                        (double) get(id, ShopInfo.BUYPRICE),
                                        (double) get(id, ShopInfo.SELLPRICE),
                                        ShopType.ADMIN);

                                addShop(newShop);

                                return ShopType.ADMIN;
                            }
                            return ShopType.valueOf(shoptype);
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
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

            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(ps, null);
        }
    }

    /**
     * @return Whether ShopChest is connected to the database
     */
    private boolean isConnected() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        boolean connected = false;

        try {
            ps = connection.prepareStatement("SELECT * FROM shop_list");
            rs = ps.executeQuery();
            connected = true;
        } catch (SQLException e) {
            connected = false;
        } finally {
            close(ps, rs);
        }

        return connected;
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
            ex.printStackTrace();
        }
    }

    public enum ShopInfo {
        SHOP,
        VENDOR,
        PRODUCT,
        WORLD,
        X,
        Y,
        Z,
        LOCATION,
        BUYPRICE,
        SELLPRICE,
        SHOPTYPE
    }

    public enum DatabaseType {
        SQLite,
        MySQL
    }
}