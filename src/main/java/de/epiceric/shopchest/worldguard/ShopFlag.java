package de.epiceric.shopchest.worldguard;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ShopFlag {

    private static Flag<?>[] customFlagList;

    public static final StateFlag CREATE_SHOP;
    public static final StateFlag USE_SHOP;
    public static final StateFlag USE_ADMIN_SHOP;

    static {
        CREATE_SHOP = new StateFlag("create-shop", false);
        USE_SHOP = new StateFlag("use-shop", false);
        USE_ADMIN_SHOP = new StateFlag("use-admin-shop", false);

        customFlagList = new Flag[] {CREATE_SHOP, USE_SHOP, USE_ADMIN_SHOP};
    }

    public static void init() {
        // Add custom flags to WorldGuard's flag list
        try {
            Field flagListField = DefaultFlag.class.getField("flagsList");

            Flag<?>[] flags = new Flag[DefaultFlag.flagsList.length + customFlagList.length];
            System.arraycopy(DefaultFlag.flagsList, 0, flags, 0, DefaultFlag.flagsList.length);
            System.arraycopy(customFlagList, 0, flags, DefaultFlag.flagsList.length, customFlagList.length);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(flagListField, flagListField.getModifiers() & ~Modifier.FINAL);

            flagListField.set(null, flags);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
