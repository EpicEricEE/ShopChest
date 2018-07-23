package de.epiceric.shopchest.utils;

import com.google.common.collect.Lists;
import de.epiceric.shopchest.nms.CustomBookMeta;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

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
            if (Utils.getMajorVersion() >= 9) {
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                return potionMeta.getBasePotionData().isExtended();
            } else {
                Potion potion = Potion.fromItemStack(itemStack);
                return potion.hasExtendedDuration();
            }
        }

        return false;
    }

    public static boolean isMusicDisc(ItemStack itemStack) {
        List<Material> musicDiscMaterials = Lists.newArrayList(
                Material.MUSIC_DISC_11, Material.MUSIC_DISC_13, Material.MUSIC_DISC_BLOCKS, Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD
        );

        return musicDiscMaterials.contains(itemStack.getType());
    }

    public static CustomBookMeta.Generation getBookGeneration(ItemStack itemStack) {
        if (itemStack.getType() == Material.WRITTEN_BOOK) {
            return CustomBookMeta.getGeneration(itemStack);
        }

        return null;
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
