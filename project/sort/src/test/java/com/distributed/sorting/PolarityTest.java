package com.distributed.sorting;

import com.distributed.common.testing.TestUtils;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class PolarityTest {

    private static final int ROUNDS = 200;

    private final Random mRandom = TestUtils.newRandom();

    @Test
    public void testAscending() {
        final Polarity polarity = Polarity.ASCENDING;

        for (int i = 0; i < ROUNDS; i++) {

            final int element1 = mRandom.nextInt();
            final int element2 = mRandom.nextInt();

            if (element1 <= element2) {
                assertTrue(polarity.shouldSwap(element2, element1));
                assertFalse(polarity.shouldSwap(element1, element2));
            } else {
                assertTrue(polarity.shouldSwap(element1, element2));
                assertFalse(polarity.shouldSwap(element2, element1));
            }

        }
    }

    @Test
    public void testDescending() {
        final Polarity polarity = Polarity.DESCENDING;

        for (int i = 0; i < ROUNDS; i++) {

            final int element1 = mRandom.nextInt();
            final int element2 = mRandom.nextInt();

            if (element1 <= element2) {
                assertTrue(polarity.shouldSwap(element1, element2));
                assertFalse(polarity.shouldSwap(element2, element1));
            } else {
                assertTrue(polarity.shouldSwap(element2, element1));
                assertFalse(polarity.shouldSwap(element1, element2));
            }

        }
    }

}