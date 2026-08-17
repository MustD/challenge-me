package leetcode.utils.type_converters

import leetcode.ProblemTest
import leetcode.expects
import leetcode.expectsAnyOrder
import leetcode.testCases
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * End-to-end check of the `expectsAnyOrder` DSL through the full
 * `testCases` → `check` path, not just [leetcode.utils.TypeConverters.equal] in isolation.
 */
class AnyOrderDslTest {

    private typealias Permute = (IntArray) -> List<List<Int>>

    /**
     * Emits permutations in a deliberately non-lexicographic order — the order a "swap in place"
     * backtracker would produce — to prove the comparison doesn't care about ordering.
     */
    private fun permuteShuffled(nums: IntArray): List<List<Int>> =
        listOf(listOf(2, 1, 3), listOf(1, 2, 3), listOf(3, 2, 1), listOf(1, 3, 2), listOf(2, 3, 1), listOf(3, 1, 2))

    private val anyOrderHarness = object : ProblemTest<Permute> {
        override val cases = testCases<Permute>(
            "[1,2,3]" expectsAnyOrder "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]",
        )
    }

    @Test
    fun `expectsAnyOrder accepts a correct but reordered result`() {
        // check() throws on failure; reaching the end means the shuffled order matched.
        anyOrderHarness.check(::permuteShuffled)
    }

    @Test
    fun `same reordered result fails plain expects`() {
        val positional = testCases<Permute>(
            "[1,2,3]" expects "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]",
        )
        val failure = positional.single().invoke(::permuteShuffled)
        // First element already differs ([2,1,3] vs [1,2,3]), so positional comparison reports a mismatch.
        assertEquals(true, failure?.startsWith("expected: "))
    }

    @Test
    fun `failure message flags any-order mode`() {
        val cases = testCases<Permute>(
            "[1,2,3]" expectsAnyOrder "[[1,2,3],[1,3,2]]", // wrong multiset on purpose
        )
        val failure = cases.single().invoke(::permuteShuffled)
        assertEquals(true, failure?.startsWith("expected (any order): "))
    }
}
