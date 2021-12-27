package de.epiceric.shopchest.language;

import org.bukkit.potion.PotionType;

public class PotionName {

    private String localizedName;
    private PotionItemType potionItemType;
    private PotionType potionType;

    public PotionName(PotionItemType potionItemType, PotionType potionType, String localizedName) {
        this.potionItemType = potionItemType;
        this.localizedName = localizedName;
        this.potionType = potionType;
    }

    /**
     * @return {@link PotionItemType} linked to the Potion name
     */
    public PotionItemType getPotionItemType() {
        return potionItemType;
    }

    /**
     * @return Potion Type linked to the Potion name
     */
    public PotionType getPotionType() {
        return potionType;
    }

    /**
     * @return Localized Name of the Potion
     */
    public String getLocalizedName() {
        return localizedName;
    }

    public enum PotionItemType {
        POTION,
        LINGERING_POTION,
        SPLASH_POTION,
        TIPPED_ARROW
    }
}
