package de.epiceric.shopchest.language;

import de.epiceric.shopchest.config.Placeholder;

public class Replacement {

    private Placeholder placeholder;
    private String replacement;

    public Replacement(Placeholder placeholder, String replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    /**
     * @return String which will replace the placeholder
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * @return Placeholder that will be replaced
     */
    public Placeholder getPlaceholder() {
        return placeholder;
    }

}
