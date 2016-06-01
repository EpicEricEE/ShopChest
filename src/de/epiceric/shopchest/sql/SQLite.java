package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

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
}
