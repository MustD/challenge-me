package leetcode.dp_1d

import leetcode.expects
import org.junit.jupiter.api.Nested
import java.util.*
import kotlin.test.Test

/**
 * 198. House Robber  (https://leetcode.com/problems/house-robber/)
 *
 * You are a professional robber planning to rob houses along a street, where each house holds a certain amount of
 * money. Adjacent houses have connected security systems: robbing two directly adjacent houses on the same night
 * triggers the police. Given an integer array `nums` where `nums[i]` is the money stashed in house `i`, return the
 * maximum amount of money you can rob tonight without alerting the police.
 *
 * Constraints:
 * - 1 <= nums.length <= 100
 * - 0 <= nums[i] <= 400
 */
typealias I0198 = (IntArray) -> Int

class I0198rob {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0198> {

        override val cases = leetcode.testCases<I0198>(
            "[1,2,3,1]" expects 4,      // rob houses 0 and 2 -> 1 + 3
            "[2,7,9,3,1]" expects 12,   // rob houses 0, 2 and 4 -> 2 + 9 + 1
            "[5]" expects 5,            // single house
            "[2,1]" expects 2,          // two houses: pick the richer one
            "[0,0,0]" expects 0,        // nums[i] may be 0
            "[2,1,1,2]" expects 4,      // greedy-from-the-left trap: 2 + 2, not 2 + 1
            "[2, 7, 9, 7, 3, 1]" expects 15, //wrong for my solution
        )

        @Test
        fun test() = check(::rob, ::referenceSolution, ::referenceSolution2)

        //wrong approach
        fun robFail(nums: IntArray): Int {
            val heap = PriorityQueue<Pair<Int, Int>>(compareByDescending { it.first })
            nums.mapIndexed { index, i -> i to index }.forEach { heap.add(it) }
            val visited = mutableSetOf<Int>()

            var result = 0
            while (heap.isNotEmpty()) {
                val (nextVal, nextIdx) = heap.poll() ?: throw IllegalStateException("")
                if (visited.contains(nextIdx - 1) || visited.contains(nextIdx + 1)) {
                    continue
                } else {
                    visited.add(nextIdx)
                    result += nextVal
                }
            }
            return result
        }

        fun rob(nums: IntArray): Int {
            if (nums.size == 1) return nums[0]

            val dp = IntArray(nums.size)
            dp[0] = nums[0]
            dp[1] = maxOf(nums[0], nums[1])

            for (i in 2..nums.lastIndex) {
                dp[i] = maxOf(
                    dp[i - 1],
                    dp[i - 2] + nums[i]
                )
            }
            return dp[nums.size - 1]
        }

