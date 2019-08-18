package de.epiceric.shopchest.api.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;

/**
 * A wrapper for {@code Map<Material, Double>}, optimised for config
 * serialization
 * 
 * @since 1.13
 */
public class MaterialDoubleMap {
    private Map<Material, Double> map;

    /**
     * Creates a map with the given map's entries
     * 
     * @param map the map to wrap
     * @since 1.13
     */
    public MaterialDoubleMap(Map<Material, Double> map) {
        this.map = map;
    }

    /**
     * Creates an empty map
     * 
     * @since 1.13
     */
    public MaterialDoubleMap() {
        this(new EnumMap<>(Material.class));
    }

    /**
     * Parses a comma seperated list of {@code <Material, Double>} map entries
     * <p>
     * Format: {@code a=b,c=d,e=f}
     * <p>
     * The materials are not case sensitive.
     * 
     * @param str the comma seperated list of map entries
     * @return the parsed map
     * @since 1.13
     */
    public static MaterialDoubleMap valueOf(String str) {
        if (str.isEmpty()) {
            return new MaterialDoubleMap(Collections.emptyMap());
        }

        Map<Material, Double> ret = new EnumMap<>(Material.class);

        if (str.contains(",")) {
            Arrays.stream(str.split("\\,")).forEach(entry -> {
                String[] spl = entry.split("\\=");
                Material key = Material.valueOf(spl[0].toUpperCase(Locale.US));
                double value = Double.parseDouble(spl[1]);
                ret.put(key, value);
            });
        } else {
            String[] spl = str.split("\\=");
            Material key = Material.valueOf(spl[0].toUpperCase(Locale.US));
            double value = Double.parseDouble(spl[1]);
            ret.put(key, value);
        }

        return new MaterialDoubleMap(ret);
    }

    @Override
    public String toString() {
        return map.entrySet().stream().map(entry -> {
            String key = entry.getKey().name().toLowerCase(Locale.US);
            double val = entry.getValue();
            return key + "=" + ((val == (int) val) ? String.valueOf((int) val) : String.valueOf(val));
        }).collect(Collectors.joining(","));
    }

    /**
     * Gets an instance of the map
     * 
     * @return the map
     * @since 1.13
     */
    public Map<Material, Double> getMap() {
        return map;
    }
}