package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQL extends Database {

    public MySQL(ShopChest plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false&serverTimezone=UTC",
                Config.databaseMySqlHost, Config.databaseMySqlPort, Config.databaseMySqlDatabase));
        config.setUsername(Config.databaseMySqlUsername);
        config.setPassword(Config.databaseMySqlPassword);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    /**
     * Sends an asynchronous ping to the database
     */
    public void ping() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        Statement s = con.createStatement()) {
                    plugin.debug("Pinging to MySQL server...");
                    s.execute("/* ping */ SELECT 1");
                } catch (SQLException ex) {
                    plugin.getLogger().severe("Failed to ping to MySQL server. Trying to reconnect...");
                    plugin.debug("Failed to ping to MySQL server. Trying to reconnect...");
                    connect(null);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    String getQueryCreateTableShops() {
        return "CREATE TABLE IF NOT EXISTS " + tableShops + " ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
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
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
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
            + "taxed_price FLOAT NOT NULL,"
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
        return "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=?";
    }
}
