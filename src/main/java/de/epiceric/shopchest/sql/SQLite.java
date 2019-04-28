package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLite extends Database {

    public SQLite(ShopChest plugin) {
        super(plugin);
    }

    @Override
    HikariDataSource getDataSource() {
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
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:sqlite:" + dbFile));
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    /**
     * Vacuums the database to reduce file size
     * 
     * @param async Whether the call should be executed asynchronously
     */
    public void vacuum(boolean async) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                        Statement s = con.createStatement()) {
                    s.executeUpdate("VACUUM");

                    plugin.debug("Vacuumed SQLite database");
                } catch (final SQLException ex) {
                    plugin.getLogger().severe("Failed to vacuum database");
                    plugin.debug("Failed to vacuum database");
                    plugin.debug(ex);
                }
            }
        };

        if (async) {
            runnable.runTaskAsynchronously(plugin);
        } else {
            runnable.run();
        }
    }
}
