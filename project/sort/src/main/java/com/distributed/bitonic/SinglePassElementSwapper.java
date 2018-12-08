package com.distributed.bitonic;

import com.distributed.common.MathUtils;
import com.distributed.sorting.ISwapDecision;
import com.distributed.sorting.IElementSwapper;
import com.distributed.sorting.Polarity;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents an {@link IElementSwapper} implementation that takes a single
 * pass over the data and performs a swapping operation based on a provided
 * range and {@link ISwapDecision}.
 */
@SuppressWarnings("WeakerAccess")
public class SinglePassElementSwapper implements IElementSwapper {

    /**
     * Create a new {@link SinglePassElementSwapper}.
     *
     * @param data         The array of {@link Integer} primitives to operate on.
     */
    public SinglePassElementSwapper(int[] data) {
        this(data, Polarity.ASCENDING);
    }

    /**
     * Create a new {@link SinglePassElementSwapper}.
     *
     * @param data         The array of {@link Integer} primitives to operate on.
     * @param swapDecision The {@link ISwapDecision} implementation that specifies
     *                     whether or not the compared elements should be swapped.
     */
    public SinglePassElementSwapper(int[] data, ISwapDecision swapDecision) {
        this(data, 0, data.length, swapDecision);
    }

    /**
     * Create a new {@link SinglePassElementSwapper}.
     *
     * @param data         The array of {@link Integer} primitives to operate on.
     * @param start        The inclusive start of the range to be operated on.
     * @param stop         The exclusive start of the range to be operated on.
     * @param swapDecision The {@link ISwapDecision} implementation that specifies
     *                     whether or not the compared elements should be swapped.
     */
    public SinglePassElementSwapper(int[] data, int start, int stop, ISwapDecision swapDecision) {
        assert data != null;
        mData = data;

        assert start >= 0;
        assert start < stop;

        final int diff = stop - start;
        // 1 is a valid power of 2, and would result in 0 comparisons.
        assert MathUtils.isPowerOfTwo(diff);

        mStart = start;
        mComparisons = diff / 2;

        assert swapDecision != null;
        mSwapDecision = swapDecision;
    }

    private final int[] mData;
    private final int mStart;
    private final int mComparisons;
    private final ISwapDecision mSwapDecision;

    @Override
    public void swap() {

        for (int i = mStart; i < mStart + mComparisons; i++) {

            //noinspection UnnecessaryLocalVariable
            final int index_left = i;
            final int index_right = i + mComparisons;
            final int left = mData[index_left];
            final int right = mData[index_right];

            if (mSwapDecision.shouldSwap(left, right)) {
                ArrayUtils.swap(mData, index_left, index_right);
            }
        }

    }

}
