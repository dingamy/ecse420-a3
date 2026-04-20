package ca.mcgill.ecse420.a3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class FineListTest {

    @Test
    public void testContainsSingleThread() {
        FineList<Integer> list = new FineList<>();

        // Initially empty
        assertFalse(list.contains(10));

        // Add elements
        list.add(10);
        list.add(20);
        list.add(30);

        // Check existing elements
        assertTrue(list.contains(10));
        assertTrue(list.contains(20));
        assertTrue(list.contains(30));

        // Check non-existing element
        assertFalse(list.contains(40));

        // Remove and test again
        list.remove(20);
        assertFalse(list.contains(20));
    }

    @Test
    public void testContainsMultiThreaded() throws InterruptedException {
        FineList<Integer> list = new FineList<>();

        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                assertTrue(list.contains(i));
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 100; i < 200; i++) {
                list.add(i);
            }
        });

        Thread t3 = new Thread(() -> {
            for (int i = 50; i < 100; i++) {
                list.remove(i);
            }
        });

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        for (int i = 0; i < 50; i++) {
            assertTrue(list.contains(i));
        }

        for (int i = 50; i < 100; i++) {
            assertFalse(list.contains(i));
        }

        for (int i = 100; i < 200; i++) {
            assertTrue(list.contains(i));
        }
    }
}