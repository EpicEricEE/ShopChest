package de.epiceric.shopchest.sql;

import java.util.logging.Level;

import de.epiceric.shopchest.ShopChest;

public class Error {
    public static void execute(ShopChest plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);    
    }
    public static void close(ShopChest plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
