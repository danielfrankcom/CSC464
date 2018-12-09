package com.distributed.events;

import java.util.function.Consumer;

/**
 * Represents a source of events, which are signalled to the currently
 * registered event {@link Consumer} output. All derivations must
 * ensure that signalled events are not {@code null}.
 *
 * @param <T> The type of event to be signalled.
 */
public interface IEventNotifier<T> {

    /**
     * @return The number of events that are expected to be signalled
     * by this {@link IEventNotifier}.
     */
    int getExpected();

    /**
     * Register a {@link Consumer} object that will receive events that
     * occur. Derivations may decide whether called the method again in
     * order to replace the previously registered {@link Consumer} is
     * acceptable behaviour.
     * <p>
     * <strong>Caution:</strong> Calling this method multiple times
     * requires synchronization from the user, as there is no guarantee
     * that thread interleaving will not cause an event to be signalled
     * to the wrong {@link Consumer}.
     * </p>
     *
     * @param consumer The {@link Consumer} object that will receive the
     *                 events. May not be {@code null}.
     */
    void registerOutput(Consumer<T> consumer);

}
