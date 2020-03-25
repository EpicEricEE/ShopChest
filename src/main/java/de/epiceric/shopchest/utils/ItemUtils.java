package de.epiceric.shopchest.utils;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class ItemUtils {

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            return esm.getStoredEnchants();
        } else {
            return itemStack.getEnchantments();
        }
    }

    public static PotionType getPotionEffect(ItemStack itemStack) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {    
            if (Utils.getMajorVersion() < 9) {
                return Potion.fromItemStack(itemStack).getType();
            } else {
                return ((PotionMeta) itemStack.getItemMeta()).getBasePotionData().getType();
            }
        }

        return null;
    }

    public static boolean isExtendedPotion(ItemStack itemStack) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            if (Utils.getMajorVersion() < 9) {
                return Potion.fromItemStack(itemStack).hasExtendedDuration();
            } else {
                return ((PotionMeta) itemStack.getItemMeta()).getBasePotionData().isExtended();
            }
        }

        return false;
    }

    public static boolean isBannerPattern(ItemStack itemStack) {
        return itemStack.getType().name().endsWith("BANNER_PATTERN");
    }

    public static boolean isAir(Material type) {
        return Arrays.asList("AIR", "CAVE_AIR", "VOID_AIR").contains(type.name());
    }

    /**
     * Get the {@link ItemStack} from a String
     * @param item Serialized ItemStack e.g. {@code "STONE"} or {@code "STONE:1"}
     * @return The de-serialized ItemStack or {@code null} if the serialized item is invalid
     */
    public static ItemStack getItemStack(String item) {
        if (item.trim().isEmpty()) return null;

        if (item.contains(":")) {
            Material mat = Material.getMaterial(item.split(":")[0]);
            if (mat == null) return null;
            return new ItemStack(mat, 1, Short.parseShort(item.split(":")[1]));
        } else {
            Material mat = Material.getMaterial(item);
            if (mat == null) return null;
            return new ItemStack(mat, 1);
        }
    }

}
