package coderun

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * Игра со спичками
 * https://coderun.yandex.ru/selections/algorithm-training-september-2025/problems/playing-with-matches
 *
 * На столе лежит кучка из N спичек. Двое играют по очереди. За один ход разрешается взять
 * одну, две или три спички — так, чтобы оставшееся в кучке количество спичек **не было простым**
 * (можно оставить 1 или 4, нельзя оставить 2 или 3). Выигрывает тот, кто забирает последнюю спичку.
 *
 * Ввод:  одно число N (1 <= N <= 10 000).
 * Вывод: 1 — если при правильной игре побеждает первый игрок, иначе 2.
 *
 * Approach (win/lose DP, backward induction on positions):
 *   win[n] == "игрок, который ходит при n спичках, побеждает при правильной игре".
 *   win[0] = false — ходить нечем, значит последнюю спичку забрал соперник.
 *   win[n] = exists k in {1,2,3}: n - k >= 0 && !isPrime(n - k) && !win[n - k]
 *   Ответ: 1 если win[N], иначе 2.
 *
 * Pitfalls:
 *   - 0 и 1 не простые — оставить их всегда можно; решето начинает отмечать с 2.
 *   - Ограничение "не простое" касается только *оставляемого* количества, само N может быть простым.
 *   - Бывают позиции, где все три хода запрещены (например, из 4 нельзя оставить ни 3, ни 2) —
 *     позиция без ходов проигрышная.
 *   - Простые позиции недостижимы ходом, но могут быть стартовыми: считать win[] надо для всех n.
 *
 * Сложность: O(N log log N) на решето + O(N) на DP, память O(N).
 */
typealias TMatches = (Int) -> Int

class PlayingWithMatches {

    @Nested
    inner class Solution : ProblemTest<TMatches> {

        override val cases = testCases<TMatches>(
            1 expects 1,
            2 expects 1,
            3 expects 1,
            4 expects 2,
            5 expects 1,
            8 expects 2,
            9 expects 1,
            12 expects 2,
            100 expects 2,
            101 expects 1,
            9999 expects 1,
            10000 expects 2,
        )

        @Test
        fun test() = check(::whoWins)

        fun whoWins(n: Int): Int {
            TODO("build the sieve, fill win[0..n], return 1 if win[n] else 2")
        }

    }
}
