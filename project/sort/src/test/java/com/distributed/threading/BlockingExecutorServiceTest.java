package com.distributed.threading;

import com.distributed.common.PrimitiveUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class BlockingExecutorServiceTest {

    private static final int NUM_VALUES = 50_000;
    private static final int NUM_THREADS = 5;

    @Test
    public void test() {
        final ExecutorService service = new BlockingExecutorService(NUM_THREADS);
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

        for (int i = 0; i < NUM_VALUES; i++) {
            // Must be final to be used in the lambda below.
            final int value = i;
            service.submit(() -> {
                queue.offer(value);
            });
        }

        final Collection<Integer> source = Arrays.asList(ArrayUtils.toObject(PrimitiveUtils.range(0, NUM_VALUES - 1)));

        // Get the events that made it into the queue.
        final ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {

            boolean success = false;
            while (!success) {
                try {
                    final Integer value = queue.take();
                    result.add(value);
                    success = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        assertTrue(result.containsAll(source));
    }

}