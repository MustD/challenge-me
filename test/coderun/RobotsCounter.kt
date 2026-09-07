package coderun

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * Robots counter (robot users counter) — design/simulation task
 *
 * Design a class that, given a stream of "a user made a request" events, can at any moment report
 * how many users are behaving like robots:
 *
 * ```kotlin
 * class EventProcessor(requestThreshold: Int, timeThreshold: Long) {
 *     fun registerEvent(now: Long, userId: Long)     // register a user request
 *     fun getRobotsCount(now: Long): Int             // how many users made
 *                                                    // > requestThreshold requests
 *                                                    // within the last timeThreshold of time
 * }
 * ```
 *
 * Stream guarantee (as in a real service): time never goes backwards — `now` is monotonically
 * non-decreasing across consecutive `registerEvent` / `getRobotsCount` calls.
 *
 * The window is the half-open interval `(now - timeThreshold, now]`: an event is "alive" while
 * `eventTime > now - timeThreshold`. The "robot" condition is strict: `count > requestThreshold`.
 *
 * The harness cannot test classes here (a `typealias` is a function), so the solution is shaped as
 * a driver: it constructs the processor state and replays a list of operations. An operation is a
 * row `[type, now, userId]`:
 *   - `type = 0` → `registerEvent(now, userId)`;
 *   - `type = 1` → `getRobotsCount(now)`, `userId` is ignored (write 0).
 * Only the answers to type-1 operations are returned, in order.
 */
typealias TRobots = (Int, Long, Array<IntArray>) -> List<Int>

class RobotsCounter {

    @Nested
    inner class Solution : ProblemTest<TRobots> {

        override val cases = testCases<TRobots>(
            // 3 requests from one user with threshold 2 — a robot
            args(2, 10L, "[[0,1,1],[0,2,1],[0,3,1],[1,5,0]]") expects "[1]",
            // events age out: by now=6 only one request is left inside the window
            args(1, 5L, "[[0,1,1],[0,2,1],[1,3,0],[1,6,0]]") expects "[1,0]",
            // three users, two robots (the second one is exactly at the threshold, not above)
            args(
                2, 100L, """
                    [[0,1,1],[0,2,1],[0,3,1],
                     [0,1,2],[0,2,2],
                     [0,4,3],[0,5,3],[0,6,3],[0,7,3],
                     [1,10,0]]
                """
            ) expects "[2]",
            // threshold 0 — anyone with at least one request counts as a robot
            args(0, 10L, "[[1,0,0],[0,1,9],[1,1,0],[1,12,0]]") expects "[0,1,0]",
            // the window edge is strict: at now=15 the event at t=10 is already dead
            args(1, 5L, "[[0,10,1],[0,11,1],[1,14,0],[1,15,0]]") expects "[1,0]",
            // registration after a query: robot status appears and then disappears again
            args(2, 3L, "[[0,1,1],[0,2,1],[1,2,0],[0,3,1],[1,3,0],[1,5,0]]") expects "[0,1,0]",
            // no requests at all
            args(1, 100L, "[[1,0,0],[1,50,0]]") expects "[0,0]",
            // a query long after the last registration: the expired head must not hide the live tail
            args(0, 5L, "[[0,1,1],[0,2,1],[0,3,1],[1,7,0]]") expects "[1]",
        )

        @Test
        fun test() = check(::robotsCounter)

        /**
         * Approach (time-based sliding window over a global deque):
         *   Because `now` is monotonically non-decreasing, every event enters the deque in time
         *   order, so the deque is sorted by construction and expired events always sit at its head.
         *   That gives a single shared primitive:
         *     - `events: ArrayDeque<Event>` — the events still inside the window, oldest first;
         *     - `evict(now)` — drop events from the head while `time <= now - timeThreshold`.
         *   `registerEvent` appends to the tail and evicts; `getRobotsCount` evicts first, then
         *   groups the surviving events by user and counts how many users exceed the threshold.
         *
         *   `evict` must run in `getRobotsCount` too, not only on registration: time keeps moving
         *   between events, so a user can stop being a robot without making any new request.
         *
         * Complexity: eviction is amortized O(1) per operation (each event is pushed and popped
         *   once), but the per-query `groupingBy` walks the whole live window, so a query costs
         *   O(W) in the number of live events. Memory is O(W).
         *
         *   The incremental upgrade to O(1) per query: keep `counts: HashMap<Long, Int>` of live
         *   events per user plus a running `robots` total, and adjust `robots` **only when a user
         *   crosses the threshold** — `robots++` when a count becomes exactly `requestThreshold + 1`
         *   on insert, `robots--` when it drops from `requestThreshold + 1` back to
         *   `requestThreshold` on eviction. Erase the key at zero, or the map grows with every user
         *   ever seen (a memory leak in a long-lived service).
         *
         * Pitfalls:
         *   - Strictness of both comparisons: "greater than the threshold", not "at least"; the
         *     window edge is strict too — an event exactly at `now - timeThreshold` is already dead.
         *   - Eviction must **skip past** expired events, not stop at the first one. Scanning the
         *     deque from the head and breaking on the first expired entry returns 0 whenever a stale
         *     event is still sitting at the head, hiding every live event behind it.
         *   - Time and ids are `Long` (epoch milliseconds do not fit in `Int`); do the
         *     `now - timeThreshold` arithmetic in `Long`.
         *   - Without the monotonicity guarantee on `now`, one global deque would not be enough:
         *     it would take a per-user deque with eviction on access (or a timer heap).
         */
        fun robotsCounter(requestThreshold: Int, timeThreshold: Long, ops: Array<IntArray>): List<Int> {
            data class Op(val opId: Int, val time: Long, val user: Long)
            data class Event(val time: Long, val user: Long)

            val operations = ops.map { (op, time, user) -> Op(op, time.toLong(), user.toLong()) }

            val events = ArrayDeque<Event>()

            fun evict(now: Long) {
                val windowStart = now - timeThreshold
                while (events.isNotEmpty() && events.first().time <= windowStart) {
                    events.removeFirst()
                }
            }

            fun registerEvent(now: Long, userId: Long) {
                evict(now)
                events.addLast(Event(now, userId))
            }

            fun getRobotsCount(now: Long): Int {
                evict(now)
                return events.groupingBy { it.user }
                    .eachCount()
                    .count { (_, count) -> count > requestThreshold }
            }

            return operations.mapNotNull { op ->
                when (op.opId) {
                    0 -> registerEvent(op.time, op.user).let { null }
                    1 -> getRobotsCount(op.time)
                    else -> throw IllegalStateException("Op id not supported")
                }
            }
        }

    }
}
