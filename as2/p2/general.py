from enum import Enum
import copy, random

Decision = Enum("Decision", "retreat attack")


class Node():

    def __init__(self, decision):
        self.decision = decision
        self.path = ""
        self.output = None
        self.children = []

    def update_majority(self):
        if len(self.children) == 0:
            self.output = self.decision
            return

        decisions = {"retreat": 0, "attack": 0}
        for node in self.children:
            node.update_majority()
            decisions[node.output.name] += 1
        output = max(decisions, key=decisions.get)
        self.output = Decision[output]

    def add_child(self, node):
        self.children.append(node)


class General():

    def __init__(self, identifier, decision, is_traitor):
        self.identifier = identifier
        self.decision = decision
        self.tree = None
        self.is_traitor = is_traitor

    def __get_nodes__(self, round, root):
        if round == 0:
            return [root]

        result = []
        for child in root.children:
            result = result + self.__get_nodes__(round - 1, child)

        return result


    def broadcast(self, round, generals):
        if not self.tree:
            self.tree = Node(self.decision)

        nodes = self.__get_nodes__(round, self.tree)
        for node in nodes:
            if self.identifier not in node.path:
                for general in generals:
                    to_send = copy.deepcopy(node)
                    to_send.path += self.identifier
                    to_send.children = []
                    general.receive(to_send)


    def __find_parent__(self, path, root):
        for child in root.children:
            if path.startswith(child.path):
                return self.__find_parent__(path, child)

        return root


    def receive(self, node):
        if self.is_traitor:
            node.decision = random.choice([Decision.attack, Decision.retreat])

        if not self.tree:
            self.tree = node
            return

        parent = self.__find_parent__(node.path, self.tree)
        parent.add_child(node)


GENERAL = 1
GOOD = 3
BAD = 2
TOTAL = GENERAL + GOOD + BAD
RECURSION = 2

def run(commander_is_traitor, faithful_generals, traitor_generals):

    commander = General("1", Decision.attack, commander_is_traitor)
    generals = []

    for i in range(2, faithful_generals + 2):
        generals.append(General(str(i), None, False))

    for i in range(faithful_generals + 2, traitor_generals + faithful_generals + 2):
        generals.append(General(str(i), None, True))

    commander.broadcast(0, generals)

    for i in range(RECURSION):
        for general in generals:
            general.broadcast(i, generals)

    
    decisions = {"retreat": 0, "attack": 0}
    for general in generals:
        general.tree.update_majority()
        decisions[general.tree.output.name] += 1

    if decisions["attack"] >= faithful_generals:
        return True
    return False


if __name__ == "__main__":
    RUNS = 50000
    success = 0
    for i in range(RUNS):
        if run(False, 4, 2):
            success += 1

    print(str(success / RUNS) + "% success rate")
