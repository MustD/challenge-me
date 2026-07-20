package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 841. Keys and Rooms  (https://leetcode.com/problems/keys-and-rooms/)
 *
 * There are n rooms labeled 0..n-1. All rooms are locked except room 0. Each room i contains a set
 * of keys (rooms[i]); each key unlocks the room with that number. Starting in room 0, you collect
 * the keys you find and use them to enter other rooms. Return true if you can visit every room.
 *
 * Constraints:
 * - n == rooms.length; 2 <= n <= 1000
 * - 0 <= rooms[i].length <= 1000; 1 <= sum(rooms[i].length) <= 3000
 * - 0 <= rooms[i][j] < n; all values within a single rooms[i] are unique
 */
typealias I0841 = (List<List<Int>>) -> Boolean

class I0841canVisitAllRooms {

    @Nested
    inner class Solution : ProblemTest<I0841> {

        override val cases = testCases<I0841>(
            "[[1],[2],[3],[]]" expects true,        // 0->1->2->3, all visited
            "[[1,3],[3,0,1],[2],[0]]" expects false, // room 2 is never unlocked
            "[[],[]]" expects false,                 // room 1 has no key reachable
            "[[1],[]]" expects true,                 // room 0 hands out key 1
        )

        @Test
        fun test() = check(::canVisitAllRooms)

        /**
         * Failed on 2 rooms with keys from each other with no path from room 0
         */
        fun canVisitAllRoomsWrong(rooms: List<List<Int>>): Boolean {
            if (rooms.getOrElse(0) { emptyList() }.isEmpty()) return false

            val keysToFind = (1..rooms.lastIndex).toMutableSet()
            rooms.forEachIndexed { roomNum, room ->
                room.forEach { roomKey ->
                    if (roomKey != roomNum) keysToFind.remove(roomKey)
                }
            }

            return keysToFind.isEmpty()
        }


        /**
         * BFS graph-reachability from a fixed source (room 0).
         *
         * Pattern: this is graph reachability / "count nodes reachable from a source". Rooms are
         * nodes, a key `j` in `rooms[i]` is a directed edge i -> j. The question "can I visit every
         * room" reduces to "is every node reachable from node 0" — solved by any traversal (BFS/DFS)
         * that counts distinct nodes reached. `foundKeys` doubles as the *visited* set (its real job)
         * and `foundKeys.add(it)` returning `true` only on first insertion is what prevents re-queuing
         * a node — a clean way to fuse "mark visited" and "enqueue if new" into one line (61).
         *
         * Time:  O(n + K), where n = number of rooms and K = sum(rooms[i].length) (total keys, <= 3000
         *        here). Each room is dequeued at most once (guarded by the set), and each key is
         *        inspected exactly once across the whole run — the `filter`+`addAll` on line 61-62 is
         *        O(len(room)) amortized O(1) per key via the hash set. This is the standard O(V + E)
         *        graph-traversal bound and is asymptotically optimal — every edge/key must be read at
         *        least once to know the graph's shape.
         *
         * Space: O(n) auxiliary — `foundKeys` holds at most n room ids and `queue` holds at most n ids.
         *        No recursion, so no call-stack growth (a plus over recursive DFS on adversarial
         *        chain-shaped inputs). Output is a single Boolean, so all space is working space.
         *
         * Correctness / edge cases:
         * - The line-53 guard (`rooms[0]` empty -> false) is *redundant but harmless*: if room 0 has no
         *   keys, BFS visits only {0} and `foundKeys.size (1) == rooms.size (>=2)` is already false
         *   since the constraints give n >= 2. Removing it wouldn't change any result; it's a cheap
         *   early-out, not a correctness requirement.
         * - Self-loops (a room holding its own number) are handled for free: `add` returns false, no
         *   re-queue.
         * - Duplicate keys across rooms are deduped by the set; the "unique within a row" constraint is
         *   not relied upon.
         * - Contrast with `canVisitAllRoomsWrong` above: that one only checked "is every key handed out
         *   *somewhere*", ignoring *reachability* — two rooms holding each other's keys look satisfied
         *   but are unreachable from 0. BFS is exactly what fixes that.
         *
         * Alternatives:
         * - DFS (recursive or explicit stack): same O(n + K) / O(n) bounds, identical asymptotics.
         *   Recursive DFS is terser but risks stack depth O(n) on a chain (n up to 1000 is fine on the
         *   JVM, but the iterative BFS here sidesteps the question entirely). No approach beats
         *   O(V + E) — you must read every edge.
         * - Union-Find does NOT fit cleanly: edges are directed, and we need reachability from a
         *   specific source, not undirected connectivity. It would over-connect.
         *
         * Parallelism: not worth it here. BFS has a data dependency (level k+1 depends on level k) and
         *   n <= 1000 is tiny — thread/coordination overhead would dwarf the work. Level-synchronous
         *   parallel BFS is a real technique (frontier expanded in parallel, e.g. on GPUs / large
         *   distributed graphs), but it only pays off at millions of nodes where a single frontier is
         *   large enough to amortize sync barriers.
         *
         * Real-world: this exact shape is the *mark* phase of a mark-and-sweep garbage collector —
         *   "which objects are reachable from the GC roots?" is reachability from a source set, and the
         *   visited set is the mark bitmap. It also shows up in dependency/capability resolution (which
         *   packages/permissions are reachable from an entry point), dead-code elimination, and web
         *   crawling. At real scale the differences from the interview version dominate: graphs don't
         *   fit in memory (external-memory / streaming BFS), edges arrive incrementally, and the
         *   "visited" set becomes a probabilistic structure (Bloom filter) or a sharded distributed set
         *   — correctness trade-offs the O(V + E) textbook answer never has to make.
         */
        fun canVisitAllRooms(rooms: List<List<Int>>): Boolean {
            if (rooms.getOrElse(0) { emptyList() }.isEmpty()) return false
            val foundKeys = mutableSetOf<Int>()
            foundKeys.add(0)

            val queue = ArrayDeque<Int>()
            queue.add(0)
            while (queue.isNotEmpty()) {
                val room = rooms[queue.removeFirst()]
                val notVisitedRooms = room.filter { foundKeys.add(it) }
                queue.addAll(notVisitedRooms)
            }

            return foundKeys.size == rooms.size
        }

    }
}
