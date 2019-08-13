package de.epiceric.shopchest.language;

import org.bukkit.potion.PotionEffectType;

public class PotionEffectName {

    private PotionEffectType effect;
    private String localizedName;

    public PotionEffectName(PotionEffectType effect, String localizedName) {
        this.effect = effect;
        this.localizedName = localizedName;
    }

    /**
     * @return Potion Effect linked to the name
     */
    public PotionEffectType getEffect() {
        return effect;
    }

    /**
     * @return Localized Name of the potion effect
     */
    public String getLocalizedName() {
        return localizedName;
    }

}
