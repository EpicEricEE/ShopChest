package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends Database {

    public SQLite(ShopChest plugin) {
        super(plugin);
    }

    @Override
    public Connection getConnection() {
        File folder = plugin.getDataFolder();
        File dbFile = new File(folder, "shops.db");

        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().severe("Failed to create database file");
                plugin.debug("Failed to create database file");
                plugin.debug(ex);
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);

            return connection;
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Failed to get database connection");
            plugin.debug("Failed to get database connection");
            plugin.debug(ex);
        }

        return null;
    }
}
