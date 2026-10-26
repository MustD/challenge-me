---
context: fork
name: lt-ref
description: Get unstuck on an open LeetCode task in this repo. Writes teaching material (restatement, pattern, intuition, core concepts, approach, complexity, pitfalls) and a verified referenceSolution into the task file, wired into check(...) next to the user's own functions. Never comments on the user's code. Invoke as /lt-ref [N] (e.g. /lt-ref 123); with no N it uses the only open task in test/leetcode/.
argument-hint: "[N]"
---

# lt-ref

The user is stuck on an **open task** (or is preparing one for an interview) and wants a worked reference. This is a
learning repo: lead with teaching — the reference solution comes *with* its explanation, never bare.

Part of the `lt-*` set: `/lt-init` → `/lt-stat start` → solve → [`/lt-ref`] → `/lt-check` → `/lt-stat stop` →
`/lt-close`.

## Rules

- **Root files only.** Targets are open tasks: `I####*.kt` directly in `test/leetcode/`. Never read, edit or mention
  closed copies under `test/leetcode/<category>/`.
- **No word about the user's code** — not in the file, not in chat. No assessment, no hints about their attempt, no
  "your approach", no fix suggestions. The only things you may say about a user function are mechanical facts the
  run produced: that it is commented out in `check(...)` because it doesn't compile, or that it failed cases `[j…]`.
  Failures of user functions are **reported, never fixed**.
- **Touch only two things**: the `check(...)` call and the added reference function (with its KDoc). Everything else —
  cases, user functions, their KDocs, and any `// lt-stat {` … `// }` stat-comment — stays **byte-identical**.
- **No git writes.** Never stage or commit.
- Don't state the LeetCode difficulty or which category directory the task belongs to. (Naming the technique is the
  point of this skill and is fine.)

## Steps

### 1. Locate the task file

- **`N` given** → zero-pad to 4 digits and look for `test/leetcode/I####*.kt` (`find test/leetcode -maxdepth 1 -name
  'I####*.kt'`). If none, **stop with an error**: there is no open task for N (suggest `/lt-init N`). Do not fall back
  to a closed copy.
- **No `N`** → list root `I[0-9][0-9][0-9][0-9]*.kt` files. Exactly one → that is the target. Zero or several → **stop
  with an error** and list the candidates (or say there are none); ask the user to pass N.
- Before editing, save a backup in the scratchpad directory (`cp <file> <scratchpad>/<name>.orig`) — step 6 diffs
  against it.

### 2. Identify the problem accurately

- Confirm the real problem name, statement, constraints and examples for N. If unsure, use WebSearch/WebFetch — do
  **not** guess from the number alone. Cross-check against the `typealias` and `cases` in the file.
- **User functions** = every solution function in `Solution` except `referenceSolution*`. Note which exist and which
  `check(...)` currently wires in. Read them only to the extent needed for wiring — they are not analyzed.

### 3. Write the teaching KDoc

Capture this as a KDoc block directly above the reference function, headed `## Reference (lt-ref) — <technique>`,
in this order:

- **Restatement** of the problem in plain terms.
- **Pattern / technique** that applies (e.g. Union-Find, BFS, sliding window) and **why** it fits.
- `### Intuition` — the key insight that makes the solution obvious once seen.
- `### Core concepts` — 2–5 bullets: the named ideas the solution stands on (e.g. *in-degree*, *monotonic stack
  invariant*), each with a one-line definition and why it matters *here*. Keep them general and transferable.
- `### Approach` — step by step.
- `### Complexity` — time and space, each with a one-line justification. Write it as `O(…) time / O(…) space` on
  one line first, so `/lt-stat stop` can read the `optimal` field from it.
- `### Common pitfalls` — for this problem.

**The block is autonomous.** Write it as if the file contained no other code — it must still make sense after the user
rewrites or deletes their own functions.

### 4. Add the reference solution

- Add it as a **new function** in the `Solution` class, after the existing functions. Name it `referenceSolution`; if
  that exists, `referenceSolution2`, then `referenceSolution3`, and so on.
- Match the `typealias` signature exactly and the file's Kotlin style. Add any imports it needs (imports are part of the
  reference addition).

### 5. Rewrite `check(...)` in multi-line form

Wire every user function that compiles, then every `referenceSolution*`, one per line with a trailing comma:

```kotlin
@Test
fun test() = check(
    ::minLights,
    // ::minLightsWip,   // doesn't compile
    ::referenceSolution,
)
```

- Keep user functions that were wired before; add user functions that weren't, if they compile.
- **Comment out only user functions that don't compile when wired** (e.g. their signature no longer matches the
  `typealias`), with `// doesn't compile`. Determine this from `mise run build` errors, not by eye.
- If a user function's **body** doesn't compile, commenting it out of `check(...)` can't make the file build. Then, to
  verify the reference only: temporarily replace that body with `TODO()`, run step 6, and **restore it from the backup
  byte-identical** (`cmp` to prove it). Leave it commented out in `check(...)` and tell the user the file won't build
  until that function compiles.
- A `TODO()` stub compiles — keep it wired; it simply fails its cases.

### 6. Verify

- Run `mise run test-one leetcode.<OuterClassName>` (outer class = file name without `.kt`; the mise task appends the
  load-bearing trailing `*`).
- Failures are reported as `solution[i] case[j]`, with `i` = position in `check(...)` (1-based, commented lines don't
  count). The new reference must have **no** failures. If it fails, fix the reference (never the harness or cases) and
  re-run until it passes everything.
- Diff the file against the step-1 backup: the only changes may be `check(...)`, the new reference + its KDoc, and
  imports the reference needs. Any other change (including the stat-comment) is a bug — revert it.

### 7. Report

- In chat: a concise version of the insight that unblocks (pattern + intuition), the reference's complexity, and that
  the full write-up is in the KDoc above `referenceSolution…`.
- The test result: the reference passes N/N; for user functions only the mechanical facts (`::fn` failed cases …,
  `::fnWip` commented out — doesn't compile). No commentary on why.
