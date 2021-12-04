package de.epiceric.shopchest.external;

import java.util.Optional;

import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;

public class WorldGuardShopFlag {

    public static void register(final ShopChest plugin) {
        try {
	    	WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
	        Optional<IWrappedFlag<WrappedState>> createFlag = null;
	        Optional<IWrappedFlag<WrappedState>> useFlag = null;
	        Optional<IWrappedFlag<WrappedState>> useAdminFlag = null;
	        
	        if(! wrapper.getFlag("create-shop", WrappedState.class).isPresent() ) {
	            createFlag = wrapper.registerFlag("create-shop",
	                    WrappedState.class, Config.wgAllowCreateShopDefault ? WrappedState.ALLOW : WrappedState.DENY);
	        }
	        else {
	        	createFlag = wrapper.getFlag("create-shop", WrappedState.class);
	        }
	        
	        if(! wrapper.getFlag("use-shop", WrappedState.class).isPresent() ) {
	            useFlag = wrapper.registerFlag("use-shop",
	                    WrappedState.class, Config.wgAllowUseShopDefault ? WrappedState.ALLOW : WrappedState.DENY);
	        }
	        else {
	        	useFlag = wrapper.getFlag("use-shop", WrappedState.class);
	        }
	        
	        if(! wrapper.getFlag("use-admin-shop", WrappedState.class).isPresent() ) {
	        	useAdminFlag = wrapper.registerFlag("use-admin-shop",
	                    WrappedState.class, Config.wgAllowUseAdminShopDefault ? WrappedState.ALLOW : WrappedState.DENY);
	        }
	        else {
	        	useAdminFlag = wrapper.getFlag("use-admin-shop", WrappedState.class);
	        }
	                
	                
	
	        plugin.debug("Flag create-shop: " + String.valueOf(createFlag.isPresent()));
	        plugin.debug("Flag use-shop: " + String.valueOf(useFlag.isPresent()));
	        plugin.debug("Flag use-admin-shop: " + String.valueOf(useAdminFlag.isPresent()));
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

}
