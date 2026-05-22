package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharArray2DConverterTest {

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
