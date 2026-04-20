package ca.mcgill.ecse420.a3;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue<T> {
    private final T[] items;
    private int head = 0;
    private int tail = 0;
    private final int capacity;

    private final AtomicInteger size = new AtomicInteger(0);

    private final ReentrantLock enqLock = new ReentrantLock();
    private final ReentrantLock deqLock = new ReentrantLock();

    private final Condition notFullCondition = enqLock.newCondition();
    private final Condition notEmptyCondition = deqLock.newCondition();

    @SuppressWarnings("unchecked")
    public BoundedQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }
        this.capacity = capacity;
        this.items = (T[]) new Object[capacity];
    }

    public void enq(T x) throws InterruptedException {
        boolean mustWakeDequeuers = false;

        enqLock.lock();
        try {
            while (size.get() == capacity) {
                notFullCondition.await();
            }

            items[tail] = x;
            tail = (tail + 1) % capacity;

            if (size.getAndIncrement() == 0) {
                mustWakeDequeuers = true;
            }
        } finally {
            enqLock.unlock();
        }

        if (mustWakeDequeuers) {
            deqLock.lock();
            try {
                notEmptyCondition.signalAll();
            } finally {
                deqLock.unlock();
            }
        }
    }

    public T deq() throws InterruptedException {
        T result;
        boolean mustWakeEnqueuers = false;

        deqLock.lock();
        try {
            while (size.get() == 0) {
                notEmptyCondition.await();
            }

            result = items[head];
            items[head] = null;
            head = (head + 1) % capacity;

            if (size.getAndDecrement() == capacity) {
                mustWakeEnqueuers = true;
            }
        } finally {
            deqLock.unlock();
        }

        if (mustWakeEnqueuers) {
            enqLock.lock();
            try {
                notFullCondition.signalAll();
            } finally {
                enqLock.unlock();
            }
        }

        return result;
    }

    public int size() {
        return size.get();
    }
}