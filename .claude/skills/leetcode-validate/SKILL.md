---
context: fork
name: leetcode-validate
description: Validate and analyze a solution the user has already written for a LeetCode problem in this repo. Adds the edge/boundary test cases needed to really check it, runs the problem's tests, then analyzes the user's actual implementation — time/space complexity, correctness notes, alternative approaches, whether a multithreaded/parallel approach applies, and real-world experience — and writes the analysis as a KDoc comment above their solution function. Invoke as /leetcode-validate problem-number (e.g. /leetcode-validate 3333).
---

# leetcode-validate

The user has **already written a solution** and wants it validated and analyzed — not solved for them. The argument is
*normally* the **LeetCode problem number** (e.g. `3333`); if it is omitted, infer it from the repo (see step 1). This is
a learning repo (see the `explain-before-solving` memory): the value here is the *post-mortem* on their own code —
confirming it's correct, then deepening their understanding of why it works, what it costs, and what else was possible.

This is the third skill in the trio: `/leetcode-start` scaffolds, `/leetcode-help` unblocks, `/leetcode-validate`
reviews a finished attempt. Do **not** rewrite the user's solution — analyze the code they wrote.

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

- Zero-pad the number to 4 digits → e.g. `3333` becomes `I3333`.
- Search `test/` for a file matching `I3333*.kt`.
    - **Found** → that is the target.
    - **Not found** → tell the user the problem isn't scaffolded yet; suggest `/leetcode-start <N>` to create it first.
      Don't fabricate a file.
- Read the file. Identify the user's solution function(s) wired into `check(...)` and the `typealias` signature.

### 2. Confirm the problem & verify the solution passes

- Confirm the real problem (title, statement, constraints, examples) for that LeetCode number. If unsure, use
  WebSearch/WebFetch — don't guess from the number. Cross-check against the `typealias` and `cases` already in the file.
- **Add the cases needed to actually check the solution — before running the tests, and regardless of whether you
  already suspect the solution is wrong.** The scaffolded `cases` usually hold only the official examples, which
  rarely exercise the tricky paths; a pass on them alone proves little. Audit the existing cases against the
  constraints and the user's code, then append what's missing to `cases`:
  - **Boundaries from the constraints** — minimum size (empty / single element / `n = 1`), maximum-value elements,
    negative numbers and zero if allowed, all-equal elements, strictly increasing / decreasing input.
  - **Problem-specific traps** — duplicates, "no valid answer" results (`-1`, `""`, `[]`), the answer at the very
    first/last position, integer overflow (sums/products near `Int.MAX_VALUE` → does the result need `Long`?),
    ties, off-by-one around window/partition edges, cycles or disconnected components for graphs, skewed trees.
  - **Cases aimed at this implementation** — read their code and add inputs that hit its branches, early returns,
    and loop edges; if you suspect a bug, add the input that would expose it.
  - **At most one larger input** when the complexity matters and it stays cheap to write (e.g. a generated literal of
    a few hundred elements); skip it if it would bloat the file — the harness has no timeout, so it only proves
    correctness, not speed.

  Keep every added case **cheap to run**. These cases stay in the file and run with the whole suite on every
  `mise run test`, against every solution in `check(...)` — including slow ones like the user's own. A case that
  takes seconds against an exponential/factorial solution taxes every future run. Size inputs so that even the
  user's actual implementation finishes in well under a second; if proving behaviour at the constraint ceiling
  would be slow (e.g. `n = 10` for a factorial-time subsets solution), stop at a smaller size and state the cost
  in the analysis instead — quantify it there (call counts, growth) rather than making the test suite pay for it.

  Rules for added cases:
  - **Derive every expected value from the problem statement, never from running the user's code** — otherwise a
    wrong solution validates itself. Work it out by hand, or for non-trivial inputs compute it with a throwaway
    brute force in the scratchpad directory (never added to the problem file or `check(...)`).
  - Use `expectsAnyOrder` when the problem says "return in any order" and `expectsAnyOf(...)` when several distinct
    answers are valid, so a correct solution isn't failed on ordering or choice.
  - Write every expected value as a **literal** in the case (a `"""…"""` multiline string is fine for big ones).
    Don't add helper functions, reference generators, or computed expectations to the problem file — the brute
    force belongs in the scratchpad, and its output is pasted in. A generator in the file is a second
    implementation that can share the user's bug, and it clutters a file that should hold only their solution.
  - Keep inputs within the stated constraints. Don't remove the existing cases or change their inputs/expected
    values; append after them, with a short trailing comment on each added case saying what it probes (e.g.
    `// single element`, `// overflow`). The one permitted edit to an existing case is the `expects` →
    `expectsAnyOrder` switch described below.
  - Keep it focused — typically 3–8 added cases that each probe something distinct, not a pile of redundant ones.
