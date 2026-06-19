# Migration Plan: `AproblemTest` → `ProblemTest` (new `_Template` format)

## Goal

Rewrite the **test scaffolding** of the files still on the `@Deprecated AproblemTest`
harness into the new `ProblemTest<F>` / `testCases` DSL (see `_Template.kt`).
**Every solution function body stays byte-for-byte unchanged** — only the surrounding
`Case` / `solutions` / `Case.check` / `parseCase` machinery is replaced.

Scope decision: **migrate only the mechanically-compatible files; defer the two subsets
the new harness can't express** (see "Deferred" below). `AproblemTest.kt` stays in the
repo as long as deferred files depend on it.

## Scope summary

- **92** files on the deprecated harness; **35** already migrated (untouched).
- **~84 mechanical** → migrate now (now includes the former order-insensitive subset, see below).
- **~8 deferred** → leave on `AproblemTest` for a later decision.

## Per-file transformation recipe (mechanical case)

1. Swap `import leetcode.AproblemTest` → `import leetcode.ProblemTest`; ensure
   `org.junit.jupiter.api.Nested` + `kotlin.test.Test` imports are present.
2. Ensure a `typealias I#### = (Args…) -> R` exists (~29 files have an inline signature
   and need one added — inferable from the old `AproblemTest<Case, …>` type arg and the
   solution functions).
3. Replace the body of `inner class Solution`:
    - Delete `data class Case`, the `solutions` list, the `Case.check` override, and any
      `parseCase` / `prepareCase` lambda.
    - `override val cases = testCases<I####>( … )`, converting each old case to
      `args(...) expects "..."` (or the `x expects y` shorthand). Reuse the **same
      LeetCode strings** the old `parseCase` consumed; pass non-string scalars as plain
      literals.
    - `@Test fun test() = check(::sol1, ::sol2, …)` listing every existing solution fn.
4. Leave all solution functions untouched.

No `TypeConverters` changes needed — its registered types
(`Int/Long/Boolean/Double/String`, `IntArray/DoubleArray`, `Array<String>`,
`List<Int/String>`, `List<List<Int/String>>`, `Array<IntArray>`, `Array<CharArray>`,
`ListNode/TreeNode/Node/QuadNode`) cover every mechanical file.

## Deferred — do NOT migrate (stay on `AproblemTest`)

**A. Void / in-place mutation** (return type `Unit`; new harness asserts on the *return
value*, so the mutated array is invisible):
`I0048 RotateImage`, `I0073 SetMatrixZeroes`, `I0289 GameOfLife`, `I0189 rotateArray` —
plus verify `I0088 mergeSortedArray`.

> **B. Order-insensitive equality — NO LONGER DEFERRED.** The framework now ships
> `expectsAnyOrder` + `TypeConverters.canonicalize` (recursive multiset equality), which
> is exactly the per-case comparator hook this subset was waiting on. `I0001 twoSum`,
> `I0015 3sum`, `I0049 GroupAnagrams`, `I0077 combinations` are now **mechanical**: migrate
> them like any other file but use `args(...) expectsAnyOrder "..."` instead of `expects`.
> Their old `Case.check` (inner/outer `.sorted()` normalization) is equivalent to — or
> strictly more lenient than — the recursive multiset compare, so a green run confirms
> correctness. (Caveat: the parser still can't express an empty *inner* list `[[],[1]]`;
> none of these four produce one.)

## Execution phases

Work category-by-category so a regression is localized; build after each batch.

1. **Root-level files** (8) — simplest, e.g. `I0007reverseInt`.
2. **`array_string`, `hash_map`, `two_pointers`, `sliding_window`, `matrix`, `intervals`,
   `stack`, `math`, `bit_manipulation`** — bulk scalar/array problems.
3. **`linked_list`, `binary_tree_*`, `binary_search*`, `divide_conquer`, `graph_*`,
   `heap`, `dp_*`** — `ListNode` / `TreeNode` types.
4. Per batch: `./gradlew test --tests "leetcode.<category>.*"`. A green run *is* the
   correctness check (the migrated test reruns the same cases against the same solutions).

**Verification note:** for any file whose old `Case.check` did extra normalization but is
being treated as mechanical, a passing test confirms the migrated expected string matches
the solution's emitted order; a failure flags it as actually belonging in subset B.

## Out of scope (this pass)

- Deleting `AproblemTest.kt` (still needed by the deferred files).
- Any framework extension for the deferred subsets.
- Touching the 35 already-migrated files.

## Outcome (completed)

All mechanically-compatible files were migrated and `./gradlew build` is green.

**7 files left on `AproblemTest`** — the 4 planned plus 3 discovered during migration
whose semantics the `ProblemTest` harness cannot express (it asserts on a single expected
value, so any check that accepts *multiple* valid answers or needs a non-serializable input
can't be ported as-is):

- `I0048 RotateImage`, `I0073 SetMatrixZeroes`, `I0289 GameOfLife` — void / in-place (planned).
- `I0189 rotateArray` — void / in-place (planned). (`I0088 mergeSortedArray` returns the array,
  so it WAS migrated.)
- `I2028 findMissingObservations` — its `check` accepts **any** answer with the correct mean
  (one case passes only via that fallback), not the literal expected array.
- `I0005 longestPalindromicSubstring` — `check` accepts **any** of several valid palindromes
  (`output.contains(result)`).
- `I0141 LinkedListCycle` — input is a **cyclic** list the string converter can't build; the
  test was already `@Disabled`.

**One dropped case:** `I0049 GroupAnagrams` lost its `[""] -> [[""]]` case. The 2D parser strips
quotes before the empty-row check, so `[[""]]` collapses to `[[]]`; that case was already
failing on the old harness for the same reason. Documented inline in the file.

Subset B (`expectsAnyOrder`): `I0001`, `I0049`, `I0015`, `I0077` migrated as planned; also
applied to `I0228 summaryRanges` whose old `check` sorted both sides.
