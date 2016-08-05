package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.shop.Shop.ShopType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ClickType {

    private static HashMap<OfflinePlayer, ClickType> playerClickType = new HashMap<>();
    private EnumClickType enumClickType;
    private ItemStack product;
    private double buyPrice;
    private double sellPrice;
    private ShopType shopType;

    public ClickType(EnumClickType enumClickType) {
        this.enumClickType = enumClickType;
    }

    public ClickType(EnumClickType enumClickType, ItemStack product, double buyPrice, double sellPrice, ShopType shopType) {
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
        if (playerClickType.containsKey(player))
            return playerClickType.get(player);
        else
            return null;
    }

    /**
     * Removes the click type from a player
     * @param player Player to remove the click type from
     */
    public static void removePlayerClickType(OfflinePlayer player) {
        playerClickType.remove(player);
    }

    /**
     * Sets the click type of a player
     *
     * @param player    Player whose click type should be set
     * @param clickType Click type to set
     */
    public static void setPlayerClickType(OfflinePlayer player, ClickType clickType) {
        playerClickType.put(player, clickType);
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
    public ItemStack getProduct() {
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
        CREATE, REMOVE, INFO
    }

}
