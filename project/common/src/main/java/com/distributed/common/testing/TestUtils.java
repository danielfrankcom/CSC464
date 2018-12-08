package com.distributed.common.testing;

import java.util.Random;

/**
 * Provides methods that are useful in writing unit tests.
 */
public class TestUtils {

    /**
     * Creates a new {@link Random} object for use by a test,
     * and prints the seed for the {@link Random} so that any
     * failures in randomized tests can be reliably reproduced
     * for debugging purposes.
     *
     * @return The generated {@link Random} object.
     */
    public static Random newRandom() {
        final long SEED = System.currentTimeMillis();
        System.out.printf("Random seed: %d\n", SEED);
        return new Random(SEED);
    }

}
