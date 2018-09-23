from threading import Semaphore
from threading import Thread
import random
import datetime
import time

class Lightswitch:
    def __init__(self):
        self.counter = 0
        self.mutex = Semaphore(1)

    def lock(self, semaphore):
        self.mutex.acquire()

        self.counter += 1
        if self.counter == 1:
            semaphore.acquire()

        self.mutex.release()

    def unlock(self, semaphore):
        self.mutex.acquire()

        self.counter -= 1
        if self.counter == 0:
            semaphore.release()

        self.mutex.release()

class TimeTracker:
    def __init__(self):
        self.highest = float("-inf")
        self.total = 0
        self.count = 0

    def notify(self, nanos):
        if self.highest < nanos:
            self.highest = nanos

        self.total += nanos
        self.count += 1

    def getHighest(self):
        return self.highest

    def getAverage(self):
        return self.total / self.count


# constants
READERS = WRITERS = 30_000

# primitives
switch = Lightswitch()
roomEmpty = Semaphore(1)
turnstile = Semaphore(1)

tracker = TimeTracker()

def main():

    threads = []

    for i in range(READERS):
        reader_t = Thread(target=reader, args=())
        threads.append(reader_t)

    for i in range(WRITERS):
        writer_t = Thread(target=writer, args=())
        threads.append(writer_t)

    random.shuffle(threads)

    for thread in threads:
        thread.start()

    for thread in threads:
        thread.join()

    print("Average: {:f} ms".format(tracker.getAverage()))
    print("Highest: {:f} ms".format(tracker.getHighest()))

def writer():
    start = datetime.datetime.now()

    turnstile.acquire()
    roomEmpty.acquire()
    print("write")
    turnstile.release()
    roomEmpty.release()

    end = datetime.datetime.now()
    micros = (end - start).total_seconds() * 1000
    tracker.notify(micros)

def reader():
    start = datetime.datetime.now()

    turnstile.acquire()
    turnstile.release()
    switch.lock(roomEmpty)
    print("read")
    switch.unlock(roomEmpty)

    end = datetime.datetime.now()
    micros = (end - start).total_seconds() * 1000
    tracker.notify(micros)


if __name__ == "__main__":
    start_time = time.time()
    main()
    print("--- %s seconds ---" % (time.time() - start_time))

