package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.sql.Database;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Collection;
import java.util.HashMap;

public class ShopUtils {

    private HashMap<Location, Shop> shopLocation = new HashMap<>();
    private ShopChest plugin;

    public ShopUtils(ShopChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the shop at a given location
     *
     * @param location Location of the shop
     * @return Shop at the given location or <b>null</b> if no shop is found there
     */
    public Shop getShop(Location location) {
        Location newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());

        return shopLocation.get(newLocation);
    }

    /**
     * Checks whether there is a shop at a given location
     * @param location Location to check
     * @return Whether there is a shop at the given location
     */
    public boolean isShop(Location location) {
        Location newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
        return shopLocation.containsKey(newLocation);
    }

    /**
     * Get all Shops
     * @return Array of all Shops
     */
    public Shop[] getShops() {
        Collection<Shop> shops = shopLocation.values();
        return shops.toArray(new Shop[shops.size()]);
    }

    /**
     * Add a shop
     * @param shop Shop to add
     * @param addToDatabase Whether the shop should also be added to the database
     */
    public void addShop(Shop shop, boolean addToDatabase) {
        InventoryHolder ih = shop.getInventoryHolder();
        plugin.debug("Adding shop... (#" + shop.getID() + ")");

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            plugin.debug("Added shop as double chest. (#" + shop.getID() + ")");

            shopLocation.put(r.getLocation(), shop);
            shopLocation.put(l.getLocation(), shop);
        } else {
            plugin.debug("Added shop as single chest. (#" + shop.getID() + ")");

            shopLocation.put(shop.getLocation(), shop);
        }

        if (addToDatabase)
            plugin.getShopDatabase().addShop(shop);

    }

    /**
     * Remove a shop
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     */
    public void removeShop(Shop shop, boolean removeFromDatabase) {
        plugin.debug("Removing shop (#" + shop.getID() + ")");

        InventoryHolder ih = shop.getInventoryHolder();

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            shopLocation.remove(r.getLocation());
            shopLocation.remove(l.getLocation());
        } else {
            shopLocation.remove(shop.getLocation());
        }

        shop.removeItem();
        shop.removeHologram();

        if (removeFromDatabase)
            plugin.getShopDatabase().removeShop(shop);
    }

    /**
     * Get the shop limits of a player
     * @param p Player, whose shop limits should be returned
     * @return The shop limits of the given player
     */
    public int getShopLimit(Player p) {
        int limit = 0;
        boolean useDefault = true;

        for (PermissionAttachmentInfo permInfo : p.getEffectivePermissions()) {
            if (permInfo.getPermission().startsWith("shopchest.limit.") && p.hasPermission(permInfo.getPermission())) {
                if (permInfo.getPermission().equalsIgnoreCase(Permissions.NO_LIMIT)) {
                    limit = -1;
                    useDefault = false;
                    break;
                } else {
                    String[] spl = permInfo.getPermission().split("shopchest.limit.");

                    if (spl.length > 1) {
                        try {
                            int newLimit = Integer.valueOf(spl[1]);

                            if (newLimit < 0) {
                                limit = -1;
                                break;
                            }

                            limit = Math.max(limit, newLimit);
                            useDefault = false;
                        } catch (NumberFormatException ignored) {
                            /* Ignore and continue */
                        }
                    }
                }
            }
        }

        if (limit < -1) limit = -1;
        return (useDefault ? plugin.getShopChestConfig().default_limit : limit);
    }

    /**
     * Get the amount of shops of a player
     * @param p Player, whose shops should be counted
     * @return The amount of a shops a player has (if {@link Config#exclude_admin_shops} is true, admin shops won't be counted)
     */
    public int getShopAmount(OfflinePlayer p) {
        float shopCount = 0;

        for (Shop shop : getShops()) {
            if (shop.getVendor().equals(p)) {
                if (shop.getShopType() != Shop.ShopType.ADMIN || !plugin.getShopChestConfig().exclude_admin_shops) {
                    shopCount++;

                    InventoryHolder ih = shop.getInventoryHolder();

                    if (ih instanceof DoubleChest)
                        shopCount -= 0.5;
                }
            }
        }

        return Math.round(shopCount);
    }

    /**
     * Reload the shops
     * @param reloadConfig Whether the configuration should also be reloaded
     * @param showConsoleMessages Whether messages about the language file should be shown in the console
     * @return Amount of shops, which were reloaded
     */
    public int reloadShops(boolean reloadConfig, boolean showConsoleMessages) {
        plugin.debug("Reloading shops...");

        plugin.getShopDatabase().connect();

        if (reloadConfig) plugin.getShopChestConfig().reload(false, true, showConsoleMessages);

        for (Shop shop : getShops()) {
            removeShop(shop, false);
            plugin.debug("Removed shop (#" + shop.getID() + ")");
        }

        int highestId = plugin.getShopDatabase().getHighestID();

        int count = 0;
        for (int id = 1; id <= highestId; id++) {

            try {
                plugin.debug("Trying to add shop. (#" + id + ")");
                Shop shop = plugin.getShopDatabase().getShop(id);
                addShop(shop, false);
            } catch (Exception e) {
                plugin.debug("Error while adding shop (#" + id + "):");
                plugin.debug(e);
                continue;
            }

            count++;
        }

        return count;
    }
}
