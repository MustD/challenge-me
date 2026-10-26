---
model: sonnet
name: lt-stat
description: Time and log one attempt at an open LeetCode task. /lt-stat start N [fn] inserts a stat-comment above the attempt's function; /lt-stat stop N asks for any blank self-reported fields, computes the rest (tests, ref-used, checked, optimal, difficulty, category, attempt), appends one line to .stats/leetcode.jsonl and removes the stat-comment. Stats are opt-in. Invoke as /lt-stat start 3964 or /lt-stat stop 3964.
argument-hint: "start N [fn] | stop N"
---

# lt-stat

Logs **attempts**: one `/lt-stat start N` … `/lt-stat stop N` span = one line in `.stats/leetcode.jsonl`. Stats are
opt-in — a task solved without `lt-stat` leaves no stats, and that is fine. This skill runs inline so it can ask
the user questions.

Part of the `lt-*` set: `/lt-init` → `/lt-stat start` → solve → [`/lt-ref`] → `/lt-check` → `/lt-stat stop` →
`/lt-close`.

## Arguments

`$ARGUMENTS` is `start N [fn]` or `stop N`. `N` is **required** (the LeetCode number). Anything else → stop with an
error showing the usage.

## Rules

- **Root files only.** The target is the open task `test/leetcode/I####*.kt` (N zero-padded to 4,
  `find test/leetcode -maxdepth 1 -name 'I####*.kt'`). None → **stop with an error** (suggest `/lt-init N`). Never touch
  closed copies under `test/leetcode/<category>/`.
- **User function** = any solution function in `Solution` except `referenceSolution*`.
- **No spoilers before `stop` finishes.** During `start` — and in `stop` up to the point the line is written — say
  nothing beyond the LeetCode statement: no difficulty, tags, category, hints or complexity targets. Don't print
  fetched metadata while asking the user questions.
- **No git writes.** Never stage or commit.
- Timestamps are local time, `date '+%Y-%m-%d %H:%M'`.

## Stat-comment format

`//` lines, not a KDoc, so a KDoc can still sit directly above `fun`. Indented like the function:

```kotlin
// lt-stat {
//   "started-at": "2026-10-26 12:00",
//   "analysis": "",              // time to find the idea, e.g. "5m"
//   "solution-complexity": "",   // e.g. "count map, O(n) / O(1)"
//   "implementation": "",        // time to code it, e.g. "5m"
//   "self-result": ""            // done | partial | gave up
// }
/** Analysis (lt-check) … */      // only after /lt-check
fun minLights(lights: IntArray): Int {
```

The block runs from the line `// lt-stat {` to the next line that is just `// }` (ignoring indentation).

## `start N [fn]`

1. Locate the root file. If it contains **any** `// lt-stat {` line, **stop with the error "previous attempt still
   exists"** and change nothing (the user runs `/lt-stat stop N` first).
