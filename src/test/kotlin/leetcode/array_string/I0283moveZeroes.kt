package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias I0283 = (IntArray) -> IntArray

class I0283moveZeroes {

    @Nested
    inner class Solution : ProblemTest<I0283> {

        override val cases = testCases<I0283>(
            "[0,1,0,3,12]" expects "[1,3,12,0,0]",
            "[0]" expects "[0]",
        )

        @Test
        fun test() = check(::moveZeroes01, ::moveZeroes)

        /**
         * Move Zeroes (LeetCode 283) — both [moveZeroes01] and [moveZeroes] implement the
         * **same algorithm** (in-place fast/slow two-pointer with a swap); they differ only in
         * cosmetics (`lastNotZero` pre-incremented to -1 vs. `slow` post-incremented from 0,
         * `for` vs. `forEach`). The analysis below applies to both.
         *
         * ### Pattern
         * Two-pointer "partition in place" (a.k.a. the *write-index* / *slow-runner* technique,
         * the same skeleton as `removeElement` 27 and `removeDuplicates` 26). `i` is the **read**
         * pointer scanning every element; `slow`/`lastNotZero+1` is the **write** boundary marking
         * the next slot for a non-zero value. Every non-zero hit is swapped into the write slot and
         * the boundary advances, so non-zeros pack to the front in their original relative order and
         * zeros fall to the tail — exactly the stable in-place partition the problem asks for.
         *
         * ### Complexity
         * - **Time: O(n)** — a single pass over `nums.indices`; each iteration is O(1) (one compare,
         *   at most one swap). No nesting.
         * - **Space: O(1)** — two int pointers + one temp; mutates the input array in place, no
         *   auxiliary allocation. The returned `IntArray` is the same object, not a copy.
         *
         * ### Correctness / edge cases
         * - **Stability:** because `i` only ever moves forward and writes happen at the boundary in
         *   encounter order, the relative order of non-zeros is preserved (required by the problem).
         * - **Self-swap is harmless:** while there have been no zeros yet, `slow == i`, so the swap
         *   exchanges an element with itself — correct, just a redundant write.
         * - **`[0]`, all-zeros, no-zeros, empty:** all handled with no special-casing. All-zeros never
         *   enters the `if`; no-zeros swaps each element with itself; empty array iterates zero times.
         * - **No overflow risk** — pointers are bounded by `nums.size`.
         *
         * ### Swap vs. overwrite trade-off
         * This swap variant does up to *n* writes. The classic alternative writes the non-zero to
         * `slow`, then in a **second** short loop fills `[slow, n)` with zeros. That can do fewer
         * total writes when the array is mostly zeros (non-zeros copied once, zeros written once),
         * but it touches the array twice and is not obviously better; the swap version is the more
         * common one-pass answer and is optimal asymptotically. Both are O(n)/O(1).
         *
         * ### Alternatives
         * No asymptotically better approach exists — every element must be inspected at least once,
         * so **O(n) time is a lower bound**, and the in-place pointers already hit **O(1) space**.
         * A non-in-place version (filter non-zeros into a new array, pad with zeros) is also O(n)
         * but costs O(n) extra space and violates the in-place requirement — strictly worse here.
         *
         * ### Parallelism
         * **Not worth it.** The write boundary `slow` is a running prefix-count of non-zeros — a
         * sequential data dependency, so naive partitioning across threads would race on the write
         * position. It *can* be parallelized as a two-pass **prefix-sum (scan)**: pass 1 counts
         * non-zeros per chunk to compute each chunk's start offset, pass 2 scatters in parallel — the
         * standard parallel-filter/stream-compaction primitive on GPUs. But with n ≤ 10^4 the
         * sync/overhead dwarfs the work; the single-threaded scan wins outright at this scale.
         *
         * ### Real-world
         * This is **stream compaction / array densification** — removing "holes" (nulls, tombstones,
         * deleted rows) while keeping order. It shows up in columnar databases and Arrow-style
         * vectors (compacting after a filter), GC compaction, and SIMD/GPU `compact`/`stream
         * compaction` kernels (CUDA Thrust `remove`, `std::remove` in C++ — which is exactly this
         * write-pointer idiom). In production the zeros are usually a *predicate mask* rather than a
         * literal value, and the hot path is the branchless SIMD scatter rather than a scalar loop.
         */
        fun moveZeroes01(nums: IntArray): IntArray {
            var lastNotZero = -1

            for (i in nums.indices) {
                if (nums[i] != 0) {
                    lastNotZero++
                    val tmp = nums[i]
                    nums[i] = nums[lastNotZero]
                    nums[lastNotZero] = tmp
                }
            }
            return nums
        }

        fun moveZeroes(nums: IntArray): IntArray {
            var slow = 0
            nums.indices.forEach { i ->
                if (nums[i] != 0) {
                    val tmp = nums[i]
                    nums[i] = nums[slow]
                    nums[slow] = tmp
                    slow++
                }
            }
            return nums
        }
    }

}
