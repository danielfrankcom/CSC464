package com.distributed.bitonic;

import com.distributed.common.PrimitiveUtils;
import com.distributed.common.testing.TestUtils;
import com.distributed.sorting.Assertions;
import com.distributed.sorting.Polarity;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class BitonicExecutorTest {

    private static final int ARRAY_SIZE = 512;
    private static final int ROUNDS = 30;
    private static final int MAX_THREADS = 20;

    private final Random mRandom = TestUtils.newRandom();

    @Test
    public void testSorting() {
        for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads++) {

            for (int i = 0; i < ROUNDS; i++) {
                final int[] data = PrimitiveUtils.randomArray(mRandom, ARRAY_SIZE);
                final BitonicExecutor executor = new BitonicExecutor(numThreads, data, Polarity.ASCENDING);
                executor.run();
                Assertions.assertAscending(data);
            }

            for (int i = 0; i < ROUNDS; i++) {
                final int[] data = PrimitiveUtils.randomArray(mRandom, ARRAY_SIZE);
                final BitonicExecutor executor = new BitonicExecutor(numThreads, data, Polarity.DESCENDING);
                executor.run();
                Assertions.assertDescending(data);
            }
        }
    }

    @Test
    public void testInvalidConstructor() {

        // 0 threads is invalid.
        boolean exceptionThrow = false;
        try {
            new BitonicExecutor(0, new int[2], Polarity.ASCENDING);
        } catch (AssertionError e) {
            exceptionThrow = true;
        }
        assertTrue(exceptionThrow);

        // Null data is invalid.
        exceptionThrow = false;
        try {
            new BitonicExecutor(1, null, Polarity.ASCENDING);
        } catch (AssertionError e) {
            exceptionThrow = true;
        }
        assertTrue(exceptionThrow);

        // Data that has a length that is not a power of 2 is invalid.
        exceptionThrow = false;
        try {
            new BitonicExecutor(1, new int[3], Polarity.ASCENDING);
        } catch (AssertionError e) {
            exceptionThrow = true;
        }
        assertTrue(exceptionThrow);

        // Null polarity is invalid.
        exceptionThrow = false;
        try {
            new BitonicExecutor(1, new int[2], null);
        } catch (AssertionError e) {
            exceptionThrow = true;
        }
        assertTrue(exceptionThrow);
    }

}