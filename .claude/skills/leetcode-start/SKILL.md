---
context: fork
name: leetcode-start
description: Scaffold a new, ready-to-solve LeetCode problem file in this repo. Looks up the problem statement, infers the function signature, writes test cases from the official examples, and leaves the solution body as a stub for the user to fill in. Invoke as /leetcode-start problem-number (e.g. /leetcode-start 3333). Creates the file in the root leetcode/ dir; it can be moved into a themed category later.
---

# leetcode-start

The user wants a fresh problem scaffolded so they can solve it themselves. The argument is the **LeetCode problem number
** (e.g. `3333`).

**This is a learning repo — DO NOT solve the problem.** The whole point of this skill is to produce a *ready-to-solve
template*: correct signature, real description, real test cases, and an empty solution stub. The user writes the
algorithm. (When they get stuck, `/leetcode-help` is the skill that supplies a worked reference.) See the
`explain-before-solving` memory.

## Steps

### 1. Check it doesn't already exist

- Zero-pad the number to 4 digits → e.g. `3333` becomes `I3333`.
- Search `test/` for `I3333*.kt`. If found, stop and tell the user it's already scaffolded (give the path) —
  offer `/leetcode-help` instead. Don't overwrite.

### 2. Identify the problem accurately (WebSearch — do NOT guess)

- Use WebSearch/WebFetch to find LeetCode problem #N: its exact **title**, **statement**, **constraints**, **function
  signature**, and the **worked examples**. The number alone is not enough — confirm via search.
- From the official signature, work out:
    - the **camelCase method name** (e.g. `mergeAlternately`) — used for both the file name and the solution function.
    - the **argument types** and **return type**, mapped to the harness's supported types (see step 4).

### 3. Create the file

- Path: `test/leetcode/I####<methodName>.kt` (root `leetcode/` dir — themed move happens later).
- `package leetcode` (root files use the bare package — no category subpackage).
- File/class name: `I####<methodName>` (e.g. `I3333countOfSubstrings`), matching the LeetCode number zero-padded to 4.

Use this structure (copy of `_Template.kt`, root-package variant):

```kotlin
package leetcode

import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * <N>. <Official Title>  (https://leetcode.com/problems/<slug>/)
 *
 * <The problem statement as published on LeetCode, verbatim or near-verbatim.>
 *
 * Constraints:
 * - <every constraint listed on the problem page, verbatim>
 */
typealias I#### = (<ArgTypes>) -> <ReturnType>

class I####<methodName> {

    @Nested
    inner class Solution : ProblemTest<I####> {

        override val cases = testCases<I####>(
            // from the official examples — see step 5 for arg/expects syntax
        )

        @Test
        fun test() = check(::<methodName>)

        fun <methodName>(<params>): <ReturnType> {
            TODO("implement")
        }

    }
}
```

**The doc comment carries ONLY what the LeetCode page itself states** — title, URL, statement, constraints, and the
follow-up if the page has one. Nothing else:

- No hints, intuition, approach or pattern names, complexity targets, or "note that..." observations.
- No edge cases, pitfalls, or warnings the page does not spell out.
- No restatement that simplifies or interprets — reproduce the statement's wording and keep its definitions.
- No commentary about the harness, the tests, or how to structure the solution.

If you catch yourself explaining *how* to solve it, or pointing at something the statement leaves implicit, delete that
line. That material belongs to `/leetcode-help`.

Note: root-package files reference `ProblemTest`, `testCases`, `args`, `expects` from package `leetcode` directly — no
`import leetcode.*` needed (same package). If you place the file in a category subdir instead, add
`import leetcode.ProblemTest`, `import leetcode.testCases`, `import leetcode.args`, `import leetcode.expects` and set
the matching subpackage.

### 4. Map types to the harness

Only these types are auto-converted from LeetCode-style strings (see `utils/TypeConverters.kt`). Pick the typealias
signature from this set:

- Scalars: `Int`, `Long`, `Boolean`, `Double`, `String` (write literals like `7`, `true`; strings as `"abc"`).
- Arrays: `IntArray`, `DoubleArray`, `Array<String>` (write `"[1,2,3]"`, `"[\"a\",\"b\"]"`).
- Lists: `List<Int>`, `List<String>`, `List<List<Int>>`, `List<List<String>>`.
- 2D arrays: `Array<IntArray>`, `Array<CharArray>` (write `"[[1,2],[3,4]]"`).
- Data structures: `ListNode`, `TreeNode`, `Node` (write the LeetCode array form, e.g. `"[1,2,3,null,4]"`).

If the problem needs a type **not** in this list, say so and either (a) register a `Handler` in `TypeConverters.init {}`
per CLAUDE.md, or (b) ask the user how they'd like to model it. Don't silently pick a lossy type.

### 5. Fill in test cases from the official examples

- Single-arg shorthand: `<input> expects <output>` → e.g. `"[1,2,4]" expects 3`.
- Multi-arg: `args(<a>, <b>, ...) expects <output>` → e.g. `args("[1,2]", 7) expects "[[2,2,3]]"`.
- Inputs and expected values are LeetCode-style **strings** for any non-scalar type; plain Kotlin literals (`7`, `true`)
  pass through untouched.
- Add **every** worked example from the problem page, and nothing else — no invented edge cases. The examples are the
  user's correctness oracle, so they must match the page exactly.
- **Ordering**: harness equality for `List<List<...>>` etc. is order-sensitive. If the problem statement itself says the
  answer may be returned in any order, use `expectsAnyOrder`; if it says any valid answer is accepted, use
  `expectsAnyOf(...)`. Do not add commentary beyond what the statement says.

### 6. Verify the scaffold compiles (but expect tests to fail)

- The solution is a `TODO()` stub, so tests are *meant* to fail — that's correct. Just confirm the file compiles and the
  harness wiring is valid:
  `mise run build` (or `./kotlin build` — it compiles test sources too)
- If it doesn't compile, fix the signature/imports/case syntax (not the solution logic) until it does.
- Report: the created path, the inferred signature, how many cases you added, and any type-mapping or any-order caveats.
  Hand it back to the user to solve — do not write the algorithm.

## Notes

- Leave `TODO("implement")` as the body (or a trivial signature-satisfying stub) — never a real solution.
- Keep the description faithful to the official statement — statement and constraints only. Any hint, tip, complexity
  target, or edge-case warning is out of scope here; that is `/leetcode-help`'s job.
- Nothing outside the doc comment editorializes either: no `// hint:` comments near the cases, no notes in the stub body
  beyond `TODO("implement")`.
- If WebSearch is unavailable or the problem can't be confirmed, ask the user to paste the statement rather than
  guessing.
