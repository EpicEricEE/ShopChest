package de.epiceric.shopchest.external;

import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

import org.bukkit.entity.Player;

import de.epiceric.shopchest.ShopChest;

import java.util.Locale;

public class PlotSquaredOldShopFlag {
    public enum Group {
        OWNERS, MEMBERS, TRUSTED, EVERYONE, NONE
    }

    public static final GroupFlag CREATE_SHOP = new GroupFlag("create-shop");
    public static final GroupFlag USE_SHOP = new GroupFlag("use-shop");
    public static final GroupFlag USE_ADMIN_SHOP = new GroupFlag("use-admin-shop");

    private static boolean registered = false;

    public static void register(ShopChest plugin) {
        if (registered) return;

        Flags.registerFlag(CREATE_SHOP);
        Flags.registerFlag(USE_SHOP);
        Flags.registerFlag(USE_ADMIN_SHOP);
        registered = true;

        plugin.debug("Registered custom PlotSquared flags");
    }

    /**
     * Check if a flag is allowed for a player on a plot from PlotSquared
     * @param plot Plot from PlotSquared
     * @param flag Flag to check
     * @param p Player to check
     * @return Whether the flag is allowed for the player
     */
    public static boolean isFlagAllowedOnPlot(Plot plot, GroupFlag flag, Player p) {
        if (plot != null && flag != null) {
            Group group = plot.getFlag(flag, Group.NONE);
            ShopChest.getInstance().debug("Flag " + flag.getName() + " is set to " + group);

            switch (group) {
                case OWNERS:
                    return plot.getOwners().contains(p.getUniqueId());
                case TRUSTED:
                    return plot.getOwners().contains(p.getUniqueId()) || plot.getTrusted().contains(p.getUniqueId());
                case MEMBERS:
                    return plot.getOwners().contains(p.getUniqueId()) || plot.getTrusted().contains(p.getUniqueId()) || plot.getMembers().contains(p.getUniqueId());
                case EVERYONE:
                    return true;
                case NONE:
                    return false;
            }
        }

        ShopChest.getInstance().debug("Flag or plot is null, or value of flag is not a group");

        return true;
    }

    public static class GroupFlag extends Flag<Group> {
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
                case "disallow":
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