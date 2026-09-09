package coderun

import leetcode.ProblemTest
import leetcode.args
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

typealias TFortress = (IntArray, Int) -> List<Int>

/**
 * Fortress (Petya's blocks)
 * https://coderun.yandex.ru/selections/algorithm-training-september-2025/problems/tower
 *
 * A fortress is a sequence of N pillars of height A\[i\]. A tower is any K consecutive pillars.
 * A tower's defensiveness = (sum of its pillar heights) * (minimum of its pillar heights).
 * A fortress's impregnability = sum of the defensiveness of the chosen towers. Towers must not
 * overlap; covering the whole fortress is not required. Maximize the impregnability.
 *
 * Input:  N and K (1 <= K <= N <= 1000), then N numbers A\[i\] (1 <= A\[i\] <= 1000).
 * Output: Q — the number of towers in the optimal partition, then Q first-pillar indices.
 *
 * Here `solution` returns the list of first-pillar indices (1-based, ascending); Q is just its
 * size, so it doesn't need to be returned separately.
 */
class Fortress {

    @Nested
    inner class Solution : ProblemTest<TFortress> {

        override val cases = testCases<TFortress>(
            args("[7]", 1) expects "[1]",
            args("[1,2,3,4,5]", 2) expects "[2,4]",
            args("[1,1,1,1]", 2) expects "[1,3]",
            args("[5,5,5]", 1) expects "[1,2,3]",
            args("[3,1,4,1,5,9,2,6]", 3) expects "[3,6]",
            args("[2,3,1,7,4,6,2,8,9,1]", 4) expects "[2,6]",
            args("[1,2,3,4,5,6]", 6) expects "[1]",
        )

        @Test
        fun test() = check(::fortressTowers)

        /**
         * Approach (1D prefix DP — "take or skip", as in weighted interval scheduling):
         *   dp[i] = maximum impregnability achievable using only the first i pillars.
         *   dp[0..K-1] = 0
         *   dp[i] = max(dp[i - 1],                       // pillar i belongs to no tower
         *               dp[i - K] + value(i - K + 1, i)) // a tower ending at pillar i
         *   where value(l, r) = (sum A[l..r]) * (min A[l..r]).
         *   The answer is dp[N]; the towers themselves are recovered by walking the decision
         *   array backwards.
         *
         * Computing value quickly:
         *   - window sum — prefix sums, O(1) per window;
         *   - window minimum — either a monotonic deque (sliding-window minimum, O(N) for all
         *     windows), or, since N <= 1000, a brute-force scan of K elements — O(N*K) <= 10^6,
         *     which also fits.
         *
         * Pitfalls:
         *   - The partition need not be a cover: skipping pillars is often better (see the case
         *     [1,2,3,4,5], K=2 — the optimum starts at the second pillar).
         *   - The answer's indices are 1-based, while the array is 0-based.
         *   - Reconstruction yields towers right-to-left — don't forget to reverse the result.
         *   - Values: up to 2*10^9 per the constraints, already past Int range, so accumulate in
         *     Long (in Kotlin, `sum * min` on Int would silently overflow).
         *   - When dp values tie, which branch is preferred matters; the optimum may not be
         *     unique (in the tests below it is).
         *
         * Complexity: O(N) time and memory with the deque (or O(N*K) time with the brute-force
         * minimum).
         */
        fun fortressTowers(a: IntArray, k: Int): List<Int> {
            val n = a.size

            // Prefix sums (1-based) so window sums are O(1): sum(l..r) = prefix[r] - prefix[l-1].
            val prefix = LongArray(n + 1)
            for (i in 1..n) prefix[i] = prefix[i - 1] + a[i - 1]

            // Sliding-window minimum via a monotonic increasing deque of 1-based indices.
            // minEndingAt[i] = min(a[i-k+1..i]), defined only for i >= k.
            val minEndingAt = LongArray(n + 1)
            val deque = IntArray(n + 1)
            var head = 0
            var tail = -1
            for (i in 1..n) {
                while (tail >= head && a[deque[tail] - 1] >= a[i - 1]) tail--
                deque[++tail] = i
                while (deque[head] <= i - k) head++
                if (i >= k) minEndingAt[i] = a[deque[head] - 1].toLong()
            }

            // value(i) = defensiveness of the length-k tower ending at pillar i (1-based).
            fun value(i: Int): Long {
                val windowSum = prefix[i] - prefix[i - k]
                return windowSum * minEndingAt[i]
            }

            // dp[i] = maximum impregnability using the first i pillars.
            // take[i] = true if the optimum for dp[i] uses a tower ending at i.
            val dp = LongArray(n + 1)
            val take = BooleanArray(n + 1)
            for (i in 1..n) {
                dp[i] = dp[i - 1] // pillar i is skipped
                if (i >= k) {
                    val withTower = dp[i - k] + value(i)
                    if (withTower > dp[i]) {
                        dp[i] = withTower
                        take[i] = true
                    }
                }
            }

            // Reconstruct right-to-left, then reverse into ascending order.
            val startsDescending = mutableListOf<Int>()
            var i = n
            while (i > 0) {
                if (take[i]) {
                    startsDescending.add(i - k + 1)
                    i -= k
                } else {
                    i--
                }
            }
            return startsDescending.reversed()
        }

    }
}
