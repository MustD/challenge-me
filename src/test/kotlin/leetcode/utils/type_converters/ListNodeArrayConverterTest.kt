package leetcode.utils.type_converters

import leetcode.utils.ListNode
import leetcode.utils.TypeConverters
import leetcode.utils.toListNodeArray
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ListNodeArrayConverterTest {

    @Test
    fun `convert string to Array of ListNode`() {
        @Suppress("UNCHECKED_CAST")
        val result = TypeConverters.convert("[[1,4,5],[1,3,4],[2,6]]", typeOf<Array<ListNode?>>())
                as Array<ListNode?>
        assertEquals(3, result.size)
        assertNotNull(result[0])
        assertEquals("1, 4, 5", result[0].toString())
        assertEquals("1, 3, 4", result[1].toString())
        assertEquals("2, 6", result[2].toString())
    }

    @Test
    fun `empty outer array yields zero lists`() {
        @Suppress("UNCHECKED_CAST")
        val result = TypeConverters.convert("[]", typeOf<Array<ListNode?>>()) as Array<ListNode?>
        assertEquals(0, result.size)
    }

    @Test
    fun `empty inner list yields null head`() {
        @Suppress("UNCHECKED_CAST")
        val result = TypeConverters.convert("[[]]", typeOf<Array<ListNode?>>()) as Array<ListNode?>
        assertEquals(1, result.size)
        assertEquals(null, result[0])
    }

    @Test
    fun `equal Array of ListNode - string expected`() =
        assertTrue(
            TypeConverters.equal(
                "[[1,4,5],[2,6]]".toListNodeArray(),
                "[[1,4,5],[2,6]]",
                typeOf<Array<ListNode?>>()
            )
        )

    @Test
    fun `equal Array of ListNode - typed expected`() =
        assertTrue(
            TypeConverters.equal(
                "[[1,4,5],[2,6]]".toListNodeArray(),
                "[[1,4,5],[2,6]]".toListNodeArray(),
                typeOf<Array<ListNode?>>()
            )
        )
}
