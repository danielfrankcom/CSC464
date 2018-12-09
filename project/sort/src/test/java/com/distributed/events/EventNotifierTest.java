package com.distributed.events;

import com.distributed.common.PrimitiveUtils;
import com.distributed.common.testing.TestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class EventNotifierTest {

    private static final int ROUNDS = 200;
    private static final int EVENTS = 1_000;

    // Represents a consumer that discards provided objects.
    private static final Consumer<Integer> NULL_CONSUMER = (value) -> {
    };

    /**
     * Represents a {@link Consumer} implementation that records all
     * objects provided to it.
     */
    private static class MockConsumer<T> implements Consumer<T> {

        private final ArrayList<T> mAccepted = new ArrayList<>();

        @Override
        public void accept(T value) {
            mAccepted.add(value);
        }

        /**
         * Checks whether a {@link Collection} of objects were provided
         * to the {@link Consumer}.
         *
         * @param toCheck The {@link Collection} of objects to check.
         * @return {@code} true if all of the provided objects were
         * received, {@code false} otherwise.
         */
        boolean wasAccepted(Collection<T> toCheck) {
            return mAccepted.containsAll(toCheck);
        }

    }

    private final Random mRandom = TestUtils.newRandom();

    @Test
    public void testInvalidConstruction() {
        boolean exceptionThrown = false;
        try {
            new EventNotifier<>(-1);
            new EventNotifier<>(Integer.MIN_VALUE);
            // No additional fail() call here as it would throw an AssertionError.
        } catch (AssertionError e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testEventsAreConsumed() {

        for (int i = 0; i < ROUNDS; i++) {

            final int expected = mRandom.nextInt(EVENTS);
            final EventNotifier<Integer> notifier = new EventNotifier<>(expected);

            assertEquals(expected, notifier.getExpected());

            final MockConsumer<Integer> consumer = new MockConsumer<>();
            notifier.registerOutput(consumer);

            final Collection<Integer> events = Arrays.asList(ArrayUtils.toObject(PrimitiveUtils.range(1, expected)));
            for (Integer event : events) {
                assertEquals(expected, notifier.getExpected());
                notifier.signal(event);
            }

            assertTrue(consumer.wasAccepted(events));

        }
    }

    @Test
    public void testUnregisteredOutput() {
        final EventNotifier<Integer> notifier = new EventNotifier<>(0);

        boolean exceptionThrown = false;
        try {
            notifier.signal(1);
            fail("Exception should have been thrown.");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testNullOutput() {
        final EventNotifier<Integer> notifier = new EventNotifier<>(0);

        boolean exceptionThrown = false;
        try {
            notifier.registerOutput(null);
            // No additional fail() call here as it would throw an AssertionError.
        } catch (AssertionError e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testSecondOutput() {
        final EventNotifier<Integer> notifier = new EventNotifier<>(0);
        final Consumer<Integer> consumer1 = new MockConsumer<>();
        notifier.registerOutput(consumer1);

        boolean exceptionThrown = false;
        try {
            final Consumer<Integer> consumer2 = new MockConsumer<>();
            notifier.registerOutput(consumer2);
            fail("Exception should have been thrown.");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testAlreadyReceivedAllEvents() {
        final int EVENTS = mRandom.nextInt(1000);

        final EventNotifier<Integer> notifier = new EventNotifier<>(EVENTS);
        notifier.registerOutput(NULL_CONSUMER);

        for (int i = 0; i < EVENTS; i++) {
            notifier.signal(i);
        }

        boolean exceptionThrown = false;
        try {
            notifier.signal(0);
            fail("Exception should have been thrown.");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

}