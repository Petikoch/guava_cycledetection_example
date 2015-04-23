package ch.petikoch.examples.guava

import com.google.common.util.concurrent.CycleDetectingLockFactory
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@SuppressWarnings("GroovyPointlessBoolean")
class Guava_CycleDetection_Example extends Specification {

    def 'cycle detection: simple cycle with two threads and two locks is detected'() {
        given:
        CycleDetectingLockFactory testee = CycleDetectingLockFactory.newInstance(CycleDetectingLockFactory.Policies.THROW)
        def lock_1 = testee.newReentrantLock("lock_1")
        def lock_2 = testee.newReentrantLock("lock_2")

        and:
        def clock = new Clock(0)
        def exceptionCollector = [].asSynchronized()

        and:
        Thread.startDaemon('thread_1: lock_1 -> lock_2') {
            clock.awaitTime(1)
            lock_1.lock()
            clock.advanceTime()

            clock.awaitTime(3)
            clock.advanceTime()
            try {
                lock_2.lock()
            } catch (CycleDetectingLockFactory.PotentialDeadlockException ex) {
                exceptionCollector.add(ex)
            } finally {
                clock.advanceTime() // signal "deadlock over"
            }
        }

        and:
        Thread.startDaemon('thread_2: lock_2 -> lock_1') {
            clock.awaitTime(2)
            lock_2.lock()
            clock.advanceTime()

            clock.awaitTime(4)
            clock.advanceTime()
            try {
                lock_1.lock()
            } catch (CycleDetectingLockFactory.PotentialDeadlockException ex) {
                exceptionCollector.add(ex)
            } finally {
                clock.advanceTime() // signal "deadlock over"
            }
        }

        when:
        clock.advanceTime() // start
        clock.awaitTime(6, TimeUnit.SECONDS.toMillis(10)) // await "deadlock over"

        then:
        exceptionCollector.size() >= 1
        //TODO more checks
    }

}
