package de.epiceric.shopchest.external;

import org.codemc.worldguardwrapper.WorldGuardWrapper;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;

public class WorldGuardShopFlag {

    public static void register(final ShopChest plugin) {
        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        plugin.debug("Flag create-shop: " + wrapper.registerStateFlag("create-shop", Config.wgAllowCreateShopDefault));
        plugin.debug("Flag use-shop: " + wrapper.registerStateFlag("use-shop", Config.wgAllowUseShopDefault));
        plugin.debug("Flag use-admin-shop: " + wrapper.registerStateFlag("use-admin-shop", Config.wgAllowUseAdminShopDefault));
    }

}
