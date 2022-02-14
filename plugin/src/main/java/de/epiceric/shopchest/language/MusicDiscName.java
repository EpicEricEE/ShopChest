package de.epiceric.shopchest.language;

import org.bukkit.Material;

public class MusicDiscName {

    private Material musicDiscMaterial;
    private String localizedName;

    public MusicDiscName(Material musicDiscMaterial, String localizedName) {
        this.musicDiscMaterial = musicDiscMaterial;
        this.localizedName = localizedName;
    }

    /**
     * @return Localized Title of the Music Disc
     */
    public String getLocalizedName() {
        return localizedName;
    }

    /**
     * @return Material of the Music Disc
     */
    public Material getMusicDiscMaterial() {
        return musicDiscMaterial;
    }

}
