package de.epiceric.shopchest.language;

import org.bukkit.Material;

public class BannerPatternName {

    private Material bannerPatternMaterial;
    private String localizedName;

    public BannerPatternName(Material bannerPatternMaterial, String localizedName) {
        this.bannerPatternMaterial = bannerPatternMaterial;
        this.localizedName = localizedName;
    }

    /**
     * @return Localized Name of the Banner Pattern
     */
    public String getLocalizedName() {
        return localizedName;
    }

    /**
     * @return Material of the Banner Pattern
     */
    public Material getBannerPatternMaterial() {
        return bannerPatternMaterial;
    }

}
