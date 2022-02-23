package de.epiceric.shopchest.exceptions;

public class NotEnoughSpaceException extends Exception {
    private static final long serialVersionUID = 3718475607700458355L;

    public NotEnoughSpaceException(String message) {
        super(message);
    }
}
