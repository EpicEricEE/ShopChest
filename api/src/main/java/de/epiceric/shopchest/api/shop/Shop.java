package de.epiceric.shopchest.api.shop;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import de.epiceric.shopchest.api.ShopManager;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;

/**
 * Represents a shop
 * 
 * @since 1.13
 * @see ShopManager#addShop(OfflinePlayer, ShopProduct, Location, double, double)
 * @see ShopManager#addAdminShop(ShopProduct, Location, double, double)
 */
public interface Shop {

    /**
     * Gets this shop's ID
     * 
     * @return the ID
     * @since 1.13
     */
    int getId();

    /**
     * Gets the player who created this shop
     * 
     * @return the vendor
     * @since 1.13
     */
    OfflinePlayer getVendor();

    /**
     * Gets a copy of the product this shop is buying or selling
     * 
     * @return the product
     * @since 1.13
     */
    ShopProduct getProduct();

    /**
     * Gets the location of this shop's chest
     * <p>
     * If the shop is on a double chest, it returns one of the chest's location.
     * 
     * @return the location
     * @since 1.13
     */
    Location getLocation();

    /**
     * Gets the world this shop is located in
     * 
     * @return the world
     * @since 1.13
     */
    default World getWorld() {
        return getLocation().getWorld();
    }

    /**
     * Gets whether this shop is on a double chest
     * 
     * @return whether the shop is on a double chest
     * @since 1.13
     */
    boolean isDoubleChest();

    /**
     * Gets the inventory of this shop's chest
     * 
     * @return the inventory
     * @throws ChestNotFoundException when there is no chest at the shop's location
     * @since 1.13
     */
    Inventory getInventory() throws ChestNotFoundException;

    /**
     * Gets whether this shop is an admin shop
     * 
     * @return whether this shop is an admin shop
     * @since 1.13
     */
    boolean isAdminShop();

    /**
     * Sets whether this shop is an admin shop
     * 
     * @param adminShop whether this shop should be an admin shop
     * @since 1.13
     */
    void setAdminShop(boolean adminShop);

    /**
     * Gets the price for which a player can buy the product from this shop
     * 
     * @return the buy price
     * @since 1.13
     */
    double getBuyPrice();

    /**
     * Sets the price for which a player can sell the product to this shop
     * <p>
     * If set to zero, a player cannot buy from this shop.
     * 
     * @param buyPrice the buy price
     * @throws IllegalStateException when a player can neither buy nor sell from this shop
     * @since 1.13
     */
    void setBuyPrice(double buyPrice);

    /**
     * Gets whether a player can buy from this shop
     * 
     * @return whether buying is enabled
     * @since 1.13
     */
    default boolean canPlayerBuy() {
        return getBuyPrice() > 0;
    }

    /**
     * Gets the price for which a player can sell the product to this shop
     * 
     * @return the sell price
     * @since 1.13
     */
    double getSellPrice();

    /**
     * Sets the price for which a player can sell the product to this shop
     * <p>
     * If set to zero, a player cannot sell to this shop.
     * 
     * @param sellPrice the sell price
     * @throws IllegalStateException when a player can neither buy nor sell from this shop
     * @since 1.13
     */
    void setSellPrice(double sellPrice);

    /**
     * Gets whether a player can sell to this shop
     * 
     * @return whether selling is enabled
     * @since 1.13
     */
    default boolean canPlayerSell() {
        return getSellPrice() > 0;
    }

}