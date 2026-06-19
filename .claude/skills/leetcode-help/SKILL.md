---
context: fork
name: leetcode-help
description: Get unstuck on a LeetCode problem in this repo. Provides educational materials (intuition, pattern, complexity, pitfalls) and a verified reference solution, written directly into the problem's task file as comments + an extra solution function. Invoke as /leetcode-help problem-number (e.g. /leetcode-help 323) when stuck or spending too long.
---

# leetcode-help

The user is stuck on a LeetCode problem (or has spent too long) and wants help. The argument is the **LeetCode problem
number** (e.g. `323`). This is a learning repo — see the `explain-before-solving` memory: lead with teaching, the
reference solution comes *with* its explanation, never bare.

## Steps

### 1. Locate the task file

- Zero-pad the number to 4 digits → e.g. `323` becomes `I0323`.
- Search `src/test/kotlin/` for a file matching `I0323*.kt`.
    - **Found** → that is the file the user is working in. This is the target.
    - **Not found** → tell the user the problem isn't scaffolded yet. Offer to create it from
      `src/test/kotlin/leetcode/_Template.kt` in the appropriate category directory (ask which category, or infer from
      the problem's topic). Do not proceed to write a solution into a file that doesn't exist without confirming.

### 2. Identify the problem accurately

- Determine the real problem name, statement, constraints, and examples for that LeetCode number. If unsure of the exact
  problem or its details, use WebSearch/WebFetch to confirm — do **not** guess the problem from the number alone.
- Cross-check against the `typealias` and test `cases` already in the task file (the function signature and examples
  confirm you have the right problem).

### 3. Teach first (educational materials)

Present this in your chat reply AND capture it as a KDoc/comment block above the reference solution in the file:

- **Restatement** of the problem in plain terms.
- **Pattern / technique** that applies (e.g. Union-Find, BFS/DFS, sliding window) and **why** it fits — the intuition,
  not just the name.
- **Approach** step by step.
- **Complexity** — time and space, with a one-line justification.
- **Common pitfalls** for this problem.
- If the user already has an attempt in the file, briefly note where their approach diverges or what they're likely
  missing — without rewriting their code.

### 4. Add the reference solution to the task file

Follow the repo's harness conventions (see CLAUDE.md):

- Add the reference as a **new solution function** in the `Solution` class — do **not** overwrite or delete the user's
  own attempt. Name it `referenceSolution` (or `referenceSolutionN` if one already exists).
- Wire it into the existing `check(...)` call so it is validated by the same test cases (e.g.
  `check(::userAttempt, ::referenceSolution)`). If the user's attempt is incomplete/non-compiling, wire
  `check(::referenceSolution)` and leave their function intact but out of `check`, noting this.
- Put the educational explanation from step 3 as a comment block directly above `referenceSolution`.
- Match the surrounding code's Kotlin style and the existing `typealias` signature exactly.

### 5. Verify

- Run only this problem's test:
  `./gradlew test --tests "<package>.<OuterClassName>"` (outer class name = file name without `.kt`).
- Confirm the reference solution passes. If a test case fails, fix the reference (not the harness) and re-run until
  green. Report the result.

## Notes

- The goal is for the user to *understand and check against* the reference, not to have their work erased — always
  preserve their attempt.
- Keep the chat explanation concise and focused on the insight that unblocks them.
