package de.epiceric.shopchest.language;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentName {

    private Enchantment enchantment;
    private String localizedName;

    public EnchantmentName(Enchantment enchantment, String localizedName) {
        this.enchantment = enchantment;
        this.localizedName = localizedName;
    }

    /**
     * @return Enchantment linked to the name
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * @return Name linked to the enchantment
     */
    public String getLocalizedName() {
        return localizedName;
    }

    public static class EnchantmentLevelName {
        private int level;
        private String localizedName;

        public EnchantmentLevelName(int level, String localizedName) {
            this.level = level;
            this.localizedName = localizedName;
        }

        /**
         * @return Level linked to the name
         */
        public int getLevel() {
            return level;
        }

        /**
         * @return Name linked to the level
         */
        public String getLocalizedName() {
            return localizedName;
        }
    }

}
