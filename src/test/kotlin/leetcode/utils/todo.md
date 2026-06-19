# File to note improvements to leetcode framework.

1. ~~Multiline string inputs like matrix. Worth cleaning/removing /n and whitespaces before parsing.~~
   **DONE** — `arraySplit()` / `array2arraySplit()` now run `stripStructuralWhitespace()` first, dropping
   spaces/tabs/line breaks outside quotes. Both forms below parse identically. See `WhitespaceToleranceTest`.

```kotlin
 "[[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]," +
        "[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]"

//VS

"""
 [[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],
 [1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]
 """
```

2. Result may be in different order than expected, consider add expectAnyOrder (or better name) to cases. Or better if
   result is array or any order matter collection - sort it before comparing.
3. Current test failure message is not very helpful. Consider adding more information about the failure.

```terminaloutput
solution[1] case[1] failed
Expected :null
Actual   :expected: [[0,1],[1,0],[1,1],[1,1],[1,0]], got: [[0,0],[1,0],[1,1],[1,1],[1,0]]
<Click to see difference>

org.opentest4j.AssertionFailedError: solution[1] case[1] failed ==> expected: <null> but was: <expected: [[0,1],[1,0],[1,1],[1,1],[1,0]], got: [[0,0],[1,0],[1,1],[1,1],[1,0]]>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:158)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:139)
	at org.junit.jupiter.api.AssertNull.failNotNull(AssertNull.java:56)
	at org.junit.jupiter.api.AssertNull.assertNull(AssertNull.java:40)
	at org.junit.jupiter.api.Assertions.assertNull(Assertions.java:306)
	at kotlin.test.junit5.JUnit5Asserter.assertNull(JUnitSupport.kt:52)
	at kotlin.test.AssertionsKt__AssertionsKt.assertNull(Assertions.kt:162)
	at kotlin.test.AssertionsKt.assertNull(Unknown Source)
	at leetcode.ProblemTest.check(ProblemTest.kt:15)
	at leetcode.divide_conquer.I0427constructQuadTree$Solution.check(I0427constructQuadTree.kt:38)
	at leetcode.divide_conquer.I0427constructQuadTree$Solution.check(I0427constructQuadTree.kt:38)
	at leetcode.divide_conquer.I0427constructQuadTree$Solution.test(I0427constructQuadTree.kt:60)
```

4. Validate type converted covered with tests.
5. Run all cases and functions, provide report for all cases and attempts at the same time
