package de.epiceric.shopchest.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.util.Vector;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopsLoadedEvent;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;

public class ShopUtils {

    private final Map<UUID, Counter> playerShopAmount = new HashMap<>();

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
     * Get a collection of all loaded shops
     * <p>
     * This collection is safe to use for looping over and removing shops.
     *
     * @return Read-only collection of all shops, may contain duplicates for double chests
     */
    public Collection<Shop> getShops() {
        return Collections.unmodifiableCollection(new ArrayList<>(shopLocationValues));
    }

    /**
     * Get all shops
     *
     * @see #getShops()
     * @return Copy of collection of all shops, may contain duplicates
     * @deprecated Use {@link #getShops()} instead
     */
    @Deprecated
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
            if (shop.getShopType() != ShopType.ADMIN) {
                playerShopAmount.compute(shop.getVendor().getUniqueId(), (uuid, amount) -> amount == null ? new Counter(1) : amount.increment());
            }
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

    /**
     * Removes (i.e. unloads) all currently loaded shops
     */
    public void removeShops() {
        shopLocation.forEach((location, shop) -> {
            if (!shop.isCreated()) return;

            plugin.debug("Removing shop " + shop.getID());
            shop.removeItem();
            shop.removeHologram();
        });
        shopLocation.clear();
    }

    /** Remove a shop. May not work properly if double chest doesn't exist!
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     * @see ShopUtils#removeShopById(int, boolean, Callback)
     */
    public void removeShop(Shop shop, boolean removeFromDatabase, Callback<Void> callback) {
        plugin.debug("Removing shop (#" + shop.getID() + ")");

        if (shop.isCreated()) {
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
        }

        if (removeFromDatabase) {
            if (shop.getShopType() != ShopType.ADMIN) {
                playerShopAmount.compute(shop.getVendor().getUniqueId(), (uuid, amount) -> amount == null ? new Counter() : amount.decrement());
            }
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

        Shop first = toRemove.values().iterator().next();
        boolean isAdmin = first.getShopType() == ShopType.ADMIN;
        UUID vendorUuid = first.getVendor().getUniqueId();

        // Database#removeShop removes shop by ID so this only needs to be called once
        if (removeFromDatabase) {
            if (!isAdmin) {
                playerShopAmount.compute(vendorUuid, (uuid, amount) -> amount == null ? new Counter() : amount.decrement());
            }
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
        return playerShopAmount.getOrDefault(p.getUniqueId(), new Counter()).get();
    }

    /**
     * Get all shops of a player from the database without loading them
     * @param p Player, whose shops should be get
     * @param callback Callback that returns a collection of the given player's shops
     */
    public void getShops(OfflinePlayer p, Callback<Collection<Shop>> callback) {
        plugin.getShopDatabase().getShops(p.getUniqueId(), new Callback<Collection<Shop>>(plugin) {
            @Override
            public void onResult(Collection<Shop> result) {
                Set<Shop> shops = new HashSet<>();
                for (Shop playerShop : result) {
                    Shop loadedShop = getShop(playerShop.getLocation());
                    if (loadedShop != null && loadedShop.equals(playerShop)) {
                        shops.add(loadedShop);
                    } else {
                        shops.add(playerShop);
                    }
                }
                if (callback != null) callback.onResult(shops);
            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) callback.onError(throwable);
            }
        });
    }

    /**
     * Loads the amount of shops for each player
     * @param callback Callback that returns the amount of shops for each player
     */
    public void loadShopAmounts(final Callback<Map<UUID, Integer>> callback) {
        plugin.getShopDatabase().getShopAmounts(new Callback<Map<UUID,Integer>>(plugin) {
            @Override
            public void onResult(Map<UUID, Integer> result) {
                playerShopAmount.clear();
                result.forEach((uuid, amount) -> playerShopAmount.put(uuid, new Counter(amount)));
                if (callback != null) callback.onResult(result);
            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) callback.onError(throwable);
            }
        });
    }

    /**
     * Gets all shops in the given chunk from the database and adds them to the server
     * @param chunk The chunk to load shops from
     * @param callback Callback that returns the amount of shops added if succeeded
     * @see ShopUtils#loadShops(Chunk[], Callback)
     */
    public void loadShops(final Chunk chunk, final Callback<Integer> callback) {
        loadShops(new Chunk[] {chunk}, callback);
    }

    /**
     * Gets all shops in the given chunks from the database and adds them to the server
     * @param chunk The chunks to load shops from
     * @param callback Callback that returns the amount of shops added if succeeded
     * @see ShopUtils#loadShops(Chunk Callback)
     */
    public void loadShops(final Chunk[] chunks, final Callback<Integer> callback) {
        plugin.getShopDatabase().getShopsInChunks(chunks, new Callback<Collection<Shop>>(plugin) {
            @Override
            public void onResult(Collection<Shop> result) {
                Collection<Shop> loadedShops = new HashSet<>();

                for (Shop shop : result) {
                    Location loc = shop.getLocation();

                    // Don't add shop if shop is already loaded
                    if (shopLocation.containsKey(loc)) {
                        continue;
                    }

                    int x = loc.getBlockX() / 16;
                    int z = loc.getBlockZ() / 16;
                    
                    // Don't add shop if chunk is no longer loaded
                    if (!loc.getWorld().isChunkLoaded(x, z)) {
                        continue;
                    }

                    if (shop.create(true)) {
                        addShop(shop, false);
                        loadedShops.add(shop);
                    }
                }

                if (callback != null) callback.onResult(loadedShops.size());

                Bukkit.getPluginManager().callEvent(new ShopsLoadedEvent(Collections.unmodifiableCollection(loadedShops)));
            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) callback.onError(throwable);
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
