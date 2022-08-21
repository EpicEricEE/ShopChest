package de.epiceric.shopchest.api.flag;

import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.ShopChest;

/**
 * Represents the flag a player has after entering the edit command
 */
public class EditFlag extends TimedFlag {
    private ItemStack itemStack;
    private int amount;
    private double buyPrice;
    private double sellPrice;

    public EditFlag(ShopChest plugin, ItemStack itemStack, int amount, double buyPrice, double sellPrice) {
        super(plugin, 15);
        this.itemStack = itemStack;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    /**
     * Gets the item stack the player has selected
     * 
     * @return the item or null if no item was selected
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the amount the player has set
     * 
     * @return the amount or -1 if no amount was set
     * @since 2.0
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the buy price the player has set
     * 
     * @return the buy price or -1 if no buy price was set
     * @since 2.0
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Gets the sell price the player has set
     * 
     * @return the sell price or -1 if no sell price was set
     * @since 2.0
     */
    public double getSellPrice() {
        return sellPrice;
    }
}