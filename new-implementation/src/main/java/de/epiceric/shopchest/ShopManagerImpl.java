package de.epiceric.shopchest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Chest;

import de.epiceric.shopchest.api.ShopManager;
import de.epiceric.shopchest.api.event.ShopLoadedEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.shop.ShopImpl;
import de.epiceric.shopchest.util.Counter;

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
    private Map<UUID, Counter> shopAmounts = new HashMap<>();

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
     * Loads shops in the given chunks from the database
     * <p>
     * This will fire a {@link ShopLoadedEvent}.
     * 
     * @param chunks a collection
     */
    public void loadShops(Chunk[] chunks, Consumer<Collection<Shop>> callback, Consumer<Throwable> errorCallback) {
        plugin.getDatabase().connect(
            amount -> {
                plugin.getDatabase().getShops(chunks,
                    shops -> {
                        for (Iterator<Shop> it = shops.iterator(); it.hasNext();) {
                            Shop shop = it.next();

                            if (getShop(shop.getLocation()).isPresent()) {
                                // A shop is already loaded at the location, which should be the same.
                                it.remove();
                                continue;
                            }

                            String worldName = shop.getWorld().getName();
                            if (!shopsInWorld.containsKey(worldName)) {
                                shopsInWorld.put(worldName, new HashMap<>());
                            }

                            ((ShopImpl) shop).create();

                            shopsInWorld.get(worldName).put(toBlockLocation(shop.getLocation()), shop);
                            ((ShopImpl) shop).getOtherLocation().ifPresent(otherLoc ->
                                    shopsInWorld.get(worldName).put(toBlockLocation(otherLoc), shop));
                        }
                        
                        callback.accept(Collections.unmodifiableCollection(shops));
                        plugin.getServer().getPluginManager().callEvent(new ShopLoadedEvent(shops));
                    },
                    errorCallback
                );
            },
            errorCallback
        );
    }

    /**
     * Loads all players' shop amounts from the database
     */
    public void loadShopAmounts(Consumer<Map<UUID, Integer>> callback, Consumer<Throwable> errorCallback) {
        plugin.getDatabase().getShopAmounts(
            shopAmounts -> {
                this.shopAmounts.clear();
                shopAmounts.forEach((uuid, amount) -> this.shopAmounts.put(uuid, new Counter(amount)));
                callback.accept(shopAmounts);
            },
            errorCallback
        );
    }

    /**
     * Gets the amount of shops a player has
     * 
     * @param player the player
     * @return the amount of shops
     * @see ShopPlayer#getShopAmount()
     */
    public int getShopAmount(OfflinePlayer player) {
        return shopAmounts.getOrDefault(player.getUniqueId(), new Counter()).get();
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

                if (vendor != null) {
                    shopAmounts.compute(vendor.getUniqueId(), (uuid, counter) -> {
                        return counter == null ? new Counter(1) : counter.increment();
                    });
                }

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

        getRemainingShopLocation(shop.getId(), shop.getWorld()).ifPresent(otherLoc ->
            shopsInWorld.get(shop.getWorld().getName()).remove(otherLoc));

        
        shop.getVendor().ifPresent(vendor -> {
            shopAmounts.compute(vendor.getUniqueId(), (uuid, counter) -> {
                return counter == null ? new Counter() : counter.decrement();
            });
        });

        plugin.getDatabase().removeShop(shop, callback, errorCallback);
    }

    @Override
    public void reloadShops(Consumer<Integer> callback, Consumer<Throwable> errorCallback) {
        List<Chunk> chunks = new ArrayList<>();
        for (World world : plugin.getServer().getWorlds()) {
            chunks.addAll(Arrays.asList(world.getLoadedChunks()));
        }

        loadShops(chunks.toArray(new Chunk[chunks.size()]),
            shops -> callback.accept(shops.size()),
            errorCallback
        );
    }
}