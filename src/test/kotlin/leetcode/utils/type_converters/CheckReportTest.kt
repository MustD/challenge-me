package leetcode.utils.type_converters

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Covers `check()`'s aggregated reporting: every solution×case is run,
 * all failures surface in a single throw, and a throwing solution is captured rather than
 * aborting the run.
 */
class CheckReportTest {

    private typealias Identity = (Int) -> Int

    private val harness = object : ProblemTest<Identity> {
        override val cases = testCases<Identity>(
            1 expects 1, // passes for the identity solution, fails for the broken ones
            2 expects 2,
            3 expects 3,
        )
    }

    private fun identity(n: Int): Int = n
    private fun alwaysZero(n: Int): Int = 0
    private fun throwsOnTwo(n: Int): Int = if (n == 2) error("boom") else n

    @Test
    fun `passing solution throws nothing`() {
        harness.check(::identity)
    }

    @Test
    fun `reports every failing case in one throw, not just the first`() {
        val error = assertFailsWith<AssertionError> { harness.check(::alwaysZero) }
        val message = error.message!!
        // All three cases mismatch and all three appear — fail-fast would have shown only case[1].
        assertTrue(message.contains("3/3 checks failed"), message)
        assertTrue(message.contains("case[1]"), message)
        assertTrue(message.contains("case[2]"), message)
        assertTrue(message.contains("case[3]"), message)
        // Helpful detail (item 3): expected/got plus the originating input.
        assertTrue(message.contains("expected: 1, got: 0 (input: 1)"), message)
    }

    @Test
    fun `a throwing solution is captured, not propagated`() {
        val error = assertFailsWith<AssertionError> { harness.check(::throwsOnTwo) }
        val message = error.message!!
        // Only case[2] fails; the throw is recorded and case[3] still ran.
        assertTrue(message.contains("1/3 checks failed"), message)
        assertTrue(message.contains("case[2]: threw IllegalStateException: boom"), message)
    }

    @Test
    fun `failures across multiple solutions are indexed and combined`() {
        val error = assertFailsWith<AssertionError> { harness.check(::identity, ::alwaysZero) }
        val message = error.message!!
        // 2 solutions x 3 cases = 6 checks; only the broken solution's 3 fail.
        assertTrue(message.contains("3/6 checks failed"), message)
        assertTrue(message.contains("solution[2] case[1]"), message)
        assertEquals(false, message.contains("solution[1]"), message)
    }
}
