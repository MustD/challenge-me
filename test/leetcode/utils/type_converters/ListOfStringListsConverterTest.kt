package leetcode.utils.type_converters

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListOfStringListsConverterTest {

    @Test
    fun `convert string to List of String lists`() {
        val result =
            TypeConverters.convert("""[["a","b"],["b","c"]]""", typeOf<List<List<String>>>()) as List<List<String>>
        assertEquals(listOf(listOf("a", "b"), listOf("b", "c")), result)
    }

    @Test
    fun `equal List of String lists - string expected`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf("a", "b"), listOf("b", "c")),
                """[["a","b"],["b","c"]]""",
                typeOf<List<List<String>>>()
            )
        )

    @Test
    fun `equal List of String lists - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                listOf(listOf("a", "b"), listOf("b", "c")),
                listOf(listOf("a", "b"), listOf("b", "c")),
                typeOf<List<List<String>>>()
            )
        )
}
