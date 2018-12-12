# Distributed Bitonic Sort 

I chose to implement the [bitonic sorting](http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/bitonic/bitonicen.htm) algorithm as my final project for this class. I wrote it in [Java](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html), with a build system that utilizes [Gradle](https://gradle.org/).

This is a sort that is not particularly fast in software but can be massively parallelized. It is commonly used in hardware sorting implementations due to the interesting property that it can be performed with the exact same set of comparisons each time, assuming that the input size is the same.

This implementation is a generalized version of the algorithm, that works for any input that has a size that is a power of 2. It models the parallel aspect of the algorithm by allocating small sorting tasks to a thread pool. The root thread controls the operation of the algorithm by distributing these tasks, ensuring that the threads are synchronized in such a way that the integrity of the algorithm is maintained.

# Evaluation

## Approach

There were a number of components that needed to be synchronized in my implementation, as all nodes were operating on the same array in memory. I decided to break the algorithm up into a series of logical steps, where each step is executed by an object that knows how to recurse and run each of the sub-steps on a threadpool. Each recursive sub-step cannot be completed until its parent step has finished (since it is modifying the same array), so I enforced synchronization by adding an event queuing system, where the parent task would only queue its children for processing once it had completed successfully. This worked beautifully for synchronizing within a task, however I initially ran into an unexpected race condition that needed to be tracked down, and ended up being caused by a lack of synchronization between root-level tasks themselves. Since I was queuing up events on a thread pool, when a set of recursive tasks had finished I was guaranteed that their tasks were queued, and that there were no conflicts between those tasks. Unfortunately, it did not guarantee that tasks that I registered from the next step would not be processed first by the threadpool, which resulted in operations being performed out of order. I solved this by collecting the results from each of the tasks that I queued in the form of `Future` objects, and by calling `#get()` on them before moving on to the next step. This guaranteed that not only had all tasks been queued, but that all tasks had been finished.

Throughout the construction of my implementation, I thoroughly tested individual components using `JUnit`, specifically for race conditions that could cause problems when running on a threadpool. The bug that I mentioned above was found by one of my unit tests, as it became clear that as soon as I started to use a threadpool, I was running into issues. Since I had already tested my other objects with race conditions in mind, though the problem could have been the cause by one of them, I was fairly confident that it was in the new code. Additionally, since each object has a fairly small amount of code in it, it wasn't too difficult to track down the problem and figure out what was going on.

At the highest heirarchical level of objects, I have a test that combines random array generation of variable size, a variable number of threads in the threadpool, and a variable order of sorting (ascending or descending). Each time the algorithm sorts, the output is verified to be sorted. Since most of the tests are randomized, the number of tested combinations and code paths only magnified the more times I built, which I did frequently during development. Each of the tests also prints out the seed for the `Random` object that is created, which allows intermittent test failures to be reliably reproduced for debugging purposes.

## Performance

![](sorting.gif)

Above, you can see a representation of the sorting process that the algorithm goes through on a random data set. The algorithm initially starts with a window size of 2, and doubles the window size each time until it reaches the full size of the array. Each time a step is processed on a window size, the 2 bitonic peaks are combined into a bitonc peak that is double the size. Eventually we end up with a single bitonic peak, and a final pass of the algorithm produces a sorted output. 

Though this algorithm is not normally used in software implementations, I decided to test its performance against the build in `Arrays.sort()` method call. I varied the number of threads in the pool, as well as the size of the data in order to get a good idea of how the algorithm performed.

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
