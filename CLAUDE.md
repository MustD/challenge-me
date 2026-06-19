# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A personal collection of algorithm/data-structure solutions (mostly LeetCode) written in Kotlin. **The notable thing
about this repo is that there is essentially no production code** — `src/main/kotlin/Main.kt` is just "Hello World".
Every solution lives under `src/test/kotlin/` and is exercised by a small custom JUnit 5 test harness. A "solution" and
its "test" are the same file.

## Educational purpose — read before solving

**This is a learning/practice repository.** The owner is working through algorithm problems to build and reinforce their
own understanding of the underlying patterns — the point is the practice, not accumulating passing code. Treat that as
the primary goal when helping here.

This changes how you should respond when asked to solve a problem:

- **Explain before (or alongside) the code, never instead of explaining.** Walk through the intuition, the chosen
  technique/pattern, and the time/space trade-offs. A bare working solution dropped in with no reasoning defeats the
  purpose of the repo.
- **Teach the pattern, not just this instance.** Name the general technique (e.g. "this is a classic backtracking
  template", "two-pointer on a sorted array") so it transfers to the next problem.
- **Prefer nudges when the user is stuck.** If they're mid-attempt, favor hints and Socratic prompts over handing over
  the full answer. The `/leetcode-help` skill exists for exactly this — it writes intuition, pattern, complexity, and
  pitfalls into the problem file alongside a verified reference solution.
- **Call out pitfalls and edge cases** (off-by-one, empty input, integer overflow, ordering requirements) as teaching
  points rather than silently handling them.

## Commands

```bash
./gradlew build          # compile + run all tests
./gradlew test           # run all tests

# Run a single problem's tests (outer class name = file name without .kt):
./gradlew test --tests "leetcode.backtracking.I0039combinationSum"

# Run by wildcard (e.g. everything in a category):
./gradlew test --tests "leetcode.backtracking.*"
```

- Kotlin 2.3.20, JVM toolchain 25, JUnit 5 + kotlin-test.
- Gradle configuration cache is enabled (`org.gradle.configuration-cache=true`).
- Detekt is declined in IDE settings; there is no lint step in the build.

## Architecture

The whole repo is a thin DSL for expressing LeetCode-style test cases declaratively. Three files define the framework:

- `src/test/kotlin/leetcode/ProblemTest.kt` — the `ProblemTest<F>` interface and the `testCases` / `args` / `expects`
  DSL.
- `src/test/kotlin/leetcode/utils/TypeConverters.kt` — central registry that converts string inputs ↔ typed values and
  defines per-type equality.
- `src/test/kotlin/leetcode/utils/ArrayUtils.kt` — string-parsing helpers (`"[1,2,3]"` → `IntArray`, 2D arrays, etc.)
  used by the converters.

### How a problem file is structured

Each problem is one file = one outer class containing a `@Nested inner class Solution : ProblemTest<F>`, where `F` is a
`typealias` capturing the function signature. Pattern (see `_Template.kt`):

```kotlin
typealias I0039 = (IntArray, Int) -> List<List<Int>>

class I0039combinationSum {
    @Nested
    inner class Solution : ProblemTest<I0039> {
        override val cases = testCases<I0039>(
            args("[2,3,6,7]", 7) expects "[[2,2,3],[7]]",   // multi-arg case
            0 expects 0,                                     // single-arg shorthand
        )

        @Test
        fun test() = check(::combinationSum, ::combinationSumNoSort)  // pass 1+ solution fns

        fun combinationSum(candidates: IntArray, target: Int): List<List<Int>> {
            ...
        }
    }
}
```

Key mechanics to understand before editing:

- **Inputs and expected values are written as LeetCode-style strings** (`"[2,3,6,7]"`, `"[[2,2,3],[7]]"`). `testCases`
  reads the `typealias F` via reflection (`typeOf<F>()`) to learn each argument type and the return type, then
  `TypeConverters.convert` parses the strings into real typed values. Plain Kotlin literals (e.g. `7`, `0`) are passed
  through untouched.
- **Conversion happens fresh on every run**, inside the case lambda, so mutable inputs like `IntArray` are not
  shared/mutated across multiple solutions.
- **Whitespace, tabs and line breaks between tokens are tolerated** — structural whitespace outside quotes is stripped
  before parsing, so a large matrix can be written as a readable multiline `"""…"""` literal instead of one long line:

  ```kotlin
  args("""
      [[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],
       [1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]
  """) expects "..."
  ```

  Whitespace *inside* quoted elements is preserved (e.g. `["a b", "c"]`).
- **`check(vararg solutions)` runs every solution against every case** — this is how multiple approaches to the same
  problem are validated together. A failure reports `solution[i] case[j] failed`.
- **Equality is type-aware.** `TypeConverters` registers custom `equals` for array/list/linked-list/tree types (e.g.
  `IntArray` compared by `.toList()`, `ListNode`/`Node`/`TreeNode` compared by `toString()`). Order-sensitive by
  default — solutions returning `List<List<Int>>` etc. must produce output matching the expected ordering.
- **Order-insensitive cases:** use `expectsAnyOrder` instead of `expects` for problems whose answer "may be returned in
  any order" (subsets, combinations, permutations, group-anagrams). It compares as a recursive multiset — ignoring
  order at *every* nesting level — so a correct solution emitting results in a different order still passes:

  ```kotlin
  "[1,2,3]" expectsAnyOrder "[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]"
  ```

  Backed by `TypeConverters.canonicalize` (recursively sorts arrays/lists by string form). If a problem needs the
  *inner* order preserved, use plain `expects`.

### Adding a new problem

1. Copy `src/test/kotlin/leetcode/_Template.kt`.
2. Set the `package` to the right category (e.g. `leetcode.sliding_window`). Categories are the directories under
   `src/test/kotlin/leetcode/` (array_string, backtracking, binary_search, dp_1d, graph_bfs, heap, linked_list, trie,
   two_pointers, etc.).
3. Name the file/class `I####<problemName>` (LeetCode number, zero-padded to 4). `C4*` files are from "Grokking
   Algorithms" (`grokking_algorithms` package).
4. Define the `typealias`, fill in `cases`, and implement one or more solution functions wired into `check(...)`.

### Supporting a new input/output type

If a problem needs a type the harness doesn't yet handle, register it in the `init {}` block of `TypeConverters`. In
short: `register(KClass)` for plain classes, `register(typeOf<...>())` for generic types (erasure makes the `KClass`
ambiguous); supply a custom `equals` for arrays and node types; add a test under `utils/type_converters/`.

**See `src/test/kotlin/leetcode/utils/CLAUDE.md` for the full type-converter rules** — the two registries, the
`Handler` contract, per-type equality, the step-by-step recipe, and gotchas. Keep that file and this section in sync
when the conversion layer changes.
