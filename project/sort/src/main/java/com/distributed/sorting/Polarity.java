package com.distributed.sorting;

/**
 * Represents the polarity of a data set that is to be sorted. The {@link #shouldSwap(int, int)}
 * method can be used to determine if 2 elements in the data set should be swapped.
 */
@SuppressWarnings("WeakerAccess")
public class Polarity implements ISwapDecision {

    public static Polarity ASCENDING = new Polarity(true);
    public static Polarity DESCENDING = new Polarity(false);

    /**
     * Create a new {@link Polarity} object.
     *
     * @param higherOnRight {@code true} if the data set is to be sorted with
     *                      the lower-valued elements on the left, {@code false}
     *                      if the data set is to be sorted with these elements
     *                      on the right.
     */
    private Polarity(boolean higherOnRight) {
        mHigherOnRight = higherOnRight;
    }

    private final boolean mHigherOnRight;

    @Override
    public boolean shouldSwap(int left, int right) {
        if (left <= right) {
            return !mHigherOnRight;
        }
        return mHigherOnRight;
    }

}
