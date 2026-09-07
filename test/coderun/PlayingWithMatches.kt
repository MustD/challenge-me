package coderun

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * Playing with matches
 * https://coderun.yandex.ru/selections/algorithm-training-september-2025/problems/playing-with-matches
 *
 * There is a pile of N matches on the table. Two players move in turn. A move takes one, two or
 * three matches — but only if the amount **left in the pile is not prime** (leaving 1 or 4 is fine,
 * leaving 2 or 3 is not). The player who takes the last match wins.
 *
 * Input:  a single number N (1 <= N <= 10 000).
 * Output: 1 — if the first player wins with optimal play, otherwise 2.
 */
typealias TMatches = (Int) -> Int

class PlayingWithMatches {

    @Nested
    inner class Solution : ProblemTest<TMatches> {

        override val cases = testCases<TMatches>(
            1 expects 1,
            2 expects 1,
            3 expects 1,
            4 expects 2,
            5 expects 1,
            8 expects 2,
            9 expects 1,
            12 expects 2,
            100 expects 2,
            101 expects 1,
            9999 expects 1,
            10000 expects 2,
        )

        @Test
        fun test() = check(::whoWins, ::whoWinsPattern)

        /**
         * Approach (win/lose DP, backward induction on positions):
         *   win[n] == "the player to move with n matches on the table wins with optimal play".
         *   win[0] = false — nothing to take, so the opponent grabbed the last match.
         *   win[n] = exists k in {1,2,3}: n - k >= 0 && !isPrime(n - k) && !win[n - k]
         *   Answer: 1 if win[N], otherwise 2.
         *
         * Pitfalls:
         *   - 0 and 1 are not prime — they may always be left; the sieve starts marking at 2.
         *   - The "not prime" restriction applies only to the amount *left*; N itself may be prime.
         *   - Some positions have all three moves forbidden (from 4 one may leave neither 3 nor 2) —
         *     a position with no legal move is losing.
         *   - Prime positions are unreachable by a move but can be the start: fill win[] for every n.
         *
         * Complexity: O(N log log N) for the sieve + O(N) for the DP, O(N) memory.
         */
        fun whoWins(n: Int): Int {
            val isPrime = BooleanArray(n + 1) { it >= 2 }
            var p = 2
            while (p.toLong() * p <= n) {
                if (isPrime[p]) {
                    var m = p * p
                    while (m <= n) {
                        isPrime[m] = false
                        m += p
                    }
                }
                p++
            }

            val win = BooleanArray(n + 1)
            for (i in 1..n) {
                win[i] = (1..3).any { k -> i - k >= 0 && !isPrime[i - k] && !win[i - k] }
            }
            return if (win[n]) 1 else 2
        }

        /**
         * The DP above makes the pattern visible: the losing positions are exactly 0, 4, 8, 12, …
         * From a multiple of 4 every legal move leaves 1, 2 or 3 (mod 4) — a winning position for
         * the opponent (leaving a prime is forbidden, which only removes options, never adds one).
         * From any other n one can always step back onto a multiple of 4, and that target is never
         * prime (4k is composite for k >= 1, and 0 is allowed). Hence O(1) time and memory.
         */
        fun whoWinsPattern(n: Int): Int = if (n % 4 == 0) 2 else 1
    }
}
