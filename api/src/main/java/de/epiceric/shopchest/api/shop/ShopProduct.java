package de.epiceric.shopchest.api.shop;

import org.bukkit.inventory.ItemStack;

/**
 * Represents the item that can be bought or sold in one transaction
 * 
 * @since 1.13
 */
public interface ShopProduct {

    /**
     * Gets the {@link ItemStack} with an amount of one
     * 
     * @return the item
     * @since 1.13
     */
    ItemStack getItemStack();

    /**
     * Sets the {@link ItemStack}
     * <p>
     * The passed item stack will be cloned and its amount set to one.
     * 
     * @param itemStack the item
     * @since 1.13
     */
    void setItemStack(ItemStack itemStack);

    /**
     * Gets the amount of items bought or sold in one transaction
     * 
     * @return the amount
     * @since 1.13
     */
    int getAmount();

    /**
     * Sets the amount of items bought or sold in one transaction
     * 
     * @param amount the amount
     * @since 1.13
     */
    void setAmount(int amount);

    /**
     * Gets the localized name of this product's item in the configured langauge file
     * 
     * @return the localized name
     * @since 1.13
     */
    String getLocalizedName();
    
}