package com.distributed.sorting;

/**
 * Represents a method that determines if 2 elements are
 * in the correct location relative to one another in an
 * array of elements that is supposed to be sorted.
 */
@FunctionalInterface
public interface ISortCondition {

    ISortCondition ASCENDING = (previous, current) -> previous <= current;
    ISortCondition DESCENDING = (previous, current) -> previous >= current;

    /**
     * Determines if the elements are in the correct order.
     *
     * @param previous The element that appears first in the array.
     * @param current  The element that appears after the first in the
     *                 array.
     * @return {@code true} if the elements are in the correct order,
     * {@code false} otherwise.
     */
    boolean isValid(int previous, int current);

}
