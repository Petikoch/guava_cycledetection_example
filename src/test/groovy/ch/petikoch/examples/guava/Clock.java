package ch.petikoch.examples.guava;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

// Just example code. Don't use in production!
public class Clock {

    private final AtomicLong internalTime;

    public Clock() {
        this(0);
    }

    public Clock(long startTime) {
        internalTime = new AtomicLong(startTime);
    }

    public void advanceTime() {
        long time = internalTime.incrementAndGet();
        System.out.println("Time is now: " + time);
    }

    public void awaitTime(long time) {
        try {
            awaitTime(time, Long.MAX_VALUE);
        } catch (TimeoutException e) {
            throw Throwables.propagate(e);
        }
    }

    public void awaitTime(long time, long timeoutMs) throws TimeoutException {
        System.out.println("Thread '" + Thread.currentThread().getName() + "' is waiting for time: " + time);
        Stopwatch stopWatch = Stopwatch.createStarted();
        Stopwatch showLifeSignStopWatch = Stopwatch.createStarted();
        while (internalTime.get() < time) {
            Thread.yield();
            if (stopWatch.elapsed(TimeUnit.MILLISECONDS) >= timeoutMs) {
                throw new TimeoutException();
            }
            if (showLifeSignStopWatch.elapsed(TimeUnit.SECONDS) >= 1) {
                showLifeSignStopWatch = Stopwatch.createStarted();
                System.out.println("Thread '" + Thread.currentThread().getName() + "' is still waiting for time: " + time);
            }
        }
        System.out.println("Thread '" + Thread.currentThread().getName() + "' time " + time + " arrived.");
    }

    @Override
    public String toString() {
        return "Clock{" +
                "internalTime=" + internalTime.get() +
                '}';
    }
}
