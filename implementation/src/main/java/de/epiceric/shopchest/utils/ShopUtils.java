package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShopUtils {

    // concurrent since it is updated in async task
    private final Map<UUID, Location> playerLocation = new ConcurrentHashMap<>();
    private final Map<Location, Shop> shopLocation = new ConcurrentHashMap<>();
    private final Collection<Shop> shopLocationValues = Collections.unmodifiableCollection(shopLocation.values());
    private final ShopChest plugin;

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
        Location newLocation = new Location(location.getWorld(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());

        return shopLocation.get(newLocation);
    }

    /**
     * Checks whether there is a shop at a given location
     * @param location Location to check
     * @return Whether there is a shop at the given location
     */
    public boolean isShop(Location location) {
        return getShop(location) != null;
    }

    /**
     * Get all shops
     * Do not use for removing while iteration!
     *
     * @see #getShopsCopy()
     * @return Read-only collection of all shops, may contain duplicates
     */
    public Collection<Shop> getShops() {
        return shopLocationValues;
    }

    /**
     * Get all shops
     * Same as {@link #getShops()} but this is safe to remove while iterating
     *
     * @see #getShops()
     * @return Copy of collection of all shops, may contain duplicates
     */
    public Collection<Shop> getShopsCopy() {
        return new ArrayList<>(getShops());
    }

    /**
     * Add a shop
     * @param shop Shop to add
     * @param addToDatabase Whether the shop should also be added to the database
     * @param callback Callback that - if succeeded - returns the ID the shop had or was given (as {@code int})
     */
    public void addShop(Shop shop, boolean addToDatabase, Callback<Integer> callback) {
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

        if (addToDatabase) {
            plugin.getShopDatabase().addShop(shop, callback);
        } else {
            if (callback != null) callback.callSyncResult(shop.getID());
        }

    }

    /**
     * Add a shop
     * @param shop Shop to add
     * @param addToDatabase Whether the shop should also be added to the database
     */
    public void addShop(Shop shop, boolean addToDatabase) {
        addShop(shop, addToDatabase, null);
    }

    /** Remove a shop. May not work properly if double chest doesn't exist!
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     * @see ShopUtils#removeShopById(int, boolean, Callback)
     */
    public void removeShop(Shop shop, boolean removeFromDatabase, Callback<Void> callback) {
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

        if (removeFromDatabase) {
            plugin.getShopDatabase().removeShop(shop, callback);
        } else {
            if (callback != null) callback.callSyncResult(null);
        }
    }

    /**
     * Remove a shop. May not work properly if double chest doesn't exist!
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     * @see ShopUtils#removeShopById(int, boolean)
     */
    public void removeShop(Shop shop, boolean removeFromDatabase) {
        removeShop(shop, removeFromDatabase, null);
    }

    /**
     * Remove a shop by its ID
     * @param shopId ID of the shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     */
    public void removeShopById(int shopId, boolean removeFromDatabase, Callback<Void> callback) {
        Map<Location, Shop> toRemove = shopLocation.entrySet().stream()
                .filter(e -> e.getValue().getID() == shopId)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        plugin.debug(String.format("Removing %d shop(s) with ID %d", toRemove.size(), shopId));

        if (toRemove.isEmpty()) {
            if (callback != null) callback.callSyncResult(null);
            return;
        }

        toRemove.forEach((loc, shop) -> {
            shopLocation.remove(loc);

            shop.removeItem();
            shop.removeHologram();
        });

        // Database#removeShop removes shop by ID so this only needs to be called once
        if (removeFromDatabase) {
            plugin.getShopDatabase().removeShop(toRemove.values().iterator().next(), callback);
        } else {
            if (callback != null) callback.callSyncResult(null);
        }
    }

    /**
     * Remove a shop by its ID
     * @param shopId ID of the shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     */
    public void removeShopById(int shopId, boolean removeFromDatabase) {
        removeShopById(shopId, removeFromDatabase, null);
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
        return (useDefault ?Config.defaultLimit : limit);
    }

    /**
     * Get the amount of shops of a player
     * @param p Player, whose shops should be counted
     * @return The amount of a shops a player has (if {@link Config#excludeAdminShops} is true, admin shops won't be counted)
     */
    public int getShopAmount(OfflinePlayer p) {
        float shopCount = 0;

        for (Shop shop : getShops()) {
            if (shop.getVendor().equals(p)) {
                if (shop.getShopType() != Shop.ShopType.ADMIN) {
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
     * @param callback Callback that - if succeeded - returns the amount of shops that were reloaded (as {@code int})
     */
    public void reloadShops(boolean reloadConfig, final boolean showConsoleMessages, final Callback<Integer> callback) {
        plugin.debug("Reloading shops...");

        if (reloadConfig) {
            plugin.getShopChestConfig().reload(false, true, showConsoleMessages);
            plugin.getHologramFormat().reload();
            plugin.getUpdater().restart();
        }

        plugin.getShopDatabase().connect(new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                for (Shop shop : getShopsCopy()) {
                    removeShop(shop, false);
                    plugin.debug("Removed shop (#" + shop.getID() + ")");
                }

                plugin.getShopDatabase().getShops(showConsoleMessages, new Callback<Collection<Shop>>(plugin) {
                    @Override
                    public void onResult(Collection<Shop> result) {
                        for (Shop shop : result) {
                            if (shop.create(showConsoleMessages)) {
                                addShop(shop, false);
                            }
                        }

                        if (callback != null) callback.callSyncResult(result.size());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (callback != null) callback.callSyncError(throwable);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) callback.callSyncError(throwable);
            }
        });
    }

    /**
     * Update hologram and item of all shops for a player
     * @param player Player to show the updates
     */
    public void updateShops(Player player) {
        updateShops(player, false);
    }

    /**
     * Update hologram and item of all shops for a player
     * @param player Player to show the updates
     * @param force Whether update should be forced even if player has not moved
     */
    public void updateShops(Player player, boolean force) {
        if (!force && player.getLocation().equals(playerLocation.get(player.getUniqueId()))) {
            // Player has not moved, so don't calculate shops again.
            return;
        }

        if (Config.onlyShowShopsInSight) {
            updateVisibleShops(player);
        } else {
            updateNearestShops(player);
        }

        playerLocation.put(player.getUniqueId(), player.getLocation());
    }

    /**
     * Remove a saved location of a player to force a recalculation
     * of whether the hologram should be visible.
     * This should only be called when really needed
     * @param player Player whose saved location will be reset
     */
    public void resetPlayerLocation(Player player) {
        playerLocation.remove(player.getUniqueId());
    }

    private void updateVisibleShops(Player player) {
        double itemDistSquared = Math.pow(Config.maximalItemDistance, 2);
        double maxDist = Config.maximalDistance;

        double nearestDistSquared = Double.MAX_VALUE;
        Shop nearestShop = null;

        Location pLoc = player.getEyeLocation();
        Vector pDir = pLoc.getDirection();

        // Display holograms based on sight
        for (double i = 0; i <= maxDist; i++) {
            Location loc = pLoc.clone();
            Vector dir = pDir.clone();
            double factor = Math.min(i, maxDist);
            
            loc.add(dir.multiply(factor));
            Location locBelow = loc.clone().subtract(0, 1, 0);

            // Check block below as player may look at hologram
            Shop shop = getShop(loc);
            if (shop == null) {
                shop = getShop(locBelow);
            }

            if (shop != null && shop.hasHologram()) {
                double distSquared = pLoc.distanceSquared(loc);
                if (distSquared < nearestDistSquared) {
                    nearestDistSquared = distSquared;
                    nearestShop = shop;
                }
            }
        }

        for (Shop shop : getShops()) {
            if (!shop.equals(nearestShop) && shop.hasHologram()) {
                shop.getHologram().hidePlayer(player);
            }

            // Display item based on distance
            Location shopLocation = shop.getLocation();
            if (shopLocation.getWorld().getName().equals(player.getWorld().getName())) {
                double distSquared = shop.getLocation().distanceSquared(player.getLocation());

                if (shop.hasItem()) {
                    if (distSquared <= itemDistSquared) {
                        shop.getItem().showPlayer(player);
                    } else {
                        shop.getItem().hidePlayer(player);
                    }
                }
            }
        }

        if (nearestShop != null) {
            nearestShop.getHologram().showPlayer(player);
        }
    }

    private void updateNearestShops(Player p) {
        double holoDistSqr = Math.pow(Config.maximalDistance, 2);
        double itemDistSqr = Math.pow(Config.maximalItemDistance, 2);

        Location playerLocation = p.getLocation();

        for (Shop shop : getShops()) {
            if (playerLocation.getWorld().getName().equals(shop.getLocation().getWorld().getName())) {
                double distSqr = shop.getLocation().distanceSquared(playerLocation);

                if (shop.hasHologram()) {
                    if (distSqr <= holoDistSqr) {
                        shop.getHologram().showPlayer(p);
                    } else {
                        shop.getHologram().hidePlayer(p);
                    }
                }

                if (shop.hasItem()) {
                    if (distSqr <= itemDistSqr) {
                        shop.getItem().showPlayer(p);
                    } else {
                        shop.getItem().hidePlayer(p);
                    }
                }
            }
        }
    }
}
