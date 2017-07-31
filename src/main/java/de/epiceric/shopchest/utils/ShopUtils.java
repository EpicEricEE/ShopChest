package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Bukkit;
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

public class ShopUtils {

    // concurrent since it is updated in async task
    private final Map<UUID, Location> playerLocation = new ConcurrentHashMap<>();
    private final Map<Location, Shop> shopLocation = new HashMap<>();
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

    /** Remove a shop
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
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
     * Remove a shop
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     */
    public void removeShop(Shop shop, boolean removeFromDatabase) {
        removeShop(shop, removeFromDatabase, null);
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

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            updateShops(player, true);
                        }

                        if (callback != null) callback.callSyncResult(result.size());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (callback != null) callback.callSyncError(throwable);
                        plugin.debug("Error while adding shops");
                        plugin.debug(throwable);
                    }
                });
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

        if (plugin.getShopChestConfig().only_show_shops_in_sight) {
            updateVisibleShops(player);
        } else {
            updateNearestShops(player);
        }

        playerLocation.put(player.getUniqueId(), player.getLocation());
    }

    /**
     * Remove a player from the {@code playerLocation} map.
     * This should only be called when really needed
     */
    public void resetPlayerLocation(Player player) {
        playerLocation.remove(player.getUniqueId());
    }

    private static final double TARGET_THRESHOLD = 1;

    private void updateVisibleShops(Player player) {
        double itemDistSquared = Math.pow(plugin.getShopChestConfig().maximal_item_distance, 2);
        double hologramDistSquared = Math.pow(plugin.getShopChestConfig().maximal_distance, 2);

        boolean firstShopInSight = plugin.getShopChestConfig().only_show_first_shop_in_sight;

        // used if only_show_first_shop_in_sight
        List<Shop> otherShopsInSight = firstShopInSight ? new ArrayList<Shop>() : null;
        double nearestDistance = 0;
        Shop nearestShop = null;

        Location pLoc = player.getEyeLocation();
        double pX = pLoc.getX();
        double pY = pLoc.getY();
        double pZ = pLoc.getZ();
        Vector pDir = pLoc.getDirection();
        double dirLength = pDir.length();

        for (Shop shop : getShops()) {
            Location shopLocation = shop.getLocation();

            if (shopLocation.getWorld().getName().equals(player.getWorld().getName())) {
                double distanceSquared = shop.getLocation().distanceSquared(player.getLocation());

                // Display item based on distance
                if (shop.hasItem()) {
                    if (distanceSquared <= itemDistSquared) {
                        shop.getItem().showPlayer(player);
                    } else {
                        shop.getItem().hidePlayer(player);
                    }
                }

                // Display hologram based on sight
                if (shop.hasHologram()) {
                    if (distanceSquared < hologramDistSquared) {
                        Location holoLocation = shop.getHologram().getLocation();

                        double x = holoLocation.getX() - pX;
                        double y = shopLocation.getY() - pY + 1.15; // chest block + item offset
                        double z = holoLocation.getZ() - pZ;

                        // See: org.bukkit.util.Vector#angle(Vector)
                        double angle = FastMath.acos(
                                (x * pDir.getX() + y * pDir.getY() + z * pDir.getZ())
                                        / (FastMath.sqrt(x * x + y * y + z * z) * dirLength)
                        );

                        double distance = FastMath.sqrt(distanceSquared);

                        // Check if is targeted
                        if (angle * distance < TARGET_THRESHOLD) {
                            // Display even if not the nearest
                            if (!firstShopInSight) {
                                shop.getHologram().showPlayer(player);
                                continue;
                            }

                            if (nearestShop == null) {
                                // nearestShop is null
                                // => we guess this one will be the nearest
                                nearestShop = shop;
                                nearestDistance = distance;
                                continue;
                            } else if (distance < nearestDistance) {
                                // nearestShop is NOT null && this shop is nearest
                                // => we'll hide nearestShop, and guess this one will be the nearest
                                otherShopsInSight.add(nearestShop);
                                nearestShop = shop;
                                nearestDistance = distance;
                                continue;
                            }
                            // else: hologram is farther than nearest, so we hide it
                        }
                    }

                    // If not in sight
                    shop.getHologram().hidePlayer(player);
                }
            }
        }

        if (firstShopInSight) {
            // we hide other shop as we wan't to display only the first
            for (Shop shop : otherShopsInSight) {
                // we already checked hasHologram() before adding it
                shop.getHologram().hidePlayer(player);
            }
        }

        if (nearestShop != null && nearestShop.hasHologram()) {
            nearestShop.getHologram().showPlayer(player);
        }
    }

    private void updateNearestShops(Player p) {
        double holoDistSqr = Math.pow(plugin.getShopChestConfig().maximal_distance, 2);
        double itemDistSqr = Math.pow(plugin.getShopChestConfig().maximal_item_distance, 2);

        for (Shop shop : getShops()) {
            if (p.getLocation().getWorld().getName().equals(shop.getLocation().getWorld().getName())) {
                double distSqr = shop.getLocation().distanceSquared(p.getLocation());

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
