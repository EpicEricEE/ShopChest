package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.sql.Database;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShopUtils {

    private static HashMap<Location, Shop> shopLocation = new HashMap<>();

    public static Shop getShop(Location location) {
        Location newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());

        if (shopLocation.containsKey(newLocation)) {
            return shopLocation.get(newLocation);
        } else {
            return null;
        }
    }

    public static boolean isShop(Location location) {
        Location newLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
        return shopLocation.containsKey(newLocation);
    }

    public static Shop[] getShops() {
        ArrayList<Shop> shops = new ArrayList<>();

        for (Shop shop : shopLocation.values()) {
            shops.add(shop);
        }

        return shops.toArray(new Shop[shops.size()]);
    }

    public static void addShop(Shop shop, boolean addToDatabase) {
        Location loc = shop.getLocation();
        Block b = loc.getBlock();

        if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
            Chest c = (Chest) b.getState();
            InventoryHolder ih = c.getInventory().getHolder();

            if (ih instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) ih;
                Chest r = (Chest) dc.getRightSide();
                Chest l = (Chest) dc.getLeftSide();

                shopLocation.put(r.getLocation(), shop);
                shopLocation.put(l.getLocation(), shop);
                return;
            } else {
                shopLocation.put(shop.getLocation(), shop);
            }

            if (addToDatabase)
                ShopChest.database.addShop(shop);
        }
    }

    public static void removeShop(Shop shop, boolean removeFromDatabase) {
        Location loc = shop.getLocation();
        Block b = loc.getBlock();

        if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
            Chest c = (Chest) b.getState();
            InventoryHolder ih = c.getInventory().getHolder();

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
                ShopChest.database.removeShop(shop);
        }
    }

    public static int getShopLimit(Player p) {
        int limit = Config.default_limit();

        if (ShopChest.perm.hasGroupSupport()) {
            List<String> groups = new ArrayList<String>();

            for (String key : Config.shopLimits_group()) {
                for (int i = 0; i < ShopChest.perm.getGroups().length; i++) {
                    if (ShopChest.perm.getGroups()[i].equals(key)) {
                        if (ShopChest.perm.playerInGroup(p, key)) {
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

        for (String key : Config.shopLimits_player()) {
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

    public static int getShopAmount(OfflinePlayer p) {
        int shopCount = 0;

        for (Shop shop : ShopUtils.getShops()) {
            if (shop.getVendor().equals(p)) shopCount++;
        }

        return shopCount;
    }

    public static void reloadShops(Player player) {
        for (Shop shop : ShopUtils.getShops()) {
            ShopUtils.removeShop(shop, false);
        }

        int count = 0;
        for (int id = 1; id < ShopChest.database.getHighestID() + 1; id++) {

            try {
                Shop shop = (Shop) ShopChest.database.get(id, Database.ShopInfo.SHOP);
                ShopUtils.addShop(shop, false);
            } catch (NullPointerException e) {
                continue;
            }

            count++;
        }

        if (player != null) player.sendMessage(Config.reloaded_shops(count));

        for (Player p : Bukkit.getOnlinePlayers()) {
            Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
        }
    }
}
