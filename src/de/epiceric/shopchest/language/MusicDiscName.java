package de.epiceric.shopchest.language;

import org.bukkit.Material;

public class MusicDiscName {

    private Material musicDiscMaterial;
    private String localizedName;

    public MusicDiscName(Material musicDiscMaterial, String localizedName) {
        this.musicDiscMaterial = musicDiscMaterial;
        this.localizedName = localizedName;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public Material getMusicDiscMaterial() {
        return musicDiscMaterial;
    }

}