        /**
         * ### Restatement
         * Pick a subset of array positions with **no two adjacent indices** so that the sum of the picked values is as
         * large as possible. "Security systems" is just a no-two-neighbours constraint on a line.
         *
         * ### Pattern: 1-D dynamic programming ("linear DP over prefixes")
         * This is the canonical *maximum-weight independent set on a path* problem, and the textbook first exercise in
         * 1-D DP. The signature move: instead of asking "which houses do I take?" (an exponential search over
         * subsets), walk left to right and ask a **local** question at each house — *take it or skip it* — where the
         * answer only needs one number from the past, not the whole chosen set.
         *
         * Why a DP and not a greedy? Because the value of a house is not what matters; what matters is the value of a
         * house **relative to what taking it forbids**. A locally-best pick can destroy two better neighbours, and no
         * ordering by value alone can see that. DP fixes this by *deferring* the decision: it keeps the best answer for
         * **both** worlds (last house taken / not taken) and only lets the future collapse them.
         *
         * ### Approach
         * Define `dp[i]` = the best loot obtainable from the prefix `nums[0..i]`. At house `i` there are exactly two
         * mutually exclusive choices:
         *
         * - **Rob house `i`** → then `i - 1` is off-limits, so you add `nums[i]` to the best prefix ending two houses
         *   back: `nums[i] + dp[i - 2]`.
         * - **Skip house `i`** → you simply inherit the best answer so far: `dp[i - 1]`.
         *
         * ```
         * dp[i] = max(dp[i - 1], nums[i] + dp[i - 2])
         * dp[0] = nums[0]
         * dp[1] = max(nums[0], nums[1])
         * ```
         *
         * That recurrence is correct because the two branches are exhaustive (you either take `i` or you don't) and
         * each branch's remainder is an *independent smaller instance of the same problem* — the optimal substructure
         * that makes DP legal here.
         *
         * The recurrence looks back at most two cells, so the whole `dp` array is unnecessary: carry two rolling
         * variables. Reading them as "best if I already committed to skipping the current house" (`prev`) and "best
         * including everything up to the previous house" (`curr`) gives the tight loop below:
         *
         * ```
         * nums:  [2,  7,  9,  7,  3,  1]
         * curr:   2   7  11  14  14  15     <- dp[i]
         * prev:   0   2   7  11  14  14     <- dp[i-1]
         * ```
         *
         * Answer: 15 — houses 1, 3 and 5 (7 + 7 + 1), which no value-ordered greedy will find.
         *
         * ### Complexity
         * - **Time O(n)** — one pass, constant work per house.
         * - **Space O(1)** — two ints; the `dp` array is collapsed away. (`referenceSolution2` keeps the explicit
         *   array: same O(n) time, O(n) space, easier to read while learning.)
         *
         * ### Where your attempt diverges
         * Your solution is a **greedy** one: sort houses by value (via a max-heap) and take each one if neither
         * neighbour is already taken. That is a reasonable-looking heuristic, but greedy is unsound for this problem,
         * and your own extra case proves it. On `[2,7,9,7,3,1]` it grabs `9` first, which permanently blocks *both*
         * sevens; it then settles for `2 + 9 + 3 = 14`, while the optimum `7 + 7 + 1 = 15` deliberately declines the
         * single largest house. The flaw is structural, not a bug to patch: taking the global max first is an
         * irrevocable commitment made without knowing what it costs later. (Also, `PriorityQueue.poll()` on a
         * non-empty queue never returns `null`, so the `?: throw` is dead code; and comparing only `it.first` leaves
         * ties between equal values resolved arbitrarily — which is itself another source of nondeterministic wrong
         * answers.)
         *
         * ### Common pitfalls
         * - **Indexing `dp[i - 2]` when `i < 2`** — handle `n == 1` (and `dp[1]`) before the loop, or use the rolling
         *   form below where `prev` starts at 0 and the base cases fall out for free.
         * - **`max(dp[i-1], dp[i-2] + nums[i])`, not `dp[i-2] + nums[i]`** — skipping must stay an option; forcing the
         *   "take" branch turns the algorithm into alternating-sum and breaks on `[2,1,1,2]`.
         * - **Assuming the answer alternates** (every other house). It often does not: on `[2,7,9,7,3,1]` the gap
         *   between the two chosen sevens is 2, and on longer inputs runs of three skipped houses can be optimal.
         * - **Zeros are legal values** (`0 <= nums[i]`), so "the answer is positive" is not a safe assumption; here
         *   all values are non-negative, which is what makes `max` monotone — with negative values you would also need
         *   to allow robbing nothing.
         * - Follow-up worth knowing: **213. House Robber II** puts the houses in a circle. The trick is to reuse this
         *   exact function twice, on `nums[0..n-2]` and `nums[1..n-1]`, and take the max — that is how you forbid
         *   houses `0` and `n-1` from being chosen together.
         */
        fun referenceSolution(nums: IntArray): Int {
            var prev = 0     // dp[i - 2] — best loot up to two houses back
            var curr = 0     // dp[i - 1] — best loot up to the previous house
            for (money in nums) {
                val take = prev + money
                prev = curr
                curr = maxOf(curr, take)
            }
            return curr
        }

        /**
         * Same recurrence, written with the explicit `dp` table — O(n) time, O(n) space.
         *
         * Useful while learning: `dp[i]` is literally "best loot from `nums[0..i]`", so you can print the array and
         * check the reasoning house by house. `referenceSolution` is this function with the table collapsed into two
         * variables, which is the standard last step of a 1-D DP ("rolling window over the recurrence's depth").
         */
        fun referenceSolution2(nums: IntArray): Int {
            if (nums.size == 1) return nums[0]

            val dp = IntArray(nums.size)
            dp[0] = nums[0]
            dp[1] = maxOf(nums[0], nums[1])
            for (i in 2 until nums.size) {
                dp[i] = maxOf(dp[i - 1], dp[i - 2] + nums[i])
            }
            return dp[nums.size - 1]
        }

    }
}
