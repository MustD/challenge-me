---
context: fork
model: sonnet
name: lt-close
description: File a finished open LeetCode task into its category. Refuses while an lt-stat attempt is open, picks the category (existing closed copy, else inferred from LeetCode tags + technique; the argument overrides), deletes any old closed copy, moves the file, sets the package, adds DSL imports and runs its test. Invoke as /lt-close N [category] (e.g. /lt-close 3964 or /lt-close 3964 intervals).
argument-hint: N [category]
---

# lt-close

Turns an **open task** (`test/leetcode/I####*.kt`, `package leetcode`) into a **closed task**
(`test/leetcode/<category>/I####*.kt`, `package leetcode.<category>`). This is the only `lt-*` command that touches
closed copies — and it only deletes them.

Part of the `lt-*` set: `/lt-init` → `/lt-stat start` → solve → [`/lt-ref`] → `/lt-check` → `/lt-stat stop` →
`/lt-close`.

## Rules

- `N` is **required**; `category` is optional. Missing `N` → error with the usage.
- **No git writes.** Never `git add`, `git mv`, `git rm` or commit — use plain `mv` / `rm`. `git diff --cached` must be
  the same before and after.
- **Other root files are untouched.** Only the one open task for N moves.

## Steps

### 1. Locate and gate

- Zero-pad N to 4 digits; the target is `find test/leetcode -maxdepth 1 -name 'I####*.kt'`. None → **stop with an
  error** (nothing open for N). More than one → error listing them.
- If the file contains a `// lt-stat {` line, **stop with the error "run `/lt-stat stop N` first"** and change nothing.
  A task without any stat-comment (stats are opt-in) closes normally.
- Record `git diff --cached --stat` so you can prove step 5 left it unchanged.

### 2. Choose the category

First match wins:

1. The `category` argument — it must be an existing directory under `test/leetcode/` (excluding `utils`); otherwise
   error and list the valid ones. Never create a new category directory.
2. The category of an existing closed copy: `find test/leetcode -mindepth 2 -name 'I####*.kt' -not -path '*/utils/*'`.
   If copies exist in several categories, use the most recently modified one's.
3. Inferred: fetch the LeetCode topic tags (slug from the header URL),

   ```bash
   curl -s https://leetcode.com/graphql -H 'Content-Type: application/json' \
     -d '{"query":"query q($s:String!){question(titleSlug:$s){topicTags{name}}}","variables":{"s":"<slug>"}}'
   ```

   then weigh them together with the technique the user's solution actually uses (the analysis/reference KDocs help),
   and map onto the existing directories (`ls -d test/leetcode/*/`). The technique wins over a generic tag like
   `Array`: a heap-based solution tagged `Array, Heap` goes to `heap`.

Print the choice and why (argument / closed copy / inferred from tags + technique).

### 3. Delete old closed copies

Delete **every** closed `I####*.kt` for N in **any** category (`rm`), and print each deleted path. (They are
superseded by this file; git still holds the tracked version.)

### 4. Move and repackage

- `mv test/leetcode/I####<methodName>.kt test/leetcode/<category>/`.
- Change `package leetcode` → `package leetcode.<category>`.
- Add imports for the root-package DSL names the file uses, now that it left package `leetcode` — any of
  `leetcode.ProblemTest`, `leetcode.testCases`, `leetcode.args`, `leetcode.expects`, `leetcode.expectsAnyOrder`,
  `leetcode.expectsAnyOf` that appear in the file. Keep the import list sorted the way the file already sorts it
  (lexicographic, as in other closed files). Change nothing else in the file.

### 5. Test and report

- `mise run test-one leetcode.<category>.I####<methodName>` and report the result (pass, or the failing
  `solution[i] case[j]` lines). A failure is reported, not fixed — the file stays closed.
- Confirm `git diff --cached --stat` is unchanged from step 1 and that the other root `I####*.kt` files are untouched.
- Report: the chosen category and why, deleted old copies, the new path, and the test result.
