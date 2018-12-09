package com.distributed.bitonic;

import com.distributed.common.MathUtils;
import com.distributed.events.EventQueue;
import com.distributed.sorting.Polarity;
import com.distributed.threading.BlockingExecutorService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Represents a {@link Runnable} that sorts an array using a thread pool
 * and the bitonic sorting algorithm.
 */
@SuppressWarnings("WeakerAccess")
public class BitonicExecutor implements Runnable {

    private static final int TIMEOUT_AMOUNT = 1;
    private static final TimeUnit TIMEOUT_UNITS = TimeUnit.MINUTES;

    /**
     * Create a new {@link BitonicExecutor} object.
     *
     * @param data The data to sort. Must have a length that is a
     *             power of 2, and must not be {@code null}.
     */
    public BitonicExecutor(int[] data) {
        this(1, data);
    }

    /**
     * Create a new {@link BitonicExecutor} object.
     *
     * @param numThreads The number of threads to create in the thread pool.
     * @param data       The data to sort. Must have a length that is a
     *                   power of 2, and must not be {@code null}.
     */
    public BitonicExecutor(int numThreads, int[] data) {
        this(numThreads, data, Polarity.ASCENDING);
    }

    /**
     * Create a new {@link BitonicExecutor} object.
     *
     * @param numThreads The number of threads to create in the thread pool.
     * @param data       The data to sort. Must have a length that is a
     *                   power of 2, and must not be {@code null}.
     * @param polarity   The desired {@link Polarity} of the result. Must
     *                   not be {@code null}.
     */
    public BitonicExecutor(int numThreads, int[] data, Polarity polarity) {
        assert numThreads > 0;
        mThreadPool = new BlockingExecutorService(numThreads);

        assert data != null;
        assert MathUtils.isPowerOfTwo(data.length);
        assert polarity != null;
        mCoordinator = new BitonicCoordinator(data, polarity);
    }

    private final ExecutorService mThreadPool;
    private final Iterator<Collection<RecursiveElementSwapper>> mCoordinator;

    @Override
    public void run() {

        while (mCoordinator.hasNext()) {

            final Collection<Future> results = new ArrayList<>();

            // Represents the initial round of swappers for the recursive step.
            final Collection<RecursiveElementSwapper> initial = mCoordinator.next();

            final EventQueue<RecursiveElementSwapper> queue = new EventQueue<>();
            for (RecursiveElementSwapper swapper : initial) {
                queue.registerNotifier(swapper.getEventNotifier());
                final Future result = mThreadPool.submit(swapper::execute);
                results.add(result);
            }

            // All recursive operations are signalled to the queue by the initial swappers.
            while (queue.hasRemaining()) {
                final RecursiveElementSwapper swapper = queue.get();
                queue.registerNotifier(swapper.getEventNotifier());
                final Future result = mThreadPool.submit(swapper::execute);
                results.add(result);
            }

            /*
             * RecursiveElementSwapper objects synchronize themselves by only providing
             * notifications when their current step has been executed, however it is also
             * necessary to synchronize between recursive rounds. Due to the fact that
             * execution is delegated to a thread pool, it is possible for the last few
             * steps of a recursive round to not be executed before the initial steps of
             * the next round, which breaks the bitonic sorting algorithm.
             *
             * Here we perform #get() on all of the results in order to ensure that their
             * execution has completed, and in order to cancel execution of this method by
             * throwing any exceptions that have occur asynchronously.
             */
            for (Future result : results) {
                try {
                    result.get();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

        }

        // Events that have already been submitted will not be cancelled.
        mThreadPool.shutdown();

        // Ensure that all tasks have finished before returning.
        boolean success = false;
        while (!success) {
            try {
                if (!mThreadPool.awaitTermination(TIMEOUT_AMOUNT, TIMEOUT_UNITS)) {
                    final String message = "Sort did not finish in a reasonable amount of time.";
                    throw new RuntimeException(message);
                }
                success = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
