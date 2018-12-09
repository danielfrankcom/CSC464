package com.distributed.bitonic;

import com.distributed.common.MathUtils;
import com.distributed.common.PrimitiveUtils;
import com.distributed.common.testing.TestUtils;
import com.distributed.events.EventQueue;
import com.distributed.sorting.Assertions;
import com.distributed.sorting.Polarity;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;


public class BitonicCoordinatorTest {

    private static final int ARRAY_SIZE = 512;
    private static final int ROUNDS = 30;

    private final Random mRandom = TestUtils.newRandom();

    /**
     * Sorts an array using the provided {@link Polarity} and a
     * {@link BitonicCoordinator} object.
     *
     * @param data     The data to sort. Must have a length that is a
     *                 power of 2, and must not be {@code null}.
     * @param polarity The {@link Polarity} that dictates how the data will be sorted.
     *                 Must not be {@code null.}
     */
    private static void sortArray(int[] data, Polarity polarity) {
        assert data != null;
        assert MathUtils.isPowerOfTwo(data.length);
        assert polarity != null;

        final Iterator<Collection<RecursiveElementSwapper>> coordinator = new BitonicCoordinator(data, polarity);
        while (coordinator.hasNext()) {

            // Represents the initial round of swappers for the recursive step.
            final Collection<RecursiveElementSwapper> initial = coordinator.next();

            final EventQueue<RecursiveElementSwapper> queue = new EventQueue<>();
            for (RecursiveElementSwapper swapper : initial) {
                queue.registerNotifier(swapper.getEventNotifier());
                swapper.execute();
            }

            // All recursive operations are signalled to the queue by the initial swappers.
            while (queue.hasRemaining()) {
                final RecursiveElementSwapper swapper = queue.get();
                queue.registerNotifier(swapper.getEventNotifier());
                swapper.execute();
            }
        }

    }

    @Test
    public void testSorting() {
        for (int i = 0; i < ROUNDS; i++) {
            final int[] array = PrimitiveUtils.randomArray(mRandom, ARRAY_SIZE);
            sortArray(array, Polarity.ASCENDING);
            Assertions.assertAscending(array);
        }

        for (int i = 0; i < ROUNDS; i++) {
            final int[] array = PrimitiveUtils.randomArray(mRandom, ARRAY_SIZE);
            sortArray(array, Polarity.DESCENDING);
            Assertions.assertDescending(array);
        }
    }

}