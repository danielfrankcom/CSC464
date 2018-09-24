1. This problem models almost exactly what it describes, as it is not possible for readers and writers to inspect/modify the same data at the same time as it may change unexpectedly. A file for example must have the same kind of synchronization to protect its state and ensure that it is consistent.

2. Both solutions in this implementation stemmed from the 'Little Book of Semaphores', and are designed to investigate the performance difference between starving and starving-resistent implementations. By using Python's object-oriented features, the code is pretty easy to read and maintain in both cases. The differences between the solutions is subtle, and they only differ in the use of a turnstile in the starving-resistent solution. Some code was written to keep track of the time that each thread was forced to wait, and to track the highest and the average values.

|  Implementation    |  Metric  |  Run 1  |  Run 2  |  Run 3  |  Run 4  | 
|-|-|-|-|-|-|
|  Fair     |  Total time (secs)  | 12.12 | 12.43 | 12.26 | 12.11 |
|           |  Average wait (ms) | 0.45 | 0.70 | 0.56 | 0.55 |
|           |  Longest wait (ms) | 20.91 | 87.08 | 15.32 | 25.64 |
|  Starving |  Total time (secs)  | 10.86 | 10.54 | 11.01 | 10.46 |
|           |  Average wait (ms) | 0.33 | 0.28 | 0.32 | 0.33 |
|           |  Longest wait (ms) | 20.56 | 24.33 | 30.63 | 21.27 |
Table: 30,000 reads, 30,000 writes

Ignoring the relatively small sample size of these results, we can see that the fair implementation tends to have a higher overall processing time and average thread wait time, but a lower maximum value. Result 2 fair contradicts this, but it appears to be an outlier, caused by a stutter in the system, as both the average time and the highest time are detached from the norm.

