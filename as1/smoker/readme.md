1. This problem shows up commonly in operating system process scheduling, as certain resources are available to the processes at different times. Each process needs a specific set of resources, which cannot be consumed by other processes at the same time. When the processes are available, they must acquire them all exclusively.

2. I wrote both of the implementations in Python, with the first being heavily inspired by the 'Little Book of Semaphores' solution, and the second being a custom design. The first solution is correct, as described in the book. I wrote it while attempting to minimize duplicate code. As a result, the code itself is concise, but not necessarily easy to understand as it is quite abstract. Though it would be potentially easier to understand if each agent, pusher, and smoker was a seperate method, it would be much more difficult to make modifications to the code.
 The second solution is a custom design, so is only correct as far as I can tell. It was written as an experiment in fairness, and is not actually a good solution to the problem. In the solution, an orchestrating thread triggers each of the worker threads repeatedly in the same order, and if the thread is awoken with nothing to do, it goes back to sleep. For this solution, I also adopted more of an object-oriented approach, to improve readability. Though this is not what I expected, as you can see below, it is actually faster than the solution from the book. I believe this is because there are 3 less threads, as the middle-man pushers are not necessary.

|  Implementation    |  Time  |
|-|-|
| Push-based    |  4.34 secs  |
| Round-robin   |  3.03 secs  |
Table: 10,000 cigarettes per smoker

This result surprised me quite a bit, as I had no idea that such a performance gain was possible in this case. Considering that the code between the languages was practically identical, and that Go was using an non-ideal synchronization primitive, I am very impressed.

