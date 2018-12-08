package com.distributed.sorting;

/**
 * Represents a function that accepts 2 inputs elements from a data source, and
 * determines whether or not their locations should be swapped.
 */
public interface ISwapDecision {

    /**
     * Determines whether or not 2 elements from a data source should be
     * swapped based on the sorting configuration.
     *
     * @param left  The element to be compared that appears first in the data.
     * @param right The element to be compared that appears second in the data.
     * @return {@code true} if the elements should be swapped, {@code false}
     * otherwise.
     */
    boolean shouldSwap(int left, int right);

}
