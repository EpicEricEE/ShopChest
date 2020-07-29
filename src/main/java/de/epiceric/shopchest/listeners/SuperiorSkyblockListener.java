package de.epiceric.shopchest.listeners;

import java.util.Collection;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import com.bgsoftware.superiorskyblock.api.events.*;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class SuperiorSkyblockListener implements Listener {
    private ShopChest plugin;

    public SuperiorSkyblockListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandDeleted(IslandDisbandEvent e) {
        deleteShops(e.getIsland().getIslandMembers(true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandQuit(IslandQuitEvent e) {
        deleteShops(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeamKick(IslandKickEvent e) {
        deleteShops(e.getTarget());
    }

    private void deleteShops(List<SuperiorPlayer> members) {
    	deleteShops(members, null);
    }

    private void deleteShops(SuperiorPlayer vendorUuid) {
    	deleteShops(null, vendorUuid);
    }
    
	
    private void deleteShops(List<SuperiorPlayer> members, SuperiorPlayer vendorUuid) {
        if (!Config.enableSuperiorSkyblockIntegration)
            return;
            
        Collection<Shop> shops = plugin.getShopUtils().getShops();
        for (Shop shop : shops) {
        	
            if(members != null) {
	            for(SuperiorPlayer sp : members) {
	            	if(shop.getVendor().getName().equalsIgnoreCase(sp.getName().toLowerCase())) {
	                	plugin.getShopUtils().removeShop(shop, true);
	            	}
	            }
            }
            
            if(vendorUuid != null) {
            	if(shop.getVendor().getName().equalsIgnoreCase(vendorUuid.getName().toLowerCase())) {
                	plugin.getShopUtils().removeShop(shop, true);
            	}
            }
            
        }
    }
    
}