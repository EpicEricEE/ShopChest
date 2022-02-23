package de.epiceric.shopchest.language;

import org.bukkit.Material;

public class ItemName {

    private Material material;
    private int subId;
    private String localizedName;

    public ItemName(Material material, String localizedName) {
        this(material, 0, localizedName);
    }

    public ItemName(Material material, int subId, String localizedName) {
        this.material = material;
        this.subId = subId;
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
    public int getSubId() {
        return subId;
    }

    /**
     * @return Name linked to the item
     */
    public String getLocalizedName() {
        return localizedName;
    }

}
