package de.epiceric.shopchest.api.config;

/**
 * Represents a configurable property
 * 
 * @since 2.0
 */
public class Property<T> {
    private final Class<T> type;
    private final String section;
    private final String key;
    private T value;

    /* package-private */  Property(Class<T> type, String section, String key, T defaultValue) {
        this.type = type;
        this.section = section;
        this.key = key;
        this.value = defaultValue;
    }

    /**
     * Gets the section of this property
     * 
     * @return the section
     * @since 2.0
     */
    public String getSection() {
        return section;
    }

    /**
     * Gets the key of this property
     * 
     * @return the key
     * @since 2.0
     */
    public String getKey() {
        return section + "." + key;
    }

    /**
     * Gets the class of this property's type
     * 
     * @return the type
     * @since 2.0
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Gets the current value of this property
     * 
     * @return the value
     * @since 2.0
     */
    public T get() {
        return value;
    }

    /**
     * Sets the value of this property
     * <p>
     * The value is not stored until the config is saved by the plugin.
     * 
     * @param value the value
     * @throws IllegalArgumentException if {@code value} is null
     * @since 2.0
     */
    public void set(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Property value cannot be null");
        }
        this.value = value;
    }
}