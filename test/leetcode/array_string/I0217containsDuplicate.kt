package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 217. Contains Duplicate  (https://leetcode.com/problems/contains-duplicate/)
 *
 * Given an integer array `nums`, return `true` if any value appears **at least twice** in the array,
 * and return `false` if every element is distinct.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^5
 * - -10^9 <= nums[i] <= 10^9
 */
typealias I0217 = (IntArray) -> Boolean

class I0217containsDuplicate {

    @Nested
    inner class Solution : ProblemTest<I0217> {

        override val cases = testCases<I0217>(
            "[1,2,3,1]" expects true,
            "[1,2,3,4]" expects false,
            "[1,1,1,3,3,4,3,2,4,2]" expects true,
            "[7]" expects false,                                  // single element (n = 1) — can never duplicate
            "[5,5]" expects true,                                 // smallest possible duplicate
            "[-1000000000,1000000000]" expects false,             // value-range extremes, distinct
            "[1000000000,-1000000000,1000000000]" expects true,   // extreme value duplicated, non-adjacent
            "[0,-0,1]" expects true,                              // zero written two ways is the same Int
            "[-3,-2,-1,0,1,2,3]" expects false,                   // negatives + zero, strictly increasing
            "[9,8,7,6,5,4,3,2,1,9]" expects true,                 // duplicate pair at very first/last position
        )

        @Test
        fun test() = check(::containsDuplicate)

        /**
         * ## Analysis — set-cardinality comparison
         *
         * **Idea:** a set drops duplicates, so `nums` has a duplicate exactly when the set is smaller than the array.
         * Correct by definition: `|set(nums)| < n` ⇔ some value appears at least twice.
         *
         * **Pattern:** *hash-set membership / dedup* — the go-to tool whenever a question is "have I seen this
         * value before?". Same family as Two Sum (hash map of seen values), Longest Substring Without Repeating
         * Characters (set inside a sliding window), and Happy Number (set of visited states for cycle detection).
         *
         * ### Time — O(n) expected, Θ(n) always
         * - `nums.toSet()` iterates the whole array once, doing one hash insert (expected O(1)) per element.
         *   For n > 1 Kotlin builds a `LinkedHashSet` pre-sized via `mapCapacity(n)`, so there is no rehashing.
         * - `.size` on both sides is O(1).
         * - **No early exit:** `[5,5,…10^5 more…]` still hashes all 10^5 elements even though the answer was known
         *   after the 2nd. Best case = worst case = Θ(n). An explicit loop with `if (!seen.add(x)) return true`
         *   keeps the same worst case but stops at the first repeat.
         * - Worst case of hashing in theory is O(n²) (all keys collide), but `Integer.hashCode()` is the value
         *   itself and Java 8+ HashMap buckets degrade to trees, so this does not happen in practice.
         *
         * ### Space — O(n) auxiliary
         * - Up to n distinct entries in the set. On the JVM this is far heavier than the 4n bytes of the
         *   `IntArray`: every element is **boxed** to `Integer` (only −128..127 are cached) and every
         *   `LinkedHashSet` entry is a node with hash/key/next/before/after pointers — roughly 50–60 bytes per
         *   element, i.e. ~5–6 MB for n = 10^5 instead of 400 KB.
         * - Output is a single `Boolean`, no output space.
         *
         * ### Correctness / edge cases
         * - n = 1 → set size 1 = array size → `false`. Covered.
         * - Extreme values ±10^9 are just Ints; no arithmetic, so no overflow risk.
         * - Does not mutate `nums` (unlike the sorting alternative below).
         *
         * ### Alternatives
         * | Approach                              | Time       | Extra space | Notes                                          |
         * |---------------------------------------|------------|-------------|------------------------------------------------|
         * | Brute force, all pairs                | O(n²)      | O(1)        | 5·10^9 comparisons at n = 10^5 — TLE.          |
         * | **Sort, then compare neighbours**     | O(n log n) | O(1)–O(log n)| Duplicates become adjacent. Mutates input (or copy it → O(n)). Primitive `IntArray.sort()` is cache-friendly and unboxed, so often *faster* than hashing at this n. |
         * | HashSet with early exit (`add` → false) | O(n) worst | O(n)       | Same asymptotics as yours, but stops at the first repeat. |
         * | Bitset over the value range           | O(n)       | O(range)    | Range is 2·10^9+1 → ~250 MB bitset. Only viable when the value range is small (e.g. values in 1..n). |
         *
         * O(n) time is optimal: any correct algorithm must read every element (an unread element could be the
         * duplicate). Getting O(n) time *and* O(1) space requires extra structure (e.g. values in 1..n, which
         * enables the index-marking / Floyd tricks of LeetCode 287/442) — not available here.
         *
         * ### Parallelism
         * Not worth it at n ≤ 10^5: the whole job is ~1 ms single-threaded, below thread-pool hand-off cost.
         * If it were huge: (a) `Arrays.parallelSort` then a parallel neighbour scan — embarrassingly parallel
         * after the sort; or (b) **hash-partition** — split values into P buckets by `hash % P`, give each thread
         * one bucket with its own private set (a duplicate pair always lands in the same bucket, so no sharing
         * or locking needed), OR the results. A shared `ConcurrentHashMap.newKeySet()` works too but contends.
         * Ceiling is memory bandwidth, not cores — the work per element is tiny.
         *
         * ### Real world
         * - Uniqueness checks usually live in the database: a `UNIQUE` index (B-tree) enforces this on insert.
         * - Dedup of streams/huge datasets that don't fit in memory → **Bloom filter** (no false negatives, tunable
         *   false positives, a few bits per element) as a pre-filter, or external sort + adjacent compare
         *   (what `sort | uniq -d` does). Distributed: shuffle by key hash (the partition idea above), as in
         *   Spark's `distinct`/`groupBy`.
         * - "How many distinct?" at scale → HyperLogLog (approximate, kilobytes of memory).
         * - On the JVM with primitive data, boxing overhead matters: libraries like fastutil (`IntOpenHashSet`)
         *   or Eclipse Collections store raw ints and cut memory ~5–10×.
         */
        fun containsDuplicate(nums: IntArray): Boolean {
            return nums.size != nums.toSet().size
        }

    }
}
