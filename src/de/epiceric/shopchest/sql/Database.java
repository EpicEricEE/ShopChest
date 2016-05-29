package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class Database {

    private ShopChest plugin;
    private Connection connection;

    public Database(ShopChest instance) {
        plugin = instance;
        initialize();
    }

    private Connection getSQLConnection() {
        File dbFile = new File(plugin.getDataFolder(), "shops.db");

        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);

            return connection;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void initialize() {
        connection = getSQLConnection();
        try {
            String queryCreateTable = "CREATE TABLE IF NOT EXISTS shop_list (" +
                    "`id` int(11) NOT NULL," +
                    "`vendor` varchar(32) NOT NULL," +
                    "`product` varchar(32) NOT NULL," +
                    "`world` varchar(32) NOT NULL," +
                    "`x` int(11) NOT NULL," +
                    "`y` int(11) NOT NULL," +
                    "`z` int(11) NOT NULL," +
                    "`buyprice` float(32) NOT NULL," +
                    "`sellprice` float(32) NOT NULL," +
                    "`shoptype` varchar(32) NOT NULL," +
                    "PRIMARY KEY (`id`)" +
                    ");";

            Statement s = connection.createStatement();
            s.executeUpdate(queryCreateTable);
            s.close();

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM shop_list WHERE id = ?");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

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

    public int getShopID(Shop shop) {
        for (int i = 1; i < getHighestID() + 1; i++) {
            try {
                Shop s = (Shop) get(i, null);
                if (s.getLocation().equals(shop.getLocation())) {
                    return i;
                }
            } catch (NullPointerException ex) { /* Empty catch block... */ }
        }

        return 0;
    }

    public void removeShop(Shop shop) {
        int id = getShopID(shop);
        if (id == 0) return;

        if (shop.hasItem()) shop.getItem().remove();

        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("DELETE FROM shop_list WHERE id = " + id + ";");
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(ps, null);
        }

    }

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
                            Shop shop = ShopUtils.getShop((Location) get(id, ShopInfo.LOCATION));
                            if (shop != null)
                                return shop;
                            else {
                                return new Shop(plugin,
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

                                Shop newShop = new Shop(plugin,
                                        (OfflinePlayer) get(id, ShopInfo.VENDOR),
                                        (ItemStack) get(id, ShopInfo.PRODUCT),
                                        (Location) get(id, ShopInfo.LOCATION),
                                        (double) get(id, ShopInfo.BUYPRICE),
                                        (double) get(id, ShopInfo.SELLPRICE),
                                        ShopType.ADMIN);

                                setShop(id, newShop);
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

    public void setShop(int id, Shop shop) {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("REPLACE INTO shop_list (id,vendor,product,world,x,y,z,buyprice,sellprice,shoptype) VALUES(?,?,?,?,?,?,?,?,?,?)");

            ps.setInt(1, id);
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

    public void addShop(Shop shop) {
        int id = getNextFreeID();
        setShop(id, shop);
    }

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
        SHOPTYPE;
    }
}