import kotlin.reflect.KFunction
import kotlin.test.assertTrue


interface AproblemTest<Case, Solution : Function<Any?>> {
    val cases: List<Case>
    val solutions: List<Pair<String, Solution>>

    fun Case.check(solution: Solution): Boolean

    fun check() {
        check(cases.isNotEmpty()) {"Cases must be not empty"}
        check(solutions.isNotEmpty()) {"Solutions must be not empty"}
        cases.forEach { case ->
            solutions.forEach { (name, solution) ->
                val result = case.check(solution)
                assertTrue(result, "Unexpected case($case) solution(${name}) result")

            }
        }
    }
}