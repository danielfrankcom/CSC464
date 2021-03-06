# Unisex Bathroom Problem

This problem is described [here](http://greenteapress.com/semaphores/LittleBookOfSemaphores.pdf#section.6.2).

1. This problem represents a categorical exclusion problem, where a resource can only be allocated to a specific category of processes at the same time. In such a problem, the resource may be allocated to a specific number of processes of a certain category, but the categories cannot mix. An example that may require such a solution, is a set of processes that require exclusive access to individual mutexes, and are in the same category as they can run concurrently. A set of duplicate processes may be in another category, as if any 2 are mixed between the categories then one of the processes will be unable to operate, as they cannot acquire exclusive access to their required mutex.

2. The first of these solutions was based on the 'Little Book of Semaphores', while the second was a custom solution. I wrote both solutions in Go, so that comparing the 2 did not involve complications caused by the choice of language. As with one of my solutions to another problem, I had to add semaphore support in Go using a pattern found [here](http://www.golangpatterns.info/concurrency/semaphores). In this case, I also added an object to represent a lightswitch, which was required by the solution in the book. For my second solution, I decided to use channels exclusively (even though the other solution technically only uses channel, they're wrapped up and hidden). Go seems to be designed with channels as the primary communication mechanism, so I thought that it was only fair to the language to give them a shot. Both of the solutions are relatively easy to understand, however I found the channel syntax to be a little confusing, probably due to my inexperience with them.

   Another major difference between the implementations is the use of goroutines, as in the semaphore solution from the book, each person is represented as a new goroutine. The goroutine handles its own synchronization, and moves itself through the bathroom. In the solution that I implemented with channels, I created a slightly more synchronous flow. In the solution there is a managing thread that removes people from the queue and places them in the bathroom when they can be put there, and a second thread that removes people from the bathroom and notifies the managing thread when it is empty so that people of a different gender may enter.

|  Implementation    |  Time  |
|-|-|
| Channel-based    |  50.01 secs  |
| Semaphore-based  |  8.28  secs  |
Table: 1,000,000 people using the bathroom, randomized order of men and women

I was very surprised by this result, as I thought that creating so many excessive goroutines would cause a massive slowdown in the program. Additionally, the semaphore-based solution is hiding the use of channels behind 2 layered abstractions, so I was certain that the more synchronous channel-based solution would be faster.
