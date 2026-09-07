---
context: fork
name: leetcode-help
description: Get unstuck on a LeetCode problem in this repo. Provides educational materials (intuition, core concepts, pattern, complexity, pitfalls), an assessment of the user's own attempt, and a verified reference solution, written directly into the problem's task file as comments + an extra solution function. Invoke as /leetcode-help problem-number (e.g. /leetcode-help 123) when stuck or spending too long.
---

# leetcode-help

The user is stuck on a LeetCode problem (or has spent too long) and wants help. The argument is *normally* the
**LeetCode problem number** (e.g. `123`); if it is omitted, infer it from the repo (see step 1). This is a learning
repo — see the `explain-before-solving` memory: lead with teaching, the reference solution comes *with* its explanation,
never bare.

## Steps

### 1. Resolve the problem number, then locate the task file

**If the user gave a number, use it.** If **no number** was provided, infer it from the repo — do not ask first, try
these in order:

1. **Most recently edited problem file** — the `I####*.kt` under `test/` with the newest mtime:
   ```bash
   find test -name 'I[0-9][0-9][0-9][0-9]*.kt' -printf '%T@ %p\n' | sort -rn | head -1
   ```
2. **Git working changes** — a modified or not-yet-committed (untracked) `I####*.kt`:
   ```bash
   git status --porcelain -- 'test/*I[0-9][0-9][0-9][0-9]*.kt'
   ```

Extract the 4-digit number from the resolved file name (`I0918…` → `918`). **Stop with an error** if the result is not
clear — no matching file, or the candidates point at more than one distinct problem number. Tell the user no number was
given and none could be unambiguously determined, and ask them to pass it explicitly; do not guess past an ambiguous
result.

Then locate the file:

- Zero-pad the number to 4 digits → e.g. `123` becomes `I0123`.
- Search `test/` for a file matching `I0123*.kt`.
    - **Found** → that is the file the user is working in. This is the target.
    - **Not found** → tell the user the problem isn't scaffolded yet. Offer to create it from
      `test/leetcode/_Template.kt` in the appropriate category directory (ask which category, or infer from
      the problem's topic). Do not proceed to write a solution into a file that doesn't exist without confirming.

### 2. Identify the problem accurately

- Determine the real problem name, statement, constraints, and examples for that LeetCode number. If unsure of the exact
  problem or its details, use WebSearch/WebFetch to confirm — do **not** guess the problem from the number alone.
- Cross-check against the `typealias` and test `cases` already in the task file (the function signature and examples
  confirm you have the right problem).

### 3. Teach first (educational materials)

Present this in your chat reply AND capture it as a KDoc block above the reference solution in the file, in this
order:

- **Restatement** of the problem in plain terms.
- **Pattern / technique** that applies (e.g. Union-Find, BFS/DFS, sliding window) and **why** it fits — the intuition,
  not just the name.
- `### Intuition` — the key insight that makes the solution obvious once seen.
- `### Core concepts` — a short glossary block, directly **after** `### Intuition`. List the 2-5 named ideas the
  solution stands on (e.g. *in-degree*, *back edge*, *DAG / topological order*, *monotonic stack invariant*), one
  bullet each: the name, a one-line definition, and why it matters *here*. These are the transferable pieces the
  user should be able to recognise in the next problem — keep them general, not phrased around this input.
- **Approach** step by step.
- **Complexity** — time and space, with a one-line justification.
- **Common pitfalls** for this problem.

**The reference block must be autonomous.** Write it as if the file contained no other code: never say "the attempt
above", "your `visited` set", "the fix to the code below", or otherwise make the explanation only readable next to
the user's solution. It has to still make sense after the user rewrites or deletes their own attempt. All commentary
on the user's code belongs in step 4, not here.

### 4. Analyze the user's existing attempt

Read whatever the user already wrote in the task file.

- **Skip this step entirely** if the attempt is empty, a bare stub (`return false`, `TODO()`), or otherwise
  meaningless — say nothing about it, in the file or in chat.
- Otherwise, work out **what approach they were going for** (name the technique they actually reached for, even if
  it's half-built), then **estimate it against the optimal solution**: their time/space complexity vs. the
  reference's, and whether the approach can reach optimal at all or tops out somewhere worse.
- **If the attempt is close — a small, local defect** (an off-by-one, a missing reset, one mark instead of two, a
  wrong comparison, a missed edge case) — say concretely how to fix it: which line/condition, and what it should
  become. Do this **even when their approach is not the optimal one**; a suboptimal but correct solution is still
  worth finishing. Do not rewrite their function for them — describe the fix, leave the code to them.
- Write this as a short comment block directly above **their** function (not above the reference), and mirror it
  briefly in the chat reply.

### 5. Add the reference solution to the task file

Follow the repo's harness conventions (see CLAUDE.md):

- Add the reference as a **new solution function** in the `Solution` class — do **not** overwrite or delete the user's
  own attempt. Name it `referenceSolution` (or `referenceSolutionN` if one already exists).
- Wire it into the existing `check(...)` call so it is validated by the same test cases (e.g.
  `check(::userAttempt, ::referenceSolution)`). If the user's attempt is incomplete/non-compiling, wire
  `check(::referenceSolution)` and leave their function intact but out of `check`, noting this.
- Put the educational explanation from step 3 as a KDoc block directly above `referenceSolution`, and keep the
  attempt analysis from step 4 above the user's own function.
- Match the surrounding code's Kotlin style and the existing `typealias` signature exactly.

### 6. Verify

- Run only this problem's test:
  `mise run test-one "<package>.<OuterClassName>"` (outer class name = file name without `.kt`).
  Invoking the toolchain directly requires a trailing `*` — `./kotlin test --include-classes
  "<package>.<OuterClassName>*"` — because the `@Test` methods live in the `@Nested inner class Solution`,
  whose filter identity is `<package>.<OuterClassName>/Solution`. Without the wildcard it matches only the
  outer class and runs 0 tests while still reporting success.
- Confirm the reference solution passes. If a test case fails, fix the reference (not the harness) and re-run until
  green. Report the result.

## Notes

- The goal is for the user to *understand and check against* the reference, not to have their work erased — always
  preserve their attempt.
- Keep the chat explanation concise and focused on the insight that unblocks them.
- Keep the two comment blocks separate in the file: the reference KDoc explains the problem and stands on its own;
  the block above the user's function is the only place that talks about their code.
