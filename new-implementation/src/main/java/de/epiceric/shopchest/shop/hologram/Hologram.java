package de.epiceric.shopchest.shop.hologram;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.shop.ShopImpl;
import de.epiceric.shopchest.util.NmsUtil;

public class Hologram {

    /**
     * Constructs a new hologram line
     * 
     * @param location the line's location
     * @param text the line's text
     * @return the hologram line
     */
    public static IHologramLine newHologramLine(Location location, String text) {
        switch (NmsUtil.getServerVersion()) {
            case "v1_8_R1": return new de.epiceric.shopchest.shop.hologram.v1_8_R1.HologramLine(location, text);
            case "v1_8_R2": return new de.epiceric.shopchest.shop.hologram.v1_8_R2.HologramLine(location, text);
            case "v1_8_R3": return new de.epiceric.shopchest.shop.hologram.v1_8_R3.HologramLine(location, text);
            case "v1_9_R1": return new de.epiceric.shopchest.shop.hologram.v1_9_R1.HologramLine(location, text);
            case "v1_9_R2": return new de.epiceric.shopchest.shop.hologram.v1_9_R2.HologramLine(location, text);
            case "v1_10_R1": return new de.epiceric.shopchest.shop.hologram.v1_10_R1.HologramLine(location, text);
            case "v1_11_R1": return new de.epiceric.shopchest.shop.hologram.v1_11_R1.HologramLine(location, text);
            case "v1_12_R1": return new de.epiceric.shopchest.shop.hologram.v1_12_R1.HologramLine(location, text);
            case "v1_13_R1": return new de.epiceric.shopchest.shop.hologram.v1_13_R1.HologramLine(location, text);
            case "v1_13_R2": return new de.epiceric.shopchest.shop.hologram.v1_13_R2.HologramLine(location, text);
            case "v1_14_R1": return new de.epiceric.shopchest.shop.hologram.v1_14_R1.HologramLine(location, text);
            case "v1_15_R1": return new de.epiceric.shopchest.shop.hologram.v1_15_R1.HologramLine(location, text);
            default: throw new IllegalStateException("Invalid server version: " + NmsUtil.getServerVersion());
        }
    }

    private final Shop shop;
    private List<IHologramLine> lines = new ArrayList<>();

    public Hologram(ShopChest plugin, Shop shop) {
        this.shop = shop;

        int topLine = shop.canPlayerBuy() && shop.canPlayerSell() ? 3 : 2;

        // TODO: Configurable
        lines.add(newHologramLine(getLocation(topLine), shop.getVendor().map(OfflinePlayer::getName).orElse("§cAdmin Shop")));
        lines.add(newHologramLine(getLocation(topLine - 1), shop.getProduct().getAmount() + " §7x §f" + shop.getProduct().getLocalizedName()));
        if (shop.canPlayerBuy()) lines.add(newHologramLine(getLocation(topLine - 2), "§eBuy for " + plugin.formatEconomy(shop.getBuyPrice())));
        if (shop.canPlayerSell()) lines.add(newHologramLine(getLocation(0), "§eSell for " + plugin.formatEconomy(shop.getSellPrice())));
    }

    private Location getLocation(int lineFromBottom) {
        double lineHeight = 0.25;
        Location loc = shop.getLocation().subtract(0, 0.75, 0);
        Location otherLoc = ((ShopImpl) shop).getOtherLocation().orElse(null);

        if (otherLoc == null) {
            return loc.add(0.5, lineFromBottom * lineHeight, 0.5);
        }
        
        if (loc.getX() == otherLoc.getX()) {
            double zDiff = otherLoc.subtract(loc).getZ();
            return loc.add(0.5, lineFromBottom * lineHeight, zDiff);
        } else {
            double xDiff = otherLoc.subtract(loc).getX();
            return loc.add(xDiff, lineFromBottom * lineHeight, 0.5);
        }
    }

    /**
     * Hides the hologram from all players in the hologram's world and removes all lines
     */
    public void destroy() {
        lines.forEach(IHologramLine::destroy);
        lines.clear();
    }

    /**
     * Displays the hologram to the given player
     * 
     * @param player the player to show the hologram to
     */
    public void showPlayer(Player player) {
        lines.forEach(line -> line.showPlayer(player));
    }

    /**
     * Hides the hologram from the given player
     * 
     * @param player the player to hide the hologram from
     */
    public void hidePlayer(Player player) {
        lines.forEach(line -> line.hidePlayer(player));
    }
}