package de.epiceric.shopchest.api.exceptions;

import org.bukkit.Location;

public class NoSpaceAboveChestException extends Exception {
    private static final long serialVersionUID = 3718475607700458355L;

    public NoSpaceAboveChestException(Location location) {
        super(String.format("No space above chest in world '%s' at x=%d y=%d z=%d", location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
