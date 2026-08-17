package leetcode.utils.type_converters

import leetcode.ProblemTest
import leetcode.expectsAnyOf
import leetcode.testCases
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * End-to-end check of the `expectsAnyOf` DSL through the full `testCases` → `check` path.
 *
 * `expectsAnyOf` lists several acceptable answers; a case passes if the result equals any one of
 * them. Each candidate is type-converted independently (so candidates can be LeetCode strings),
 * which is why these tests run through the harness rather than [leetcode.utils.TypeConverters.equal]
 * in isolation.
 */
class AnyOfDslTest {

    // ── Scalar return: "return the index of any peak" (LC 162) ───────────────────────────────

    private typealias Peak = (IntArray) -> Int

    /** Returns the *last* peak it can find — a valid but not the lexicographically-first answer. */
    private fun lastPeak(nums: IntArray): Int = nums.indices.last { i ->
        (i == 0 || nums[i] > nums[i - 1]) && (i == nums.lastIndex || nums[i] > nums[i + 1])
    }

    @Test
    fun `passes when result equals one of several acceptable answers`() {
        val harness = object : ProblemTest<Peak> {
            // [1,2,1,3,5,6,4] has peaks at index 1 and 5; lastPeak returns 5.
            override val cases = testCases<Peak>(
                "[1,2,1,3,5,6,4]".expectsAnyOf(1, 5),
            )
        }
        // check() throws on failure; reaching the end means 5 matched one of {1, 5}.
        harness.check(::lastPeak)
    }

    @Test
    fun `fails and flags any-of mode when result matches no candidate`() {
        val cases = testCases<Peak>(
            "[1,2,1,3,5,6,4]".expectsAnyOf(0, 1), // lastPeak returns 5, which is in neither
        )
        val failure = cases.single().invoke(::lastPeak)
        assertEquals(true, failure?.startsWith("expected any of: [0, 1],"))
    }

    @Test
    fun `single acceptable answer behaves like plain expects`() {
        val cases = testCases<Peak>("[1,2,3,1]".expectsAnyOf(2))
        assertNull(cases.single().invoke(::lastPeak)) // 3 at index 2 is the only peak
    }

    // ── Type conversion is applied per candidate ─────────────────────────────────────────────

    private typealias Pair2 = (Int) -> IntArray

    /** Echoes a fixed array regardless of input, to assert against string-form candidates. */
    private fun fixed(@Suppress("UNUSED_PARAMETER") n: Int): IntArray = intArrayOf(3, 4)

    @Test
    fun `each candidate is type-converted from its LeetCode string`() {
        val harness = object : ProblemTest<Pair2> {
            override val cases = testCases<Pair2>(
                0.expectsAnyOf("[1,2]", "[3,4]"), // result intArrayOf(3,4) matches the 2nd string
            )
        }
        harness.check(::fixed)
    }
}
