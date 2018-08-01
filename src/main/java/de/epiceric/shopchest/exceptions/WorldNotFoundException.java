package de.epiceric.shopchest.exceptions;

public class WorldNotFoundException extends Exception {

    public WorldNotFoundException(String worldName) {
        super("Could not find world with name \"" + worldName + "\"");
    }

}
