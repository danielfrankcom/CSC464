import threading, queue, random, sys

class Vector():
    """
    Represents a vector of integers, each of which is the logical clock
    for a specific thread. Provides a methods to manipulate the vector,
    as well as to broadcast the current state of the vector to other
    objects through the use of a collection of queues.
    """

    def __init__(self, size, index, broadcast_queues):
        """
        Instantiate a new Vector object.

        size:               The number of integers in the vector.
        index:              The index in the vector that represents the logical clock
                            for the calling process.
        broadcast_queues:   The queues that will be written to on a call to the
                            broadcast() method.
        """
        self.values = [0 for i in range(size)]
        self.index = index
        self.broadcast_queues = broadcast_queues

    def increment(self):
        """
        Increase the logical clock of the vector owner by one tick.
        """
        self.values[self.index] += 1

    def merge(self, other_vector):
        """
        Replaces the current vector with an element-wise maximum of the current vector
        and the provided vector.

        other_vector:   The vector to combine with the current vector.
        """
        length = min(len(self.values), len(other_vector))
        self.values = [max(self.values[i], other_vector[i]) for i in range(length)]

    def get(self):
        """
        returns:    A list representation of the current vector.
        """
        return self.values

    def broadcast(self):
        """
        Send the current vector to all known message queues.
        """
        for queue in self.broadcast_queues:
            queue.put(self.values)


class Thread(threading.Thread):
    """
    Represents a thread that runs a series of events.
    """

    def __init__(self, message_queue, event_limit, vector, barrier):
        """
        Instantiate a new Thread object.

        message_queue:  The Queue object that provides vectors from events on other threads.
        event_limit:    The number of events for the thread to process.
        vector:         A Vector object that represents the vector for this thread.
        barrier:        A barrier that will be waited on when all events have been processed
                        by this thread to ensure synchronization.
        """
        super(Thread, self).__init__()
        self.message_queue = message_queue
        self.event_limit = event_limit
        self.vector = vector
        self.barrier = barrier

    def __receive__(self):
        """
        For all messages in the queue, update the vector for this thread.
        """
        done = False
        while not done:
            message = None
            try:
                message = self.message_queue.get_nowait()
            except queue.Empty:
                done = True

            if message:
                self.vector.increment()
                self.vector.merge(message)

    def __internal_event__(self):
        """
        Process an internal event that does not notify other threads.
        """
        self.__receive__()
        self.vector.increment()

    def __external_event__(self):
        """
        Process an external event that notifies other threads.
        """
        self.__receive__()
        self.vector.increment()
        self.vector.broadcast()

    def run(self):
        """
        Main loop of the thread. When it exits, the thread exits.
        """
        for i in range(self.event_limit):
            possible_events = [self.__internal_event__, self.__external_event__]
            event = random.choice(possible_events)
            event()

        """
        Ensure that all threads have processed their messages by blocking until
        all threads are finished sending messages, and then performing a final
        receive() call.
        """
        self.barrier.wait()
        self.__receive__()

    def join(self, timeout=None):
        """
        Block until the thread is finished.

        returns:    The final vector of the thread.
        """
        super(Thread, self).join(timeout)
        return self.vector


NUM_THREADS = 20
NUM_MESSAGES = 10

def verify_state(vectors):
    """
    Assert that all conditions specified by the 'vector clocks' algorithm hold.
    """

    """
    Each thread should have the highest value for their own logical clock in
    their own vector.
    """
    for i in range(NUM_THREADS):
        greater_than_own = [vector for vector in vectors if vector[i] > vectors[i][i]]
        assert greater_than_own == []

    """
    Check that all thread's independent vectors share a common state.
    """
    for thread in range(NUM_THREADS):
        for left_element in range(NUM_THREADS):
            for right_element in range(NUM_THREADS):

                """
                A thread may have experienced internal events following the final broadcast
                to other threads, so their own logical counter may have surpassed the logical
                counter for other threads locally. We want to exclude this case from the
                assertion as this is expected to be inconsistent with other thread's vectors.
                """
                if (vectors[thread][left_element] < vectors[thread][right_element]) and (thread != right_element):

                    # Ensure all threads share the same state.
                    for other_thread in range(NUM_THREADS):
                        # The comparing thread is excluded for the same reason as listed above.
                        if other_thread != left_element:
                            assert vectors[other_thread][left_element] <= vectors[other_thread][right_element]


def main():

    barrier = threading.Barrier(NUM_THREADS)
    message_queues = [queue.Queue() for i in range(NUM_THREADS)]
    threads = []

    # Generate threads, each with their own message queue and vector.
    for i in range(NUM_THREADS):
        broadcast_queues = message_queues.copy()
        del broadcast_queues[i]
        vector = Vector(NUM_THREADS, i, broadcast_queues)

        threads.append(Thread(message_queues[i], NUM_MESSAGES, vector, barrier))

    # Start all generated threads.
    for thread in threads:
        thread.start()

    # Wait for all threads to finish and retreive their vectors.
    resulting_vectors = []
    for thread in threads:
        vector_object = thread.join()
        vector_array = vector_object.get()
        resulting_vectors.append(vector_array)

    # Ensure that the returned vectors are correct.
    verify_state(resulting_vectors)


if __name__ == "__main__":
    for i in range(10):
        main()
