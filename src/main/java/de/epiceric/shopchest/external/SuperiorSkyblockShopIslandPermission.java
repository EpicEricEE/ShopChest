package de.epiceric.shopchest.external;

import de.epiceric.shopchest.*;
import de.epiceric.shopchest.config.Config;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;

public class SuperiorSkyblockShopIslandPermission {
	private static String name = Config.SuperiorSkyblockIslandPrivilegeName;
	
    public static void register(ShopChest plugin) {
    	if(!Config.SuperiorSkyblockEnableIslandPrivilege) return;
		try {
    		IslandPrivilege.register(name);
    		IslandFlag.register(name);
    		
    		SuperiorSkyblockAPI.getSuperiorSkyblock().getMenus().updatePermission(IslandPrivilege.getByName(name));

    		plugin.debug("Registered SuperiorSkyblock shop Island Privilege");
		}catch(Exception e) {
            plugin.getLogger().warning("Failed to register SuperiorSkyblock shop Island Privilege");
            plugin.debug("Failed to register SuperiorSkyblock shop Island Privilege");
            e.printStackTrace();
		}
    }
}