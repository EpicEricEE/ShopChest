package de.epiceric.shopchest.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.epiceric.shopchest.ShopChest;

public class SQLite extends Database {

    public SQLite(ShopChest plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
        try {
            // Initialize driver class so HikariCP can find it
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to initialize SQLite driver");
            plugin.debug("Failed to initialize SQLite driver");
            plugin.debug(e);
            return null;
        }

        File folder = plugin.getDataFolder();
        File dbFile = new File(folder, "shops.db");

        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().severe("Failed to create database file");
                plugin.debug("Failed to create database file");
                plugin.debug(ex);
                return null;
            }
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:sqlite:" + dbFile));
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    /**
     * Vacuums the database synchronously to reduce file size
     */
    public void vacuum() {
        try (Connection con = dataSource.getConnection();
                Statement s = con.createStatement()) {
            s.executeUpdate("VACUUM");

            plugin.debug("Vacuumed SQLite database");
        } catch (final SQLException ex) {
            plugin.getLogger().warning("Failed to vacuum database");
            plugin.debug("Failed to vacuum database");
            plugin.debug(ex);
        }
    }

    @Override
    String getQueryCreateTableShops() {
        return "CREATE TABLE IF NOT EXISTS " + tableShops + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
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
    }

    @Override
    String getQueryCreateTableLog() {
        return "CREATE TABLE IF NOT EXISTS " + tableLogs + " ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
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
    }

    @Override
    String getQueryCreateTableLogout() {
        return "CREATE TABLE IF NOT EXISTS " + tableLogouts + " ("
            + "player VARCHAR(36) PRIMARY KEY NOT NULL,"
            + "time LONG NOT NULL)";
    }

    @Override
    String getQueryCreateTableFields() {
        return "CREATE TABLE IF NOT EXISTS " + tableFields + " ("
            + "field VARCHAR(32) PRIMARY KEY NOT NULL,"
            + "value INTEGER NOT NULL)";
    }

    @Override
    String getQueryGetTable() {
        return "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
    }
}
