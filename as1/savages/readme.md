1. This problem may occur in a task queue, where some orchestrating object is responsible for filling a queue with work for threads to process. If the work is seperated into logic chunks, the orchestrating thread may wait for all of one set of work to be complete before refilling the queue.

2. These implementations were both based on the solution in the 'Little Book of Semaphores', but were written in both Python and Go. The intention was to write identical code in both languages, and compare the syntax and performance as such. Both solutions are very readable, however they use global variables for the synchronization primitives for simplicity. They both start the same number of executors, and process the same amount of data.
The Python solution uses the built in semaphores, and was very easy to put together. The way that the threads are started and subsequently joined is understandable, but could use improvement. There was also a complication around modifying the global 'servings' variable, as in Python you need to use the 'global' keyword to make global variable mutable. I also had to do an integer conversion, as Python decided to convert the integer to a float.
The Go solution was a little more difficult to debug, though I suspect that this is due to my inexperience with the language. Everything in the Go code looks clean, and the WaitGroup is a nice addition to wait for threads to finish. Additionally, there was no complication around the global variables in Go, nor any integer conversions. I had to implement a simple semaphore using channels from the following link: 'http://www.golangpatterns.info/concurrency/semaphores'. This was because I could not find a Go package that provided the semaphore functionality that I needed. In this aspect, Python had a clear edge. Once I added the functionality using channels however, there were no problems with the implementation. I understand that Go may be designed to take advantage of channels primarily, and other synchronization primitives, so its ease of use for shoe-horned semaphores is impressive.

|  Implementation    |  Time  |
|-|-|
|  Go    |  1.01 secs  |
| Python |  10.17 secs |
Table: 2,000,000 servings produced by 2 cooks, consumed by 8 savages

This result surprised me quite a bit, as I had no idea that such a performance gain was possible in this case. Considering that the code between the languages was practically identical, and that Go was using an non-ideal synchronization primitive, I am very impressed.

