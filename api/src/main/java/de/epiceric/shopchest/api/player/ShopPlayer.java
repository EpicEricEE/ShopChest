package de.epiceric.shopchest.api.player;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;

import org.bukkit.entity.Player;

import de.epiceric.shopchest.api.ShopManager;
import de.epiceric.shopchest.api.flag.Flag;
import de.epiceric.shopchest.api.shop.Shop;

/**
 * A wrapper for a {@link Player} with additional functions
 * 
 * @since 2.0
 */
public interface ShopPlayer {

    /**
     * Gets the wrapped Bukkit player
     * 
     * @return the wrapped player
     * @since 2.0
     */
    Player getBukkitPlayer();

    /**
     * Sends a (formatted) message to this player
     * <p>
     * Format arguments are referenced via <code>{0}</code>, <code>{1}</code>, etc.
     * 
     * @param message the message (a format string)
     * @param args    the arguments referenced by the format
     * @since 2.0
     */
    default void sendMessage(String message, Object... args) {
        getBukkitPlayer().sendMessage(MessageFormat.format(message, args));
    }

    /**
     * Gets whether this player has the given permission
     * 
     * @param permission the permission
     * @return whether this player has the permission
     */
    default boolean hasPermission(String permission) {
        return getBukkitPlayer().hasPermission(permission);
    }

    /**
     * Gets this player's flag
     * 
     * @return the flag or an empty optional if the player does not have one
     * @since 2.0
     */
    Optional<Flag> getFlag();

    /**
     * Sets this player's flag
     * 
     * @param flag the flag
     * @since 2.0
     */
    void setFlag(Flag flag);

    /**
     * Gets whether this player has a flag
     * 
     * @return whether this player has a flag
     * @since 2.0
     */
    default boolean hasFlag() {
        return getFlag().isPresent();
    }

    /**
     * Removes this player's flag
     * 
     * @since 2.0
     */
    default void removeFlag() {
        setFlag(null);
    }

    /**
     * Gets the amount of shops the given player is allowed to have
     * <p>
     * If the player has no shop limit {@link Integer#MAX_VALUE} is returned.
     * 
     * @return the shop limit
     * @since 2.0
     */
    int getShopLimit();

    /**
     * Gets the amount of shops the given player currently has
     * <p>
     * This number includes shops that are not loaded.
     * 
     * @return the amount of shops
     * @since 2.0
     */
    int getShopAmount();

    /**
     * Gets the loaded shops this player owns
     * 
     * @return a collection of shops
     * @since 2.0
     * @see ShopManager#getShops(org.bukkit.OfflinePlayer)
     */
    Collection<Shop> getShops();

    /**
     * Gets whether this player is the vendor of the given shop
     * 
     * @param shop the shop
     * @return whether this player is the vendor
     * @since 2.0
     */
    default boolean isVendor(Shop shop) {
        return shop != null && shop.getVendor()
                .map(vendor -> vendor.getUniqueId().equals(getBukkitPlayer().getUniqueId()))
                .orElse(false);
    }
}