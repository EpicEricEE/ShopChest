package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.shop.Shop.ShopType;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClickType {

    private static Map<UUID, ClickType> playerClickType = new HashMap<>();
    private static Map<UUID, BukkitTask> playerTimers = new HashMap<>();

    private EnumClickType enumClickType;
    private ShopProduct product;
    private double buyPrice;
    private double sellPrice;
    private ShopType shopType;

    public ClickType(EnumClickType enumClickType) {
        this.enumClickType = enumClickType;
    }

    public ClickType(EnumClickType enumClickType, ShopProduct product, double buyPrice, double sellPrice, ShopType shopType) {
        this.enumClickType = enumClickType;
        this.product = product;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.shopType = shopType;
    }

    /**
     * Gets the click type of a player
     *
     * @param player Player whose click type should be gotten
     * @return The Player's click type or <b>null</b> if he doesn't have one
     */
    public static ClickType getPlayerClickType(OfflinePlayer player) {
        return playerClickType.get(player.getUniqueId());
    }

    /**
     * Removes the click type from a player and cancels the 15 second timer
     * @param player Player to remove the click type from
     */
    public static void removePlayerClickType(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        playerClickType.remove(uuid);
        
        // If a timer is still running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());
        playerTimers.remove(uuid);
    }

    /**
     * Sets the click type of a player and removes it after 15 seconds
     *
     * @param player    Player whose click type should be set
     * @param clickType Click type to set
     */
    public static void setPlayerClickType(OfflinePlayer player, ClickType clickType) {
        UUID uuid = player.getUniqueId();
        playerClickType.put(uuid, clickType);

        // If a timer is already running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());

        // Remove ClickType after 15 seconds if player has not clicked a chest
        playerTimers.put(uuid, new BukkitRunnable() {
            @Override
            public void run() {
                playerClickType.remove(uuid);
            }
        }.runTaskLater(ShopChest.getInstance(), 300));
    }

    /**
     * @return Type of the click type
     */
    public EnumClickType getClickType() {
        return enumClickType;
    }

    /**
     * @return If {@link #getClickType()} returns {@link EnumClickType#CREATE}, this returns the item, the player has hold in his hands, else <b>null</b>.
     */
    public ShopProduct getProduct() {
        return product;
    }

    /**
     * @return If {@link #getClickType()} returns {@link EnumClickType#CREATE}, this returns the buy price, the player has entered, else <b>null</b>.
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * @return If {@link #getClickType()} returns {@link EnumClickType#CREATE}, this returns the sell price, the player has entered, else <b>null</b>.
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * @return If {@link #getClickType()} returns {@link EnumClickType#CREATE}, this returns the shop type, the player has entered, else <b>null</b>.
     */
    public ShopType getShopType() {
        return shopType;
    }

    public enum EnumClickType {
        CREATE, REMOVE, INFO, OPEN
    }

}
