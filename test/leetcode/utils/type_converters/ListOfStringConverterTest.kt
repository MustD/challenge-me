package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListOfStringConverterTest {

    @Test
    fun `convert string to List of String`() {
        val result = TypeConverters.convert("""["a", "b", "c"]""", typeOf<List<String>>())
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `convert empty array to empty List of String`() {
        val result = TypeConverters.convert("[]", typeOf<List<String>>())
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `equal List of String - string expected`() =
        assertTrue(TypeConverters.equal(listOf("a", "b", "c"), """["a", "b", "c"]""", typeOf<List<String>>()))

    @Test
    fun `equal List of String - typed expected`() =
        assertTrue(TypeConverters.equal(listOf("a", "b", "c"), listOf("a", "b", "c"), typeOf<List<String>>()))
}
