package de.epiceric.shopchest.external.listeners;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.utils.Utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.*;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class SuperiorSkyblockListener implements Listener {
    private final ShopChest plugin;

    public SuperiorSkyblockListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enableSuperiorSkyblockIntegration)
            return;

        Set<Location> chestLocations = Utils.getChestLocations(e.getShop());
        for (Location loc : chestLocations) {
            if (handleForLocation(e.getPlayer(), loc, e))
                return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExtendShop(ShopExtendEvent e) {
        if (!Config.enableSuperiorSkyblockIntegration)
            return;

        handleForLocation(e.getPlayer(), e.getNewChestLocation(), e);
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        Island island = SuperiorSkyblockAPI.getIslandAt(loc);
        if (island == null) 
            return false;
        
        SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(player);
        if(sp == null)
        	return false;
        
        if (Config.SuperiorSkyblockEnableIslandPrivilege) {
        	
	        IslandPrivilege ip;
	        
	        try {
	        	ip = IslandPrivilege.getByName(Config.SuperiorSkyblockIslandPrivilegeName);
	        }catch(Exception ex) {
	        	e.setCancelled(true);
	        	plugin.debug("Cancel Reason: SuperiorSkyblock Couldn't find an IslandPrivilege with the name "+Config.SuperiorSkyblockIslandPrivilegeName);
	        	ex.printStackTrace();
	        	return true;
	        }
	        
	        Bukkit.getConsoleSender().sendMessage("Perm: "+ip+" status: "+sp.hasPermission(ip));
	        
	        if(!sp.hasPermission(ip)) {
	        	e.setCancelled(true);
	        	plugin.debug("Cancel Reason: SuperiorSkyblock no permission "+Config.SuperiorSkyblockIslandPrivilegeName);
	        	return true;
	    	}
        }
        
        if (!island.isMember(sp) && !island.getOwner().getName().equalsIgnoreCase(sp.getName().toLowerCase())) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: SuperiorSkyblock");
            return true;
        }

        return false;
    }
    
}