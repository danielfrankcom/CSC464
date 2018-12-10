# Distributed Bitonic Sort 

I chose to implement the [bitonic sorting](http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/bitonic/bitonicen.htm) algorithm as my final project for this class. I wrote it in [Java](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html), with a build system that utilizes [Gradle](https://gradle.org/).

This is a sort that is not particularly fast in software but can be massively parallelized. It is commonly used in hardware sorting implementations due to the interesting property that it can be performed with the exact same set of comparisons each time, assuming that the input size is the same.

This implementation is a generalized version of the algorithm, that works for any input that has a size that is a power of 2. It models the parallel aspect of the algorithm by allocating small sorting tasks to a thread pool. The root thread controls the operation of the algorithm by distributing these tasks, ensuring that the threads are synchronized in such a way that the integrity of the algorithm is maintained.

## Building

1. Make sure the Java 8 JDK in installed.
2. Run `./gradlew` (on Linux or Windows).

You can find the produced `.jar` file in `build/libs/`.

The tests exercise all parts of the implementation for better than can be demonstrated through the entry point, however, the `.jar` file can be used to sort an array via the command line parameters.

## Using the `.jar` file

A limitation of the bitonic sorting algorithm is that it must have a set of data which has a length that is a power of 2 (i.e. 4, 16, 256). I could have dealt with this limitation by performing a regular sort on any overhanging data but decided to stick within the confines of the algorithm for demonstrative purposes.

Run `java -jar file-name-here.jar 12 5 4 6 1 0 8 7` (a data size of 8).

This above command will produce a sorted output on the command line.

The implementation that is accessible through the command line has a fixed number of threads and also has a fixed sorting order (ascending). If more control is required, see the heading below.

## Modifying the source

As mentioned above, there are some limitations that are enforced by using the `.jar` file. Modifying the source files will allow more specific scenarios.

The entry point (`src/main/java/com/distributed/Entry.java`) contains the `main()` method and can be modified to contain the desired parameters. Alternatively, if you are using [IntelliJ IDEA](https://www.jetbrains.com/idea/) or a similar IDE, you can modify and run the included [JUnit](https://junit.org/) tests.
