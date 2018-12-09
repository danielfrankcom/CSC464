package com.distributed.bitonic;

import com.distributed.common.MathUtils;
import com.distributed.events.EventNotifier;
import com.distributed.events.IEventNotifier;
import com.distributed.sorting.IElementSwapper;
import com.distributed.sorting.ISwapDecision;
import com.distributed.sorting.Polarity;

/**
 * Represents an {@link IElementSwapper} implementation that performs
 * a single pass on the data, and then uses an {@link IEventNotifier}
 * to signal listeners of the remaining recursive steps.
 * <p>
 * Each step of the recursion splits the swapping window into 2, which
 * performs a single pass on each half of the window. One the window
 * reaches a size of 1 element, the recursion stops.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class RecursiveElementSwapper implements IElementSwapper {

    /**
     * Create a new {@link RecursiveElementSwapper}.
     *
     * @param data The array of {@link Integer} primitives to operate on.
     */
    public RecursiveElementSwapper(int[] data) {
        this(data, Polarity.ASCENDING);
    }

    /**
     * Create a new {@link RecursiveElementSwapper}.
     *
     * @param data         The array of {@link Integer} primitives to operate on.
     * @param swapDecision The {@link ISwapDecision} implementation that specifies
     *                     whether or not the compared elements should be swapped.
     */
    public RecursiveElementSwapper(int[] data, ISwapDecision swapDecision) {
        this(data, 0, data.length, swapDecision);
    }

    /**
     * Create a new {@link RecursiveElementSwapper}.
     *
     * @param data         The array of {@link Integer} primitives to operate on.
     * @param start        The inclusive start of the range to be operated on.
     * @param stop         The exclusive start of the range to be operated on.
     * @param swapDecision The {@link ISwapDecision} implementation that specifies
     *                     whether or not the compared elements should be swapped.
     */
    public RecursiveElementSwapper(int[] data, int start, int stop, ISwapDecision swapDecision) {
        mExecutor = new SinglePassElementSwapper(data, start, stop, swapDecision);

        final int diff = stop - start;
        // 1 is a valid power of 2, and would result in 0 comparisons.
        assert MathUtils.isPowerOfTwo(diff);

        /*
         * Recursing when 1 element will be in each group is
         * pointless, as a list of 1 element is already sorted.
         */
        if (diff > 2) {
            mEventNotifier = new EventNotifier<>(2);
        } else {
            mEventNotifier = new EventNotifier<>(0);
        }

        /*
         * Cache the parameters as fields so that recursive objects
         * can be created lazily on a call to execute(). This is to
         * prevent a recursive explosion of object instantiation, as
         * each object created here would also create all of its
         * recursive objects and so forth.
         */
        mData = data;
        mStart = start;
        mStop = stop;
        mSwapDecision = swapDecision;
    }

    private final IElementSwapper mExecutor;
    private final EventNotifier<RecursiveElementSwapper> mEventNotifier;

    private final int[] mData;
    private final int mStart;
    private final int mStop;
    private final ISwapDecision mSwapDecision;

    /**
     * @return The {@link IEventNotifier} that will be notified when new
     * recursion steps are available.
     */
    public IEventNotifier<RecursiveElementSwapper> getEventNotifier() {
        return mEventNotifier;
    }

    @Override
    public void execute() {
        // Perform the single pass associated with this object.
        mExecutor.execute();

        // If there are recursion steps are available, notify listeners.
        if (mEventNotifier.getExpected() != 0) {
            final int currentWindowSize = mStop - mStart;
            final int nextWindowSize = currentWindowSize / 2;
            final int windowCenter = mStart + nextWindowSize;

            final RecursiveElementSwapper firstHalf = new RecursiveElementSwapper(
                    mData, mStart, windowCenter, mSwapDecision
            );
            mEventNotifier.signal(firstHalf);

            final RecursiveElementSwapper secondHalf = new RecursiveElementSwapper(
                    mData, windowCenter, mStop, mSwapDecision
            );
            mEventNotifier.signal(secondHalf);
        }
    }

}
