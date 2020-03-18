package de.epiceric.shopchest.shop.hologram;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.shop.ShopImpl;

public class Hologram {
    private final Shop shop;
    private List<HologramLine> lines = new ArrayList<>();

    public Hologram(ShopChest plugin, Shop shop) {
        this.shop = shop;

        int topLine = shop.canPlayerBuy() && shop.canPlayerSell() ? 3 : 2;

        // TODO: Configurable
        lines.add(new HologramLine(getLocation(topLine), shop.getVendor().map(OfflinePlayer::getName).orElse("§cAdmin Shop")));
        lines.add(new HologramLine(getLocation(topLine - 1), shop.getProduct().getAmount() + " §7x §f" + shop.getProduct().getLocalizedName()));
        if (shop.canPlayerBuy()) lines.add(new HologramLine(getLocation(topLine - 2), "§eBuy for " + plugin.formatEconomy(shop.getBuyPrice())));
        if (shop.canPlayerSell()) lines.add(new HologramLine(getLocation(0), "§eSell for " + plugin.formatEconomy(shop.getSellPrice())));
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

    public void destroy() {
        lines.forEach(HologramLine::destroy);
    }

    public void showPlayer(Player player) {
        lines.forEach(line -> line.showPlayer(player));
    }

    public void hidePlayer(Player player) {
        lines.forEach(line -> line.hidePlayer(player));
    }
}