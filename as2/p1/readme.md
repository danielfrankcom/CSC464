# Vector Clocks

This is an implementation of the [Vector Clock](https://en.wikipedia.org/wiki/Vector_clock) algorithm.

The main method of this implementation spins up 20 threads by default, each of which experience 10 events. Events may be internal, or they may be external, in which case the thread's vector is broadcast to all other threads. At the end of this execution, the state of each of the vectors is verified, as (excluding internal events) they should be aligned.
