package de.epiceric.shopchest.shop.hologram;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.shop.ShopImpl;

public class Hologram {
    private final Shop shop;
    private final List<IHologramLine> lines = new ArrayList<>();
    private final IHologramItem item;

    public Hologram(ShopChest plugin, Shop shop) {
        this.shop = shop;
        this.item = HologramFactory.newHologramItem(getItemLocation(), shop.getProduct().getItemStack());

        int topLine = shop.canPlayerBuy() && shop.canPlayerSell() ? 3 : 2;

        // TODO: Configurable

        lines.add(HologramFactory.newHologramLine(getLineLocation(topLine),
            shop.getVendor().map(OfflinePlayer::getName).orElse("§cAdmin Shop")));

        lines.add(HologramFactory.newHologramLine(getLineLocation(topLine - 1),
                shop.getProduct().getAmount() + " §7x §f" + shop.getProduct().getLocalizedName()));

        if (shop.canPlayerBuy()) lines.add(HologramFactory.newHologramLine(getLineLocation(topLine - 2),
                "§eBuy for " + plugin.formatEconomy(shop.getBuyPrice())));

        if (shop.canPlayerSell()) lines.add(HologramFactory.newHologramLine(getLineLocation(0),
                "§eSell for " + plugin.formatEconomy(shop.getSellPrice())));
    }

    private Location getCenterOfChest() {
        ShopImpl shop = (ShopImpl) this.shop;
        Location location = shop.getLocation();
        Optional<Location> optional = shop.getOtherLocation();

        if (optional.isPresent()) {
            Location otherLoc = optional.get();
            Vector diff = otherLoc.subtract(location).toVector();

            // Invert cross vector direction if diff has negative x or z
            Vector cross = (diff.getX() < 0.1 || diff.getZ() < 0.1)
                ? diff.getCrossProduct(new Vector(0, -0.5, 0))
                : diff.getCrossProduct(new Vector(0, 0.5, 0));

            location.add(diff.add(cross));
        } else {
            location.add(0.5, 0, 0.5);
        }

        return location;
    }

    private Location getItemLocation() {
        double itemLift = 0.9;
        return getCenterOfChest().add(0, itemLift, 0);
    }

    private Location getLineLocation(int lineFromBottom) {
        double lineHeight = 0.25;
        double armorStandLift = -0.75;
        return getCenterOfChest().add(0, lineFromBottom * lineHeight - armorStandLift, 0);
    }

    /**
     * Hides the hologram from all players in the hologram's world and removes all lines
     */
    public void destroy() {
        item.destroy();
        lines.forEach(IHologramLine::destroy);
        lines.clear();
    }

    /**
     * Displays the hologram text to the given player
     * 
     * @param player the player to show the hologram to
     */
    public void showPlayer(Player player) {
        lines.forEach(line -> line.showPlayer(player));
    }

    /**
     * Hides the hologram text from the given player
     * 
     * @param player the player to hide the hologram from
     */
    public void hidePlayer(Player player) {
        lines.forEach(line -> line.hidePlayer(player));
    }
}