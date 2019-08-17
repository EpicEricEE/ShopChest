package de.epiceric.shopchest.api.flag;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.shop.ShopProduct;

/**
 * Represents the flag a player has after entering the create command
 */
public class CreateFlag extends TimedFlag {
    private ShopProduct product;
    private double buyPrice;
    private double sellPrice;
    private boolean admin;

    public CreateFlag(ShopChest plugin, ShopProduct product, double buyPrice, double sellPrice, boolean admin) {
        super(plugin, 15);
        this.product = product;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.admin = admin;
    }

    /**
     * Gets the product the player wants to create a shop with
     * 
     * @return the product
     */
    public ShopProduct getProduct() {
        return product;
    }

    /**
     * Gets the price the player wants others to buy the product for
     * 
     * @return the buy price
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Gets the price the player wants others to sell the product for
     * 
     * @return the sell price
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Gets whether the player wants to create an admin shop
     * 
     * @return whether the players wants to create an admin shop
     */
    public boolean isAdminShop() {
        return admin;
    }

}