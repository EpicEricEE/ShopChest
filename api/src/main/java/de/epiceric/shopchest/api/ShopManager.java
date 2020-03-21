package de.epiceric.shopchest.api;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
     * @return a completable future returning the new shop
     * @since 1.13
     * @see ShopManager#addAdminShop(ShopProduct, Location, double, double, Consumer, Consumer)
     */
    CompletableFuture<Shop> addShop(OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice);

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
     * @return a completable future returning the new shop
     * @since 1.13
     * @see ShopManager#addShop(OfflinePlayer, ShopProduct, Location, double, double, Consumer, Consumer)
     */
    CompletableFuture<Shop> addAdminShop(ShopProduct product, Location location, double buyPrice, double sellPrice);

    /**
     * Removes a shop from the database
     * 
     * @param shop the shop to remove
     * @return a completable future returning nothing
     * @since 1.13
     */
    CompletableFuture<Void> removeShop(Shop shop);

    /**
     * Removes all shops and reloads the shops in currently loaded chunks
     * <p>
     * This does not trigger the {@link ShopReloadEvent}.
     * 
     * @return a completable future returning the loaded shops
     * @since 1.13
     */
    CompletableFuture<Collection<Shop>> reloadShops();

}