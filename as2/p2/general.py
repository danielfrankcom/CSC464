from enum import Enum
import copy, argparse

# Represents the possible orders that a commander can give
Order = Enum("Order", "retreat attack")


class Node():
    """
    Represents a decision node that contains a path.
    """

    def __init__(self, decision):
        """
        Create a new Node object.
        decision:   The Order associated with this node
        """
        self.decision = decision
        self.path = ""
        self.output = None
        self.children = []

    def update_majority(self):
        """
        Updates the output of this node and all of its children.
        """
        if len(self.children) == 0:
            self.output = self.decision
            return

        decisions = {"retreat": 0, "attack": 0}
        for node in self.children: # only capture direct children
            node.update_majority()
            decisions[node.output.name] += 1
        output = max(decisions, key=decisions.get)
        self.output = Order[output]

    def add_child(self, node):
        """
        Add the provided node as a child.
        node:   The new child
        """
        self.children.append(node)


class General():
    """
    Represents a general that may or may not be a traitor.
    """

    def __init__(self, identifier, decision, is_traitor):
        """
        Create a new General object.
        identifier:     A string that represents the name of the general
        decision:       The Order that this general has chosen
        is_traitor:     True if the general is a traitor, False otherwise
        """
        self.identifier = identifier
        self.decision = decision
        self.tree = None
        self.is_traitor = is_traitor

    def __get_nodes__(self, round, root):
        """
        Internal use only.
        Finds all nodes that are associated with a particular recursion round.
        round:      The recursion round to search for
        root:       The node to start the search from
        returns:    A list of nodes that are associated with the provided recursion round
        """

        if round == 0:
            return [root]

        result = []
        for child in root.children:
            result = result + self.__get_nodes__(round - 1, child)

        return result


    def broadcast(self, round, generals):
        """
        Broadcast the current round of nodes to all provided generals
        round:      An int representing the recursion round
        generals:   A list of Generals to send messages to
        """
        if not self.tree:
            self.tree = Node(self.decision)

        nodes = self.__get_nodes__(round, self.tree)
        for node in nodes:
            if self.identifier not in node.path: # prevent cycles in the path
                for general in generals:
                    to_send = copy.deepcopy(node)
                    to_send.path += self.identifier
                    to_send.children = [] # may have children on our node
                    if self.is_traitor and int(general.identifier) % 2 == 1:
                        # flip the order if the receiving general number is even
                        current = to_send.decision
                        to_send.decision = Order(current.value % 2 + 1)
                    general.receive(to_send)


    def __find_parent__(self, path, root):
        """
        Internal use only.
        Finds the appropriate parent for a node with a specific path.
        path:       The path of the node that is being added
        root:       The node to start the search from
        returns:    The node that should be a parent of the new one
        """
        for child in root.children:
            if path.startswith(child.path):
                return self.__find_parent__(path, child)

        return root


    def receive(self, node):
        """
        Receive a message from another general.
        node:   The message to receive
        """
        if not self.tree:
            self.tree = node
            return

        parent = self.__find_parent__(node.path, self.tree)
        parent.add_child(node)


def run(recursion, decision, alliances):
    """
    Convert a command line string to an 'Order' type.
    recursion:  An int representing the level of message-sending recursion
    decision:   An Order representing the commander's choice
    alliances:  A list containing a bool for each general, where True represents a traitor.
                Must contain at least one value.
    returns:    A list of the decisions of each provided general
    """

    commander = General("1", decision, alliances[0])

    generals = []
    for i in range(1, len(alliances)):
        generals.append(General(str(i + 1), None, alliances[i]))

    commander.broadcast(0, generals)

    for i in range(recursion):
        for general in generals:
            general.broadcast(i, generals)

    
    decisions = [decision] # represents the commander
    for general in generals:
        general.tree.update_majority()
        decisions.append(general.tree.output)

    return decisions


def to_decision(arg):
    """
    Convert a command line string to an 'Order' type.
    arg:        The string that contains the order
    returns:    An 'Order' value matching the input
    """
    if arg.lower() == "attack":
        return Order.attack
    elif arg.lower() == "retreat":
        return Order.retreat
    else:
        raise argparse.ArgumentTypeError("Order value expected.")


def to_bool(arg):
    """
    Convert a command line boolean value to an actual boolean.
    arg:        The string that contains the value
    returns:    A 'bool' value matching the input
    """
    if arg.lower() in ("true", "t"):
        return True
    elif arg.lower() in ("false", "f"):
        return False
    else:
        raise argparse.ArgumentTypeError("Boolean value expected.")


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Byzantine generals problem.")
    parser.add_argument("-r", "--recursion", type=int, default=2, help="level of recursion for the algorithm")
    parser.add_argument("-o", "--order", type=to_decision, default=Order.retreat, help="decision that the commander broadcasts (if loyal)")
    parser.add_argument("generals", nargs="*", type=to_bool, help="defines the alliance and quantity of generals, 'true' is a traitor")

    args = parser.parse_args()
    if len(args.generals) < 1:
        raise argparse.ArgumentTypeError("No commander specified")

    result = run(args.recursion, args.order, args.generals)

    successes = 0
    failures = 0
    for i in range(len(result)):
        if not args.generals[i]: # general is loyal
            if result[i] == args.order:
                successes += 1
            else:
                failures += 1

    print("%.2f%% of loyal generals chose the correct command." % (successes / (successes + failures)))
