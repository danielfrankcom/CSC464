from threading import Semaphore
from threading import Lock
import _thread
import time

ITERATIONS = 10_000

class Table:
    """
    A platform that stores resources.
    """

    def __init__(self):
        self.resources = (None, None)

    def put(self, resource1, resource2):
        self.resources = (resource1, resource2)

    def peek(self):
        return self.resources

    def take(self):
        resources = self.resources
        self.resources = (None, None)
        return resources

    def is_empty(self):
        return self.resources == (None, None)

class Resource:
    """
    Represents an object that can be consumed for smoking.
    """

    def __init__(self, name):
        self.name = name

    def __str__(self):
        return self.name

class Tobacco(Resource):
    def __init__(self):
        super().__init__("tobacco")

class Paper(Resource):
    def __init__(self):
        super().__init__("paper")

class Match(Resource):
    def __init__(self):
        super().__init__("match")

class Counter:
    """
    Counts the number of successful smokes.
    """

    def __init__(self, goal):
        self.count = [0, 0, 0]
        self.goal = goal

    def increment(self, clazz):
        if clazz is Tobacco:
            self.count[0] += 1
        elif clazz is Paper:
            self.count[1] += 1
        elif clazz is Match:
            self.count[2] += 1

    def is_complete(self):
        return self.count == [self.goal, self.goal, self.goal]

def main():
    
    # prevents the main thread from triggering another task before the
    # previous task is complete
    trigger_sem = Semaphore(1)

    # create a table to store resources
    table = Table()

    # create a counter to end the program when each smoker reaches n cigarettes
    counter = Counter(ITERATIONS)

    # define a set of triggers to run each thread in a round-robin style
    triggers = []
    for i in range(6):
        triggers.append(Semaphore(0))

    # parameters shared by all threads
    shared_params = (trigger_sem, table)

    # create agents with different resources
    create_agent_thread(shared_params + (triggers[0], Tobacco, Paper))
    create_agent_thread(shared_params + (triggers[1], Paper, Match))
    create_agent_thread(shared_params + (triggers[2], Match, Tobacco))

    # create smokers that wait for a set of resources to be available
    create_smoker_thread(shared_params + (counter, triggers[3], Match, ))
    create_smoker_thread(shared_params + (counter, triggers[4], Tobacco, ))
    create_smoker_thread(shared_params + (counter, triggers[5], Paper, ))

    # trigger each thread in a round-robin style
    while(True):
        for trigger in triggers:
            trigger_sem.acquire()
            trigger.release()

            # exit when each smoker has had n cigarettes
            if counter.is_complete():
                return

def create_agent_thread(params):
    """
    Creates a thread that is responsible for putting resources on the table.
    When a resource has been placed on the table, the agent signals the pusher
    that is associated with the resource that it is available.
    """
    _thread.start_new_thread(agent, params)

def agent(trigger_sem, table, trigger, resource1, resource2):
    while True:
        trigger.acquire()

        if table.is_empty():
            # create each new resource and put them on the table
            table.put(resource1(), resource2())

        trigger_sem.release()

def create_smoker_thread(params):
    """
    Creates a thread that is responsible for smoking when the required resources
    are available.
    """
    _thread.start_new_thread(smoker, params)

def smoker(trigger_sem, table, counter, trigger, current_resource):
    while True:
        trigger.acquire()

        # check if the things on the table are the things we need
        available_resources = table.peek()
        if( not isinstance(available_resources[0], current_resource) and
                not isinstance(available_resources[1], current_resource)):

            table.take()
            print("Smoker with " + str(current_resource()) + " is smoking.")
            counter.increment(current_resource)

        trigger_sem.release()


if __name__ == "__main__":
    start_time = time.time()
    main()
    print("--- %s seconds ---" % (time.time() - start_time))
