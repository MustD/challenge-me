package other.concurrency

import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * LESSON 06 — The Producer–Consumer pattern (bounded buffer).
 *
 * THE CORE IDEA
 * -------------
 * Producers create work; consumers process it. They usually run at different speeds, so we
 * decouple them with a SHARED BOUNDED BUFFER sitting between them:
 *
 *     [producers] --put--> [ buffer, capacity N ] --take--> [consumers]
 *
 * The buffer being *bounded* is the whole point — it provides BACKPRESSURE:
 *   - When the buffer is FULL, `put` BLOCKS. Fast producers are forced to wait for slow consumers,
 *     so an unbounded backlog can never build up and blow out memory.
 *   - When the buffer is EMPTY, `take` BLOCKS. Consumers idle cheaply instead of spinning.
 * Bounded capacity is what turns "two sides running at different speeds" into a self-regulating
 * system: the slower side sets the pace, and neither side has to know about the other.
 *
 * The hard part is coordination: how does a producer *wait* for a free slot, and how does it
 * *wake up* a consumer that's blocked on an empty buffer (and vice versa)? Doing that correctly
 * with raw threads is fiddly, so this file shows it twice:
 *
 * THE TWO APPROACHES
 * ------------------
 *   1. HIGH-LEVEL — `ArrayBlockingQueue<Int>(capacity)`. The JDK's blocking queue does all the
 *      waiting and signaling for you. `put()` blocks when full, `take()` blocks when empty. You
 *      just write straight-line code and let the queue handle every hard part.
 *   2. LOW-LEVEL — a hand-rolled `BoundedBuffer` using a plain `ArrayDeque` guarded by
 *      `synchronized`, with `wait()`/`notifyAll()`. This is *exactly* the machinery
 *      `ArrayBlockingQueue` wraps. Building it once demystifies the high-level version.
 *
 * TERMINATION — the POISON PILL
 * -----------------------------
 * A consumer blocked on `take()` will wait forever. How does it know production is done? We send
 * one POISON PILL per consumer — a sentinel value (here `Int.MIN_VALUE`) that means "stop". Each
 * consumer, on seeing a pill, exits its loop. N pills => all N consumers terminate. This keeps the
 * test deterministic and guarantees it never hangs.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Concurrency is nondeterministic in *timing* but the aggregate result must be deterministic. We
 * produce a known set of numbers, so the exact count of consumed items and their exact sum are
 * fixed no matter how threads interleave. Both tests assert those two totals with `assertEquals`,
 * every run. If a single item were dropped or double-counted, the sum would betray it.
 */
class L06ProducerConsumer {

    private val producers = 4
    private val consumers = 3
    private val itemsPerProducer = 50_000
    private val poisonPill = Int.MIN_VALUE               // sentinel: never a real payload value.

    // Every producer emits values 1..itemsPerProducer, so the totals are known ahead of time.
    private val totalItems = producers * itemsPerProducer
    private val sumPerProducer = itemsPerProducer.toLong() * (itemsPerProducer + 1) / 2  // 1+2+...+n
    private val expectedSum = sumPerProducer * producers

