package com.distributed;

import com.distributed.bitonic.BitonicExecutor;
import com.distributed.common.MathUtils;

import java.util.Arrays;

public class Entry {

    private static final int NUM_THREADS = 5;

    public static void main(String args[]) {

        final int length = args.length;
        if (!MathUtils.isPowerOfTwo(length)) {
            final String message = "Input must have a length that is a power of 2.";
            throw new IllegalArgumentException(message);
        }

        final int[] data = new int[args.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = Integer.parseInt(args[i]);
        }

        final BitonicExecutor executor = new BitonicExecutor(NUM_THREADS, data);
        executor.run();

        final String output = Arrays.toString(data);
        System.out.println(output);

    }

}
