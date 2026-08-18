# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A personal collection of algorithm/data-structure solutions (mostly LeetCode) written in Kotlin. **The notable thing
about this repo is that there is essentially no production code** — `src/Main.kt` is just "Hello World".
Every solution lives under `test/` and is exercised by a small custom JUnit 5 test harness. A "solution" and
its "test" are the same file.

Two exceptions live under `src/`: `src/cases/cache/LruCache.kt`, and `src/database/isolation/` — runnable PostgreSQL
transaction-isolation demos (see `src/database/isolation/README.md`) backed by the
`docker-compose.yml` at the repo root. They are lessons, not tests: they have their own `main` and are run with
`mise run isolation`, not by the JUnit suite.

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

Prefer the `mise` tasks — they carry the required test filter flags (see the two gotchas below):

```bash
mise run build           # compile only — does NOT run tests
mise run test            # run all tests (excludes other/concurrency)
mise run test-one leetcode.backtracking.I0039combinationSum   # single problem
mise run test-one leetcode.backtracking                       # whole category
mise run clean

mise run db-up           # start Postgres for src/database/isolation (fixed host port 5433)
mise run isolation       # run the transaction-isolation demos (needs db-up first)
mise run db-down         # stop the container
```

The underlying Kotlin Toolchain commands:

```bash
./kotlin build           # compile only — unlike `gradlew build`, this does NOT run tests
./kotlin test --include-classes "*" --exclude-classes "other.concurrency.*"   # all tests
./kotlin test --include-classes "leetcode.backtracking.I0039combinationSum*"  # one problem
./kotlin test --include-classes "leetcode.backtracking.*"                     # one category
./kotlin run             # runs Main.kt
```

Two flags are load-bearing when running the full suite; both are baked into the mise tasks:

- **`--include-classes "*"` is mandatory.** The JUnit Console Launcher applies a default class-name
  filter (`^(Test.*|.+[.$]Test.*|.*Tests?)$`), which the Gradle test task did not. Without an explicit
  `--include-classes`, only the 26 harness utility classes ending in `Test` are discovered; the 197
  problem and lesson classes — named `I####problemName`, `C4*`, `K##*`, `L##*`, none of which end in
  `Test` — are **silently skipped**. That's 116 tests instead of 319, reported as a clean pass. Any
  explicit `--include-classes` pattern replaces that default, which is why the
  single-problem and per-category commands don't need it.
- **A trailing `*` on a class pattern is mandatory.** The `@Test` methods live in the `@Nested inner class
  Solution`, whose filter identity is `leetcode.backtracking.I0039combinationSum/Solution` — nested classes
  use `/`, not `.`. A pattern without the wildcard matches only the outer class, which holds no tests, and
  runs 0 tests. Wildcards match across both `.` and `/`.

`--exclude-classes "other.concurrency.*"` replaces Gradle's `exclude("other/concurrency/*")`; those lesson
files are deliberately racy/deadlocking demos, so a bare `./kotlin test --include-classes "*"` will hang.
Run a single lesson on demand with `mise run test-one other.concurrency.K01CoroutineBasics`. Test filtering
is CLI-only in the Kotlin Toolchain — there is no `module.yaml` equivalent.

- Build: **Kotlin Toolchain 0.11.1** (`module.yaml`, `./kotlin` wrapper); no Gradle.
- The only main-source dependency is `org.postgresql:postgresql` (JDBC driver for the isolation demos). The module now
  has two `main` functions, so `./kotlin run` still picks the auto-detected `Main.kt`; anything else needs
  `--main-class`, as the `isolation` mise task does.
- Kotlin 2.4.10, JVM toolchain 25, JUnit 6.1.2 + kotlin-test.
- Source layout is the Kotlin Toolchain **default**: main sources in `src/`, test sources in `test/`
  (no `main/kotlin` / `test/kotlin` nesting — that was the Gradle/Maven convention). Package directories
  start directly under `test/`, e.g. `test/leetcode/backtracking/`.
- Detekt is declined in IDE settings; there is no lint step in the build.
- The Kotlin Toolchain is Alpha software — re-read the changelog on each `./kotlin update`.

## Architecture

The whole repo is a thin DSL for expressing LeetCode-style test cases declaratively. Three files define the framework:

- `test/leetcode/ProblemTest.kt` — the `ProblemTest<F>` interface and the `testCases` / `args` / `expects`
  DSL.
- `test/leetcode/utils/TypeConverters.kt` — central registry that converts string inputs ↔ typed values and
  defines per-type equality.
- `test/leetcode/utils/ArrayUtils.kt` — string-parsing helpers (`"[1,2,3]"` → `IntArray`, 2D arrays, etc.)
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
- **Multiple valid answers:** use `expectsAnyOf(...)` for problems that explicitly accept more than one correct
  output ("return the index of *any* peak", "find *any* valid path"). The case passes if the result equals **any one**
  of the listed candidates. Unlike `expects`/`expectsAnyOrder` it is a regular **vararg call**, not `infix` (Kotlin
  `infix` functions take exactly one argument), so it is written with a dot:

  ```kotlin
  "[1,2,1,3,5,6,4]".expectsAnyOf(1, 5)              // index 1 or 5 — either peak passes
  args("[1,2,3]", 0).expectsAnyOf("[1,2]", "[3]")   // each candidate type-converted independently
  ```

  Each candidate is converted from its LeetCode string and compared positionally (same order rules as `expects`);
  it does not combine with the any-order semantics of `expectsAnyOrder`.

### Adding a new problem

1. Copy `test/leetcode/_Template.kt`.
2. Set the `package` to the right category (e.g. `leetcode.sliding_window`). Categories are the directories under
   `test/leetcode/` (array_string, backtracking, binary_search, dp_1d, graph_bfs, heap, linked_list, trie,
   two_pointers, etc.).
3. Name the file/class `I####<problemName>` (LeetCode number, zero-padded to 4). `C4*` files are from "Grokking
   Algorithms" (`grokking_algorithms` package).
4. Define the `typealias`, fill in `cases`, and implement one or more solution functions wired into `check(...)`.

### Supporting a new input/output type

If a problem needs a type the harness doesn't yet handle, register it in the `init {}` block of `TypeConverters`. In
short: `register(KClass)` for plain classes, `register(typeOf<...>())` for generic types (erasure makes the `KClass`
ambiguous); supply a custom `equals` for arrays and node types; add a test under `utils/type_converters/`.

**See `test/leetcode/utils/CLAUDE.md` for the full type-converter rules** — the two registries, the
`Handler` contract, per-type equality, the step-by-step recipe, and gotchas. Keep that file and this section in sync
when the conversion layer changes.
