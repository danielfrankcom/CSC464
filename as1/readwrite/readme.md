1.
This problem models almost exactly what it describes, as it is not possible for readers and writers to inspect/modify the same data at the same time as it may change unexpectedly. A file for example must have the same kind of synchronization to protect its state and ensure that it is consistent.

2.
Both solutions in this implementation stemmed from the 'Little Book of Semaphores', and are designed to investigate the performance difference between starving and starving-resistent implementations. By using Python's object-oriented features, the code is pretty easy to read and maintain in both cases. The differences between the solutions is subtle, and they only differ in the use of a turnstile in the starving-resistent solution. Some code was written to keep track of the time that each thread was forced to wait, and to track the highest and the average values.

30,000 reads, 30,000 writes
Fair:
    1
        total time: 12.12 seconds
        average:    0.45 ms
        highest:    20.91 ms
    2
        total time: 12.43 seconds
        average:    0.70 ms
        highest:    87.08 ms
    3
        total time: 12.26 seconds
        average:    0.56 ms
        highest:    15.32 ms
    4
        total time: 12.11 seconds
        average:    0.55 ms
        highest:    25.64 ms
Starving:
    1
        total time: 10.86 seconds
        average:    0.33 ms
        highest:    20.56 ms
    2
        total time: 10.54 seconds
        average:    0.28 ms
        highest:    24.33 ms
    3
        total time: 11.01 seconds
        average:    0.32 ms
        highest:    30.63 ms
    4
        total time: 10.46 seconds
        average:    0.33 ms
        highest:    21.27 ms

Ignoring the relatively small sample size of these results, we can see that the fair implementation tends to have a higher overall processing time and average thread wait time, but a lower maximum value. Result 2 fair contradicts this, but it appears to be an outlier, caused by a stutter in the system, as both the average time and the highest time are detached from the norm.
