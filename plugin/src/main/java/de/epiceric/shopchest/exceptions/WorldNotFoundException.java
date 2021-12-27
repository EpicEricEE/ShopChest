package de.epiceric.shopchest.exceptions;

public class WorldNotFoundException extends Exception {
    private static final long serialVersionUID = -555886332156936972L;

    public WorldNotFoundException(String worldName) {
        super("Could not find world with name \"" + worldName + "\"");
    }
}
