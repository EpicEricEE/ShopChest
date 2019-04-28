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
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false",
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
}
