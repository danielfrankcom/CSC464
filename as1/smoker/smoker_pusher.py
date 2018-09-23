from threading import Semaphore
from threading import Lock
import _thread
import time

ITERATIONS = 10_000

def main():
    
    # will prevent main thread from exiting before its children
    threads_sem = Semaphore(0)

    # protects table from multiple agents adding items at once
    agent_sem = Semaphore(1)

    # protects actions performed by pushers
    mutex = Lock()

    # signal a resource is present to the relevant pusher
    tobacco = Semaphore(0)
    paper = Semaphore(0)
    match = Semaphore(0)

    # allow pushers to keep track of the items on the table
    resource_map = {
        tobacco: False,
        paper: False,
        match: False
    }

    # signal required resources present to smoker
    tobacco_sem = Semaphore(0)
    paper_sem = Semaphore(0)
    match_sem = Semaphore(0)

    # (tobacco, paper, match)
    # determine who should be signalled if the resources are present
    sem_map = {
        (True, True, False): match_sem,
        (True, False, True): paper_sem,
        (False, True, True): tobacco_sem,
    }

    # create agents with different resources
    agent_params = (threads_sem, agent_sem)
    create_agent_thread(agent_params + (tobacco, paper))
    create_agent_thread(agent_params + (paper, match))
    create_agent_thread(agent_params + (match, tobacco))

    # create pushers that wait for different resources to be available
    pusher_params = (threads_sem, mutex, sem_map, resource_map)
    create_pusher_thread(pusher_params + (tobacco, ))
    create_pusher_thread(pusher_params + (paper, ))
    create_pusher_thread(pusher_params + (match, ))

    # create smokers that wait for a set of resources to be available
    smoker_params = (threads_sem, agent_sem)
    create_smoker_thread(smoker_params + ("tobacco", tobacco_sem))
    create_smoker_thread(smoker_params + ("paper", paper_sem))
    create_smoker_thread(smoker_params + ("match", match_sem))

    # wait for threads to finish
    for i in range(9):
        threads_sem.acquire()

def create_agent_thread(params):
    """
    Creates a thread that is responsible for putting resources on the table.
    When a resource has been placed on the table, the agent signals the pusher
    that is associated with the resource that it is available.
    """
    _thread.start_new_thread(agent, params)

def agent(threads_sem, agent_sem, resource1_sem, resource2_sem):
    for i in range(ITERATIONS):
        agent_sem.acquire()
        resource1_sem.release()
        resource2_sem.release()

    # signal that the thread is complete
    threads_sem.release()

def create_pusher_thread(params):
    """
    Creates a thread that is responsible for notifying smokers when their
    required resources are available. If the pusher has been notified but only
    one of two required resources are available, it goes back to sleep.
    """
    _thread.start_new_thread(pusher, params)

def pusher(threads_sem, mutex, sem_map, resource_map, resource):
    for i in range(2 * ITERATIONS):
        resource.acquire()
        mutex.acquire(True) # block until acquired
        
        # mark the pusher's resource as present
        resource_map[resource] = True

        # get the semaphore to signal, or None if not enough resources
        sem_key = tuple(resource_map.values())
        sem = sem_map.get(sem_key)
        if sem:
            sem.release()
            
            # set all resources to not present
            falsed_map = dict.fromkeys(resource_map, False)
            resource_map.update(falsed_map)

        mutex.release()

    # signal that the thread is complete
    threads_sem.release()

def create_smoker_thread(params):
    """
    Creates a thread that is responsible for smoking when the required resources
    are available.
    """
    _thread.start_new_thread(smoker, params)

def smoker(threads_sem, agent_sem, name, resource_sem):
    for i in range(ITERATIONS):
        resource_sem.acquire()
        print("Smoker with " + name + " is smoking.")
        agent_sem.release()

    # signal that the thread is complete
    threads_sem.release()


if __name__ == "__main__":
    start_time = time.time()
    main()
    print("--- %s seconds ---" % (time.time() - start_time))
