package de.epiceric.shopchest.api.config;

import java.util.Arrays;
import java.util.List;

/**
 * A wrapped for {@code List<String>}, optimised for config serialization
 * 
 * @since 1.13
 */
public class StringList {
    private List<String> list;

    /**
     * Creates a list with the given list's entries
     * 
     * @param list the list to wrap
     * @since 1.13
     */
    public StringList(List<String> list) {
        this.list = list;
    }

    /**
     * Creates a list with the given strings
     * 
     * @param strings the strings
     * @since 1.13
     */
    public StringList(String... strings) {
        this(Arrays.asList(strings));
    }

    /**
     * Parses a comma seperated list of strings
     * 
     * @param str the comma seperated list
     * @return the parsed list
     * @since 1.13
     */
    public static StringList valueOf(String str) {
        if (str.isEmpty()) {
            return new StringList();
        }

        if (str.contains(",")) {
            return new StringList((str.split("\\,")));
        }

        return new StringList(str);
    }

    @Override
    public String toString() {
        return String.join(",", list);
    }

    /**
     * Gets an instance of the list
     * 
     * @return the list
     * @since 1.13
     */
    public List<String> getList() {
        return list;
    }
}