- Run **only this problem's test** to establish correctness before analyzing:
  `mise run test-one "<package>.<OuterClassName>"` (outer class name = file name without `.kt`).
  Invoking the toolchain directly requires a trailing `*` — `./kotlin test --include-classes
  "<package>.<OuterClassName>*"` — because the `@Test` methods live in the `@Nested inner class Solution`,
  whose filter identity is `<package>.<OuterClassName>/Solution`. Without the wildcard it matches only the
  outer class and runs 0 tests while still reporting success, which would look like a pass.
    - **Pass** → proceed to analysis; the analysis describes a verified-correct solution.
  - **Fail** → report the failing case (s) and the harness output, and say whether each is an original case or one you
    added (and what it probes). First double-check the expected value of any failing added case — a wrong expectation
    is your bug, not theirs; fix it and re-run. Keep the added cases in the file even when they fail — they are the
    evidence. Do **not** fix their code silently. Point out where the logic likely diverges (as a teaching hint, per
    the repo's educational rule), and offer `/leetcode-help` if they want a worked reference. Only continue to full
    analysis once they decide — analyzing broken code as if correct is misleading.
- **Any-order problems: switch *all* cases, not just the failing ones.** Harness equality is order-sensitive under
  `expects`. When the statement says the answer may be returned "in any order", order is not part of the contract,
  so every case — original and added — should use `expectsAnyOrder`, before the first run. Do this even for cases
  that currently pass (e.g. a single-element `"[0]" expects "[[],[0]]"`): they pass only because this implementation
  happens to emit that order, and a later correct rewrite would fail them. Switching only the case that failed leaves
  the file inconsistent. Never ask the user to sort their output to satisfy the harness. Mention the switch in the
  report so it's clear the original failure was the test's strictness, not their code.

### 3. Analyze the user's actual implementation

Read **their** code carefully and analyze what they actually wrote (not a textbook version). Produce:

- **Time complexity** — Big-O with a one-line justification tied to *their* loops/recursion/data-structure operations.
  Note best/average/worst if they differ.
- **Space complexity** — auxiliary space, with recursion stack depth counted explicitly if recursive. Distinguish output
  space from working space.
- **Correctness notes** — why the approach is sound; any edge cases it handles well or relies on (empty input,
  duplicates, overflow, ordering).
- **Pattern named** — the general technique used, so it transfers.
- **Alternative approaches** — at least one other viable approach and how it trades off against theirs (e.g. "sorting
  first → O(n log n) time but O(1) extra space vs. their hash-set O(n)/O(n)"). If their solution is already optimal, say
  so and explain why no asymptotically better approach exists.
- **Parallelism / multithreading** — does the problem admit a multithreaded or parallel/SIMD approach? Be honest: most
  LeetCode problems are too small or too sequential (data dependencies) to benefit, and that itself is the teaching
  point. When it *does* apply (embarrassingly parallel map/reduce, independent subproblems, divide-and-conquer like
  parallel merge sort, matrix work), sketch how you'd partition the work and the realistic speedup ceiling (Amdahl's
  law, overhead vs. input size).
- **Real-world experience** — where this pattern or problem shows up in production systems, and how the constraints
  differ from the interview version (e.g. streaming/unbounded input, distributed data, cache effects, library functions
  you'd actually reach for, why the "optimal" answer sometimes loses to a simpler one at real scale).

### 4. Write the analysis into the file

- Add the analysis as a **KDoc comment block (`/** ... */`) directly above the user's solution function** — the one they
  wrote. If multiple solution functions exist, annotate each, or focus on the one(s) in `check(...)` and say which.
- Do **not** modify, rename, or reorder their code. Comment only. Match the file's existing comment style and
  indentation.
- Keep it readable in-editor: structured with clear headings/bullets, concise. The chat reply can be a brief summary;
  the file comment is the durable artifact.
- If you also explained alternatives in prose, you may optionally add a single commented-out stub or note pointing at
  the alternative — but only as a comment, never a competing `check(...)` solution (that's `/leetcode-help`'s job, not
  this skill's).

### 5. Report

- List the cases you added and what each probes.
- Confirm the test result (pass/fail and which cases).
- Summarize the headline complexity (time/space) and the single most useful insight from the analysis in chat.
- Note that the full write-up now lives in the function's KDoc.

## Notes

- **Never replace the user's solution.** This skill reviews; it does not author. If their code is wrong, teach toward
  the fix, don't hand it over. Besides the KDoc, the only permitted changes are appending test cases and switching
  cases to `expectsAnyOrder` for any-order problems.
- Tie every complexity claim to a specific line/construct in *their* code — generic Big-O without justification isn't
  the point.
- Be candid about parallelism: "not worth it here, and here's why" is a more useful answer than forcing a contrived
  threaded version.
