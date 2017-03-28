package de.epiceric.shopchest.external;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.Flags;
import de.epiceric.shopchest.ShopChest;

import java.util.Locale;

public class PlotSquaredShopFlag {

    private static boolean registered = false;

    public enum Group {
        OWNERS, MEMBERS, TRUSTED, EVERYONE, NONE
    }

    public static Flag CREATE_SHOP;
    public static Flag USE_SHOP;
    public static Flag USE_ADMIN_SHOP;

    private GroupFlag createShop = new GroupFlag("create-shop");
    private GroupFlag useShop = new GroupFlag("use-shop");
    private GroupFlag useAdminShop = new GroupFlag("use-admin-shop");

    public void register(ShopChest plugin) {
        if (registered) return;

        CREATE_SHOP = createShop;
        USE_SHOP = useShop;
        USE_ADMIN_SHOP = useAdminShop;

        Flags.registerFlag(createShop);
        Flags.registerFlag(useShop);
        Flags.registerFlag(useAdminShop);
        registered = true;

        plugin.debug("Registered custom PlotSquared flags");
    }

    public class GroupFlag extends Flag<Group> {

        public GroupFlag(String name) {
            super(name);
        }

        @Override
        public String valueToString(Object value) {
            return String.valueOf(value);
        }

        @Override
        public Group parseValue(String s) {
            String val = s.toLowerCase(Locale.ENGLISH);

            switch (val) {
                case "owners":
                case "owner":
                    return Group.OWNERS;
                case "members":
                case "member":
                case "helpers":
                case "helper":
                    return Group.MEMBERS;
                case "trusted":
                    return Group.TRUSTED;
                case "everyone":
                case "all":
                    return Group.EVERYONE;
                case "deny":
                case "false":
                case "no":
                case "0":
                case "none":
                case "noone":
                    return Group.NONE;
            }

            return null;
        }

        @Override
        public String getValueDescription() {
            return "Flag value must be a group: 'owner' , 'members', 'trusted', 'everyone' or 'none'";
        }
    }

}
