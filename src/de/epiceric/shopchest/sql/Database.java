package de.epiceric.shopchest.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUtils;

public abstract class Database {
    ShopChest plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    public String table = "shop_list";
    
    public String world = "";
    public String vendor = "";
    public ItemStack product = null;
    public Location location = null;
    public double buyPrice = 0;
    public double sellPrice = 0;
    public boolean infinite = false;
    
    public Database(ShopChest instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE id = ?");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
         
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }
    
    public int getNextFreeID() {    	    	
    	for (int i = 1; i < getHighestID() + 1; i++) {
    		if (getProduct(i) == null) {
    			return i;
    		} else {
    			if (i == getHighestID()) {
    				return i + 1;
    			}
    		}
    	}
    	
    	return 1;
    }
    
    public int getHighestID() {
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        int highestID = 1;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") > highestID) { 
                    highestID = rs.getInt("id");
                }
            }
            return highestID;
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;    
    }

    public int getShopID(Shop shop) {
    	
    	
    	for (int i = 1; i < getHighestID() + 1; i++) {
    		
    		try {
        		Shop s = getShop(i);
        		if (s.getLocation().equals(shop.getLocation())) {
        			return i;
        		}
    		} catch (NullPointerException ex) {
    			continue;
    		}  		    		
    		
    	}
    	
    	return 0;
    }
    
    public void removeShop(Shop shop) {
    	   	
    	int id = getShopID(shop);
    	if (id == 0) return;
    	
    	shop.getItem().remove();
    	    	
    	Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " where id = " + id + ";");     
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }    	
    	
    }
    
    public void removeShop(int id) {
    	
    	if (id == 0) return;
    	removeShop(getShop(id));
    	
    }
    
    private World getWorld(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id) { 
                    return Bukkit.getWorld(rs.getString("world"));
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;    
    }

	public OfflinePlayer getVendor(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("vendor")));
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;    
    }
    
    public ItemStack getProduct(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return Utils.decode(rs.getString("product"));
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;    
    }
    
    private int getX(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getInt("x");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;    
    }
    
    private int getY(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getInt("y");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;    
    }
    
    private int getZ(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getInt("z");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;    
    }
    
    public Location getLocation(int id) {
    	return new Location(getWorld(id), getX(id), getY(id), getZ(id));
    }
    
    public double getBuyPrice(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getDouble("buyprice");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;    
    }
    
    public double getSellPrice(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getDouble("sellprice");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;    
    }
    
    public boolean isInfinite(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE id = " + id + ";");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getBoolean("infinite");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        
        return false;    
    }
    
    public Shop getShop(int id) {
    	OfflinePlayer vendor = getVendor(id);
    	Location location = getLocation(id);
    	ItemStack product = getProduct(id);
    	double buyPrice = getBuyPrice(id);
    	double sellPrice = getSellPrice(id);
    	boolean infinite = isInfinite(id);
    	
    	if (ShopUtils.isShop(location)) return ShopUtils.getShop(location);
    	else return new Shop(plugin, vendor, product, location, buyPrice, sellPrice, infinite);
    }


    public void setShop(int id, Shop shop) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + table + " (id,vendor,product,world,x,y,z,buyprice,sellprice,infinite) VALUES(?,?,?,?,?,?,?,?,?,?)");
            
            ps.setInt(1, id);                             
            ps.setString(2, shop.getVendor().getUniqueId().toString());
            ps.setString(3, Utils.encode(shop.getProduct()));
            ps.setString(4, shop.getLocation().getWorld().getName());
            ps.setInt(5, shop.getLocation().getBlockX());
            ps.setInt(6, shop.getLocation().getBlockY());
            ps.setInt(7, shop.getLocation().getBlockZ());
            ps.setDouble(8, shop.getBuyPrice());
            ps.setDouble(9, shop.getSellPrice());
            ps.setBoolean(10, shop.isInfinite());
            
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;            
    }
    
    public void addShop(Shop shop) {
    	int id = getNextFreeID();
    	setShop(id, shop);
    }
 

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}