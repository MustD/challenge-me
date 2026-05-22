package leetcode.utils.type_converters

import leetcode.utils.TreeNode
import leetcode.utils.TypeConverters
import leetcode.utils.toTreeNode
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TreeNodeConverterTest {

    @Test
    fun `convert 1,2,3 to TreeNode`() {
        val result = TypeConverters.convert("[1,2,3]", typeOf<TreeNode>()) as TreeNode?
        assertNotNull(result)
        assertEquals(1, result.`val`)
        assertEquals(2, result.left?.`val`)
        assertEquals(3, result.right?.`val`)
    }

    @Test
    fun `convert 1,null,2,3 to TreeNode`() {
        val result = TypeConverters.convert("[1,null,2,3]", typeOf<TreeNode>()) as TreeNode?
        assertNotNull(result)
        assertEquals(1, result.`val`)
        assertEquals(null, result.left)
        assertEquals(2, result.right?.`val`)
        assertEquals(3, result.right?.left?.`val`)
    }

    @Test
    fun `equal TreeNode - string expected`() =
        assertTrue(TypeConverters.equal("[1,2,3]".toTreeNode(), "[1,2,3]", typeOf<TreeNode>()))

    @Test
    fun `equal TreeNode - typed expected`() =
        assertTrue(TypeConverters.equal("[1,2,3]".toTreeNode(), "[1,2,3]".toTreeNode(), typeOf<TreeNode>()))
}
