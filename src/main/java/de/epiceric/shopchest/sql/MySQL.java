package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            plugin.debug("Connecting to MySQL Server \"" + connectUrl + "\" as user \"" + plugin.getShopChestConfig().database_mysql_username + "\"");

            connection = DriverManager.getConnection(connectUrl, plugin.getShopChestConfig().database_mysql_username, plugin.getShopChestConfig().database_mysql_password);

            return connection;
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to get database connection");
            plugin.debug("Failed to get database connection");
            plugin.debug(ex);
        }

        return null;
    }

    public void ping() {
        try (PreparedStatement ps = connection.prepareStatement("/* ping */ SELECT 1")) {
            plugin.debug("Pinging to MySQL server...");
            ps.executeQuery();
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to ping to MySQL server. Trying to reconnect...");
            plugin.debug("Failed to ping to MySQL server. Trying to reconnect...");
            connect();
        }
    }
}
