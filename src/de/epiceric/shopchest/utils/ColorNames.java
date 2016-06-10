package de.epiceric.shopchest.utils;

import com.google.common.collect.ImmutableMap;
import org.bukkit.DyeColor;

import java.util.Map;

public class ColorNames {

    private static final Map<DyeColor, String> map = ImmutableMap.<DyeColor, String>builder()
            .put(DyeColor.BLACK, "Black")
            .put(DyeColor.BLUE, "Blue")
            .put(DyeColor.BROWN, "Brown")
            .put(DyeColor.CYAN, "Cyan")
            .put(DyeColor.GRAY, "Light Gray")
            .put(DyeColor.GREEN, "Green")
            .put(DyeColor.LIGHT_BLUE, "Light Blue")
            .put(DyeColor.LIME, "Lime")
            .put(DyeColor.MAGENTA, "Magenta")
            .put(DyeColor.ORANGE, "Orange")
            .put(DyeColor.PINK, "Pink")
            .put(DyeColor.PURPLE, "Purple")
            .put(DyeColor.RED, "Red")
            .put(DyeColor.SILVER, "Gray")
            .put(DyeColor.WHITE, "White")
            .put(DyeColor.YELLOW, "Yellow")
            .build();

    public static String getColorString(DyeColor dyeColor) {
        return map.get(dyeColor);
    }

}
