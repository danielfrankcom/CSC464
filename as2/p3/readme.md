# Vector Clocks

This is a verification of the [Vector Clock](https://en.wikipedia.org/wiki/Vector_clock) algorithm using [PlusCal](https://lamport.azurewebsites.net/tla/pluscal.html).

> PlusCal (formerly called +CAL) is an algorithm language based on TLA+.  A PlusCal algorithm is translated to a TLA+ specification, which can be checked with the TLC model checker.

TLA+ is a specification language that allows rigorous checking of the correctness of an abstract algorithm. The abstract algorithm is written in PlusCal, which gets converted to TLA+, where the algorithm is represented as a state machine. The TLC model checker brute forces all state orderings, and verifies that assertions ar e true throughout execution. The model checker also provides access to temporal operators, such as 'if this thing is true now, then at some point this other thing will be true'.
