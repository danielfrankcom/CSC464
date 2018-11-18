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
