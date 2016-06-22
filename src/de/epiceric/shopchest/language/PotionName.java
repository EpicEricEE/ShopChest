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

    public PotionItemType getPotionItemType() {
        return potionItemType;
    }

    public PotionType getPotionType() {
        return potionType;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public enum PotionItemType {
        POTION,
        LINGERING_POTION,
        SPLASH_POTION,
        TIPPED_ARROW;
    }
}
