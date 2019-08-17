package de.epiceric.shopchest.api.shop;

import org.bukkit.inventory.ItemStack;

/**
 * Represents the item that can be bought or sold in one transaction
 * 
 * @since 1.13
 */
public abstract class ShopProduct implements Cloneable {
    private ItemStack itemStack;
    private int amount;

    public ShopProduct(ItemStack itemStack, int amount) {
        this.itemStack = itemStack;
        this.amount = amount;
    }

    /**
     * Gets a copy of this product's {@link ItemStack} with an amount of one
     * 
     * @return the item
     * @since 1.13
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * Sets this product's {@link ItemStack}
     * <p>
     * The passed item stack will be copied and its amount set to one.
     * 
     * @param itemStack the item
     * @since 1.13
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    /**
     * Gets the amount of items bought or sold in one transaction
     * 
     * @return the amount
     * @since 1.13
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of items bought or sold in one transaction
     * 
     * @param amount the amount
     * @since 1.13
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the localized name of this product's item in the configured langauge file
     * 
     * @return the localized name
     * @since 1.13
     */
    public abstract String getLocalizedName();

    @Override
    public ShopProduct clone() {
        try {
            ShopProduct shopProduct = (ShopProduct) super.clone();

            if (this.itemStack != null) {
                shopProduct.itemStack = this.itemStack.clone();
            }

            return shopProduct;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
    
}