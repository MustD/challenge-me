---
context: fork
name: lt-check
description: Validate and analyze a solution the user has written for an open LeetCode task in this repo. Re-wires commented-out user functions, adds the edge/boundary cases needed to really check it, runs the tests, then analyzes the user's actual implementation — complexity, correctness, alternatives, parallelism, real-world use — as an (lt-check) KDoc directly above their function. Invoke as /lt-check [N] (e.g. /lt-check 3333); with no N it uses the only open task in test/leetcode/.
argument-hint: "[N]"
---

# lt-check

The user has **already written a solution** to an **open task** and wants it validated and analyzed — not solved for
them. The value is the *post-mortem* on their own code: confirm it's correct, then deepen their understanding of why it
works, what it costs, and what else was possible. Do **not** rewrite the user's solution.

Part of the `lt-*` set: `/lt-init` → `/lt-stat start` → solve → [`/lt-ref`] → `/lt-check` → `/lt-stat stop` →
`/lt-close`.

## Rules

- **Root files only.** Targets are open tasks: `I####*.kt` directly in `test/leetcode/`. Never read, edit or mention
  closed copies under `test/leetcode/<category>/`.
- **User function** = any solution function in `Solution` except `referenceSolution*`.
- **Permitted edits**: uncommenting user functions in `check(...)`, appending cases, the `expects` → `expectsAnyOrder`
  switch (step 3), and the analysis KDoc. Nothing else — never modify, rename or reorder user code.
- **Stat-comment is sacred.** A `// lt-stat {` … `// }` block above a function stays **byte-identical** and stays
  *above* the analysis KDoc.
- **No git writes.** Never stage or commit.
- Don't state the LeetCode difficulty or which category directory the task belongs to.

## Steps

### 1. Locate the task file

- **`N` given** → zero-pad to 4 digits and look for `test/leetcode/I####*.kt` (`find test/leetcode -maxdepth 1 -name
  'I####*.kt'`). If none, **stop with an error**: there is no open task for N (suggest `/lt-init N`). Do not fall back
  to a closed copy.
- **No `N`** → list root `I[0-9][0-9][0-9][0-9]*.kt` files. Exactly one → that is the target. Zero or several → **stop
  with an error** and list the candidates (or say there are none); ask the user to pass N.
- Save a backup in the scratchpad directory (`cp <file> <scratchpad>/<name>.orig`).

### 2. Re-wire commented-out user functions

- If `check(...)` contains commented-out user functions (e.g. `// ::minLightsWip,   // doesn't compile`, as `/lt-ref`
  leaves them), uncomment them (drop the `// doesn't compile` note too) and run `mise run build`.
- If any of them **still doesn't compile**, restore the file from the backup (`cp` + `cmp` to prove it), and **stop
  with an error** naming the function (s) and the compiler message. Change nothing.

### 3. Confirm the problem and add the cases needed to really check it

- Confirm the real problem (title, statement, constraints, examples) for N. If unsure, use WebSearch/WebFetch — don't
  guess from the number. Cross-check against the `typealias` and `cases` already in the file.
- **The target function (s)**: if a stat-comment exists, the function directly below it; otherwise the user functions
  wired into `check(...)` (analyze each, or say which one you focused on).
- **Add cases before running the tests, regardless of whether you suspect a bug.** The official examples rarely
  exercise the tricky paths. Audit the existing cases against the constraints and the user's code, then append:
  - **Boundaries from the constraints** — minimum size (empty / single element / `n = 1`), maximum-value elements,
    negatives and zero if allowed, all-equal elements, strictly increasing / decreasing input.
  - **Problem-specific traps** — duplicates, "no valid answer" results (`-1`, `""`, `[]`), the answer at the very
    first/last position, integer overflow (does the result need `Long`?), ties, off-by-one around window/partition
    edges, cycles or disconnected components for graphs, skewed trees.
  - **Cases aimed at this implementation** — inputs that hit its branches, early returns and loop edges; if you suspect
    a bug, add the input that would expose it.
  - **At most one larger input** when complexity matters and it stays cheap to write; skip it if it bloats the file.
