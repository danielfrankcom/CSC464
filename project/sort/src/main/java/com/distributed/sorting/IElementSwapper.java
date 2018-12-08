package com.distributed.sorting;

/**
 * Represents a wrapped object that has all the required context in order
 * to perform a swapping operation on a configured array. The purpose of
 * the operation is defined by the derivations of this class, however the
 * operations performed by all derivations consist of swapping the
 * locations of array elements in order to achieve the defined goal.
 * <p>
 * Until the {@link #swap()} method has been called, the configured
 * swapping operation has not be performed.
 * </p>
 */
public interface IElementSwapper {

    /**
     * Executes the configured swapping operation.
     *
     * <strong>Warning:</strong> Executing multiple concurrent swap
     * operations on the same array without careful consideration
     * may cause result in an unexpected state, unless {@link IElementSwapper}
     * instances are synchronized.
     */
    void swap();

}
