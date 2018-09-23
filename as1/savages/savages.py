from threading import Semaphore
from threading import Thread
import time

CAPACITY = 20 # servings in a pot
COOKS = 2
COOK_QUOTA = 5_000
SAVAGES = COOKS * 4
SAVAGE_QUOTA = int((COOKS * COOK_QUOTA * CAPACITY) / SAVAGES)

# primitives
servings = 0
mutex = Semaphore(1)
empty_pot = Semaphore(0)
full_pot = Semaphore(0)

def main():

    threads = []

    for i in range(COOKS):
        cook_t = Thread(target=cook, args=())
        cook_t.start()
        threads.append(cook_t)

    for i in range(SAVAGES):
        savage_t = Thread(target=savage, args=())
        savage_t.start()
        threads.append(savage_t)

    for thread in threads:
        thread.join()

def cook():
    for i in range(COOK_QUOTA):
        empty_pot.acquire()
        fill_pot(CAPACITY)
        full_pot.release()

def fill_pot(amount):
    print("Pot is now full")

def savage():
    global servings

    for i in range(SAVAGE_QUOTA):
        mutex.acquire()
        
        if servings == 0:
            empty_pot.release()
            full_pot.acquire()
            servings = CAPACITY

        servings -= 1
        get_serving()

        mutex.release()

        print("Savage is eating")

def get_serving():
    print("Savage retreived serving")

if __name__ == "__main__":
    start_time = time.time()
    main()
    print("--- %s seconds ---" % (time.time() - start_time))
