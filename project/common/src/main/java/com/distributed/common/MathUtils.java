package com.distributed.common;

/**
 * Provides utilities for mathematical calculations.
 */
public class MathUtils {

    /**
     * Checks whether or not a number is a power of 2.
     *
     * @param num The {@link Integer} to check. Must be greater than 0.
     * @return {@code true} if the number is a power of 2, {@code false}
     * otherwise.
     */
    public static boolean isPowerOfTwo(int num) {
        return num > 0 && (num & (num - 1)) == 0;
    }

    /**
     * Performs integer division where the result is rounded up rather than
     * down in the case that {@code numerator % denominator != 0}. Avoids
     * floating point arithmetic.
     *
     * @param numerator   The {@link Integer} representing the numerator.
     * @param denominator The {@link Integer} representing the denominator.
     * @return The result of the division.
     */
    public static int ceilingDivide(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

}
