package de.epiceric.shopchest.utils;

/**
 * Represents a counter for integers greather than or equal to zero.
 */
public final class Counter {
    private int value;

    /**
     * Creates a counter with a starting value of zero
     */
    public Counter() {
        this(0);
    }

    /**
     * Creates a counter with the given starting value
     * @param value the starting value of this counter
     */
    public Counter(int value) {
        set(value);
    }

    /**
     * Increments the counter by one and returns itself
     */
    public final Counter increment() {
        this.value++;
        return this;
    }

    /**
     * Decrements the counter by one if its value is greater than zero and returns itself
     */
    public final Counter decrement() {
        this.value = Math.max(0, this.value - 1);
        return this;
    }

    /**
     * Sets the counter's value to the given value or zero if the given value is negative
     * @param value the value to set the counter to
     */
    public final Counter set(int value) {
        this.value = Math.max(0, value);
        return this;
    }

    /**
     * Returns the current value
     */
    public final int get() {
        return value;
    }
}