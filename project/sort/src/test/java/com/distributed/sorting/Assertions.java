package com.distributed.sorting;

/**
 * Provides methods that are designed to throw an {@link AssertionError}
 * if the provided arguments are invalid.
 */
public class Assertions {

    /**
     * Confirm that the provided array is in sorted order, with
     * lower-valued elements on the left-hand side of the array,
     * and higher-valued elements on the right-hand side.
     *
     * @param source The array that will have its order checked.
     * @throws AssertionError if the provided array is not sorted.
     */
    public static void assertAscending(int[] source) {
        assertSorted(source, ISortCondition.ASCENDING);
    }

    /**
     * Confirm that the provided array is in sorted order, with
     * higher-valued elements on the left-hand side of the array,
     * and lower-valued elements on the right-hand side.
     *
     * @param source The array that will have its order checked.
     * @throws AssertionError if the provided array is not sorted.
     */
    public static void assertDescending(int[] source) {
        assertSorted(source, ISortCondition.DESCENDING);
    }

    /**
     * Confirms that the provided array is in sorted order, as
     * specified by the provided {@link ISortCondition}.
     *
     * @param source    The array that will have its order checked.
     * @param condition The {@link ISortCondition} that specifies
     *                  the relationship between adjacent array
     *                  elements.
     * @throws AssertionError if the provided array is not sorted.
     */
    public static void assertSorted(int[] source, ISortCondition condition) {
        if (source.length <= 1) {
            return;
        }

        int previous;
        if (condition == ISortCondition.ASCENDING) {
            previous = Integer.MIN_VALUE;
        } else if (condition == ISortCondition.DESCENDING) {
            previous = Integer.MAX_VALUE;
        } else {
            final String message = "An illegal ISortCondition was provided.";
            throw new IllegalArgumentException(message);
        }

        for (int current : source) {
            assert condition.isValid(previous, current);
            previous = current;
        }
    }

}
