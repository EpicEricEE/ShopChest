package de.epiceric.shopchest.api.shop;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import de.epiceric.shopchest.api.ShopManager;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;

/**
 * Represents a shop
 * 
 * @since 2.0
 * @see ShopManager#addShop(OfflinePlayer, ShopProduct, Location, double, double)
 * @see ShopManager#addAdminShop(ShopProduct, Location, double, double)
 */
public interface Shop {

    /**
     * Gets this shop's ID
     * 
     * @return the ID
     * @throws IllegalStateException if the shop has not been added to the database yet
     * @since 2.0
     */
    int getId();

    /**
     * Gets the player who owns this shop
     * 
     * @return the vendor or an empty optional if this shop is an admin shop
     * @since 2.0
     */
    Optional<OfflinePlayer> getVendor();

    /**
     * Gets a copy of the product this shop is buying or selling
     * 
     * @return the product
     * @since 2.0
     */
    ShopProduct getProduct();

    /**
     * Gets the location of this shop's chest
     * <p>
     * If the shop is on a double chest, it returns one of the chest's location.
     * 
     * @return the location
     * @since 2.0
     */
    Location getLocation();

    /**
     * Gets the world this shop is located in
     * 
     * @return the world
     * @since 2.0
     */
    default World getWorld() {
        return getLocation().getWorld();
    }

    /**
     * Gets whether this shop is on a double chest
     * 
     * @return whether the shop is on a double chest
     * @since 2.0
     */
    boolean isDoubleChest();

    /**
     * Gets the inventory of this shop's chest
     * 
     * @return the inventory
     * @throws ChestNotFoundException when there is no chest at the shop's location
     * @since 2.0
     */
    Inventory getInventory() throws ChestNotFoundException;

    /**
     * Gets whether this shop is an admin shop
     * 
     * @return whether this shop is an admin shop
     * @since 2.0
     */
    default boolean isAdminShop() {
        return !getVendor().isPresent();
    }

    /**
     * Gets the price for which a player can buy the product from this shop
     * 
     * @return the buy price
     * @since 2.0
     */
    double getBuyPrice();

    /**
     * Gets whether a player can buy from this shop
     * 
     * @return whether buying is enabled
     * @since 2.0
     */
    default boolean canPlayerBuy() {
        return getBuyPrice() > 0;
    }

    /**
     * Gets the price for which a player can sell the product to this shop
     * 
     * @return the sell price
     * @since 2.0
     */
    double getSellPrice();

    /**
     * Gets whether a player can sell to this shop
     * 
     * @return whether selling is enabled
     * @since 2.0
     */
    default boolean canPlayerSell() {
        return getSellPrice() > 0;
    }

}