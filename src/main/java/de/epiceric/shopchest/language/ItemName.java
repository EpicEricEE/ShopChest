package de.epiceric.shopchest.language;

import org.bukkit.Material;

public class ItemName {

    private Material material;
    private String localizedName;

    public ItemName(Material material, String localizedName) {
        this.material = material;
        this.localizedName = localizedName;
    }

    /**
     * @return Material linked to the name
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @return Name linked to the item
     */
    public String getLocalizedName() {
        return localizedName;
    }

}
