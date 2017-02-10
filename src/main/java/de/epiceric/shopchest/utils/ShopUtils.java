package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ShopUtils {

    private HashMap<Location, Shop> shopLocation = new HashMap<>();
    private HashMap<Player, Location> playerLocation = new HashMap<>();
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
            plugin.getShopDatabase().addShop(shop, null);

    }

    /**
     * Remove a shop
     * @param shop Shop to remove
     * @param removeFromDatabase Whether the shop should also be removed from the database
     */
    public void removeShop(Shop shop, boolean removeFromDatabase, boolean useCurrentThread) {
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
        shop.removeHologram(useCurrentThread);

        if (removeFromDatabase)
            plugin.getShopDatabase().removeShop(shop, null);
    }

    public void removeShop(Shop shop, boolean removeFromDatabase) {
        removeShop(shop, removeFromDatabase, false);
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
    public void reloadShops(boolean reloadConfig, boolean showConsoleMessages, final Callback callback) {
        plugin.debug("Reloading shops...");

        if (reloadConfig) {
            plugin.getShopChestConfig().reload(false, true, showConsoleMessages);
            plugin.getUpdater().setMaxDelta(plugin.getShopChestConfig().update_quality.getTime());
        }

        plugin.getShopDatabase().connect(new Callback(plugin) {
            @Override
            public void onResult(Object result) {

                for (Shop shop : getShops()) {
                    removeShop(shop, false);
                    plugin.debug("Removed shop (#" + shop.getID() + ")");
                }

                plugin.getShopDatabase().getShops(new Callback(plugin) {
                    @Override
                    public void onResult(Object result) {
                        if (result instanceof Shop[]) {
                            Shop[] shops = (Shop[]) result;
                            for (Shop shop : shops) {
                                if (shop.create()) {
                                    addShop(shop, false);
                                }
                            }
                            if (callback != null) callback.callSyncResult(shops.length);
                        }
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
        if (player.getLocation().equals(playerLocation.get(player))) {
            // Player has not moved, so don't calculate shops again.
            return;
        }

        if (plugin.getShopChestConfig().only_show_shops_in_sight) {
            HashSet<Material> transparent = new HashSet<>();
            transparent.add(Material.AIR);

            List<Block> sight;

            try {
                sight = player.getLineOfSight(transparent, (int) plugin.getShopChestConfig().maximal_distance);
            } catch (IllegalStateException e) {
                // This method is occasionally throwing this exception but the exact reason is unknown...
                plugin.debug("Failed to get line of sight: " + e.getMessage());
                return;
            }

            ArrayList<Shop> shopsInSight = new ArrayList<>();

            for (Block block : sight) {
                if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                    if (isShop(block.getLocation())) {
                        Shop shop = getShop(block.getLocation());
                        shopsInSight.add(shop);

                        if (shop.getHologram() != null && !shop.getHologram().isVisible(player)) {
                            shop.getHologram().showPlayer(player);
                        }
                    }
                } else {
                    Block below = block.getRelative(BlockFace.DOWN);
                    if (isShop(below.getLocation())) {
                        Shop shop = getShop(below.getLocation());
                        shopsInSight.add(shop);

                        if (shop.getHologram() != null && !shop.getHologram().isVisible(player)) {
                            shop.getHologram().showPlayer(player);
                        }
                    }
                }
            }

            double itemDistSqr = Math.pow(plugin.getShopChestConfig().maximal_item_distance, 2);

            for (Shop shop : getShops()) {
                if (shop.getItem() != null && shop.getLocation().getWorld().getName().equals(player.getWorld().getName())) {
                    if (shop.getLocation().distanceSquared(player.getEyeLocation()) <= itemDistSqr) {
                        shop.getItem().setVisible(player, true);
                    } else {
                        shop.getItem().setVisible(player, false);
                    }
                }

                if (!shopsInSight.contains(shop)) {
                    if (shop.getHologram() != null) {
                        shop.getHologram().hidePlayer(player);
                    }
                }
            }
        } else {
            for (Shop shop : getShops()) {
                updateShop(shop, player);
            }
        }

        playerLocation.put(player, player.getLocation());
    }

    /**
     * Update hologram and item of the shop for a player based on their distance to each other
     * @param shop Shop to update
     * @param player Player to show the update
     */
    public void updateShop(Shop shop, Player player) {
        double holoDistSqr = Math.pow(plugin.getShopChestConfig().maximal_distance, 2);
        double itemDistSqr = Math.pow(plugin.getShopChestConfig().maximal_item_distance, 2);

        if (player.getLocation().getWorld().getName().equals(shop.getLocation().getWorld().getName())) {
            double distSqr = shop.getLocation().distanceSquared(player.getLocation());

            if (distSqr <= holoDistSqr) {
                if (shop.getHologram() != null) {
                    Material type = shop.getLocation().getBlock().getType();

                    if (type != Material.CHEST && type != Material.TRAPPED_CHEST) {
                        plugin.getShopUtils().removeShop(shop, plugin.getShopChestConfig().remove_shop_on_error);
                        return;
                    }
                    if (!shop.getHologram().isVisible(player)) {
                        shop.getHologram().showPlayer(player);
                    }
                }
            } else {
                if (shop.getHologram() != null) {
                    shop.getHologram().hidePlayer(player);
                }
            }

            if (distSqr <= itemDistSqr) {
                if (shop.getItem() != null) {
                    shop.getItem().setVisible(player, true);
                }
            } else {
                if (shop.getItem() != null) shop.getItem().setVisible(player, false);
            }
        }
    }
}
