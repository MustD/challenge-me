package leetcode

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.assertNull

interface ProblemTest<F : Function<Any?>> {
    val cases: List<F.() -> String?>

    fun check(vararg solutions: F) {
        require(cases.isNotEmpty()) { "Cases must not be empty" }
        require(solutions.isNotEmpty()) { "Solutions must not be empty" }
        solutions.forEachIndexed { si, solution ->
            cases.forEachIndexed { ci, case ->
                assertNull(case(solution), "solution[${si + 1}] case[${ci + 1}] failed")
            }
        }
    }
}

fun render(value: Any?): String = when (value) {
    is Array<*> -> value.contentDeepToString()
    is IntArray -> value.contentToString()
    is DoubleArray -> value.contentToString()
    is CharArray -> value.contentToString()
    else -> value.toString()
}

data class TestInput(val values: Array<out Any?>)

fun args(vararg values: Any?) = TestInput(values)
infix fun Any?.expects(expected: Any?) = TestInput(arrayOf(this)) to expected
infix fun TestInput.expects(expected: Any?) = this to expected

inline fun <reified F : Function<Any?>> testCases(
    vararg inputs: Pair<TestInput, Any?>,
): List<F.() -> String?> {
    val ktype = typeOf<F>()
    val typeArgs = ktype.arguments
    val argTypes = typeArgs.dropLast(1).map { it.type!! }
    val returnType = typeArgs.last().type!!

    return inputs.map { (input, expected) ->
        val rawArgs = input.values.toList()
        // Explicit type needed so Kotlin knows 'this' in the lambda is F
        // Conversion happens inside so mutable types (IntArray) are fresh each run
        val case: F.() -> String? = {
            val converted = rawArgs.mapIndexed { i, arg -> TypeConverters.convert(arg, argTypes[i]) }
            val result = TypeConverters.callFunction(this as Function<Any?>, converted)
            if (TypeConverters.equal(result, expected, returnType)) null
            else "expected: $expected, got: ${render(result)}"
        }
        case
    }
}
