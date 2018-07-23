package de.epiceric.shopchest.language;

import org.bukkit.inventory.meta.BookMeta;

public class BookGenerationName {

    private String localizedName;
    private BookMeta.Generation generation;

    public BookGenerationName(BookMeta.Generation generation, String localizedName) {
        this.generation = generation;
        this.localizedName = localizedName;
    }

    /**
     * @return Generation linked to the name
     */
    public BookMeta.Generation getGeneration() {
        return generation;
    }

    /**
     * @return Name linked to the book generation
     */
    public String getLocalizedName() {
        return localizedName;
    }
}
