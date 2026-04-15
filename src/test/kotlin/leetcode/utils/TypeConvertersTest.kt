package leetcode.utils

import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypeConvertersTest {

    // ── Int ──────────────────────────────────────────────────────────────────

    @Test
    fun `convert string to Int`() =
        assertEquals(42, TypeConverters.convert("42", typeOf<Int>()))

    @Test
    fun `equal Int - string expected`() =
        assertTrue(TypeConverters.equal(42, "42", typeOf<Int>()))

    @Test
    fun `equal Int - typed expected`() =
        assertTrue(TypeConverters.equal(42, 42, typeOf<Int>()))

    // ── Long ─────────────────────────────────────────────────────────────────

    @Test
    fun `convert string to Long`() =
        assertEquals(100L, TypeConverters.convert("100", typeOf<Long>()))

    @Test
    fun `equal Long - string expected`() =
        assertTrue(TypeConverters.equal(100L, "100", typeOf<Long>()))

    @Test
    fun `equal Long - typed expected`() =
        assertTrue(TypeConverters.equal(100L, 100L, typeOf<Long>()))

    // ── Boolean ──────────────────────────────────────────────────────────────

    @Test
    fun `convert string to Boolean`() =
        assertEquals(true, TypeConverters.convert("true", typeOf<Boolean>()))

    @Test
    fun `equal Boolean - string expected`() =
        assertTrue(TypeConverters.equal(true, "true", typeOf<Boolean>()))

    @Test
    fun `equal Boolean - typed expected`() =
        assertTrue(TypeConverters.equal(false, false, typeOf<Boolean>()))

    // ── Double ───────────────────────────────────────────────────────────────

    @Test
    fun `convert string to Double`() =
        assertEquals(3.14, TypeConverters.convert("3.14", typeOf<Double>()))

    @Test
    fun `equal Double - string expected`() =
        assertTrue(TypeConverters.equal(3.14, "3.14", typeOf<Double>()))

    @Test
    fun `equal Double - typed expected`() =
        assertTrue(TypeConverters.equal(3.14, 3.14, typeOf<Double>()))

    // ── String ───────────────────────────────────────────────────────────────

    @Test
    fun `convert string to String`() =
        assertEquals("hello", TypeConverters.convert("hello", typeOf<String>()))

    @Test
    fun `equal String - string expected`() =
        assertTrue(TypeConverters.equal("hello", "hello", typeOf<String>()))

    @Test
    fun `equal String - typed expected`() =
        assertTrue(TypeConverters.equal("hello", "hello", typeOf<String>()))

    // ── TreeNode ─────────────────────────────────────────────────────────────

    @Test
    fun `convert string to TreeNode`() {
        val result = TypeConverters.convert("[1,2,3]", typeOf<TreeNode>()) as TreeNode?
        assertNotNull(result)
        assertEquals(1, result.`val`)
        assertEquals(2, result.left?.`val`)
        assertEquals(3, result.right?.`val`)
    }

    @Test
    fun `equal TreeNode - string expected`() =
        assertTrue(TypeConverters.equal("[1,2,3]".toTreeNode(), "[1,2,3]", typeOf<TreeNode>()))

    @Test
    fun `equal TreeNode - typed expected`() =
        assertTrue(TypeConverters.equal("[1,2,3]".toTreeNode(), "[1,2,3]".toTreeNode(), typeOf<TreeNode>()))

    // ── ListNode ─────────────────────────────────────────────────────────────

    @Test
    fun `convert string to ListNode`() {
        val result = TypeConverters.convert("[1,2,3]", typeOf<ListNode>()) as ListNode?
        assertNotNull(result)
        assertEquals("1, 2, 3", result.toString())
    }

    @Test
    fun `equal ListNode - string expected`() =
        assertTrue(TypeConverters.equal("[1,2,3]".toListNode(), "[1,2,3]", typeOf<ListNode>()))

    @Test
    fun `equal ListNode - typed expected`() =
        assertTrue(TypeConverters.equal("[1,2,3]".toListNode(), "[1,2,3]".toListNode(), typeOf<ListNode>()))

    // ── IntArray ─────────────────────────────────────────────────────────────

    @Test
    fun `convert string to IntArray`() {
        val result = TypeConverters.convert("[1,2,3]", typeOf<IntArray>()) as IntArray
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun `equal IntArray - string expected`() =
        assertTrue(TypeConverters.equal(intArrayOf(1, 2, 3), "[1,2,3]", typeOf<IntArray>()))

    @Test
    fun `equal IntArray - typed expected`() =
        assertTrue(TypeConverters.equal(intArrayOf(1, 2, 3), intArrayOf(1, 2, 3), typeOf<IntArray>()))

    // ── DoubleArray ───────────────────────────────────────────────────────────

    @Test
    fun `convert string to DoubleArray`() {
        val result = TypeConverters.convert("[1.0,2.5,3.0]", typeOf<DoubleArray>()) as DoubleArray
        assertEquals(listOf(1.0, 2.5, 3.0), result.toList())
    }

    @Test
    fun `equal DoubleArray - string expected`() =
        assertTrue(TypeConverters.equal(doubleArrayOf(1.0, 2.5, 3.0), "[1.0,2.5,3.0]", typeOf<DoubleArray>()))

    @Test
    fun `equal DoubleArray - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                doubleArrayOf(1.0, 2.5, 3.0),
                doubleArrayOf(1.0, 2.5, 3.0),
                typeOf<DoubleArray>()
            )
        )

    // ── Array<CharArray> ──────────────────────────────────────────────────────

    @Test
    fun `convert string to Array of CharArray`() {
        val result = TypeConverters.convert("[[a,b],[c,d]]", typeOf<Array<CharArray>>()) as Array<CharArray>
        assertEquals(listOf(listOf('a', 'b'), listOf('c', 'd')), result.map { it.toList() })
    }

    @Test
    fun `equal Array of CharArray - string expected`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(charArrayOf('a', 'b'), charArrayOf('c', 'd')),
                "[[a,b],[c,d]]",
                typeOf<Array<CharArray>>()
            )
        )

    @Test
    fun `equal Array of CharArray - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(charArrayOf('a', 'b'), charArrayOf('c', 'd')),
                arrayOf(charArrayOf('a', 'b'), charArrayOf('c', 'd')),
                typeOf<Array<CharArray>>()
            )
        )

    @Test
    fun `convert string to Array of CharArray quoted`() {
        val result = TypeConverters.convert(
            """[["X","X","X","X"],["X","O","O","X"],["X","X","O","X"],["X","O","X","X"]]""",
            typeOf<Array<CharArray>>()
        ) as Array<CharArray>
        assertEquals(
            listOf(
                listOf('X', 'X', 'X', 'X'),
                listOf('X', 'O', 'O', 'X'),
                listOf('X', 'X', 'O', 'X'),
                listOf('X', 'O', 'X', 'X'),
            ),
            result.map { it.toList() }
        )
    }

    @Test
    fun `equal Array of CharArray - string expected quoted`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(
                    charArrayOf('X', 'X', 'X', 'X'),
                    charArrayOf('X', 'O', 'O', 'X'),
                    charArrayOf('X', 'X', 'O', 'X'),
                    charArrayOf('X', 'O', 'X', 'X'),
                ),
                """[["X","X","X","X"],["X","O","O","X"],["X","X","O","X"],["X","O","X","X"]]""",
                typeOf<Array<CharArray>>()
            )
        )

    @Test
    fun `equal Array of CharArray - typed expected quoted`() =
        assertTrue(
            TypeConverters.equal(
                arrayOf(
                    charArrayOf('X', 'X', 'X', 'X'),
                    charArrayOf('X', 'O', 'O', 'X'),
                    charArrayOf('X', 'X', 'O', 'X'),
                    charArrayOf('X', 'O', 'X', 'X'),
                ),
                arrayOf(
                    charArrayOf('X', 'X', 'X', 'X'),
                    charArrayOf('X', 'O', 'O', 'X'),
                    charArrayOf('X', 'X', 'O', 'X'),
                    charArrayOf('X', 'O', 'X', 'X'),
                ),
                typeOf<Array<CharArray>>()
            )
        )
}
