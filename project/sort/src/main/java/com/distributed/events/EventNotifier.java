package com.distributed.events;

import java.util.function.Consumer;

/**
 * Represents a source of events which can have a {@link Consumer}
 * registered to receive signalled events.
 *
 * @param <T> The type of event to be signalled.
 */
public class EventNotifier<T> implements IEventNotifier<T> {

    /**
     * Create a new {@link EventNotifier} object.
     *
     * @param expected The number of expected events.
     */
    public EventNotifier(int expected) {
        assert expected >= 0;
        mExpected = expected;
        mReceived = 0;
    }

    private final Object mMutex = new Object();
    private final int mExpected;

    private int mReceived;
    private volatile Consumer<T> mConsumer = null;

    @Override
    public int getExpected() {
        return mExpected;
    }

    /**
     * Register a {@link Consumer} object that will receive events that
     * occur. This method may not be called multiple times in order to
     * register replacement {@link Consumer} objects.
     *
     * @param consumer The {@link Consumer} object that will receive the
     *                 events. May not be {@code null}.
     * @throws IllegalStateException if a {@link Consumer} has already
     *                               been registered.
     */
    @Override
    public void registerOutput(Consumer<T> consumer) {
        assert consumer != null;

        if (mConsumer != null) {
            final String message = "Output has already been registered.";
            throw new IllegalStateException(message);
        }

        mConsumer = consumer;
    }

    /**
     * Signal one of the expected events to the registered output.
     * <p>
     * <strong>Caution:</caution> This method may not be called before
     * the {@link #registerOutput(Consumer)} is called.
     * </p>
     * <p>
     * <strong>Caution:</caution> Only the expected number of events
     * may be signalled.
     * </p>
     *
     * @param event The event to signal. May be {@code null} depending
     *              on the implementation of the output.
     * @throws IllegalStateException if the {@link #registerOutput(Consumer)}
     *                               method has not yet been called, or if all
     *                               expected events have already been signalled.
     */
    public void signal(T event) {
        if (mConsumer == null) {
            final String message = "Output has not yet been registered.";
            throw new IllegalStateException(message);
        }

        synchronized (mMutex) {
            if (mReceived >= mExpected) {
                final String message = "All expected events have already been signalled.";
                throw new IllegalStateException(message);
            }

            mConsumer.accept(event);
            mReceived += 1;
        }
    }

}
