package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQL extends Database {

    public MySQL(ShopChest plugin) {
        super(plugin);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("com.mysql.jdbc.Driver");

            String connectUrl = "jdbc:mysql://" + Config.database_mysql_host + ":" + Config.database_mysql_port + "/" + Config.database_mysql_database;
            plugin.getLogger().info("Connecting to MySQL Server \"" + connectUrl + "\" as user \"" + Config.database_mysql_username + "\"");

            connection = DriverManager.getConnection(connectUrl, Config.database_mysql_username, Config.database_mysql_password);

            return connection;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
