# CLAUDE.md — `leetcode/utils`

Guidance for the type-conversion layer that backs the test harness. The parent `CLAUDE.md` covers the repo as a whole;
this file is the detail for editing the converters.

## What lives here

- `TypeConverters.kt` — the central registry. Converts LeetCode-style **string** inputs ↔ typed values and defines
  per-type equality. This is the single source of truth for "which types the harness understands".
- `ArrayUtils.kt` — low-level string parsers (`"[1,2,3]"` → `IntArray`, `"[[1,2],[3,4]]"` → 2D, etc.). The converters
  delegate here; they don't parse strings themselves.
- Data-structure types and their parsers: `ListNode.kt`, `TreeNode.kt`, `Node.kt`, `QuadNode.kt`. Each exposes a
  `String.toX()` parser and a `toString()` used for equality.
- `type_converters/` — one test file per registered type. **Every converter must have a matching test here.**
- `todo.md` — known framework gaps / wishlist. Read it before proposing harness improvements.

## How conversion works (read before editing)

`testCases<F>()` reads the `typealias F` via reflection to learn each argument's `KType` and the return `KType`, then
calls `TypeConverters.convert` / `equal`. Two parallel registries are keyed differently:

- **`handlers: Map<KClass, Handler>`** — for plain (non-generic) classes: `Int`, `IntArray`, `TreeNode`, `ListNode`…
  Register with `register(SomeClass::class, …)`.
- **`ktypeHandlers: Map<KType, Handler>`** — for **generic** types where erasure makes the `KClass` ambiguous:
  `List<Int>`, `List<List<Int>>`, `Array<IntArray>`, `Array<CharArray>`, `Array<String>`… Register with
  `register(typeOf<List<Int>>(), …)`.

`ktypeHandlers` is checked **first** in both `convert` and `equal`, then it falls back to `handlers` via the type's
`classifier`. Pick the right registry: if the type has a type parameter, use `typeOf<…>()`; otherwise use `::class`.

A `Handler` is `(fromString, equals?)`:

- `fromString: (String) -> Any?` — parse the LeetCode string into the real value. May legitimately return `null`
  (e.g. `"[]"` → `null` `ListNode`); the registry treats "handler returned null" differently from "no handler", so
  always register a handler rather than relying on fallthrough.
- `equals: ((Any?, Any?) -> Boolean)?` — optional. Omit it to use `==`. **Required for any type whose `==` is identity
  or reference-based** — arrays and the linked/tree node types. See the equality rules below.

## Rules for adding a new type

1. Decide the registry: generic → `register(typeOf<…>())`; plain class → `register(KClass)`.
2. Write the parser in `ArrayUtils.kt` (for array/list shapes) or as a `String.toX()` extension next to the data type
   (for node types). Reuse `arraySplit()` / `array2arraySplit()` — they already handle the `"[]"` empty-vs-single-empty-
   string edge case. Don't hand-roll bracket stripping.
3. Provide an `equals` whenever default `==` is wrong:
    - **Arrays** (`IntArray`, `DoubleArray`, `Array<IntArray>`, `Array<CharArray>`, `Array<String>`): compare by
      `.toList()` / `.map { it.toList() }`. Array `==` is reference equality — without this every case fails.
    - **Node types** (`ListNode`, `TreeNode`, `Node`, `QuadNode`): compare by `a?.toString() == b?.toString()`. Their
      `toString()` is the canonical serialization, so keep `toString()` and the parser as exact inverses.
    - Plain scalars and `List<…>` of scalars: `==` is correct, omit `equals`.
4. Add a test in `type_converters/` (one file per type) covering: normal case, empty (`"[]"`), and the
   round-trip/equality behavior. A converter without a test is incomplete.

## Equality is order-sensitive

`equal` does positional comparison (lists/arrays compared element-by-element in order). Solutions that return
`List<List<Int>>` etc. must emit output in the **exact expected order**. There is currently no order-insensitive
comparison — see `todo.md` item 2 if you intend to add one.

## Gotchas

- **Conversion runs fresh per case**, inside the case lambda, so mutable inputs (`IntArray`) aren't shared across the
  multiple solutions passed to `check(...)`. Don't cache converted values.
- **Non-string args pass through untouched** — `convert` returns the value as-is if it isn't a `String` (e.g. a raw
  `7` literal in `args(...)`). Only stringified inputs go through `fromString`.
- **`equal` normalizes the expected side**: if `expected` is a `String` it's run through `fromString` first, then
  compared. So expected values can be written as LeetCode strings even for custom types.
- **Multiline string inputs are cleaned** — `arraySplit()` / `array2arraySplit()` strip structural whitespace
  (spaces, tabs, line breaks) *outside* double-quoted segments via `stripStructuralWhitespace()`, so indented
  `"""…"""` matrix literals parse the same as single-line ones. Whitespace *inside* `"…"` is preserved (string
  elements may contain spaces). If you add a new parser that splits on literal markers, route it through these helpers.
- `callFunction` only supports arity 1–4; higher-arity signatures need a new branch.
