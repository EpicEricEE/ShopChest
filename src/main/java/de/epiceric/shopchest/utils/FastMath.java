package de.epiceric.shopchest.utils;

public class FastMath {

    /**
     * Fast sqrt, 1.57% precision
     *
     * @param n value to calculate square root from
     * @return the square root of n
     */
    public static double sqrt(double n) {
        return n * Double.longBitsToDouble(6910470738111508698L - (Double.doubleToRawLongBits(n) >> 1));
    }

    /**
     * Fast acos, 2.9% precision
     *
     * @param n value to calculate arc cosine from
     * @return the arc cosine of n
     */
    public static double acos(double n) {
        int v = (int) (n * MULTIPLIER + OFFSET);
        while (v > PRECISION) v -= PRECISION;
        while (v < 0) v += PRECISION;
        return acos[v];
    }

    // Below is lookup table generation
    // It is only executed once at initialization

    private static final int PRECISION = 512;
    private static final double MULTIPLIER = PRECISION / 2D;
    private static final double OFFSET = MULTIPLIER + 0.5D; // + 0.5 as cast truncate and don't round
    private static final double[] acos = new double[PRECISION + 1];

    static {
        for (int i = 0; i <= PRECISION; i++) {
            acos[i] = Math.acos(i * (2D / PRECISION) - 1);
        }
    }

}
