package other.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * LESSON K05 — Channels: communicating between coroutines.
 *
 * THE CORE IDEA
 * -------------
 * A `Channel` is a coroutine-safe QUEUE: one coroutine `send`s values, another `receive`s them,
 * and the channel handles all the hand-off safely. It is the coroutine embodiment of a famous rule:
 *
 *     "Do not communicate by sharing state; share state by communicating."
 *
 * Instead of several coroutines poking at one shared mutable variable (and needing locks/atomics to
 * do it safely — see lessons L01–L05), you pass ownership of each value THROUGH a channel. Only one
 * coroutine ever holds a given value at a time, so there is no shared-mutable-state to race over.
 *
 * This is the exact COROUTINE ANALOGUE of L06's producer/consumer with a JVM `BlockingQueue` +
 * poison pills. Compare the two side by side:
 *   - `BlockingQueue.put/take` BLOCK a whole OS thread; `Channel.send/receive` SUSPEND the coroutine
 *     (the thread is released to do other work — see K01 for "suspend, don't block").
 *   - In L06 we terminated consumers with a POISON PILL per consumer. A channel has a real
 *     end-of-stream signal built in: `close()`. A `for (x in channel)` loop ENDS when the channel is
 *     closed — no sentinel value needed, no risk of a pill being mistaken for data.
 *
 * BUFFERING (the capacity argument)
 * ---------------------------------
 *   - `Channel<T>()`            — RENDEZVOUS (capacity 0, the default). `send` suspends until a
 *                                 receiver is ready to `receive`, and vice versa. Zero buffer: the
 *                                 hand-off is a direct meeting point. This gives natural backpressure.
 *   - `Channel<T>(capacity)`    — BUFFERED. `send` only suspends once the buffer is full (bounded
 *                                 backpressure, just like L06's ArrayBlockingQueue(capacity)).
 *   - `Channel.UNLIMITED`       — buffer grows without bound; `send` never suspends (can blow memory).
 *   - `Channel.CONFLATED`       — keeps only the LATEST value; older un-received values are dropped.
 *
 * HOW TO READ THE TESTS
 * ---------------------
 * Concurrency is nondeterministic in TIMING, but the aggregate result must be deterministic. Each
 * test sends a known set of numbers, so the exact count of received items and their exact sum are
 * fixed no matter how the coroutines interleave — we assert those totals with `assertEquals`, every
 * run. Nothing can hang because every channel is ALWAYS closed (either explicitly or by `produce`,
 * which closes for us), so every `for (x in channel)` consumer loop is guaranteed to terminate. All
 * tests run inside `runBlocking { }`, so the method only returns once all coroutines have finished.
 */
@OptIn(ExperimentalCoroutinesApi::class)   // `produce { }` is still an experimental coroutines builder
class K05Channels {

    // Every test sends the values 1..N, so the totals are known ahead of time.
    private val n = 1_000
    private val expectedCount = n
    private val expectedSum = n.toLong() * (n + 1) / 2      // 1 + 2 + ... + n

    // ---------------------------------------------------------------------------------------------
    // 1. BASIC Channel: one producer sends then close()s; one consumer iterates until closed.
    // ---------------------------------------------------------------------------------------------

    /**
     * The simplest possible channel usage. We create a default (RENDEZVOUS, zero-buffer) channel.
     *
     * `send` and `receive` are SUSPENDING functions. On a rendezvous channel `send(x)` suspends until
     * some coroutine is ready to `receive` it — the two coroutines "meet" to hand the value over.
     * There is no buffer sitting between them (that is what capacity 0 means).
     *
     * TERMINATION: the producer calls `close()` when it has nothing more to send. The consumer's
     * `for (x in channel)` loop drains any remaining values and then ENDS on the close — this is the
     * clean, built-in alternative to L06's poison pill.
     */
    @Test
    fun `send then close, consume with a for-loop until closed`() = runBlocking {
        val channel = Channel<Int>()        // rendezvous: capacity 0, send suspends until a receiver waits

        // Producer coroutine: emit 1..n, then signal end-of-stream by closing.
        launch {
            for (x in 1..n) channel.send(x) // suspends here until the consumer takes each value
            channel.close()                 // MUST close, or the consumer's for-loop would hang forever
        }

        // Consumer: `for (x in channel)` receives until the channel is closed AND drained.
        var count = 0
        var sum = 0L
        for (x in channel) {
            count++
            sum += x
        }

        assertEquals(expectedCount, count)
        assertEquals(expectedSum, sum)
    }

    // ---------------------------------------------------------------------------------------------
    // 2. PRODUCER via the `produce { }` builder — returns a ReceiveChannel and closes automatically.
    // ---------------------------------------------------------------------------------------------

    /**
     * `produce { }` is a coroutine builder that launches a producer AND gives you back a
     * `ReceiveChannel<T>`. Inside the block, `this` is the channel, so you just `send`. The big win:
     * when the block ENDS the channel is CLOSED for you automatically (and it is also closed if the
     * block throws). No manual `close()` to forget — this is the idiomatic way to expose a stream.
     */
    @Test
    fun `produce builder emits a stream and closes automatically`() = runBlocking {
        // `produce` returns a ReceiveChannel; the receiver end is all the consumer can see.
        val numbers: ReceiveChannel<Int> = produce {
            for (x in 1..n) send(x)         // `this` is the channel here
        }                                   // <-- channel auto-closed when the block completes

        var count = 0
        var sum = 0L
        for (x in numbers) {                // terminates on the auto-close — cannot hang
            count++
            sum += x
        }

        assertEquals(expectedCount, count)
        assertEquals(expectedSum, sum)
    }

    // ---------------------------------------------------------------------------------------------
    // 3. FAN-OUT: one producer channel, MANY workers each receiving from it. Work is distributed.
    // ---------------------------------------------------------------------------------------------

    /**
     * FAN-OUT parallelizes a stream of work. A single `produce` channel is the source; several worker
     * coroutines all iterate the SAME channel with `for (x in channel)`. The channel hands each value
     * to exactly ONE waiting worker, so the items are split across workers automatically — no manual
     * partitioning, no worker ever sees the same item twice.
     *
     * Because each item is processed EXACTLY ONCE (delivered to exactly one worker) and the channel
     * closes after the source is exhausted, every worker loop terminates and the aggregate totals are
     * exact — regardless of how the scheduler divided the work between them. We use atomics only to
     * combine results computed on possibly-different threads (see L04).
     */
    @Test
    fun `fan-out distributes one stream across many workers, each item once`() = runBlocking {
        val workers = 4

        val source: ReceiveChannel<Int> = produce {
            for (x in 1..n) send(x)
        }

        val count = AtomicInteger(0)
        val sum = AtomicLong(0)

        // `coroutineScope` does not return until all workers finish — that is our join, no hang.
        coroutineScope {
            repeat(workers) {
                launch {
                    // Each worker pulls from the shared channel; the channel gives each value to just
                    // one worker, so together they cover the whole stream with no overlap.
                    for (x in source) {
                        count.incrementAndGet()
                        sum.addAndGet(x.toLong())
                    }
                }
            }
        }                                    // <-- all workers done here; source was closed by produce

        // Each of the n items was handled exactly once, so the totals match no matter the split.
        assertEquals(expectedCount, count.get())
        assertEquals(expectedSum, sum.get())
    }

    // ---------------------------------------------------------------------------------------------
    // 4. FAN-IN: MANY producers all sending into ONE shared channel; a single consumer aggregates.
    // ---------------------------------------------------------------------------------------------

    /**
     * FAN-IN is the mirror image of fan-out: several producer coroutines merge their output into ONE
     * shared channel, and a single consumer reads the combined stream.
     *
     * CLOSING WITH MULTIPLE SENDERS is the subtle part. The channel must be closed EXACTLY ONCE, and
     * only AFTER every producer has finished sending — close too early and a producer's `send` throws
     * on a closed channel; never close and the consumer hangs. The clean pattern: run all producers
     * inside a `coroutineScope { }` and let it JOIN them (it suspends until every child completes),
     * THEN call `close()`. The single close is owned by the coordinator, not by any individual sender.
     */
    @Test
    fun `fan-in merges many producers into one channel, closed once after all finish`() = runBlocking {
        val producers = 4
        val perProducer = n                          // each producer emits 1..n
        val channel = Channel<Int>()                 // one shared destination for every producer

        // Producer side: launch a coroutine that runs all producers, waits for them, then closes once.
        launch {
            coroutineScope {                         // suspends until ALL producers below have finished
                repeat(producers) {
                    launch {
                        for (x in 1..perProducer) channel.send(x)
                    }
                }
            }                                        // <-- every producer has finished sending here
            channel.close()                          // exactly one close, by the coordinator, after all sends
        }

        // Single consumer aggregates the merged stream until the (single) close ends the loop.
        var count = 0
        var sum = 0L
        for (x in channel) {
            count++
            sum += x
        }

        // Totals scale by the number of producers, since each produced the full 1..n sequence.
        assertEquals(expectedCount * producers, count)
        assertEquals(expectedSum * producers, sum)
    }
}