    // ---------------------------------------------------------------------------------------------
    // 1. HIGH-LEVEL: ArrayBlockingQueue does the blocking/signaling for us.
    // ---------------------------------------------------------------------------------------------
    @Test
    fun `ArrayBlockingQueue coordinates producers and consumers with backpressure`() {
        // Capacity 16 is deliberately tiny relative to the 200k items — it guarantees the queue
        // fills up and producers actually block on put(), exercising the backpressure path.
        val buffer: BlockingQueue<Int> = ArrayBlockingQueue(16)

        val consumedCount = AtomicInteger(0)
        val consumedSum = AtomicLong(0)

        // Consumers: take() until a poison pill arrives. take() blocks (cheaply) while empty.
        val consumerThreads = (1..consumers).map {
            thread {
                while (true) {
                    val item = buffer.take()             // blocks until an item is available.
                    if (item == poisonPill) break        // our signal that production is finished.
                    consumedCount.incrementAndGet()
                    consumedSum.addAndGet(item.toLong())
                }
            }
        }

        // Producers: put() each value; put() blocks while the buffer is full (backpressure).
        val producerThreads = (1..producers).map {
            thread {
                for (value in 1..itemsPerProducer) buffer.put(value)
            }
        }

        // Order matters: all real data must be in the pipeline before any pill, otherwise a
        // consumer could grab a pill and quit while items are still coming. So we join producers
        // FIRST, then enqueue exactly one pill per consumer.
        producerThreads.forEach { it.join() }
        repeat(consumers) { buffer.put(poisonPill) }
        consumerThreads.forEach { it.join() }            // no thread left running => no hang.

        assertEquals(totalItems, consumedCount.get())
        assertEquals(expectedSum, consumedSum.get())
    }

    // ---------------------------------------------------------------------------------------------
    // 2. LOW-LEVEL: hand-rolled bounded buffer with wait()/notifyAll() — the primitive ABQ wraps.
    // ---------------------------------------------------------------------------------------------

    /**
     * A minimal bounded buffer built from scratch. State (`items`) is guarded by `synchronized(this)`
     * so only one thread mutates it at a time. Threads that can't proceed release the monitor and
     * sleep via `wait()`; whoever changes the state calls `notifyAll()` to wake them.
     */
    private class BoundedBuffer(private val capacity: Int) {
        private val items = ArrayDeque<Int>()

        @Synchronized
        fun put(item: Int) {
            // WHY `while`, NOT `if`:
            //   (a) Spurious wakeups — wait() may return without any notify at all (allowed by the JVM).
            //   (b) Stolen slot — even after a real notify, another producer may run first and refill
            //       the buffer before we reacquire the lock. So we must RE-CHECK the predicate after
            //       every wakeup, not assume it still holds. `while` re-tests; `if` would blindly proceed.
            while (items.size == capacity) {
                (this as Object).wait()          // releases the monitor, parks until notified.
            }
            items.addLast(item)
            // WHY notifyAll, NOT notify: one condition variable (`this`) serves BOTH "not full" and
            // "not empty" waiters. notify() wakes a single arbitrary thread — it might wake another
            // producer that then re-sleeps, leaving a waiting consumer stuck (lost-wakeup deadlock).
            // notifyAll() wakes everyone; the `while` loops re-check and the right ones proceed.
            (this as Object).notifyAll()
        }

        @Synchronized
        fun take(): Int {
            while (items.isEmpty()) {
                (this as Object).wait()          // symmetric: wait for a producer to add something.
            }
            val item = items.removeFirst()
            (this as Object).notifyAll()         // signal producers that a slot just freed up.
            return item
        }
    }

    @Test
    fun `hand-rolled bounded buffer with wait-notifyAll matches the totals`() {
        val buffer = BoundedBuffer(capacity = 8)

        val consumedCount = AtomicInteger(0)
        val consumedSum = AtomicLong(0)

        val consumerThreads = (1..consumers).map {
            thread {
                while (true) {
                    val item = buffer.take()
                    if (item == poisonPill) break
                    consumedCount.incrementAndGet()
                    consumedSum.addAndGet(item.toLong())
                }
            }
        }

        val producerThreads = (1..producers).map {
            thread {
                for (value in 1..itemsPerProducer) buffer.put(value)
            }
        }

        producerThreads.forEach { it.join() }
        repeat(consumers) { buffer.put(poisonPill) }     // one pill per consumer => all terminate.
        consumerThreads.forEach { it.join() }

        // Same deterministic totals as the ArrayBlockingQueue version — because our tiny buffer
        // provides the exact same guarantees, just written out by hand.
        assertEquals(totalItems, consumedCount.get())
        assertEquals(expectedSum, consumedSum.get())
    }
}
