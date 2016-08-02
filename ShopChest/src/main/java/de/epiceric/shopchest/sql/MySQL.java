package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;

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

            String connectUrl = "jdbc:mysql://" + plugin.getShopChestConfig().database_mysql_host + ":" + plugin.getShopChestConfig().database_mysql_port + "/" + plugin.getShopChestConfig().database_mysql_database + "?autoReconnect=true";
            plugin.getLogger().info("Connecting to MySQL Server \"" + connectUrl + "\" as user \"" + plugin.getShopChestConfig().database_mysql_username + "\"");

            connection = DriverManager.getConnection(connectUrl, plugin.getShopChestConfig().database_mysql_username, plugin.getShopChestConfig().database_mysql_password);

            return connection;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
