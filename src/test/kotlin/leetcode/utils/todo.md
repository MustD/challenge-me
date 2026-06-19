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

2. ~~Result may be in different order than expected, consider add expectAnyOrder (or better name) to cases.~~
   **DONE** — added the `expectsAnyOrder` infix (alongside `expects`) and an `AnyOrder` marker. It does a recursive
   multiset compare (`TypeConverters.canonicalize` sorts every array/list by string form), so order is ignored at
   *every* nesting level and arrays unify with lists. `equal(...)` gained an `anyOrder` flag. Use for subsets /
   combinations / permutations / group-anagrams. Tests: `AnyOrderEqualityTest` (converter layer) +
   `AnyOrderDslTest` (full `testCases`→`check` path). Plain `expects` is still positional/order-sensitive.

   ```kotlin
   "[1,2,3]" expectsAnyOrder "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]"
   ```
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

4. ~~Validate type converters covered with tests.~~
   **DONE** — audited the `TypeConverters` registry against `type_converters/`; every registered handler now
   has a matching test. Filled the three gaps: `QuadNodeConverterTest` (was registered but untested),
   `ListOfIntConverterTest` (`List<Int>`), and `ListOfStringConverterTest` (`List<String>`). Each follows the
   standard shape — normal convert, empty `"[]"`, string-expected equality, typed-expected equality.
5. Run all cases and functions, provide report for all cases and attempts at the same time
6. ~~Parser can't represent an empty *inner* list: `"[[],[1]]"` parses the `[]` as `[""]`, so `toListOfIntLists()`
   throws
   `NumberFormatException` on `"".toInt()`.~~
   **DONE** — `array2arraySplit()` now maps an empty row (the `""` left between `],[` delimiters) to `emptyList()`
   instead of `[""]`, so `"[[],[1]]"` → `[[], [1]]`. Covers leading/trailing/multiple empty inner lists, `"[[]]"`,
   and subsets-style answers that include the empty set. Tests: `ListOfIntListsConverterTest`. Also benefits
   `toIntArray2D` / `toCharArray2D`, which previously crashed on empty rows too.

   ```kotlin
   "[1,2]" expectsAnyOrder "[[],[1],[2],[1,2]]"   // subsets including the empty set
   ```
7. Replace the delete-brackets + `split` parsing in `ArrayUtils.kt` with **JSON parsing**. LeetCode array literals
   (`[[],[1]]`, `["a","b"]`, `[1.5,2]`, `[1,null,2]`) are valid JSON, so `arraySplit()` / `array2arraySplit()` could
   delegate to a JSON tree parser instead of stripping `"`/`[`/`]` and splitting on `,` / `],[`. This removes the whole
   class of structural bugs the current approach keeps hitting:
   - empty inner lists (issue 6 — special-cased now, would be free under JSON),
   - the `[[""]]` (one empty string) vs `[[]]` (one empty list) ambiguity, since `replace("\"","")` no longer destroys
     quotes,
   - string elements containing `,` or `]` (e.g. `["a,b","c]d"]`), which split-on-comma currently mangles,
   - deeper/ragged nesting, handled by one parser instead of a per-shape `toX` helper.

   Plan: keep the existing `arraySplit()` / `array2arraySplit()` signatures (return `List<String>` /
   `List<List<String>>`)
   so nothing downstream changes; back them with `Json.parseToJsonElement` (`kotlinx.serialization`, add the dependency)
   and serialize each leaf to its string form. The typed `toIntArray` / `toListOfIntLists` / … helpers stay as-is.
   Accommodate LeetCode `null` in tree serializations (`[1,null,2]`). Validate against the full existing
   `type_converters/` suite (especially `WhitespaceToleranceTest`, `ListOfIntListsConverterTest`,
   `ListOfStringListsConverterTest`) — behavior must be identical, plus the new edge cases above.
