package leetcode.utils

import leetcode.utils.ArrayUtils.toCharArray
import leetcode.utils.ArrayUtils.toCharArray2D
import leetcode.utils.ArrayUtils.toDoubleArray
import leetcode.utils.ArrayUtils.toIntArray
import leetcode.utils.ArrayUtils.toIntArray2D
import leetcode.utils.ArrayUtils.toListOfIntLists
import leetcode.utils.ArrayUtils.toListOfStringLists
import leetcode.utils.ArrayUtils.toStringArray
import leetcode.utils.TypeConverters.canonicalize
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object TypeConverters {
    data class Handler(
        val fromString: (String) -> Any?,
        val equals: ((Any?, Any?) -> Boolean)? = null,
    )

    private val handlers = mutableMapOf<KClass<*>, Handler>()
    private val ktypeHandlers = mutableMapOf<KType, Handler>()

    fun register(klass: KClass<*>, handler: Handler) {
        handlers[klass] = handler
    }

    fun register(type: KType, handler: Handler) {
        ktypeHandlers[type] = handler
    }

    fun convert(value: Any?, targetType: KType): Any? {
        if (value == null) return null
        if (value !is String) return value  // already typed (e.g. Int literal)
        ktypeHandlers[targetType]?.let { return it.fromString(value) }
        val klass = targetType.classifier as? KClass<*> ?: return value
        // Use the handler if one exists, even when fromString returns null
        // (e.g. "[]" -> null ListNode); only fall back to the raw value when
        // there is no registered handler for this type.
        val handler = handlers[klass] ?: return value
        return handler.fromString(value)
    }

    /**
     * @param anyOrder when true, ordering is ignored at every nesting level (recursive multiset
     *   compare via [canonicalize]) instead of the default positional comparison. Used by
     *   `expectsAnyOrder` for "answer may be returned in any order" problems.
     */
    fun equal(result: Any?, expected: Any?, returnType: KType, anyOrder: Boolean = false): Boolean {
        ktypeHandlers[returnType]?.let { handler ->
            val normalizedExpected = if (expected is String) handler.fromString(expected) else expected
            if (anyOrder) return canonicalize(result) == canonicalize(normalizedExpected)
            return handler.equals?.invoke(result, normalizedExpected) ?: (result == normalizedExpected)
        }
        val klass = returnType.classifier as? KClass<*>
        val handler = handlers[klass]
        val normalizedExpected = if (expected is String && klass != String::class && handler != null)
            handler.fromString(expected) // may legitimately be null (e.g. "[]" -> null)
        else expected
        if (anyOrder) return canonicalize(result) == canonicalize(normalizedExpected)
        return handler?.equals?.invoke(result, normalizedExpected)
            ?: (result == normalizedExpected)
    }

    /**
     * Recursively normalizes a value into an order-independent canonical form so two collections
     * that differ only in element order compare equal. Every array/list becomes a `List` whose
     * elements are themselves canonicalized and then sorted by their string representation.
     *
     * Arrays and lists collapse to the same shape, so a solution returning `Array<IntArray>` still
     * matches an expected `List<List<Int>>` parsed from a string. Scalars are returned untouched.
     * Sorting by `toString()` only needs to be a *stable total order* — equal multisets then yield
     * structurally-equal canonical lists under `==`.
     */
    private fun canonicalize(value: Any?): Any? = when (value) {
        null -> null
        is IntArray -> value.toList().map(::canonicalize).sortedBy { it.toString() }
        is LongArray -> value.toList().map(::canonicalize).sortedBy { it.toString() }
        is DoubleArray -> value.toList().map(::canonicalize).sortedBy { it.toString() }
        is CharArray -> value.toList().map(::canonicalize).sortedBy { it.toString() }
        is BooleanArray -> value.toList().map(::canonicalize).sortedBy { it.toString() }
        is Array<*> -> value.map(::canonicalize).sortedBy { it.toString() }
        is List<*> -> value.map(::canonicalize).sortedBy { it.toString() }
        else -> value
    }

    @Suppress("UNCHECKED_CAST")
    fun callFunction(fn: Function<Any?>, args: List<Any?>): Any? = when (args.size) {
        1 -> (fn as (Any?) -> Any?).invoke(args[0])
        2 -> (fn as (Any?, Any?) -> Any?).invoke(args[0], args[1])
        3 -> (fn as (Any?, Any?, Any?) -> Any?).invoke(args[0], args[1], args[2])
        4 -> (fn as (Any?, Any?, Any?, Any?) -> Any?).invoke(args[0], args[1], args[2], args[3])
        else -> error("Unsupported arity ${args.size}")
    }

    init {
        register(Int::class, Handler({ it.trim().toInt() }))
        register(Long::class, Handler({ it.trim().toLong() }))
        register(Boolean::class, Handler({ it.trim().toBoolean() }))
        register(Double::class, Handler({ it.trim().toDouble() }))
        register(String::class, Handler({ it }))
        register(TreeNode::class, Handler({ it.toTreeNode() }))
        register(
            Node::class, Handler(
                fromString = { it.toNode() },
                equals = { a, b -> a?.toString() == b?.toString() }
            ))
        register(
            ListNode::class, Handler(
                fromString = { it.toListNode() },
                equals = { a, b -> a?.toString() == b?.toString() }
            ))
        register(
            QuadNode::class, Handler(
                fromString = { it.toQuadNode() },
                equals = { a, b -> a?.toString() == b?.toString() }
            ))
        register(
            IntArray::class, Handler(
                fromString = { it.toIntArray() },
                equals = { a, b -> (a as? IntArray)?.toList() == (b as? IntArray)?.toList() }
            ))
        register(
            DoubleArray::class, Handler(
                fromString = { it.toDoubleArray() },
                equals = { a, b -> (a as? DoubleArray)?.toList() == (b as? DoubleArray)?.toList() }
            ))
        register(
            CharArray::class, Handler(
                fromString = { it.toCharArray() },
                equals = { a, b -> (a as? CharArray)?.toList() == (b as? CharArray)?.toList() }
            ))
        register(
            typeOf<Array<String>>(), Handler(
                fromString = { it.toStringArray() },
                equals = { a, b ->
                    (a as? Array<*>)?.toList() == (b as? Array<*>)?.toList()
                }
            ))
        register(
            typeOf<Array<ListNode?>>(), Handler(
                fromString = { it.toListNodeArray() },
                equals = { a, b ->
                    (a as? Array<*>)?.map { it?.toString() } == (b as? Array<*>)?.map { it?.toString() }
                }
            ))
        register(
            typeOf<List<String>>(), Handler(
                fromString = { it.toStringArray().toList() },
            )
        )
        register(
            typeOf<List<Int>>(), Handler(
                fromString = { it.toIntArray().toList() },
            )
        )

        register(
            typeOf<List<List<String>>>(), Handler(
                fromString = { it.toListOfStringLists() }
            ))
        register(
            typeOf<List<List<Int>>>(), Handler(
                fromString = { it.toListOfIntLists() }
            ))
        register(
            typeOf<Array<IntArray>>(), Handler(
                fromString = { it.toIntArray2D() },
                equals = { a, b ->
                    (a as? Array<IntArray>)?.map { it.toList() } == (b as? Array<IntArray>)?.map { it.toList() }
                }
            ))
        register(
            typeOf<Array<CharArray>>(), Handler(
                fromString = { it.toCharArray2D() },
                equals = { a, b ->
                    (a as? Array<CharArray>)?.map { it.toList() } == (b as? Array<CharArray>)?.map { it.toList() }
                }
            ))
    }
}
