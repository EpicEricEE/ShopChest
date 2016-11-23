package de.epiceric.shopchest.worldguard;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.epiceric.shopchest.ShopChest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ShopFlag {

    private static Flag<?>[] customFlagList;
    private static boolean loaded = false;

    public static final StateFlag CREATE_SHOP;
    public static final StateFlag USE_SHOP;
    public static final StateFlag USE_ADMIN_SHOP;

    static {
        CREATE_SHOP = new StateFlag("create-shop", false);
        USE_SHOP = new StateFlag("use-shop", false);
        USE_ADMIN_SHOP = new StateFlag("use-admin-shop", false);

        customFlagList = new Flag[] {CREATE_SHOP, USE_SHOP, USE_ADMIN_SHOP};
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void init(final ShopChest plugin, boolean onLoad) {
        String worldGuardVersion = plugin.getWorldGuard().getDescription().getVersion();

        int majorVersion = 0;
        int minorVersion = 0;
        int fixVersion = 0;

        try {
            String[] spl = worldGuardVersion.split("\\.");
            if (spl.length > 0) {
                majorVersion = Integer.parseInt(spl[0]);

                if (spl.length > 1) {
                    minorVersion = Integer.parseInt(spl[1]);

                    if (spl.length > 2) {
                        int length = 0;
                        for (int i = 0; i < spl[2].toCharArray().length; i++) {
                            char c = spl[2].toCharArray()[i];
                            if (c >= '0' && c <= '9') {
                                length++;
                            } else break;
                        }

                        fixVersion = Integer.parseInt(spl[2].substring(0, length));
                    }
                }
            } else {
                plugin.getLogger().severe("Failed to initialize custom WorldGuard flags.");
                plugin.debug("Failed to initialize WorldGuard flags: Unknown/Invalid version: " + worldGuardVersion);
                return;
            }
        } catch (NumberFormatException e) {
            plugin.debug("Failed to initialize WorldGuard flags");
            plugin.debug(e);
            plugin.getLogger().severe("Failed to initialize custom WorldGuard flags.");
            return;
        }

        if (((majorVersion == 6 && minorVersion == 1 && fixVersion >= 3) || (majorVersion == 6 && minorVersion > 1) || majorVersion > 6)) {
            if (onLoad) {
                plugin.getWorldGuard().getFlagRegistry().registerAll(Lists.newArrayList(customFlagList));
                loaded = true;
            }
        } else {
            try {
                Field flagListField = DefaultFlag.class.getField("flagsList");

                Flag<?>[] flags = new Flag[DefaultFlag.flagsList.length + customFlagList.length];
                System.arraycopy(DefaultFlag.flagsList, 0, flags, 0, DefaultFlag.flagsList.length);
                System.arraycopy(customFlagList, 0, flags, DefaultFlag.flagsList.length, customFlagList.length);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(flagListField, flagListField.getModifiers() & ~Modifier.FINAL);

                flagListField.set(null, flags);
                loaded = true;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                plugin.debug("Failed to initialize WorldGuard flags");
                plugin.debug(e);
                plugin.getLogger().severe("Failed to initialize custom WorldGuard flags.");
            }
        }
    }

}
