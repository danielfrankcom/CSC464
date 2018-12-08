package com.distributed.common;

/**
 * Provides utilities for generating primitive types.
 */
public class PrimitiveUtils {

    /**
     * Creates an array of {@link Integer} primitives where the content is
     * an ordered list made up of the numbers within the provided range
     * bounds.
     * <p>
     * i.e. [fromInclusive, fromInclusive + 1, .., toInclusive - 1, toInclusive]
     * </p>
     *
     * @param fromInclusive The inclusive start of the range.
     * @param toInclusive   The inclusive end of the range.
     * @return The generated array.
     */
    public static int[] range(int fromInclusive, int toInclusive) {
        assert fromInclusive <= toInclusive;

        final int size = Math.abs(toInclusive - fromInclusive) + 1;
        final int[] result = new int[size];

        int index = 0;
        for (int value = fromInclusive; value <= toInclusive; value++) {
            result[index] = value;
            index++;
        }

        return result;
    }

}
