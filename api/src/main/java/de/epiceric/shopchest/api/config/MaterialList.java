package de.epiceric.shopchest.api.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Material;

/**
 * A wrapped for {@code List<Material>}, optimised for config serialization
 * 
 * @since 2.0
 */
public class MaterialList {
    private List<Material> list;

    /**
     * Creates a list with the given list's entries
     * 
     * @param list the list to wrap
     * @since 2.0
     */
    public MaterialList(List<Material> list) {
        this.list = list;
    }

    /**
     * Creates a list with the given materials
     * 
     * @param materials the materials
     * @since 2.0
     */
    public MaterialList(Material... materials) {
        this(Arrays.asList(materials));
    }

    /**
     * Parses a comma seperated list of materials
     * <p>
     * The materials are not case sensitive.
     * 
     * @param str the comma seperated list
     * @return the parsed list
     * @since 2.0
     */
    public static MaterialList valueOf(String str) {
        if (str.isEmpty()) {
            return new MaterialList();
        }

        if (str.contains(",")) {
            return new MaterialList(Arrays.stream(str.split("\\,"))
                    .map(entry -> Material.valueOf(entry.toUpperCase(Locale.US))).collect(Collectors.toList()));
        }

        return new MaterialList(Material.valueOf(str.toUpperCase(Locale.US)));
    }

    @Override
    public String toString() {
        return list.stream().map(mat -> mat.name().toLowerCase(Locale.US)).collect(Collectors.joining(","));
    }

    /**
     * Gets an instance of the list
     * 
     * @return the list
     * @since 2.0
     */
    public List<Material> getList() {
        return list;
    }
}