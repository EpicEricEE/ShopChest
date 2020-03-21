package de.epiceric.shopchest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        synchronized (shopsInWorld) {
            shopsInWorld.values().stream().flatMap(map -> map.values().stream())
                    .forEach(shop -> ((ShopImpl) shop).destroy());
            shopsInWorld.clear();
        }
    }

    /**
     * Loads shops in the given chunks from the database
     * <p>
     * This will fire a {@link ShopLoadedEvent}.
     * 
     * @param chunks the chunks whose shops will be loaded
     * @return a completable future returning the loaded shops
     */
    public CompletableFuture<Collection<Shop>> loadShops(Chunk[] chunks) {
        return plugin.getDatabase().getShops(chunks).thenApply(shops -> {
            for (Iterator<Shop> it = shops.iterator(); it.hasNext();) {
                Shop shop = it.next();

                if (getShop(shop.getLocation()).isPresent()) {
                    // A shop is already loaded at the location, which should be the same.
                    it.remove();
                    continue;
                }

                synchronized (shopsInWorld) {
                    String worldName = shop.getWorld().getName();
                    if (!shopsInWorld.containsKey(worldName)) {
                        shopsInWorld.put(worldName, new HashMap<>());
                    }

                    ((ShopImpl) shop).create();

                    shopsInWorld.get(worldName).put(toBlockLocation(shop.getLocation()), shop);
                    ((ShopImpl) shop).getOtherLocation().ifPresent(otherLoc ->
                            shopsInWorld.get(worldName).put(toBlockLocation(otherLoc), shop));
                }
            }

            Collection<Shop> unmodifiableShops = Collections.unmodifiableCollection(shops);
            plugin.getServer().getPluginManager().callEvent(new ShopLoadedEvent(unmodifiableShops));
            return unmodifiableShops;
        });
    }

    /**
     * Loads all players' shop amounts from the database
     * 
     * @return a completable future returning nothing
     */
    public CompletableFuture<Void> loadShopAmounts() {
        return plugin.getDatabase().getShopAmounts().thenAccept(shopAmounts -> {
            synchronized (shopAmounts) {
                this.shopAmounts.clear();
                shopAmounts.forEach((uuid, amount) -> this.shopAmounts.put(uuid, new Counter(amount)));
            }
        });
    }

    /**
     * Gets the amount of shops a player has
     * 
     * @param player the player
     * @return the amount of shops
     * @see ShopPlayer#getShopAmount()
     */
    public int getShopAmount(OfflinePlayer player) {
        synchronized (shopAmounts) {
            return shopAmounts.getOrDefault(player.getUniqueId(), new Counter()).get();
        }
    }

    /* API Implementation */

    @Override
    public Collection<Shop> getShops() {
        synchronized (shopsInWorld) {
            return shopsInWorld.values().stream().flatMap((map) -> map.values().stream()).distinct()
                    .collect(Collectors.toList());
        }
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

        synchronized (shopsInWorld) {
            return Optional.ofNullable(shopsInWorld.get(location.getWorld().getName()))
                    .map(map -> map.get(toBlockLocation(location)));
        }
    }

    @Override
    public Collection<Shop> getShops(OfflinePlayer vendor) {
        return getShops().stream().filter(shop -> !shop.isAdminShop())
                .filter(shop -> shop.getVendor().get().getUniqueId().equals(vendor.getUniqueId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Shop> getShops(World world) {
        synchronized (shopsInWorld) {
            if (!shopsInWorld.containsKey(world.getName())) {
                return new ArrayList<>();
            }

            return shopsInWorld.get(world.getName()).values().stream().distinct().collect(Collectors.toList());
        }
    }

    @Override
    public CompletableFuture<Shop> addShop(OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice) {
        ShopImpl shop = new ShopImpl(vendor, product, location, buyPrice, sellPrice); 
        return plugin.getDatabase().addShop(shop).thenApply(id -> {
            shop.create();
            shop.setId(id);

            synchronized (shopsInWorld) {
                String worldName = location.getWorld().getName();
                if (!shopsInWorld.containsKey(worldName)) {
                    shopsInWorld.put(worldName, new HashMap<>());
                }
                shopsInWorld.get(worldName).put(toBlockLocation(location), shop);

                shop.getOtherLocation().ifPresent(otherLoc ->
                        shopsInWorld.get(worldName).put(toBlockLocation(otherLoc), shop));
            }

            if (vendor != null) {
                synchronized (shopAmounts) {
                    shopAmounts.compute(vendor.getUniqueId(), (uuid, counter) -> {
                        return counter == null ? new Counter(1) : counter.increment();
                    });
                }
            }

            return shop;
        });
    }

    @Override
    public CompletableFuture<Shop> addAdminShop(ShopProduct product, Location location, double buyPrice, double sellPrice) {
        return addShop(null, product, location, buyPrice, sellPrice);
    }

    @Override
    public CompletableFuture<Void> removeShop(Shop shop) {
        return plugin.getDatabase().removeShop(shop).thenRun(() -> {
            ((ShopImpl) shop).destroy();

            synchronized (shopsInWorld) {
                shopsInWorld.get(shop.getWorld().getName()).remove(shop.getLocation());
        
                getRemainingShopLocation(shop.getId(), shop.getWorld()).ifPresent(otherLoc ->
                    shopsInWorld.get(shop.getWorld().getName()).remove(otherLoc));
            }
    
            shop.getVendor().ifPresent(vendor -> {
                synchronized (shopAmounts) {
                    shopAmounts.compute(vendor.getUniqueId(), (uuid, counter) -> {
                        return counter == null ? new Counter() : counter.decrement();
                    });
                }
            });
        });
    }

    @Override
    public CompletableFuture<Collection<Shop>> reloadShops() {
        clearShops();

        Chunk[] chunks = plugin.getServer().getWorlds().stream().map(World::getLoadedChunks)
                .flatMap(Stream::of).toArray(Chunk[]::new);

        return loadShops(chunks);
    }
}