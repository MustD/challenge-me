---
context: fork
model: sonnet
name: lt-init
description: Scaffold a new, ready-to-solve LeetCode task in this repo. Looks up the problem statement, infers the function signature, writes test cases from the official examples only, and leaves a TODO() stub for the user. Invoke as /lt-init N (e.g. /lt-init 3333). Creates an open task in the root test/leetcode/ dir; /lt-close files it into a category later.
argument-hint: N
---

# lt-init

The user wants a fresh **open task** scaffolded so they can solve it themselves. The argument `N` is the **LeetCode
problem number** (e.g. `3333`) and is **required** — if it is missing, stop with an error asking for it.

**This is a learning repo — DO NOT solve the problem.** The output is a *ready-to-solve template*: correct signature,
real statement, the official examples as cases, and a `TODO()` stub. The user writes the algorithm. (When they get
stuck, `/lt-ref` supplies a worked reference.)

Part of the `lt-*` set: `/lt-init` → `/lt-stat start` → solve → [`/lt-ref`] → `/lt-check` → `/lt-stat stop` →
`/lt-close`.

## Rules

- **No spoilers.** Nothing in the file *or in chat* goes beyond the LeetCode statement: no hints, no difficulty, no
  tags, no category, no pattern names. This holds for the whole command, including error messages.
- **No git writes.** Never stage or commit.
- **Never touch closed copies** (files under `test/leetcode/<category>/`).
- **No stat-comment.** Stats are opt-in via `/lt-stat start`; never write a `// lt-stat {` block.

## Steps

### 1. Check for existing copies

- Zero-pad `N` to 4 digits → `3333` becomes `I3333`.
- **Open task** — a root file `test/leetcode/I3333*.kt`
  (`find test/leetcode -maxdepth 1 -name 'I3333*.kt'`). If one exists, **stop with an error** naming it and change
  nothing. Restarting means the user deletes the file by hand.
- **Closed copy** — `find test/leetcode -mindepth 2 -name 'I3333*.kt'`. If one exists, tell the user only **"a closed
  copy exists"** — never its path or category (that would leak the category). Then continue and create the
  root file anyway; leave the closed copy untouched (`/lt-close` replaces it later).

### 2. Identify the problem accurately (WebSearch — do NOT guess)

- Use WebSearch/WebFetch to find LeetCode problem #N: its exact **title**, **slug**, **statement**, **constraints**,
  **function signature**, follow-up (if any), and the **worked examples**. The number alone is not enough.
- From the official signature, work out:
  - the **camelCase method name** (e.g. `mergeAlternately`) — used for both the file name and the stub function.
  - the **argument types** and **return type**, mapped to the harness's supported types (step 4).
- Ignore the difficulty, tags and hints you see on the page — they must not reach the file or the chat.

### 3. Create the file

- Path: `test/leetcode/I####<methodName>.kt` (root dir), `package leetcode`.
- File/class name: `I####<methodName>` (e.g. `I3333countOfSubstrings`).

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
  // the official examples — see step 5 for arg/expects syntax
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
follow-up if the page has one. No hints, intuition, approach or pattern names, complexity targets, edge cases or
pitfalls the page does not spell out, no interpretive restatement, no commentary about the harness. If you catch
yourself explaining *how* to solve it, delete that line.

Root-package files reference `ProblemTest`, `testCases`, `args`, `expects` from package `leetcode` directly — no
imports needed. (`/lt-close` adds them when it moves the file into a category.)

### 4. Map types to the harness

Only these types are auto-converted from LeetCode-style strings (see `test/leetcode/utils/TypeConverters.kt`):

- Scalars: `Int`, `Long`, `Boolean`, `Double`, `String` (literals like `7`, `true`; strings as `"abc"`).
- Arrays: `IntArray`, `DoubleArray`, `Array<String>` (`"[1,2,3]"`, `"[\"a\",\"b\"]"`).
- Lists: `List<Int>`, `List<String>`, `List<List<Int>>`, `List<List<String>>`.
- 2D arrays: `Array<IntArray>`, `Array<CharArray>` (`"[[1,2],[3,4]]"`).
- Data structures: `ListNode`, `TreeNode`, `Node` (LeetCode array form, e.g. `"[1,2,3,null,4]"`).

If the problem needs a type **not** in this list, check `TypeConverters` for newer registrations; if still missing,
say so and either register a `Handler` per `test/leetcode/utils/CLAUDE.md`, or ask the user how to model it. Don't
silently pick a lossy type.

### 5. Fill in the official examples — and nothing else

- Single-arg shorthand: `"[1,2,4]" expects 3`. Multi-arg: `args("[1,2]", 7) expects "[[2,2,3]]"`.
- Add **every** worked example from the page, and nothing else — no invented edge cases, no `// hint` comments.
- If the statement says the answer may be returned in any order, use `expectsAnyOrder`; if it says any valid answer is
  accepted, use `expectsAnyOf(...)`.

### 6. Verify it compiles (tests are expected to fail)

- Run `mise run build`. The `TODO()` stub makes tests fail by design — only compilation matters.
- If it doesn't compile, fix the signature/imports/case syntax (never by adding solution logic) until it does.
- Report: the created path, the inferred signature, how many cases you added, any type-mapping or any-order caveats,
  and "a closed copy exists" if step 1 found one. Hand it back to the user to solve.