2. **Choose the function**, first match wins:
    1. `fn` if given — it must be an existing user function in the file, else error ("add it first; a `TODO()` stub is
       enough").
    2. The only user function, if there is exactly one.
    3. The **last** user function (in file order) whose body is still a `TODO(...)` stub.
    4. Otherwise **ask** the user (AskUserQuestion with the user functions as options).
3. If that function has an `(lt-check)` analysis KDoc directly above it, **delete that KDoc** — so `checked` reflects
   only this attempt. Leave every other KDoc, including any other KDoc on this function, in place.
4. **Insert the stat-comment** (exactly the template above, all hand-filled fields `""`, `started-at` = now) directly
   above the function — above any remaining KDoc and annotations on it — at the function's indentation.
5. Nothing else in the file changes. Report only: the function and the start time.

## `stop N`

### 1. Find and parse the block

- Locate the root file and its stat-comment. None → error ("no attempt in progress — run `/lt-stat start N`"). More
  than one → error, show them.
- **The attempt's function** is the one directly below the block (past any KDoc/annotations).
- Save a backup in the scratchpad directory: `cp <file> <scratchpad>/<name>.orig`.
- Parse the block as JSON after stripping the leading `//`, the `lt-stat` label and trailing `// …` comments outside
  strings. Tolerate small slips — a missing or extra comma, unquoted values, single quotes, `5 m`, odd spacing. If it
  can't be understood unambiguously, **show the block and ask** what was meant. Never guess.

### 2. Fill the hand-filled fields

- For every empty field among `analysis`, `solution-complexity`, `implementation`, `self-result`, **ask** the user (one
  AskUserQuestion call; `self-result` options `done` / `partial` / `gave up`). An unfinished attempt is recorded as
  `self-result: "gave up"`, never blank.
- Keep the user's values as written (trimmed); don't reword them.

### 3. Compute the fields

| Field        | How                                                                                                                                                 |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `stopped-at` | Now.                                                                                                                                                |
| `n`, `title` | N as a number; the title from the file's header KDoc (`<N>. <Title>  (url)`).                                                                       |
| `fn`         | The attempt's function name.                                                                                                                        |
| `tests`      | See below: `pass x/y`, `fail x/y` or `compile error`.                                                                                               |
| `ref-used`   | `true` if any `fun referenceSolution*` exists in the file.                                                                                          |
| `checked`    | `true` if an `(lt-check)` KDoc sits between the block and the function.                                                                             |
| `optimal`    | The `O(…) / O(…)` bound from a `referenceSolution*` KDoc's Complexity, else from the `(lt-check)` KDoc's `Optimal:` line; `null` if neither exists. |
| `difficulty` | Fetched from LeetCode now (query below).                                                                                                            |
| `category`   | Inferred now, with the `lt-close` rule (below). `lt-close` may still pick a different one.                                                          |
| `attempt`    | 1 + the number of existing lines in `.stats/leetcode.jsonl` with this `n` (`0` lines if the file is missing).                                       |

**`tests`** — measure the attempt's function alone:

1. Rewrite `check(...)` to wire only it: `fun test() = check(::fn)`.
2. Run `mise run test-one leetcode.<OuterClassName>`.
    - Compile error → `compile error`.
    - Fail → the harness prints `k/y checks failed` → `fail <y-k>/y`.
    - Pass → `pass y/y`, with `y` = the number of cases in `cases` (count the `expects` / `expectsAnyOrder` /
      `.expectsAnyOf(` entries, ignoring commented-out lines).
3. **Restore** the file from the backup and prove it with `cmp` — `check(...)` must be byte-identical afterwards.

**Difficulty / tags** — the slug is in the header URL:

```bash
curl -s https://leetcode.com/graphql -H 'Content-Type: application/json' \
  -d '{"query":"query q($s:String!){question(titleSlug:$s){difficulty topicTags{name}}}","variables":{"s":"<slug>"}}'
```

If that fails, fall back to WebSearch; if still unknown, use `null` and say so.

**Category rule** (shared with `lt-close`): the category of an existing closed copy
(`find test/leetcode -mindepth 2 -name 'I####*.kt'`) if there is one; otherwise infer it from the LeetCode tags and the
technique of the solution, mapped onto the **existing** directories under `test/leetcode/` (never invent a new one).

### 4. Write the line, remove the block

- Append **exactly one** compact JSON line to `.stats/leetcode.jsonl` (`mkdir -p .stats`), keys in this order — the
  stat-comment fields, then the computed ones:

  ```json
  {"stopped-at": "2026-10-26 13:10", "n": 3964, "title": "Minimum Lights to Illuminate a Road", "fn": "minLights", "attempt": 1, "difficulty": "Medium", "category": "array_string", "started-at": "2026-10-26 12:00", "analysis": "5m", "solution-complexity": "count map, O(n) / O(1)", "implementation": "5m", "self-result": "done", "tests": "pass 14/14", "ref-used": false, "checked": true, "optimal": "O(n) / O(1)"}
  ```

  Build it with `jq -cn --arg …` (or `python3 -c 'import json…'`) so quoting is always valid; then `tail -1` it and
  check it parses.
- Delete the stat-comment lines from the file — **only** those lines. Verify with `diff <backup> <file>`: the only
  difference is the removed block. Then `mise run build` must pass.
- `.stats/` is gitignored and has no backup — never truncate or rewrite the file, only append.

### 5. Report

Now the spoiler rule is lifted: show the logged line in readable form (difficulty, category, tests, attempt number).
If `self-result` is not `done`, remind the user the task stays open and a later `/lt-stat start N` logs the next
attempt.
