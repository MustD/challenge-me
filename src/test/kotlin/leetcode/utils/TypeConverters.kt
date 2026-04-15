package leetcode.utils

import leetcode.utils.ArrayUtils.toCharArray2D
import leetcode.utils.ArrayUtils.toDoubleArray
import leetcode.utils.ArrayUtils.toIntArray
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
        return handlers[klass]?.fromString?.invoke(value) ?: value
    }

    fun equal(result: Any?, expected: Any?, returnType: KType): Boolean {
        ktypeHandlers[returnType]?.let { handler ->
            val normalizedExpected = if (expected is String) handler.fromString(expected) else expected
            return handler.equals?.invoke(result, normalizedExpected) ?: (result == normalizedExpected)
        }
        val klass = returnType.classifier as? KClass<*>
        val normalizedExpected = if (expected is String && klass != String::class)
            handlers[klass]?.fromString?.invoke(expected) ?: expected
        else expected
        return handlers[klass]?.equals?.invoke(result, normalizedExpected)
            ?: (result == normalizedExpected)
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
            ListNode::class, Handler(
            fromString = { it.toListNode() },
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
            typeOf<Array<CharArray>>(), Handler(
                fromString = { it.toCharArray2D() },
                equals = { a, b ->
                    (a as? Array<CharArray>)?.map { it.toList() } == (b as? Array<CharArray>)?.map { it.toList() }
                }
            ))
    }
}
