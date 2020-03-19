package de.epiceric.shopchest.util;

import java.util.logging.Level;

public class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("ShopChest");

    private Logger() {
    }

    public static void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public static void info(Throwable throwable) {
        log(Level.INFO, throwable);
    }
    
    public static void warning(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    public static void warning(Throwable throwable) {
        log(Level.WARNING, throwable);
    }

    public static void severe(String message, Object... args) {
        log(Level.SEVERE, message, args);
    }

    public static void severe(Throwable throwable) {
        log(Level.SEVERE, throwable);
    }

    public static void log(Level logLevel, String message, Object... args) {
        if (args.length > 0) {
            LOGGER.log(logLevel, message, args);
        } else {
            LOGGER.log(logLevel, message);
        }
    }

    public static void log(Level logLevel, Throwable throwable) {
        LOGGER.log(logLevel, throwable.getMessage(), throwable);
    }
}