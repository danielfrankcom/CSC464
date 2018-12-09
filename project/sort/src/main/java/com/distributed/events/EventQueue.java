package com.distributed.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Represents a collection of events that have occurred from one or
 * many {@link IEventNotifier} objects.
 *
 * @param <T> The type of event that this {@link EventQueue} tracks.
 */
public class EventQueue<T> {

    private int mNumExpected = 0;
    private int mNumEvents = 0;

    private final BlockingQueue<T> mQueue = new LinkedBlockingQueue<>();
    private final Object mMutex = new Object();
    private final Consumer<T> mEventConsumer = this::consumeEvent;

    /**
     * Represents a functional {@link Consumer} implementation that accepts
     * incoming events and stashes them in the internal collection.
     *
     * @param event The event to store.
     */
    private void consumeEvent(T event) {

        final boolean legal;
        synchronized (mMutex) {
            /*
             * Performing state validation must be in the same
             * critical section as the modification of the
             * tracking variables, as thread interleaving could
             * cause 2 incoming events to be validated before
             * the first is processed, which would could put the
             * tracking variables into an invalid state.
             */
            legal = mNumExpected > 0;

            if (legal) {
                mNumEvents++;
                mNumExpected--;
            }
        }

        if (!legal) {
            final String message = "No more events are expected.";
            throw new IllegalStateException(message);
        }

        /*
         * Our queue has no bound, so if an InterruptedException
         * occurs then simply try again.
         */
        boolean successful = false;
        while (!successful) {
            try {
                mQueue.put(event);
                successful = true;
            } catch (InterruptedException e) {
                // Do nothing.
            }
        }
    }

    /**
     * Register a new {@link IEventNotifier} with the {@link EventQueue},
     * which will provide event notifications to be stashed.
     *
     * @param notifier The {@link IEventNotifier} to attach.
     */
    public void registerNotifier(IEventNotifier<T> notifier) {
        synchronized (mMutex) {
            mNumExpected += notifier.getExpected();
        }
        notifier.registerOutput(mEventConsumer);
    }

    /**
     * Determines if the {@link EventQueue} has or will have events to be
     * consumed.
     * <p>
     * <strong>Caution:</strong> If this object is used from multiple
     * threads then the result of this method call is unreliable. A call
     * to the {@link #get()} call could occur on another thread as soon
     * as a value of {@code true} is returned from this method.
     * </p>
     *
     * @return {@code true} if there are consumable events, {@code false}
     * otherwise.
     */
    public boolean hasRemaining() {
        synchronized (mMutex) {
            return mNumEvents > 0 || mNumExpected > 0;
        }
    }

    /**
     * Returns a single event that has been notified by one of the registered
     * {@link IEventNotifier} objects.
     * <p>
     * <strong>Caution:</strong> This is a blocking call, and will not
     * return until an event has been received by the {@link EventQueue}.
     * </p>
     *
     * @return A single notified event.
     */
    public T get() {

        final boolean legal;
        synchronized (mMutex) {
            /*
             * If the call to this method is made before an event
             * has been signalled, then it is possible for this
             * variable to become negative. A negative value for the
             * variable represents threads that are blocked waiting.
             *
             * It is not possible for more threads to be blocked
             * waiting than the number of expected events, so by
             * tracking the debit of events in this way we can
             * ensure that extra threads are rejected from this call
             * and are not kept waiting.
             */
            legal = mNumEvents + mNumExpected > 0;

            if (legal) {
                mNumEvents--;
            }
        }

        if (!legal) {
            final String message = "There are no more events to read.";
            throw new IllegalStateException(message);
        }

        /*
         * We can guarantee that eventually an event will arrive, so
         * we block on the call to #take().
         */
        T result = null;
        boolean successful = false;
        while (!successful) {
            try {
                result = mQueue.take();
                successful = true;
            } catch (InterruptedException e) {
                // Do nothing.
            }
        }

        //noinspection ConstantConditions
        assert result != null;

        return result;
    }

}
