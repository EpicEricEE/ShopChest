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

    public static ClickType getPlayerClickType(OfflinePlayer player) {
        if (playerClickType.containsKey(player))
            return playerClickType.get(player);
        else
            return null;
    }

    public static void removePlayerClickType(OfflinePlayer player) {
        playerClickType.remove(player);
    }

    public static void addPlayerClickType(OfflinePlayer player, ClickType clickType) {
        playerClickType.put(player, clickType);
    }

    public EnumClickType getClickType() {
        return enumClickType;
    }

    public ItemStack getProduct() {
        return product;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public ShopType getShopType() {
        return shopType;
    }

    public enum EnumClickType {
        CREATE, REMOVE, INFO;
    }

}
