package leetcode

import leetcode.utils.TypeConverters
import kotlin.reflect.typeOf
import kotlin.test.fail

interface ProblemTest<F : Function<Any?>> {
    val cases: List<F.() -> String?>

    /**
     * Runs every [solutions] against every case and reports **all** failures at once —
     * it does not stop at the first mismatch. A solution that throws is recorded as a
     * failure rather than aborting the whole run, so one broken approach never hides the
     * results of the others.
     */
    fun check(vararg solutions: F) {
        require(cases.isNotEmpty()) { "Cases must not be empty" }
        require(solutions.isNotEmpty()) { "Solutions must not be empty" }
        val failures = buildList {
            solutions.forEachIndexed { si, solution ->
                cases.forEachIndexed { ci, case ->
                    val failure = try {
                        case(solution)
                    } catch (e: Throwable) {
                        "threw ${e::class.simpleName}: ${e.message}"
                    }
                    if (failure != null) add("  solution[${si + 1}] case[${ci + 1}]: $failure")
                }
            }
        }
        if (failures.isNotEmpty()) {
            val total = solutions.size * cases.size
            fail("${failures.size}/$total checks failed:\n" + failures.joinToString("\n"))
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

data class TestInput(val values: List<Any?>)

/**
 * Marks an expected value as order-insensitive. Wrapping is done by the `expectsAnyOrder`
 * infix functions; [testCases] unwraps it and asks [TypeConverters.equal] for a multiset
 * (order-independent) comparison instead of the default positional one.
 *
 * See `expectsAnyOrder`.
 */
data class AnyOrder(val expected: Any?)

fun args(vararg values: Any?) = TestInput(values.toList())
infix fun Any?.expects(expected: Any?) = TestInput(listOf(this)) to expected
infix fun TestInput.expects(expected: Any?) = this to expected

/**
 * Like [expects], but the comparison ignores ordering at every nesting level (a recursive
 * multiset compare). Use for problems whose answer "may be returned in any order" — subsets,
 * combinations, permutations, group-anagrams, etc. — so a correct solution that emits results
 * in a different order than the literal still passes.
 *
 *     "[1,2,3]" expectsAnyOrder "[[1,2,3],[3,2,1],[2,1,3], ...]"   // any permutation order
 *
 * Order-insensitivity is deep: both the outer list and each inner list are compared as
 * multisets. If a problem requires the *inner* order to be preserved, use plain [expects].
 */
infix fun Any?.expectsAnyOrder(expected: Any?) = TestInput(listOf(this)) to AnyOrder(expected)
infix fun TestInput.expectsAnyOrder(expected: Any?) = this to AnyOrder(expected)

inline fun <reified F : Function<Any?>> testCases(
    vararg inputs: Pair<TestInput, Any?>,
): List<F.() -> String?> {
    val ktype = typeOf<F>()
    val typeArgs = ktype.arguments
    val argTypes = typeArgs.dropLast(1).map { it.type!! }
    val returnType = typeArgs.last().type!!

    return inputs.map { (input, rawExpected) ->
        val rawArgs = input.values.toList()
        val anyOrder = rawExpected is AnyOrder
        val expected = if (rawExpected is AnyOrder) rawExpected.expected else rawExpected
        // Explicit type needed so Kotlin knows 'this' in the lambda is F
        // Conversion happens inside so mutable types (IntArray) are fresh each run
        val case: F.() -> String? = {
            val converted = rawArgs.mapIndexed { i, arg -> TypeConverters.convert(arg, argTypes[i]) }
            val result = TypeConverters.callFunction(this as Function<Any?>, converted)
            if (TypeConverters.equal(result, expected, returnType, anyOrder)) null
            else "expected${if (anyOrder) " (any order)" else ""}: $expected, got: ${render(result)}" +
                    " (input: ${rawArgs.joinToString(", ") { render(it) }})"
        }
        case
    }
}
