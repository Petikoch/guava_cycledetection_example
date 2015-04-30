package com.google.common.util.concurrent;

import com.google.common.util.concurrent.CycleDetectingLockFactory.Policies;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class CycleDetectingLockFactoryTest extends TestCase {

    private ReentrantLock lockA;
    private ReentrantLock lockB;

    @Override
    protected void setUp() throws Exception {
        CycleDetectingLockFactory factory =
                CycleDetectingLockFactory.newInstance(Policies.THROW);
        lockB = factory.newReentrantLock("LockB");
        lockA = factory.newReentrantLock("LockA");
    }

    //------
    // FALSE POSITIVE?
    //-----
    public void testDeadlock_twoLocks_falsePositive() {
        // Establish an acquisition order of lockA -> lockB.
        lockA.lock();
        lockB.lock();
        lockA.unlock();
        lockB.unlock();

        // The opposite order fails afterwards, although there is no deadlock ANYWHERE
        lockB.lock();
        lockA.lock();
    }

    //------
    // REAL DEADLOCK NOT RECOGNIZED?
    //-----
    public void testDeadlock_twoThreads_realDeadlock_notDetectedOrPrevented() throws InterruptedException {
        final CountDownLatch t1HasLockA = new CountDownLatch(1);
        final CountDownLatch t2HasLockB = new CountDownLatch(1);
        final CountDownLatch allThreadsFinished = new CountDownLatch(2);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //prepare
                    lockA.lock();
                    t1HasLockA.countDown();
                    t2HasLockB.await();

                    // now deadlock
                    lockB.lock();
                } catch (Exception ignored) {
                    // expected due to the use of CycleDetectingLockFactory
                    ignored.printStackTrace();
                } finally {
                    lockA.unlock();
                    lockB.unlock(); // just in case
                    allThreadsFinished.countDown();
                }
            }
        });
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // prepare
                    t1HasLockA.await();
                    lockB.lock();
                    t2HasLockB.countDown();

                    // now deadlock
                    lockA.lock();
                } catch (Exception ignored) {
                    // expected due to the use of CycleDetectingLockFactory
                    ignored.printStackTrace();
                } finally {
                    lockB.unlock();
                    lockA.unlock(); // just in case
                    allThreadsFinished.countDown();
                }
            }
        });
        t2.setDaemon(true);
        t2.start();

        t1HasLockA.await();
        System.out.println("Thread 1 has Lock A");
        t2HasLockB.await();
        System.out.println("Thread 2 has Lock A");

        allThreadsFinished.await();
    }
}
