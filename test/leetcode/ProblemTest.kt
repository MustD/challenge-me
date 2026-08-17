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

/**
 * Marks a set of acceptable answers: the result passes if it equals **any one** of [expected].
 * Wrapping is done by the `expectsAnyOf` functions; [testCases] unwraps it and compares the
 * result against each candidate (each type-converted independently) until one matches.
 *
 * See `expectsAnyOf`.
 */
data class AnyOf(val expected: List<Any?>)

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

/**
 * Like [expects], but accepts **several valid answers** — the case passes if the result equals
 * any one of [expected]. Use for problems that explicitly allow more than one correct output,
 * e.g. "return the index of *any* peak" (LC 162), "find *any* valid path", etc.
 *
 *     "[1,2,1,3,5,6,4]".expectsAnyOf(1, 5)   // index 1 or 5 are both peaks -> either passes
 *
 * Unlike [expects]/[expectsAnyOrder] this is a regular (vararg) call, not `infix`, since Kotlin
 * `infix` functions take exactly one argument. Each candidate is type-converted independently
 * using the return type, so they can be written as LeetCode-style strings just like [expects]:
 *
 *     args("[1,2,3]", 0).expectsAnyOf("[1,2]", "[3]")   // any one acceptable List<Int>
 *
 * Each candidate is compared positionally (use the order rules of [expects]); combine with the
 * any-order semantics of [expectsAnyOrder] is not supported.
 */
fun Any?.expectsAnyOf(vararg expected: Any?) = TestInput(listOf(this)) to AnyOf(expected.toList())
fun TestInput.expectsAnyOf(vararg expected: Any?) = this to AnyOf(expected.toList())

inline fun <reified F : Function<Any?>> testCases(
    vararg inputs: Pair<TestInput, Any?>,
): List<F.() -> String?> {
    val ktype = typeOf<F>()
    val typeArgs = ktype.arguments
    val argTypes = typeArgs.dropLast(1).map { it.type!! }
    val returnType = typeArgs.last().type!!

    return inputs.map { (input, rawExpected) ->
        val rawArgs = input.values.toList()
        // Explicit type needed so Kotlin knows 'this' in the lambda is F
        // Conversion happens inside so mutable types (IntArray) are fresh each run
        val case: F.() -> String? = {
            val converted = rawArgs.mapIndexed { i, arg -> TypeConverters.convert(arg, argTypes[i]) }
            val result = TypeConverters.callFunction(this as Function<Any?>, converted)
            val inputDesc = " (input: ${rawArgs.joinToString(", ") { render(it) }})"
            when (rawExpected) {
                is AnyOf ->
                    if (rawExpected.expected.any { TypeConverters.equal(result, it, returnType) }) null
                    else "expected any of: [${rawExpected.expected.joinToString(", ")}]," +
                            " got: ${render(result)}$inputDesc"

                is AnyOrder ->
                    if (TypeConverters.equal(result, rawExpected.expected, returnType, anyOrder = true)) null
                    else "expected (any order): ${rawExpected.expected}, got: ${render(result)}$inputDesc"

                else ->
                    if (TypeConverters.equal(result, rawExpected, returnType)) null
                    else "expected: $rawExpected, got: ${render(result)}$inputDesc"
            }
        }
        case
    }
}
