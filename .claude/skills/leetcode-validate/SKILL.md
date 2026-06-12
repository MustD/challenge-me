---
name: leetcode-validate
description: Validate and analyze a solution the user has already written for a LeetCode problem in this repo. Runs the problem's tests, then analyzes the user's actual implementation — time/space complexity, correctness notes, alternative approaches, whether a multithreaded/parallel approach applies, and real-world experience — and writes the analysis as a KDoc comment above their solution function. Invoke as /leetcode-validate <problem-number> (e.g. /leetcode-validate 3333).
---

# leetcode-validate

The user has **already written a solution** and wants it validated and analyzed — not solved for them. The argument is
the **LeetCode problem number** (e.g. `3333`). This is a learning repo (see the `explain-before-solving` memory): the
value here is the *post-mortem* on their own code — confirming it's correct, then deepening their understanding of why
it works, what it costs, and what else was possible.

This is the third skill in the trio: `/leetcode-start` scaffolds, `/leetcode-help` unblocks, `/leetcode-validate`
reviews a finished attempt. Do **not** rewrite the user's solution — analyze the code they wrote.

## Steps

### 1. Locate the task file

- Zero-pad the number to 4 digits → e.g. `3333` becomes `I3333`.
- Search `src/test/kotlin/` for a file matching `I3333*.kt`.
    - **Found** → that is the target.
    - **Not found** → tell the user the problem isn't scaffolded yet; suggest `/leetcode-start <N>` to create it first.
      Don't fabricate a file.
- Read the file. Identify the user's solution function(s) wired into `check(...)` and the `typealias` signature.

### 2. Confirm the problem & verify the solution passes

- Confirm the real problem (title, statement, constraints, examples) for that LeetCode number. If unsure, use
  WebSearch/WebFetch — don't guess from the number. Cross-check against the `typealias` and `cases` already in the file.
- Run **only this problem's test** to establish correctness before analyzing:
  `./gradlew test --tests "<package>.<OuterClassName>"` (outer class name = file name without `.kt`).
    - **Pass** → proceed to analysis; the analysis describes a verified-correct solution.
    - **Fail** → report the failing case(s) and the harness output. Do **not** fix their code silently. Point out where
      the logic likely diverges (as a teaching hint, per the repo's educational rule), and offer `/leetcode-help` if
      they want a worked reference. Only continue to full analysis once they decide — analyzing broken code as if
      correct is misleading.
- If the constraints say "return in any order," note that harness equality is order-sensitive — a passing test means
  their ordering already matches, a failing one may just need a sort.

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

- Confirm the test result (pass/fail and which cases).
- Summarize the headline complexity (time/space) and the single most useful insight from the analysis in chat.
- Note that the full write-up now lives in the function's KDoc.

## Notes

- **Never replace the user's solution.** This skill reviews; it does not author. If their code is wrong, teach toward
  the fix, don't hand it over.
- Tie every complexity claim to a specific line/construct in *their* code — generic Big-O without justification isn't
  the point.
- Be candid about parallelism: "not worth it here, and here's why" is a more useful answer than forcing a contrived
  threaded version.
