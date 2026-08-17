package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * String-to-array conversion must tolerate spaces, tabs and line breaks
 * between tokens, so multiline `"""..."""` literals parse identically to single-line ones.
 * Whitespace *inside* quoted elements must still be preserved.
 */
class WhitespaceToleranceTest {

    @Test
    fun `multiline 2D matrix parses the same as single line`() {
        val singleLine = "[[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]"
        val multiline = """
            [[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],
             [1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]
        """

        val expected = TypeConverters.convert(singleLine, typeOf<Array<IntArray>>()) as Array<IntArray>
        val actual = TypeConverters.convert(multiline, typeOf<Array<IntArray>>()) as Array<IntArray>

        assertEquals(expected.map { it.toList() }, actual.map { it.toList() })
    }

    @Test
    fun `1D int array tolerates spaces and tabs`() {
        val result = TypeConverters.convert("[ 1,\t2 ,  3 ]", typeOf<IntArray>()) as IntArray
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun `1D int array tolerates surrounding line breaks`() {
        val result = TypeConverters.convert("\n  [1,2,3]\n", typeOf<IntArray>()) as IntArray
        assertEquals(listOf(1, 2, 3), result.toList())
    }

    @Test
    fun `list of int lists tolerates indentation between rows`() {
        val result = TypeConverters.convert(
            """
            [[1,2,3],
             [4,5,6]]
            """,
            typeOf<List<List<Int>>>()
        )
        assertEquals(listOf(listOf(1, 2, 3), listOf(4, 5, 6)), result)
    }

    @Test
    fun `char matrix tolerates whitespace`() {
        val result = TypeConverters.convert(
            """
            [["X","X"],
             ["O","X"]]
            """,
            typeOf<Array<CharArray>>()
        ) as Array<CharArray>
        assertEquals(listOf(listOf('X', 'X'), listOf('O', 'X')), result.map { it.toList() })
    }

    @Test
    fun `whitespace inside quoted string elements is preserved`() {
        val result = TypeConverters.convert("""[ "a b" , "c" ]""", typeOf<Array<String>>()) as Array<*>
        assertEquals(listOf("a b", "c"), result.toList())
    }

    @Test
    fun `empty array still yields empty after whitespace stripping`() {
        val result = TypeConverters.convert("  [ ]  ", typeOf<IntArray>()) as IntArray
        assertTrue(result.isEmpty())
    }
}
