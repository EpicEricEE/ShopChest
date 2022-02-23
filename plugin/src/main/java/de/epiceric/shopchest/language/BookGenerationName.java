package de.epiceric.shopchest.language;

import de.epiceric.shopchest.nms.CustomBookMeta;

public class BookGenerationName {

    private String localizedName;
    private CustomBookMeta.Generation generation;

    public BookGenerationName(CustomBookMeta.Generation generation, String localizedName) {
        this.generation = generation;
        this.localizedName = localizedName;
    }

    /**
     * @return Generation linked to the name
     */
    public CustomBookMeta.Generation getGeneration() {
        return generation;
    }

    /**
     * @return Name linked to the book generation
     */
    public String getLocalizedName() {
        return localizedName;
    }
}
