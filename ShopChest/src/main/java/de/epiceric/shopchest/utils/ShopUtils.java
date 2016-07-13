package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.sql.Database;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
        ArrayList<Shop> shops = new ArrayList<>();

        for (Shop shop : shopLocation.values()) {
            shops.add(shop);
        }

        return shops.toArray(new Shop[shops.size()]);
    }

    /**
     * Add a shop
     * @param shop Shop to add
     * @param addToDatabase Whether the shop should also be added to the database
     */
    public void addShop(Shop shop, boolean addToDatabase) {
        InventoryHolder ih = shop.getInventoryHolder();

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            shopLocation.put(r.getLocation(), shop);
            shopLocation.put(l.getLocation(), shop);
        } else {
            shopLocation.put(shop.getLocation(), shop);
        }

        if (addToDatabase)
            plugin.getShopDatabase().addShop(shop, plugin.getShopChestConfig().database_reconnect_attempts);

    }

    /**
     * Remove a shop
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     */
    public void removeShop(Shop shop, boolean removeFromDatabase) {
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
            plugin.getShopDatabase().removeShop(shop, plugin.getShopChestConfig().database_reconnect_attempts);
    }

    /**
     * Get the shop limits of a player
     * @param p Player, whose shop limits should be returned
     * @return The shop limits of the given player
     */
    public int getShopLimit(Player p) {
        int limit = plugin.getShopChestConfig().default_limit;

        if (plugin.getPermission().hasGroupSupport()) {
            List<String> groups = new ArrayList<String>();

            for (String key : plugin.getShopChestConfig().shopLimits_group) {
                for (int i = 0; i < plugin.getPermission().getGroups().length; i++) {
                    if (plugin.getPermission().getGroups()[i].equals(key)) {
                        if (plugin.getPermission().playerInGroup(p, key)) {
                            groups.add(key);
                        }
                    }
                }
            }

            if (groups.size() != 0) {
                List<Integer> limits = new ArrayList<>();
                for (String group : groups) {
                    int gLimit = ShopChest.getInstance().getConfig().getInt("shop-limits.group." + group);
                    limits.add(gLimit);
                }

                int highestLimit = 0;
                for (int l : limits) {
                    if (l > highestLimit) {
                        highestLimit = l;
                    } else if (l == -1) {
                        highestLimit = -1;
                        break;
                    }
                }

                limit = highestLimit;
            }
        }

        for (String key : plugin.getShopChestConfig().shopLimits_player) {
            int pLimit = ShopChest.getInstance().getConfig().getInt("shop-limits.player." + key);
            if (Utils.isUUID(key)) {
                if (p.getUniqueId().equals(UUID.fromString(key))) {
                    limit = pLimit;
                }
            } else {
                if (p.getName().equals(key)) {
                    limit = pLimit;
                }
            }
        }

        return limit;
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
     * @return Amount of shops, which were reloaded
     */
    public int reloadShops(boolean reloadConfig) {
        if (reloadConfig) plugin.getShopChestConfig().reload(false, true);

        for (Shop shop : getShops()) {
            removeShop(shop, false);
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (item.hasMetadata("shopItem")) {
                        item.remove();
                    }
                }
            }
        }

        int count = 0;
        for (int id = 1; id < plugin.getShopDatabase().getHighestID(plugin.getShopChestConfig().database_reconnect_attempts) + 1; id++) {

            try {
                Shop shop = (Shop) plugin.getShopDatabase().get(id, Database.ShopInfo.SHOP, plugin.getShopChestConfig().database_reconnect_attempts);
                addShop(shop, false);
            } catch (Exception e) {
                continue;
            }

            count++;
        }

        return count;
    }
}
