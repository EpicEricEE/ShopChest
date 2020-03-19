package de.epiceric.shopchest.shop;

import java.util.Locale;
import java.util.StringJoiner;

import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.shop.ShopProduct;

public class ShopProductImpl extends ShopProduct {
    public ShopProductImpl(ItemStack itemStack, int amount) {
        super(itemStack, amount);
    }

    @Override
    public String getLocalizedName() {
        // TODO: i18n
        String lower = getItemStack().getType().toString().replace("_", " ").toLowerCase(Locale.US);
        StringJoiner joiner = new StringJoiner(" ");
        for (String word : lower.split("\\s")) {
            joiner.add(word.substring(0, 1).toUpperCase(Locale.US) + word.substring(1));
        }
        return joiner.toString();
    }
}