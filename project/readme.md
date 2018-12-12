# Distributed Bitonic Sort 

I chose to implement the [bitonic sorting](http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/bitonic/bitonicen.htm) algorithm as my final project for this class. I wrote it in [Java](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html), with a build system that utilizes [Gradle](https://gradle.org/).

This is a sort that is not particularly fast in software but can be massively parallelized. It is commonly used in hardware sorting implementations since it takes advantage of bitonic sequences in such a way that the sort can be performed with the exact same set of comparisons each time, assuming that the input size is the same.

This implementation is a generalized version of the algorithm, which works for any input that has a size that is a power of 2. It models the parallel aspect of the algorithm by allocating small sorting tasks to a thread pool. The root thread controls the operation of the algorithm by distributing these tasks, ensuring that the threads are synchronized in such a way that the integrity of the algorithm is maintained.

# Evaluation

## Approach

There were a number of components that needed to be synchronized in my implementation, as all nodes were operating on the same array in memory simultaneously. The execution of the algorithm can be broken up such that no element swapping tasks overlap, but this involves each step being executed by an object that knows how to recurse and run each of the sub-steps on a thread pool. Each recursive sub-step cannot be completed until its parent step has finished (since it is modifying the same array), so I enforced synchronization by adding an event queuing system, where the parent task would only queue its children for processing once it had completed successfully.

This worked beautifully for synchronizing within a task, however, I initially ran into an unexpected race condition that needed to be tracked down. The cause ended up being the lack of synchronization between root-level tasks themselves. Since I was queuing up events on a thread pool, when a set of recursive tasks had finished I was guaranteed that their tasks were queued and that there were no conflicts between the queued tasks. Unfortunately, as soon as I began adding the tasks for the next step, I was no longer guaranteed that tasks that had previously been registered would be processed by the thread pool before the new tasks. This resulted in operations being performed out of order. I solved this by collecting the results from each of the tasks that I had queued up in the form of `Future` objects. By calling `Future.get()` on them before moving on to the next step, I was guaranteed that not only had all tasks been queued, but that all tasks had been finished.

Throughout the development of my implementation, I thoroughly tested individual components using `JUnit`, specifically looking for race conditions that could cause problems when running the code on a thread pool. The bug that I mentioned above was found by one of my unit tests, as it became clear that as soon as I started to use a thread pool, I was running into issues. Since I had already tested my other classes with race conditions in mind, though the problem could have been the cause by one of them, I was fairly confident that it was in the new code. Additionally, since each object has a fairly small amount of code within it, it wasn't too difficult to track down the problem and figure out what was going on.

At the highest hierarchical level of objects, I have a test that combines random array generation of variable size, a variable number of threads in the thread pool, and a variable order of sorting (ascending or descending). Each time the algorithm sorts, the output is verified to be sorted. Since most of the tests are randomized, the number of tested combinations and code paths are only magnified the more times that I built, which I did frequently during development. Each of the tests also prints out the seed for the `Random` object that is created, which allows intermittent test failures to be reliably reproduced for debugging purposes.

## Performance

![](sorting.gif)

Above, you can see a representation of the sorting process that the algorithm goes through on a random data set. The algorithm initially starts with a window size of 2 and doubles the window size each time until it reaches the full size of the array. Each time a step is processed on a window size, the 2 bitonic peaks are combined into a bitonic peak that is double the size. Eventually, we end up with a single bitonic peak, and a final pass of the algorithm produces a sorted output. 

Though this algorithm is not normally used in software implementations, I decided to test its performance against the build in `Arrays.sort()` method call. I varied the number of threads in the pool, as well as the size of the data in order to get a good idea of how the algorithm performed.


#### For comparison:

`Arrays.sort()` is clearly very fast.

|Data Size | 1  |  2 |  4  |  8 | 16 | 32 | 64 | 128 | 256 | 512 | 1024 | 65536 | 262144 |
|:---------:|:---:|:---:|:---:|:--:|:--:|:--:|:--:|:---:|:---:|:---:|:----:|:-----:|:------:|
|       **Time (ms)**  |1 | 1 | 0 | 1 | 0 | 0 | 1 | 0 |  0 | 0 |  0  |  9 |  44 |

### Results

You can see in the grid below that every scenario resulted in the distributed algorithm performing slower. Part of this may be the fact that it is not truly distributed, and is only running with threads, but I suspect that any communication overhead would more than make up for the speed gained by using multiple machines.

|              | Data Size |  1  |  2 |  4  |  8 | 16 | 32 | 64 | 128 | 256 | 512 | 1024 | 65536 | 262144 |
|:------------:|:---------:|:---:|:--:|:---:|:--:|:--:|:--:|:--:|:---:|:---:|:---:|:----:|:-----:|:------:|
| **Thread Count** |           |     |    |     |    |    |    |    |     |     |     |      |       |        |
|       1      |           | 208 | 97 | 106 | 94 | 93 | 92 | 99 |  89 |  88 |  96 |  87  |  8663 |  38520 |
|       2      |           |  73 | 67 |  61 | 58 | 55 | 63 | 61 |  61 |  77 |  59 |  59  |  5574 |  28133 |
|       3      |           |  39 | 31 |  38 | 28 | 29 | 29 | 34 |  30 |  36 |  33 |  32  |  2734 |  13332 |
|       4      |           |  25 | 23 |  33 | 32 | 26 | 24 | 26 |  25 |  37 |  27 |  25  |  2051 |  10014 |
|       5      |           |  23 | 23 |  24 | 24 | 18 | 25 | 22 |  20 |  22 |  25 |  23  |  1738 |  9230  |
|       6      |           |  22 | 25 |  24 | 23 | 23 | 21 | 20 |  22 |  22 |  22 |  21  |  1671 |  8031  |
|       7      |           |  19 | 19 |  20 | 27 | 25 | 24 | 20 |  20 |  22 |  19 |  20  |  1460 |  7313  |
|       8      |           |  21 | 25 |  16 | 23 | 18 | 19 | 19 |  18 |  20 |  13 |  19  |  1425 |  6057  |
|       9      |           |  18 | 17 |  19 | 18 | 21 | 20 | 19 |  17 |  19 |  17 |  17  |  1201 |  5623  |
|      10      |           |  17 | 18 |  17 | 18 | 19 | 17 | 17 |  16 |  17 |  19 |  17  |  1032 |  5144  |
|      11      |           |  19 | 16 |  18 | 15 | 19 | 17 | 19 |  17 |  17 |  17 |  17  |  1088 |  4875  |
|      12      |           |  16 | 21 |  17 | 17 | 16 | 18 | 16 |  16 |  16 |  17 |  15  |  1063 |  4650  |
|      13      |           |  18 | 20 |  16 | 18 | 15 | 16 | 17 |  17 |  17 |  17 |  18  |  1001 |  4731  |
|      14      |           |  17 | 18 |  17 | 16 | 16 | 11 | 16 |  16 |  12 |  18 |  16  |  1028 |  4796  |
|      15      |           |  16 | 22 |  18 | 16 | 17 | 15 | 16 |  17 |  17 |  16 |  18  |  950  |  4747  |
|      16      |           |  16 | 19 |  18 | 18 | 18 | 25 | 17 |  16 |  14 |  17 |  18  |  1033 |  4680  |
|      17      |           |  17 | 19 |  17 | 10 | 18 | 16 | 16 |  17 |  15 |  12 |  18  |  978  |  4777  |
|      18      |           |  18 | 17 |  17 | 25 | 17 | 17 | 23 |  18 |  17 |  17 |  24  |  1038 |  4702  |
|      19      |           |  23 | 17 |  18 | 15 | 17 | 12 | 18 |  18 |  16 |  27 |  16  |  1007 |  4526  |
|      20      |           |  17 | 18 |  16 | 18 | 18 | 12 | 18 |  18 |  22 |  17 |  18  |  926  |  4670  |

#### All units in the above table are in ms.

# Building

1. Make sure the Java 8 JDK in installed.
2. Run `./gradlew build` (on Linux or Windows).

You can find the produced `.jar` file in `build/libs/`.

The tests exercise all parts of the implementation far better than can be demonstrated through the entry point, however, the `.jar` file can be used to sort an array via the command line parameters.

## Using the `.jar` file

A limitation of the bitonic sorting algorithm is that it must have a set of data which has a length that is a power of 2 (i.e. 4, 16, 256). I could have dealt with this limitation by performing a regular sort on any overhanging data but decided to stick within the confines of the algorithm for demonstrative purposes.

Run `java -jar file-name-here.jar 12 5 4 6 1 0 8 7` (a data size of 8).

This above command will produce a sorted output on the command line.

The implementation that is accessible through the command line has a fixed number of threads and also has a fixed sorting order (ascending). If more control is required, see the heading below.

## Modifying the source

As mentioned above, there are some limitations that are enforced by using the `.jar` file. Modifying the source files will allow more specific scenarios.

The entry point (`src/main/java/com/distributed/Entry.java`) contains the `main()` method and can be modified to contain the desired parameters. Alternatively, if you are using [IntelliJ IDEA](https://www.jetbrains.com/idea/) or a similar IDE, you can modify and run the included [JUnit](https://junit.org/) tests.
