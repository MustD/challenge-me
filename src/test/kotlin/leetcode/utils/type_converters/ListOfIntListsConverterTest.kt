package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListOfIntListsConverterTest {

    @Test
    fun `convert string to List of Int lists`() {
        val result = TypeConverters.convert("[[1,2],[3,4]]", typeOf<List<List<Int>>>()) as List<List<Int>>
        assertEquals(listOf(listOf(1, 2), listOf(3, 4)), result)
    }

    @Test
    fun `convert outer empty list`() {
        val result = TypeConverters.convert("[]", typeOf<List<List<Int>>>()) as List<List<Int>>
        assertEquals(emptyList(), result)
    }

    // Issue 6: an empty *inner* list must parse to an empty row, not a row holding one empty string
    // (which used to throw NumberFormatException on "".toInt()). Shows up in subsets-style answers
    // that include the empty set.
    @Test
    fun `convert leading empty inner list`() {
        val result = TypeConverters.convert("[[],[1]]", typeOf<List<List<Int>>>()) as List<List<Int>>
        assertEquals(listOf(emptyList(), listOf(1)), result)
    }

    @Test
    fun `convert trailing empty inner list`() {
        val result = TypeConverters.convert("[[1],[]]", typeOf<List<List<Int>>>()) as List<List<Int>>
        assertEquals(listOf(listOf(1), emptyList()), result)
    }

    @Test
    fun `convert multiple empty inner lists`() {
        val result = TypeConverters.convert("[[],[]]", typeOf<List<List<Int>>>()) as List<List<Int>>
        assertEquals(listOf(emptyList(), emptyList()), result)
    }

    @Test
    fun `convert single empty inner list`() {
        val result = TypeConverters.convert("[[]]", typeOf<List<List<Int>>>()) as List<List<Int>>
        assertEquals(listOf(emptyList()), result)
    }

    // A subsets-of [1,2] style answer including the empty set, compared order-insensitively.
    @Test
    fun `equal subsets answer with empty set - any order`() =
        assertTrue(
            TypeConverters.equal(
                listOf(emptyList(), listOf(1), listOf(2), listOf(1, 2)),
                "[[],[1],[2],[1,2]]",
                typeOf<List<List<Int>>>(),
                anyOrder = true
            )
        )

    @Test
    fun `equal List of Int lists with empty inner - string expected`() =
        assertTrue(
            TypeConverters.equal(
                listOf(emptyList(), listOf(1)),
                "[[],[1]]",
                typeOf<List<List<Int>>>()
            )
        )
}
