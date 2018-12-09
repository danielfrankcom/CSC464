package com.distributed.events;

import com.distributed.common.testing.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class EventQueueTest {

    private static final int NUM_EVENTS = 100_000;

    /**
     * Represents a task that can be registered with a {@link FutureTask}
     * in order to perform a {@link Runnable} task and either throw an
     * {@link Throwable} from the call to {@link Runnable#run()}, or return
     * {@code null} indicating a successful execution.
     */
    private static abstract class TestTask implements Callable<Object>, Runnable {

        @Override
        public Object call() {
            run();
            return null;
        }

    }

    /**
     * Represents a task that notifies an {@link EventQueue} with a
     * {@link Collection} of events.
     *
     * @param <T> The type of event to be signalled.
     */
    private static class EventRegistrar<T> extends TestTask {

        /**
         * Create a new {@link EventRegistrar} object.
         *
         * @param toNotify The {@link Collection} of events to be notified.
         * @param queue    The {@link EventQueue} to notify
         */
        EventRegistrar(Collection<T> toNotify, EventQueue<T> queue) {
            mEvents = toNotify;
            mNotifier = new EventNotifier<>(toNotify.size());
            queue.registerNotifier(mNotifier);
        }

        private final Collection<T> mEvents;
        private final EventNotifier<T> mNotifier;

        @Override
        public void run() {
            for (T event : mEvents) {
                mNotifier.signal(event);

                // Increase likelihood of thread interleaving.
                Thread.yield();
            }
        }

    }

    /**
     * Represents a task that verified that an {@link EventQueue} has
     * received a {@link Collection} of events.
     *
     * @param <T> The type of event to be checked.
     */
    private static class EventVerifier<T> extends TestTask {

        /**
         * Create a new {@link EventVerifier} object.
         *
         * @param toVerify The {@link Collection} of events to be verified.
         * @param queue    The {@link EventQueue} to notify
         */
        EventVerifier(Collection<T> toVerify, EventQueue<T> queue) {
            mEvents = toVerify;
            mQueue = queue;
        }

        private final Collection<T> mEvents;
        private final EventQueue<T> mQueue;

        @Override
        public void run() {
            for (T event : mEvents) {
                assertTrue(mQueue.hasRemaining());
                T result = mQueue.get();
                assertEquals(event, result);

                // Increase likelihood of thread interleaving.
                Thread.yield();
            }
            assertFalse(mQueue.hasRemaining());
        }

    }

    private final Random mRandom = TestUtils.newRandom();

    @Test
    public void testSynchronousUsage() {

        final EventQueue<Integer> queue = new EventQueue<>();
        final EventNotifier<Integer> notifier = new EventNotifier<>(NUM_EVENTS);
        queue.registerNotifier(notifier);

        for (int i = 0; i < NUM_EVENTS; i++) {
            assertTrue(queue.hasRemaining());
            final Integer randNum = mRandom.nextInt();
            notifier.signal(randNum);
            assertEquals(randNum, queue.get());
        }

        assertFalse(queue.hasRemaining());

        // All events should have been received already.
        boolean exceptionThrow = false;
        try {
            notifier.signal(2);
            fail("Exception should have been thrown.");
        } catch (IllegalStateException e) {
            exceptionThrow = true;
        }
        assertTrue(exceptionThrow);

        // There should be no events left to receive.
        exceptionThrow = false;
        try {
            queue.get();
            fail("Exception should have been thrown.");
        } catch (IllegalStateException e) {
            exceptionThrow = true;
        }
        assertTrue(exceptionThrow);

    }

    @Test
    public void testUsageAcrossThreads() {

        final ArrayList<Integer> events = new ArrayList<>();
        for (int i = 0; i < NUM_EVENTS; i++) {
            events.add(i);
        }

        final EventQueue<Integer> queue = new EventQueue<>();

        final Callable<Object> registrar = new EventRegistrar<>(events, queue);
        final FutureTask<Object> registrarTask = new FutureTask<>(registrar);

        final Callable<Object> verifier = new EventVerifier<>(events, queue);
        final FutureTask<Object> verifierTask = new FutureTask<>(verifier);

        final ExecutorService service = Executors.newFixedThreadPool(2);
        service.submit(registrarTask);
        service.submit(verifierTask);
        service.shutdown();

        /*
         * If asynchronous exceptions occur on either of the threads,
         * then they will be rethrown in the call to #get().
         */
        try {
            registrarTask.get();
            verifierTask.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

}