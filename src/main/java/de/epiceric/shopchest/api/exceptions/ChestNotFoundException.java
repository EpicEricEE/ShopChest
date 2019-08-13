package de.epiceric.shopchest.api.exceptions;

import org.bukkit.Location;

public class ChestNotFoundException extends Exception {
    private static final long serialVersionUID = -6446875473671870708L;

    public ChestNotFoundException(Location location) {
        super(String.format("No chest found in world '%s' at x=%d y=%d z=%d", location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
