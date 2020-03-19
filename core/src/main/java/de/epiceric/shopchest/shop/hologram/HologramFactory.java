package de.epiceric.shopchest.shop.hologram;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.util.NmsUtil;

class HologramFactory {
    private HologramFactory() {
    }

    static IHologramLine newHologramLine(Location location, String text) {
        switch (NmsUtil.getServerVersion()) {
            case "v1_8_R1": return new de.epiceric.shopchest.shop.hologram.v1_8_R1.HologramLine(location, text);
            case "v1_8_R2": return new de.epiceric.shopchest.shop.hologram.v1_8_R2.HologramLine(location, text);
            case "v1_8_R3": return new de.epiceric.shopchest.shop.hologram.v1_8_R3.HologramLine(location, text);
            case "v1_9_R1": return new de.epiceric.shopchest.shop.hologram.v1_9_R1.HologramLine(location, text);
            case "v1_9_R2": return new de.epiceric.shopchest.shop.hologram.v1_9_R2.HologramLine(location, text);
            case "v1_10_R1": return new de.epiceric.shopchest.shop.hologram.v1_10_R1.HologramLine(location, text);
            case "v1_11_R1": return new de.epiceric.shopchest.shop.hologram.v1_11_R1.HologramLine(location, text);
            case "v1_12_R1": return new de.epiceric.shopchest.shop.hologram.v1_12_R1.HologramLine(location, text);
            case "v1_13_R1": return new de.epiceric.shopchest.shop.hologram.v1_13_R1.HologramLine(location, text);
            case "v1_13_R2": return new de.epiceric.shopchest.shop.hologram.v1_13_R2.HologramLine(location, text);
            case "v1_14_R1": return new de.epiceric.shopchest.shop.hologram.v1_14_R1.HologramLine(location, text);
            case "v1_15_R1": return new de.epiceric.shopchest.shop.hologram.v1_15_R1.HologramLine(location, text);
            default: throw new IllegalStateException("Invalid server version: " + NmsUtil.getServerVersion());
        }
    }
    
    static IHologramItem newHologramItem(Location location, ItemStack itemStack) {
        switch (NmsUtil.getServerVersion()) {
            case "v1_8_R1": return new de.epiceric.shopchest.shop.hologram.v1_8_R1.HologramItem(location, itemStack);
            case "v1_8_R2": return new de.epiceric.shopchest.shop.hologram.v1_8_R2.HologramItem(location, itemStack);
            case "v1_8_R3": return new de.epiceric.shopchest.shop.hologram.v1_8_R3.HologramItem(location, itemStack);
            case "v1_9_R1": return new de.epiceric.shopchest.shop.hologram.v1_9_R1.HologramItem(location, itemStack);
            case "v1_9_R2": return new de.epiceric.shopchest.shop.hologram.v1_9_R2.HologramItem(location, itemStack);
            case "v1_10_R1": return new de.epiceric.shopchest.shop.hologram.v1_10_R1.HologramItem(location, itemStack);
            case "v1_11_R1": return new de.epiceric.shopchest.shop.hologram.v1_11_R1.HologramItem(location, itemStack);
            case "v1_12_R1": return new de.epiceric.shopchest.shop.hologram.v1_12_R1.HologramItem(location, itemStack);
            case "v1_13_R1": return new de.epiceric.shopchest.shop.hologram.v1_13_R1.HologramItem(location, itemStack);
            case "v1_13_R2": return new de.epiceric.shopchest.shop.hologram.v1_13_R2.HologramItem(location, itemStack);
            case "v1_14_R1": return new de.epiceric.shopchest.shop.hologram.v1_14_R1.HologramItem(location, itemStack);
            case "v1_15_R1": return new de.epiceric.shopchest.shop.hologram.v1_15_R1.HologramItem(location, itemStack);
            default: throw new IllegalStateException("Invalid server version: " + NmsUtil.getServerVersion());
        }
    }
}