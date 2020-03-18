package de.epiceric.shopchest.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import de.epiceric.shopchest.api.event.ShopReloadEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;

/**
 * Collection of methods to get, add or remove shops
 * 
 * @since 1.13
 */
public interface ShopManager {

    /**
     * Gets all currently loaded shops
     * 
     * @return a collection of shops
     */
    Collection<Shop> getShops();

    /**
     * Gets the shop by its ID
     * 
     * @param id the shop's ID
     * @return the shop or an empty optional if there is no shop loaded
     * @since 1.13
     */
    Optional<Shop> getShop(int id);

    /**
     * Gets the shop at the given location
     * 
     * @param location the shop's chest location
     * @return the shop or an empty optional if there is no shop loaded
     * @since 1.13
     */
    Optional<Shop> getShop(Location location);

    /**
     * Gets all loaded shops by the given player
     * 
     * @param vendor the player
     * @return a collection of shops
     * @since 1.13
     * @see ShopPlayer#getShops()
     */
    Collection<Shop> getShops(OfflinePlayer vendor);

    /**
     * Gets all loaded shops in the given world
     * 
     * @param world the world
     * @return a collection of shops
     * @since 1.13
     */
    Collection<Shop> getShops(World world);

    /**
     * Creates a shop and adds it to the database
     * <p>
     * When {@code buyPrice} is zero, a player cannot buy from the shop.
     * When {@code sellPrice} is zero, a player cannot sell to the shop.
     * You cannot have {@code buyPrice} and {@code sellPrice} be zero.
     * 
     * @param vendor        the shop's vendor
     * @param product       the shop's product
     * @param location      the shop's chest location.
     *                      Can be either chest if it's on a double chest
     * @param buyPrice      the price a player can buy the product for.
     * @param sellPrice     the price a player can sell the product for.
     * @param callback      the callback returning the created shop on success
     * @param errorCallback the callback returning the error if one occurred
     * @since 1.13
     * @see ShopManager#addAdminShop(ShopProduct, Location, double, double, Consumer, Consumer)
     */
    void addShop(OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice, Consumer<Shop> callback, Consumer<Throwable> errorCallback);

    /**
     * Creates an admin shop and adds it to the database
     * <p>
     * When {@code buyPrice} is zero, a player cannot buy from the shop.
     * When {@code sellPrice} is zero, a player cannot sell to the shop.
     * You cannot have {@code buyPrice} and {@code sellPrice} be zero.
     * 
     * @param product       the shop's product
     * @param location      the shop's chest location.
     *                      Can be either chest if it's on a double chest
     * @param buyPrice      the price a player can buy the product for.
     * @param sellPrice     the price a player can sell the product for.
     * @param callback      the callback returning the created shop on success
     * @param errorCallback the callback returning the error if one occurred
     * @since 1.13
     * @see ShopManager#addShop(OfflinePlayer, ShopProduct, Location, double, double, Consumer, Consumer)
     */
    void addAdminShop(ShopProduct product, Location location, double buyPrice, double sellPrice, Consumer<Shop> callback, Consumer<Throwable> errorCallback);

    /**
     * Removes a shop from the database
     * 
     * @param shop the shop to remove
     * @param callback      the callback returning nothing on success
     * @param errorCallback the callback returning the error if one occurred
     * @since 1.13
     */
    void removeShop(Shop shop, Consumer<Void> callback, Consumer<Throwable> errorCallback);

    /**
     * Removes all shops and reloads the shops in currently loaded chunks
     * <p>
     * This does not trigger the {@link ShopReloadEvent}.
     * 
     * @param callback      the callback returning the amount of shops on success
     * @param errorCallback the callback returning the error if one occurred
     * @since 1.13
     */
    void reloadShops(Consumer<Integer> callback, Consumer<Throwable> errorCallback);

}