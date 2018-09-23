1.
This problem models requirements in a system, where an action cannot be performed until a constraint is met. In such a scenario, threads must wait until all prerequisites are met before the action can be perfomed.

2.
The solution that I wrote in Go uses channels exclusively, and is conceptually a lot easier to understand. It is largely synchronous, with the main concurrent aspect coming from the fact that atoms are queued to be processed on one thread, and bonded into a molecule on another thread. The solution in Python is based on the 'Little Book of Semahpores', and represents each atom as a thread. It is much more difficult to understand due to the amount of synchronization primitves used, but it is far more concurrent than the Go-based solution. In both cases, the built in language libraries provided everything necessary to implement the solutions, so no extra plumbing was required on my part.

10,000 oxygen atoms, and 20,000 hydrogen atoms
Go:         28.86 ms
Python:     8.12 seconds

This is a pretty substantial difference in processing time, however I think that this was largely caused by the way that each of the languages were used for this implementation. Python's threads are not really designed to be rapidly spun up and shut down, wheras Goroutines are. Python was forced to start 30,000 threads in this implementation, while Go was only asked to start 1 Goroutine.
