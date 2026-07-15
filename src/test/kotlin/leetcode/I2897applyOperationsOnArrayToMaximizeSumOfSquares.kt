package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * https://leetcode.com/problems/apply-operations-on-array-to-maximize-sum-of-squares
 */
typealias I2897 = (List<Int>, Int) -> Int

class I2897applyOperationsOnArrayToMaximizeSumOfSquares {

    @Nested
    inner class Solution : ProblemTest<I2897> {
        override val cases = testCases<I2897>(
            args(listOf(4, 5, 4, 7), 3) expects 90,
            args(listOf(2, 6, 5, 8), 2) expects 261,
            args(listOf(96, 66, 60, 58, 32, 17, 63, 21, 30, 44, 15, 8, 98, 93), 2) expects 32258,
            args(listOf(25, 52, 75, 65), 4) expects 24051,
        )

        @Test
        fun test() = check(::solution1, ::solution2, ::solution3, ::solution4, ::imported, ::referenceSolution)

        //too long
        fun solution1(nums: List<Int>, k: Int): Int {
            fun List<Int>.operation(i: Int, j: Int): List<Int> = mapIndexed { idx, value ->
                when (idx) {
                    i -> get(i) and get(j)
                    j -> get(i) or get(j)
                    else -> value
                }
            }

            fun List<Int>.calculation() = takeLast(k).sumOf { it * it }

            var result: List<Int> = nums.sorted()
            repeat(k) { kIdx ->
                repeat(nums.size) { idx ->
                    result = result.operation(idx, result.lastIndex - kIdx)
                }
            }

            return result.calculation() % 1000000007
        }

        fun solution2(nums: List<Int>, k: Int): Int {
            fun MutableList<Int>.operation(i: Int, j: Int): MutableList<Int> {
                if (i == j) return this
                val iVal = get(i)
                val jVal = get(j)
                set(i, iVal and jVal)
                set(j, iVal or jVal)
                return this
            }

            fun List<Int>.calculation() = takeLast(k).map { it.toLong() }.sumOf { it * it }

            var result: MutableList<Int> = nums.toMutableList()
            nums.indices.map { idx ->
                repeat(k) kLoop@{ kIdx ->
                    if (result[idx] == 0) return@map
                    if (result.lastIndex - kIdx < 0) return@kLoop
                    result = result.operation(idx, result.lastIndex - kIdx)
                }
            }

            return (result.calculation() % 1000000007).toInt()

        }

        fun solution3(nums: List<Int>, k: Int): Int {
            fun MutableList<Int>.operation(i: Int, j: Int): MutableList<Int> {
                if (i == j) return this
                val iVal = get(i)
                val jVal = get(j)
                set(i, iVal and jVal)
                set(j, iVal or jVal)
                return this
            }

            fun List<Int>.calculation() = takeLast(k).map { it.toLong() }.sumOf { it * it }

            var result: MutableList<Int> = nums.toMutableList()

            repeat(k) {
                result.indices.map { idx ->
                    if (idx + 1 > result.lastIndex) return@map
                    result = result.operation(idx, idx + 1)
                }
            }

            return (result.calculation() % 1000000007).toInt()
        }

        fun solution4(nums: List<Int>, k: Int): Int {
            fun MutableList<Long>.operation(i: Int, j: Int): MutableList<Long> {
                if (i == j) return this
                val iVal = get(i)
                val jVal = get(j)
                set(i, iVal and jVal)
                set(j, iVal or jVal)
                return this
            }

            fun List<Long>.calculation() = take(k).sum()

            var result: MutableList<Long> = nums.map { it.toLong() }.toMutableList()

            repeat(k) { idx ->
                for (i in idx + 1..result.lastIndex) {
                    result = result.operation(i, idx)
                }
                result[idx] = result[idx] * result[idx]
            }

            return (result.calculation() % 1000000007).toInt()
        }

        fun imported(nums: List<Int>, k: Int): Int {
            val bitCount = IntArray(32)
            for (n in nums) {
                for (bitNum in 0..31) {
                    bitCount[bitNum] += if (((n and (1 shl bitNum)) > 0)) 1 else 0
                }
            }

            val arr = IntArray(k)
            for (bitNum in 0..31) {
                var j = 0
                while (j < k && bitCount[bitNum] > 0) {
                    arr[j] = arr[j] or (1 shl bitNum)
                    j++
                    bitCount[bitNum]--
                }
            }

            var result: Long = 0
            val MOD = 1000000007
            for (i in 0 until k) {
                val square = (arr[i].toLong() * arr[i]) % MOD
                result = (result + square) % MOD
            }
            return result.toInt()
        }

        /**
         * ## Apply Operations on Array to Maximize Sum of Squares (LeetCode 2897)
         *
         * **Restated:** You may repeatedly pick two distinct indices `i, j` and simultaneously set
         * `nums[i] = nums[i] AND nums[j]` and `nums[j] = nums[i] OR nums[j]`. After any number of
         * operations, choose `k` elements and return the maximum possible sum of their squares,
         * modulo `1e9+7`.
         *
         * **Key insight — bits are conserved per bit-position, not per number.**
         * Consider one bit position across the two chosen numbers. If the bits there are `(a, b)`,
         * they become `(a AND b, a OR b) = (min, max)`. So the *multiset* of bits at that position is
         * unchanged — one operation merely moves a set bit from one number to another. Over many
         * operations you can therefore redistribute the set bits at each position among the elements
         * however you like. The only invariant is the **total count of 1s at each bit position**.
         * (This is why hand-simulating a specific sequence of operations, as in `solution1`–`solution4`,
         * is unnecessary and easy to get wrong: any distribution respecting the per-position counts is
         * reachable.)
         *
         * **Greedy — pack value into the biggest numbers.** Sum of squares is convex, so it is
         * maximized by concentrating the available 1s into as few, as-large numbers as possible.
         * Reconstruct numbers greedily: the 1st number takes one bit from every non-empty position
         * (the largest achievable value), the 2nd takes the next of each, and so on. The first `k`
         * numbers built this way are precisely the `k` largest reconstructable values.
         *
         * **Approach:**
         *  1. `bitCount[b]` = how many `nums` have bit `b` set (`b` in `0..29`, since `nums[i] < 2^30`).
         *  2. For each of the `k` output slots, OR in `(1 shl b)` for every `b` whose count is still
         *     positive, decrementing that count. Filling every slot left-to-right makes slot 0 the
         *     largest, slot 1 the next, etc.
         *  3. Sum `slot^2` modulo `1e9+7`.
         *
         * **Complexity:** `O((n + k)·B)` time, `O(B + k)` space, where `B = 30` bit positions.
         *
         * **Pitfalls:**
         *  - `value * value` overflows `Int` (a value can be ~2^30, its square ~2^60) — square and
         *    accumulate in `Long`, taking the modulus as you go.
         *  - `k` chosen elements come from the *final* array; you never need more than `k` numbers, so
         *    only build `k` slots.
         */
        fun referenceSolution(nums: List<Int>, k: Int): Int {
            val mod = 1_000_000_007L
            val bits = 30
            val bitCount = IntArray(bits)
            for (n in nums) {
                for (b in 0 until bits) {
                    if ((n shr b) and 1 == 1) bitCount[b]++
                }
            }

            var result = 0L
            repeat(k) {
                var value = 0L
                for (b in 0 until bits) {
                    if (bitCount[b] > 0) {
                        value = value or (1L shl b)
                        bitCount[b]--
                    }
                }
                result = (result + value * value) % mod
            }
            return result.toInt()
        }

    }

}
