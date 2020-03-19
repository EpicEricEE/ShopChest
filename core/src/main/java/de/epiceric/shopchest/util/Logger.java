package de.epiceric.shopchest.util;

import java.util.logging.Level;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("ShopChest");

    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public void info(Throwable throwable) {
        log(Level.INFO, throwable);
    }
    
    public void warning(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    public void warning(Throwable throwable) {
        log(Level.WARNING, throwable);
    }

    public void severe(String message, Object... args) {
        log(Level.SEVERE, message, args);
    }

    public void severe(Throwable throwable) {
        log(Level.SEVERE, throwable);
    }

    public void log(Level logLevel, String message, Object... args) {
        if (args.length > 0) {
            LOGGER.log(logLevel, message, args);
        } else {
            LOGGER.log(logLevel, message);
        }
    }

    public void log(Level logLevel, Throwable throwable) {
        LOGGER.log(logLevel, throwable.getMessage(), throwable);
    }
}