# Byzantine Generals

This is an implementation of the [Byzantine Generals](https://marknelson.us/posts/2007/07/23/byzantine.html) problem.

The `generals.py` file can be called with arbitrary input, or the `test.py` file can be used to exercise the other file, and to test the implementation. The tests loop through a series of recursion levels, and combinations of traitors and not traitors. They verify that the outcome is unanymous among the loyal generals.

## Example

`python generals.py -r 2 -o attack false true false false`

In the above example, the following is true:
 - The algorithm will run to a recursion level of 2
 - The commander will send an order of 'attack'
 - The commander is not a traitor (he is the first in the provided list)
 - The rest of the generals in order are: (a traitor, not a traitor, not a traitor)

## Problems

It would seem that my implementation is not perfect, so I have outlined the current problems that I see with it.

### Testing Failure Cases

It is trivial to test a success case for the Byzantine Generals problem, as you can confirm that every loyal general received the correct value. It is not so trivial to test a failure case, as the traitorous generals are not intelligent enough to intentionally sabotage the system, and simply send pseudo-random decisions. It is possible that the system functions as expected for certain inputs, so it is difficult to test whether or not the traitors are sabotaging the system.

### Recursion Levels

The tests verify that all loyal generals receive the correct result, but only up to a recursion level of 3. If I raise the recursion level above this point, the results seem to vary. I believe this is caused by a bug in my code that selecs the relevant nodes in the tree based on a recursion level, but I have no hard evidence to prove this. I attempted to debug the problem, but the way that I structured the code for my trees was non-ideal, and made debugging very difficult.