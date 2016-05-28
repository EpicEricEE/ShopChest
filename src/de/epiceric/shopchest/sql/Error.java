package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;

import java.util.logging.Level;

public class Error {
    public static void execute(ShopChest plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }

    public static void close(ShopChest plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
