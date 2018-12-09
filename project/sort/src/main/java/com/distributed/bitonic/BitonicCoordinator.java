package com.distributed.bitonic;

import com.distributed.common.MathUtils;
import com.distributed.sorting.ISwapDecision;
import com.distributed.sorting.Polarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;


/**
 * Represents a coordinating object that knows how to produce {@link RecursiveElementSwapper}
 * objects for handling each step of the bitonic sort algorithm.
 * <p>
 * The algorithm is made up of a series of recursive steps, where each step has a starting
 * window size that is recursively operated on and halved. The initial step has an initial
 * window size of 2, and as the algorithm progresses, the initial window size increases until
 * it reaches the length of the data set. In each of the steps, the recursive halving and
 * processing of the window stops when the window size reaches 1.
 * </p>
 * <p>
 * Additionally, each window has a polarity opposite to the previous, where there are an equal
 * number of each polarity in each recursive step of the algorithm. Recursive halves keep the
 * polarity of their parent halves.
 * </p>
 *
 * @see <a href="http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/bitonic/bitonicen.htm">this resource</a>
 */
@SuppressWarnings("WeakerAccess")
public class BitonicCoordinator implements Iterator<Collection<RecursiveElementSwapper>> {

    /**
     * Represents a {@link Supplier} that provides the next {@link Polarity} in
     * the bitonic sort algorithm.
     */
    private static class PolarityProvider implements Supplier<ISwapDecision> {

        /**
         * Create a new {@link Polarity} object.
         *
         * @param polarity The initial {@link Polarity} to provide. In the
         *                 bitonic sorting algorithm, the first {@link Polarity}
         *                 dictates the {@link Polarity} of the result.
         */
        PolarityProvider(Polarity polarity) {
            /*
             * We flip the polarity here as the first call to #get()
             * will flip it again, resulting in the original value.
             */
            if (polarity == Polarity.ASCENDING) {
                mCurrent = Polarity.DESCENDING;
            } else {
                mCurrent = Polarity.ASCENDING;
            }
        }

        private ISwapDecision mCurrent;

        @Override
        public ISwapDecision get() {
            final ISwapDecision newDecision;
            if (mCurrent == Polarity.ASCENDING) {
                newDecision = Polarity.DESCENDING;
            } else {
                newDecision = Polarity.ASCENDING;
            }

            mCurrent = newDecision;
            return newDecision;
        }

    }

    /**
     * Create a new {@link BitonicCoordinator} object.
     *
     * @param data The data to sort.
     */
    public BitonicCoordinator(int[] data) {
        this(data, Polarity.ASCENDING);
    }

    /**
     * Create a new {@link BitonicCoordinator} object.
     *
     * @param data     The data to sort. Must have a length that is a
     *                 power of 2, and must not be {@code null}.
     * @param polarity The desired {@link Polarity} of the result. Must
     *                 not be {@code null}.
     */
    public BitonicCoordinator(int[] data, Polarity polarity) {
        assert data != null;
        assert MathUtils.isPowerOfTwo(data.length);
        mData = data;

        assert polarity != null;
        mPolarityProvider = new PolarityProvider(polarity);
    }

    private final int[] mData;
    private final Supplier<ISwapDecision> mPolarityProvider;

    /*
     * Group sizes start at 2 and double until they reach
     * the length of the array.
     */
    private int mCurrentGroupSize = 2;

    @Override
    public boolean hasNext() {
        return mCurrentGroupSize <= mData.length;
    }

    /**
     * @return The next round of recursive bitonic sorting steps.
     */
    @Override
    public Collection<RecursiveElementSwapper> next() {

        final ArrayList<RecursiveElementSwapper> result = new ArrayList<>();
        for (int start = 0; start < mData.length; start += mCurrentGroupSize) {

            final int stop = start + mCurrentGroupSize;
            final ISwapDecision polarity = mPolarityProvider.get();

            final RecursiveElementSwapper swapper = new RecursiveElementSwapper(mData, start, stop, polarity);
            result.add(swapper);
        }

        mCurrentGroupSize *= 2;
        return result;
    }

}
