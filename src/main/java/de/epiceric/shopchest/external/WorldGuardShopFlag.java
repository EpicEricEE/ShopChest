package de.epiceric.shopchest.external;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;

public class WorldGuardShopFlag {

    private static Flag<?>[] customFlagList;

    public static final StateFlag CREATE_SHOP;
    public static final StateFlag USE_SHOP;
    public static final StateFlag USE_ADMIN_SHOP;

    static {
        CREATE_SHOP = new StateFlag("create-shop", Config.wgAllowCreateShopDefault);
        USE_SHOP = new StateFlag("use-shop", Config.wgAllowUseShopDefault);
        USE_ADMIN_SHOP = new StateFlag("use-admin-shop", Config.wgAllowUseAdminShopDefault);

        customFlagList = new Flag[] {CREATE_SHOP, USE_SHOP, USE_ADMIN_SHOP};
    }

    public static void register(final ShopChest plugin) {
        plugin.getWorldGuard().getFlagRegistry().registerAll(Lists.newArrayList(customFlagList));
    }

}
