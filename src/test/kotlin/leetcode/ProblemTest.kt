package leetcode

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.assertTrue

interface ProblemTest<F : Function<Any?>> {
    val cases: List<F.() -> Boolean>

    fun check(vararg solutions: F) {
        require(cases.isNotEmpty()) { "Cases must not be empty" }
        require(solutions.isNotEmpty()) { "Solutions must not be empty" }
        solutions.forEachIndexed { si, solution ->
            cases.forEachIndexed { ci, case ->
                assertTrue(case(solution), "solution[${si + 1}] case[${ci + 1}] failed")
            }
        }
    }
}

data class TestInput(val values: Array<out Any?>)

fun args(vararg values: Any?) = TestInput(values)
infix fun Any?.expects(expected: Any?) = TestInput(arrayOf(this)) to expected
infix fun TestInput.expects(expected: Any?) = this to expected

inline fun <reified F : Function<Any?>> testCases(
    vararg inputs: Pair<TestInput, Any?>,
): List<F.() -> Boolean> {
    val ktype = typeOf<F>()
    val typeArgs = ktype.arguments
    val argTypes = typeArgs.dropLast(1).map { it.type!! }
    val returnType = typeArgs.last().type!!

    return inputs.map { (input, expected) ->
        val rawArgs = input.values.toList()
        // Explicit type needed so Kotlin knows 'this' in the lambda is F
        // Conversion happens inside so mutable types (IntArray) are fresh each run
        val case: F.() -> Boolean = {
            val converted = rawArgs.mapIndexed { i, arg -> TypeConverters.convert(arg, argTypes[i]) }
            val result = TypeConverters.callFunction(this as Function<Any?>, converted)
            TypeConverters.equal(result, expected, returnType)
        }
        case
    }
}
