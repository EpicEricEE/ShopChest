package de.epiceric.shopchest.language;

import org.bukkit.potion.PotionType;

public class PotionEffectName {

    private PotionType effect;
    private String localizedName;

    public PotionEffectName(PotionType effect, String localizedName) {
        this.effect = effect;
        this.localizedName = localizedName;
    }


    public PotionType getEffect() {
        return effect;
    }

    public String getLocalizedName() {
        return localizedName;
    }

}
