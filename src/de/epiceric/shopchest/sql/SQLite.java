package de.epiceric.shopchest.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import de.epiceric.shopchest.ShopChest;

public class SQLite extends Database {
	
    String dbname;
    
    public SQLite(ShopChest instance){
        super(instance);
        dbname = "shops";
    }

    public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS shop_list (" +
    		"`id` int(11) NOT NULL," +
            "`vendor` varchar(32) NOT NULL," +
            "`product` varchar(32) NOT NULL," +
            "`world` varchar(32) NOT NULL," +
            "`x` int(11) NOT NULL," +
            "`y` int(11) NOT NULL," +
            "`z` int(11) NOT NULL," +
            "`buyprice` float(32) NOT NULL," +
            "`sellprice` float(32) NOT NULL," +
            "`infinite` boolean NOT NULL," +
            "PRIMARY KEY (`id`)" +
            ");";

 
    // SQL creation stuff, You can leave the below stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }
 
    public void load() {
        connection = getSQLConnection();    
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
