package ch.petikoch.examples.guava;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class Clock {

    private final AtomicLong internalTime;

    public Clock() {
        this(0);
    }

    public Clock(long startTime) {
        internalTime = new AtomicLong(startTime);
    }

    public void advanceTime(){
        internalTime.incrementAndGet();
    }

    public void awaitTime(long time){
        while(internalTime.get() < time){
            Thread.yield();
        }
    }

    public void awaitTime(long time, long timeoutMs) throws TimeoutException {
        Stopwatch stopWatch = Stopwatch.createStarted();
        while(internalTime.get() < time){
            Thread.yield();
            if(stopWatch.elapsed(TimeUnit.MILLISECONDS) >= timeoutMs){
              throw new TimeoutException();
            }
        }
    }

    @Override
    public String toString() {
        return "Clock{" +
                "internalTime=" + internalTime.get() +
                '}';
    }
}