- **Keep every case cheap.** Cases run with the whole suite on every `mise run test`, against every function in
  `check(...)`. Size inputs so even the user's implementation finishes well under a second; quantify costlier limits in
  the analysis instead.
- Rules for added cases:
    - **Derive every expected value from the problem statement, never from running the user's code.** Work it out by
      hand, or compute it with a throwaway brute force in the scratchpad directory (never in the problem file).
    - Every expected value is a **literal** (a `"""…"""` multiline string is fine). No helper functions or generators in
      the file.
    - Each added case gets a short **trailing probe comment** saying what it probes (e.g. `// single element`,
      `// overflow`).
    - Stay within the constraints. Don't remove existing cases or change their inputs/expected values — append after
      them.
    - Use `expectsAnyOrder` when the statement says "return in any order" — and then switch **all** cases, original and
      added, before the first run (a passing `expects` case only passes because this implementation happens to emit that
      order). Use `expectsAnyOf(...)` when several distinct answers are valid. Never ask the user to sort their output.
    - Typically 3–8 added cases, each probing something distinct.

### 4. Run the tests

- `mise run test-one leetcode.<OuterClassName>` (outer class = file name without `.kt`; the mise task appends the
  load-bearing trailing `*`). Failures are reported as `solution[i] case[j]`, `i` = position in `check(...)`.
- **Pass** → continue to the analysis.
- **Fail** → first double-check the expected value of any failing added case — a wrong expectation is your bug; fix it
  and re-run. If the user's code really fails: keep the added cases (they are the evidence), report each failing case,
  whether it is original or added and what it probes, and give a teaching hint about where the logic likely diverges.
  Do **not** fix their code, and **write no analysis KDoc** — analyzing broken code as if correct is misleading. Stop
  here; the user re-runs `/lt-check` after fixing (or asks for `/lt-ref`).

### 5. Analyze the user's actual implementation

Analyze what they actually wrote, not a textbook version:

- **Time complexity** — Big-O tied to *their* loops/recursion/data-structure operations; best/average/worst if they
  differ.
- **Space complexity** — auxiliary space, recursion depth counted explicitly; output vs. working space.
- **Optimal** — one line `Optimal: O(…) time / O(…) space`, the best known bound for the problem, and whether theirs
  reaches it. (`/lt-stat stop` reads this line when no reference exists.)
- **Correctness notes** — why the approach is sound; edge cases it handles or relies on.
- **Pattern named** — the general technique used, so it transfers.
- **Alternative approaches** — at least one, with the trade-off against theirs. If theirs is optimal, say why nothing
  asymptotically better exists.
- **Parallelism / multithreading** — be honest: usually too small or too sequential, and that's the teaching point.
  When it applies, sketch the partitioning and the realistic speedup ceiling (Amdahl, overhead vs. input size).
- **Real-world experience** — where this pattern shows up in production and how the constraints differ.

### 6. Write the analysis KDoc

- A KDoc block (`/** … */`) headed `## Analysis (lt-check) — verified: <k> official + <m> added cases`, **directly
  above `fun`** of each analyzed function — nothing between the KDoc and `fun`.
- If a stat-comment sits above that function, the order is: stat-comment, then the KDoc, then `fun`. The stat-comment
  stays byte-identical.
- If the function already has an `(lt-check)` KDoc (an earlier run), replace it rather than stacking a second one.
- Match the file's comment style and indentation. Optionally mention an alternative as a commented note — never as a
  competing function in `check(...)` (that's `/lt-ref`'s job).
- Diff against the backup: only the permitted edits from the Rules may appear.

### 7. Report

- The cases you added and what each probes, and any `expectsAnyOrder` switch.
- The test result (pass/fail, which cases).
- Headline complexity and the single most useful insight; note that the full write-up lives in the function's KDoc.
