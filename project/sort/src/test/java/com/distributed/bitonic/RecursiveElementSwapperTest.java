package com.distributed.bitonic;

import com.distributed.common.PrimitiveUtils;
import com.distributed.common.testing.TestUtils;
import com.distributed.events.EventQueue;
import com.distributed.sorting.Assertions;
import com.distributed.sorting.ISwapDecision;
import com.distributed.sorting.Polarity;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class RecursiveElementSwapperTest {

    private static final int ARRAY_SIZE = 512; // Must be a power of 2.
    private static final int ROUNDS = 30;

    /**
     * Creates a bitonic sequence that contains random numbers. A bitonic
     * sequence is sorted in an ascending order up to a pivot point, after
     * which it is sorted in a descending order to the end of the sequence.
     *
     * @param random The {@link Random} object to use for generation.
     * @param size   The length of the sequence to generate.
     * @return The generated sequence.
     */
    private static int[] createBitonicSequence(Random random, int size) {
        final int[] sorted = PrimitiveUtils.range(1, size);

        final int bitonicPoint = random.nextInt(size); // The point where the polarity flips.
        final int[] ascending = Arrays.copyOfRange(sorted, 0, bitonicPoint);
        final int[] descending = Arrays.copyOfRange(sorted, bitonicPoint, size);

        return ArrayUtils.addAll(ascending, descending);
    }

    private final Random mRandom = TestUtils.newRandom();

    /**
     * Sorts a bitonic sequence using a recursive sweep of the
     * {@link RecursiveElementSwapper}.
     *
     * @param data     The bitonic sequence to sort.
     * @param decision The {@link ISwapDecision} representing the desired
     *                 polarity of the output.
     */
    private static void sortBitonic(int[] data, ISwapDecision decision) {

        final EventQueue<RecursiveElementSwapper> queue = new EventQueue<>();

        // Initial swapper dictates the recursion steps for the rest of the algorithm.
        final RecursiveElementSwapper initial = new RecursiveElementSwapper(data, decision);
        queue.registerNotifier(initial.getEventNotifier());
        initial.execute();

        while (queue.hasRemaining()) {
            final RecursiveElementSwapper swapper = queue.get();
            queue.registerNotifier(swapper.getEventNotifier());
            swapper.execute();
        }
    }

    @Test
    public void testSorting() {
        for (int i = 0; i < ROUNDS; i++) {
            final int[] data = createBitonicSequence(mRandom, ARRAY_SIZE);
            sortBitonic(data, Polarity.ASCENDING);
            Assertions.assertAscending(data);
        }

        for (int i = 0; i < ROUNDS; i++) {
            final int[] data = createBitonicSequence(mRandom, ARRAY_SIZE);
            sortBitonic(data, Polarity.DESCENDING);
            Assertions.assertDescending(data);
        }
    }

}