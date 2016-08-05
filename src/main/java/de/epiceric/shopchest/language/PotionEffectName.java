package de.epiceric.shopchest.language;

import org.bukkit.potion.PotionType;

public class PotionEffectName {

    private PotionType effect;
    private String localizedName;

    public PotionEffectName(PotionType effect, String localizedName) {
        this.effect = effect;
        this.localizedName = localizedName;
    }

    /**
     * @return Potion Effect linked to the name
     */
    public PotionType getEffect() {
        return effect;
    }

    /**
     * @return Localized Name of the potion effect
     */
    public String getLocalizedName() {
        return localizedName;
    }

}
