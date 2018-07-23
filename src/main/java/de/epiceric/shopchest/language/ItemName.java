package de.epiceric.shopchest.language;

import org.bukkit.Material;

public class ItemName {

    private Material material;
    private int subID;
    private String localizedName;

    @Deprecated // Deprecated for 1.13
    public ItemName(Material material, int subID, String localizedName) {
        this.material = material;
        this.subID = subID;
        this.localizedName = localizedName;
    }

    public ItemName(Material material, String localizedName) {
        this.material = material;
        this.subID = 0;
        this.localizedName = localizedName;
    }

    /**
     * @return Material linked to the name
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @return Sub ID linked to the name
     */
    public int getSubID() {
        return subID;
    }

    /**
     * @return Name linked to the item
     */
    public String getLocalizedName() {
        return localizedName;
    }

}
