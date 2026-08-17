package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListOfIntConverterTest {

    @Test
    fun `convert string to List of Int`() {
        val result = TypeConverters.convert("[1,2,3]", typeOf<List<Int>>())
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `convert empty array to empty List of Int`() {
        val result = TypeConverters.convert("[]", typeOf<List<Int>>())
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun `equal List of Int - string expected`() =
        assertTrue(TypeConverters.equal(listOf(1, 2, 3), "[1,2,3]", typeOf<List<Int>>()))

    @Test
    fun `equal List of Int - typed expected`() =
        assertTrue(TypeConverters.equal(listOf(1, 2, 3), listOf(1, 2, 3), typeOf<List<Int>>()))
}
