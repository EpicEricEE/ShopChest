package de.epiceric.shopchest.shop;

import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.language.LanguageUtils;

public class ShopProduct {

    private final ItemStack itemStack;
    private final int amount;

    public ShopProduct(ItemStack itemStack, int amount) {
        this.itemStack = new ItemStack(itemStack);
        this.itemStack.setAmount(1);
        this.amount = amount;
    }

    public ShopProduct(ItemStack itemStack) {
        this(itemStack, itemStack.getAmount());
    }

    /**
     * @return The localized name of the product's {@link ItemStack} in the selected language file.
     */
    public String getLocalizedName() {
        return LanguageUtils.getItemName(getItemStack());
    }

    /**
     * @return The {@link ItemStack} with an amount of {@code 1}.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * @return The amount
     */
    public int getAmount() {
        return amount;
    }
    
}