package com.distributed.bitonic;

import com.distributed.common.MathUtils;
import com.distributed.common.PrimitiveUtils;
import com.distributed.sorting.Assertions;
import com.distributed.sorting.ISortCondition;
import com.distributed.sorting.IElementSwapper;
import com.distributed.sorting.Polarity;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class SinglePassSwapperTest {

    /*
     * 2^10 is an array size of 1024, which is a reasonable size in
     * terms of processing power, while providing 10 intermediary
     * array sizes that are powers of 2.
     */
    private static final int MAX_POWER_OF_TWO = 10;

    /**
     * Represents the relationship between two halves of an array of
     * {@link Integer} primitives.
     */
    @FunctionalInterface
    private interface IHalfRelationship {

        /**
         * @param firstHalf  The first half of the array to check.
         * @param secondHalf The second half of the array to check.
         * @return {@code true} if the array halves have the correct
         * relationship, {@code false} otherwise.
         */
        boolean isCorrect(int[] firstHalf, int[] secondHalf);

    }

    /**
     * An implementation of {@link IHalfRelationship} where all elements in the first half
     * are less than all elements in the second half.
     */
    private static boolean firstHalfIsLessThanSecond(int[] firstHalf, int[] secondHalf) {
        final Collection<Integer> firstHalfObjects = Arrays.asList(ArrayUtils.toObject(firstHalf));
        final Collection<Integer> secondHalfObjects = Arrays.asList(ArrayUtils.toObject(secondHalf));

        final int firstMax = Collections.max(firstHalfObjects);
        final int secondMin = Collections.min(secondHalfObjects);
        return firstMax < secondMin;
    }

    /**
     * An implementation of {@link IHalfRelationship} where all elements in the first half
     * are greater than all elements in the second half.
     */
    private static boolean firstHalfIsGreaterThanSecond(int[] firstHalf, int[] secondHalf) {
        final Collection<Integer> firstHalfObjects = Arrays.asList(ArrayUtils.toObject(firstHalf));
        final Collection<Integer> secondHalfObjects = Arrays.asList(ArrayUtils.toObject(secondHalf));

        final int firstMin = Collections.min(firstHalfObjects);
        final int secondMax = Collections.max(secondHalfObjects);
        return firstMin > secondMax;
    }

    /**
     * Asserts that all elements that were supposed to be swapped are in the correct place.
     *
     * @param data          The array of values that should be verified.
     * @param sortCondition The {@link ISortCondition} that specifies the correct order of
     *                      the values.
     * @param relationship  The {@link IHalfRelationship} that can be used to check each of
     *                      the array halves to ensure that their relationship is correct.
     */
    private static void assertSwapped(int[] data, ISortCondition sortCondition, IHalfRelationship relationship) {
        assertSwapped(data, 0, data.length, sortCondition, relationship);
    }

    /**
     * Asserts that all elements that were supposed to be swapped are in the correct place.
     *
     * @param data          The array of values that should be verified.
     * @param start         The inclusive start of the range to check.
     * @param stop          The exclusive end of the range to check.
     * @param sortCondition The {@link ISortCondition} that specifies the correct order of
     *                      the values.
     * @param relationship  The {@link IHalfRelationship} that can be used to check each of
     *                      the array halves in the specified range in order to ensure that
     *                      their relationship is correct.
     */
    private static void assertSwapped(int[] data, int start, int stop,
                                      ISortCondition sortCondition, IHalfRelationship relationship) {
        assert data != null;
        assert start >= 0;
        assert start < stop;

        final int diff = stop - start;
        if (diff == 1) {
            // 1 element is always swapped.
            return;
        }

        assert MathUtils.isPowerOfTwo(diff);
        final int pivot = start + (diff / 2);

        final int[] firstHalf = Arrays.copyOfRange(data, start, pivot);
        final int[] secondHalf = Arrays.copyOfRange(data, pivot, stop);
        Assertions.assertSorted(firstHalf, sortCondition);
        Assertions.assertSorted(secondHalf, sortCondition);

        assert relationship.isCorrect(firstHalf, secondHalf);
    }

    @Test
    public void testDefaultSwap() {

        // [1, 2, .., n-1, n] -> [1, 2, .., n-1, n]
        for (int i = 0; i <= MAX_POWER_OF_TWO; i++) {
            // This cast is safe as 2^n cannot be fractional.
            final int size = (int) Math.pow(2, i);
            final int[] source = PrimitiveUtils.range(1, size);

            final IElementSwapper swapper = new SinglePassElementSwapper(source);
            swapper.swap();
            assertSwapped(source, ISortCondition.ASCENDING, SinglePassSwapperTest::firstHalfIsLessThanSecond);
        }

        // [1, 2, .., n-1, n] -> [.., 2, 1, n, n-1, ..]
        for (int i = 0; i <= MAX_POWER_OF_TWO; i++) {
            // This cast is safe as 2^n cannot be fractional.
            final int size = (int) Math.pow(2, i);
            final int[] source = PrimitiveUtils.range(1, size);
            ArrayUtils.reverse(source);

            final IElementSwapper swapper = new SinglePassElementSwapper(source);
            swapper.swap();
            assertSwapped(source, ISortCondition.DESCENDING, SinglePassSwapperTest::firstHalfIsLessThanSecond);
            System.out.println(Arrays.toString(source));
        }

    }

    @Test
    public void testWholeArraySwaps() {

        // [1, 2, .., n-1, n] -> [.., n-1, n, 1, 2, ..]
        for (int i = 0; i <= MAX_POWER_OF_TWO; i++) {
            // This cast is safe as 2^n cannot be fractional.
            final int size = (int) Math.pow(2, i);
            final int[] source = PrimitiveUtils.range(1, size);

            final IElementSwapper swapper = new SinglePassElementSwapper(source, Polarity.DESCENDING);
            swapper.swap();
            assertSwapped(source, ISortCondition.ASCENDING, SinglePassSwapperTest::firstHalfIsGreaterThanSecond);
        }

        // [n, n-1, .., 2, 1] -> [n, n-1, .., 2, 1]
        for (int i = 0; i <= MAX_POWER_OF_TWO; i++) {
            // This cast is safe as 2^n cannot be fractional.
            final int size = (int) Math.pow(2, i);
            final int[] source = PrimitiveUtils.range(1, size);
            ArrayUtils.reverse(source);

            final IElementSwapper swapper = new SinglePassElementSwapper(source, Polarity.DESCENDING);
            swapper.swap();
            assertSwapped(source, ISortCondition.DESCENDING, SinglePassSwapperTest::firstHalfIsGreaterThanSecond);
        }

        // [1, 2, .., n-1, n] -> [1, 2, .., n-1, n]
        for (int i = 0; i <= MAX_POWER_OF_TWO; i++) {
            // This cast is safe as 2^n cannot be fractional.
            final int size = (int) Math.pow(2, i);
            final int[] source = PrimitiveUtils.range(1, size);

            final IElementSwapper swapper = new SinglePassElementSwapper(source, Polarity.ASCENDING);
            swapper.swap();
            assertSwapped(source, ISortCondition.ASCENDING, SinglePassSwapperTest::firstHalfIsLessThanSecond);
        }

        // [1, 2, .., n-1, n] -> [.., 2, 1, n, n-1, ..]
        for (int i = 0; i <= MAX_POWER_OF_TWO; i++) {
            // This cast is safe as 2^n cannot be fractional.
            final int size = (int) Math.pow(2, i);
            final int[] source = PrimitiveUtils.range(1, size);
            ArrayUtils.reverse(source);

            final IElementSwapper swapper = new SinglePassElementSwapper(source, Polarity.ASCENDING);
            swapper.swap();
            assertSwapped(source, ISortCondition.DESCENDING, SinglePassSwapperTest::firstHalfIsLessThanSecond);
            System.out.println(Arrays.toString(source));
        }

    }

    @Test
    public void testPartialSwap() {
        final int POWER = 9;
        final int SIZE = (int) Math.pow(2, POWER); // 512

        final int[] SOURCE = PrimitiveUtils.range(1, SIZE);
        final int[] REVERSE = Arrays.copyOf(SOURCE, SIZE); // Not reversed yet.
        ArrayUtils.reverse(REVERSE);

        for (int i = 0; i <= POWER; i++) {

            final int groupSize = (int) Math.pow(2, i);

            for (int start = 0; start < SIZE; start += groupSize) {
                final int stop = start + groupSize;
                final int[] copy = Arrays.copyOf(SOURCE, SIZE);

                // [1, 2, .., n-1, n] -> [.., n-1, n, 1, 2, ..]
                final IElementSwapper swapper = new SinglePassElementSwapper(copy, start, stop, Polarity.DESCENDING);
                swapper.swap();
                assertSwapped(copy, start, stop, ISortCondition.ASCENDING,
                        SinglePassSwapperTest::firstHalfIsGreaterThanSecond);
            }

        }

        for (int i = 0; i <= POWER; i++) {

            final int groupSize = (int) Math.pow(2, i);

            for (int start = 0; start < SIZE; start += groupSize) {
                final int stop = start + groupSize;
                final int[] copy = Arrays.copyOf(REVERSE, SIZE);

                // [n, n-1, .., 2, 1] -> [n, n-1, .., 2, 1]
                final IElementSwapper swapper = new SinglePassElementSwapper(copy, start, stop, Polarity.DESCENDING);
                swapper.swap();
                assertSwapped(copy, start, stop, ISortCondition.DESCENDING,
                        SinglePassSwapperTest::firstHalfIsGreaterThanSecond);
            }

        }

        for (int i = 0; i <= POWER; i++) {

            final int groupSize = (int) Math.pow(2, i);

            for (int start = 0; start < SIZE; start += groupSize) {
                final int stop = start + groupSize;
                final int[] copy = Arrays.copyOf(SOURCE, SIZE);

                // [1, 2, .., n-1, n] -> [1, 2, .., n-1, n]
                final IElementSwapper swapper = new SinglePassElementSwapper(copy, start, stop, Polarity.ASCENDING);
                swapper.swap();
                assertSwapped(copy, start, stop, ISortCondition.ASCENDING,
                        SinglePassSwapperTest::firstHalfIsLessThanSecond);
            }

        }

        for (int i = 0; i <= POWER; i++) {

            final int groupSize = (int) Math.pow(2, i);

            for (int start = 0; start < SIZE; start += groupSize) {
                final int stop = start + groupSize;
                final int[] copy = Arrays.copyOf(REVERSE, SIZE);

                // [1, 2, .., n-1, n] -> [.., 2, 1, n, n-1, ..]
                final IElementSwapper swapper = new SinglePassElementSwapper(copy, start, stop, Polarity.ASCENDING);
                swapper.swap();
                assertSwapped(copy, start, stop, ISortCondition.DESCENDING,
                        SinglePassSwapperTest::firstHalfIsLessThanSecond);
            }

        }

    }

}