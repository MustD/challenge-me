package leetcode.utils.type_converters

import leetcode.utils.ListNode
import leetcode.utils.TypeConverters
import leetcode.utils.toListNode
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ListNodeConverterTest {

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
}
