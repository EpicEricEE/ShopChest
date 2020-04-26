package de.epiceric.shopchest.external;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.StaticCaption;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import de.epiceric.shopchest.ShopChest;

public class PlotSquaredShopFlag {
    public enum Group {
        OWNERS, MEMBERS, TRUSTED, EVERYONE, NONE
    }

    private static final String[] lowercaseValues = Arrays.asList(Group.values()).stream()
            .map(value -> String.valueOf(value).toLowerCase(Locale.ENGLISH))
            .toArray(String[]::new);

    public static final GroupFlag<?> CREATE_SHOP = new CreateShopFlag(Group.MEMBERS);
    public static final GroupFlag<?> USE_SHOP = new UseShopFlag(Group.EVERYONE);

    private static boolean registered = false;

    public static void register(ShopChest plugin) {
        if (registered) return;

        GlobalFlagContainer.getInstance().addFlag(CREATE_SHOP);
        GlobalFlagContainer.getInstance().addFlag(USE_SHOP);
        registered = true;

        plugin.debug("Registered custom PlotSquared flags");
    }

    /**
     * Check if a flag is allowed for a player on a plot from PlotSquared
     * 
     * @param plot Plot from PlotSquared
     * @param flag Flag to check
     * @param p Player to check
     * @return Whether the flag is allowed for the player
     */
    public static boolean isFlagAllowedOnPlot(Plot plot, GroupFlag<?> flag, Player p) {
        if (plot != null && flag != null) {
            Group group = plot.getFlag(flag);
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

    public static class CreateShopFlag extends GroupFlag<CreateShopFlag> {
        public CreateShopFlag(Group value) {
            super(value, new StaticCaption("Set to the group that is allowed to create shops."));
        }

        @Override
        protected CreateShopFlag flagOf(@NotNull Group value) {
            return new CreateShopFlag(value);
        }
    }

    public static class UseShopFlag extends GroupFlag<UseShopFlag> {
        public UseShopFlag(Group value) {
            super(value, new StaticCaption("Set to the group that is allowed to use shops."));
        }

        @Override
        protected UseShopFlag flagOf(@NotNull Group value) {
            return new UseShopFlag(value);
        }
    }

    public abstract static class GroupFlag<F extends PlotFlag<Group, F>> extends PlotFlag<Group, F> {
        public GroupFlag(Group value, Caption description) {
            super(value, Captions.FLAG_CATEGORY_ENUM, description);
        }

        @Override
        public String toString() {
            return String.valueOf(getValue()).toLowerCase(Locale.ENGLISH);
        }

        @Override
        public String getExample() {
            return "members";
        }

        @Override
        public F merge(@NotNull Group newValue) {
            return flagOf(newValue);
        }

        @Override
        public F parse(@NotNull String input) throws FlagParseException {
            switch (input.toLowerCase(Locale.ENGLISH)) {
                case "owners":
                case "owner":
                    return this.flagOf(Group.OWNERS);
                case "members":
                case "member":
                case "helpers":
                case "helper":
                    return this.flagOf(Group.MEMBERS);
                case "trusted":
                    return this.flagOf(Group.TRUSTED);
                case "everyone":
                case "all":
                    return this.flagOf(Group.EVERYONE);
                case "deny":
                case "disallow":
                case "false":
                case "no":
                case "0":
                case "none":
                case "noone":
                    return this.flagOf(Group.NONE);
            }

            throw new FlagParseException(this, input, Captions.FLAG_ERROR_ENUM, (Object[]) lowercaseValues);
        }

        @Override
        public Collection<String> getTabCompletions() {
            return Arrays.asList(lowercaseValues);
        }
    }
}
