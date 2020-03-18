package de.epiceric.shopchest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Chest;

import de.epiceric.shopchest.api.ShopManager;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.shop.ShopImpl;

public class ShopManagerImpl implements ShopManager {
    private static ShopManagerImpl instance;

    /* package-private */ static ShopManagerImpl get(ShopChestImpl plugin) {
        if (instance == null) {
            instance = new ShopManagerImpl(plugin);
        }
        return instance;
    }

    private ShopChestImpl plugin;

    private Map<String, Map<Location, Shop>> shopsInWorld = new HashMap<>();

    private ShopManagerImpl(ShopChestImpl plugin) {
        this.plugin = plugin;
    }

    private Location toBlockLocation(Location location) {
        return new Location(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Gets the location of the other half of a double chest shop
     * <p>
     * This is supposed to be called after {@link ShopManager#removeShop(Shop, Consumer, Consumer)}
     * in case the chests don't exist at the time of shop removal.
     * 
     * @param id the ID of the shop
     * @param world the world of the shop
     * @return an optional of the other chest's location if it still exists
     */
    private Optional<Location> getRemainingShopLocation(int id, World world) {
        return shopsInWorld.get(world.getName()).entrySet().stream().filter(entry -> entry.getValue().getId() == id)
                .map(Entry::getKey).findAny();
    }

    /**
     * Destoys all shops and clears the cache
     */
    public void clearShops() {
        shopsInWorld.values().stream().flatMap(map -> map.values().stream())
                .forEach(shop -> ((ShopImpl) shop).destroy());
        shopsInWorld.clear();
    }

    /**
     * Loads and caches shops from the database
     * <p>
     * Cache will be cleared before new shops are cached.
     */
    public void loadShops(Consumer<Collection<Shop>> callback, Consumer<Throwable> errorCallback) {
        plugin.getDatabase().connect(
            amount -> {
                plugin.getDatabase().getShops(
                    shops -> {
                        clearShops();
                        shops.stream().forEach(shop -> {
                            ((ShopImpl) shop).create();

                            String worldName = shop.getWorld().getName();
                            if (!shopsInWorld.containsKey(worldName)) {
                                shopsInWorld.put(worldName, new HashMap<>());
                            }
                            shopsInWorld.get(worldName).put(toBlockLocation(shop.getLocation()), shop);
            
                            ((ShopImpl) shop).getOtherLocation().ifPresent(otherLoc ->
                                    shopsInWorld.get(worldName).put(toBlockLocation(otherLoc), shop));;
                        });
                        callback.accept(shops);
                    },
                    errorCallback
                );
            },
            errorCallback
        );
    }

    /* API Implementation */

    @Override
    public Collection<Shop> getShops() {
        return shopsInWorld.values().stream().flatMap((map) -> map.values().stream()).distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Shop> getShop(int id) {
        return getShops().stream().filter(shop -> shop.getId() == id).findAny();
    }

    @Override
    public Optional<Shop> getShop(Location location) {
        if (!(location.getBlock().getState() instanceof Chest)) {
            return Optional.empty();
        }

        return Optional.ofNullable(shopsInWorld.get(location.getWorld().getName()))
                .map(map -> map.get(toBlockLocation(location)));
    }

    @Override
    public Collection<Shop> getShops(OfflinePlayer vendor) {
        return getShops().stream().filter(shop -> !shop.isAdminShop())
                .filter(shop -> shop.getVendor().get().getUniqueId().equals(vendor.getUniqueId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Shop> getShops(World world) {
        if (!shopsInWorld.containsKey(world.getName())) {
            return new ArrayList<>();
        }

        return shopsInWorld.get(world.getName()).values().stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void addShop(OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice, Consumer<Shop> callback, Consumer<Throwable> errorCallback) {
        Shop shop = new ShopImpl(vendor, product, location, buyPrice, sellPrice); 
        plugin.getDatabase().addShop(shop,
            id -> {
                ((ShopImpl) shop).create();

                String worldName = location.getWorld().getName();
                if (!shopsInWorld.containsKey(worldName)) {
                    shopsInWorld.put(worldName, new HashMap<>());
                }
                shopsInWorld.get(worldName).put(toBlockLocation(location), shop);

                ((ShopImpl) shop).getOtherLocation().ifPresent(otherLoc ->
                        shopsInWorld.get(worldName).put(toBlockLocation(otherLoc), shop));

                callback.accept(shop);
            },
            errorCallback
        );
    }

    @Override
    public void addAdminShop(ShopProduct product, Location location, double buyPrice, double sellPrice, Consumer<Shop> callback, Consumer<Throwable> errorCallback) {
        addShop(null, product, location, buyPrice, sellPrice, callback, errorCallback);
    }

    @Override
    public void removeShop(Shop shop, Consumer<Void> callback, Consumer<Throwable> errorCallback) {
        ((ShopImpl) shop).destroy();
        shopsInWorld.get(shop.getWorld().getName()).remove(shop.getLocation());

        getRemainingShopLocation(shop.getId(), shop.getWorld()).ifPresent(otherLoc -> {
            shopsInWorld.get(shop.getWorld().getName()).remove(otherLoc);
        });

        plugin.getDatabase().removeShop(shop, callback, errorCallback);
    }

    @Override
    public void reloadShops(Consumer<Integer> callback, Consumer<Throwable> errorCallback) {

    }
}