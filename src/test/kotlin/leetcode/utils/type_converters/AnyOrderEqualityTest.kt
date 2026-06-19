package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Order-insensitive comparison (the `expectsAnyOrder` DSL).
 *
 * [TypeConverters.equal] with `anyOrder = true` compares collections as multisets at every
 * nesting level, so a correct solution that emits results in a different order than the literal
 * still matches. With `anyOrder = false` (the default) the comparison stays positional.
 */
class AnyOrderEqualityTest {

    // ── List<List<Int>>: the classic "any order" return type ──────────────────────────────

    @Test
    fun `outer order ignored for list of int lists`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf(7), listOf(2, 2, 3)),
                "[[2,2,3],[7]]",
                typeOf<List<List<Int>>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `inner order ignored for list of int lists`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf(3, 2, 1), listOf(7)),
                "[[1,2,3],[7]]",                   // expected written in a different inner order
                typeOf<List<List<Int>>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `permutations match regardless of order`() =
        assertTrue(
            TypeConverters.equal(
                // emitted by a "swap in place" backtracker — a valid but non-lexicographic order
                listOf(
                    listOf(3, 2, 1),
                    listOf(1, 2, 3),
                    listOf(2, 1, 3),
                    listOf(2, 3, 1),
                    listOf(1, 3, 2),
                    listOf(3, 1, 2)
                ),
                "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]",
                typeOf<List<List<Int>>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `differing multisets still fail under any order`() =
        assertFalse(
            TypeConverters.equal(
                listOf(listOf(1, 2), listOf(3, 4)),
                "[[1,2],[3,5]]",
                typeOf<List<List<Int>>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `missing element fails even when present elements reorder`() =
        assertFalse(
            TypeConverters.equal(
                listOf(listOf(7)),                 // dropped [2,2,3]
                "[[2,2,3],[7]]",
                typeOf<List<List<Int>>>(),
                anyOrder = true,
            )
        )

    // ── Default (positional) comparison is unaffected ─────────────────────────────────────

    @Test
    fun `reordered result fails positional comparison`() =
        assertFalse(
            TypeConverters.equal(
                listOf(listOf(7), listOf(2, 2, 3)),
                "[[2,2,3],[7]]",
                typeOf<List<List<Int>>>(),
                anyOrder = false,
            )
        )

    @Test
    fun `same order passes positional comparison`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf(2, 2, 3), listOf(7)),
                "[[2,2,3],[7]]",
                typeOf<List<List<Int>>>(),
                anyOrder = false,
            )
        )

    // ── Other collection shapes ───────────────────────────────────────────────────────────

    @Test
    fun `flat int list ignores order`() =
        assertTrue(
            TypeConverters.equal(
                listOf(3, 1, 2),
                "[1,2,3]",
                typeOf<List<Int>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `IntArray ignores order`() =
        assertTrue(
            TypeConverters.equal(
                intArrayOf(3, 1, 2),
                "[1,2,3]",
                typeOf<IntArray>(),
                anyOrder = true,
            )
        )

    @Test
    fun `Array of IntArray ignores order and matches list-shaped expected`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(intArrayOf(3, 4), intArrayOf(2, 1)),
                "[[1,2],[3,4]]",
                typeOf<Array<IntArray>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `string lists ignore order`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf("bat"), listOf("nat", "tan"), listOf("ate", "eat", "tea")),
                """[["eat","tea","ate"],["tan","nat"],["bat"]]""",
                typeOf<List<List<String>>>(),
                anyOrder = true,
            )
        )

    @Test
    fun `typed expected works under any order`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf(2, 1), listOf(3)),
                listOf(listOf(3), listOf(1, 2)),
                typeOf<List<List<Int>>>(),
                anyOrder = true,
            )
        )
}
