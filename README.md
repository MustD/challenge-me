# Algorithm Practice Repository
## Overview
This repository contains solutions to various algorithmic problems from platforms like LeetCode and algorithm books. It serves as a personal collection of algorithms and data structure implementations in Kotlin.
## Project Structure
The project is organized into packages based on problem categories:
- `leetcode`: Main package for solutions
    - `array_string`: Array and string manipulation problems
    - `backtracking`: Backtracking algorithm problems
    - `hash_map`: Hash map-related problems
    - `sliding_window`: Sliding window technique problems
    - `db_multi`: Various difficult problems

## Problem Solutions
Solutions are named with a prefix indicating the source and problem number:
- `I####`: LeetCode problems (e.g., `I0001twoSum.kt`, `I0383ransomNote.kt`)
- `C4`: Custom implementations or book problems

Examples include:
- Two Sum
- Add Two Numbers
- Longest Substring Without Repeating Characters
- Median of Two Sorted Arrays
- Valid Sudoku
- Letter Combinations of a Phone Number

## Environment
- Kotlin 2.4.10
- JVM Toolchain 25
- JUnit 6.1.3 for testing
- Build: [Kotlin Toolchain](https://kotlin-toolchain.org/) 0.12.0 (configured in `module.yaml`)

## Build and Test
This project uses the Kotlin Toolchain (`./kotlin`) as the build system, with
[mise](https://mise.jdx.dev/) tasks as the entry point:

``` bash
mise run build     # compile only — unlike Gradle, this does NOT run tests
mise run test      # run all tests
mise run test-one leetcode.backtracking.I0039combinationSum   # a single problem
mise run test-one leetcode.backtracking                       # a whole category
mise run clean
```

The `mise` tasks wrap `./kotlin` and carry two required test-filter flags. If you invoke `./kotlin test`
directly, note that `--include-classes "*"` is mandatory for a full run and that class patterns need a
trailing `*` to reach the `@Nested inner class Solution` — see the *Commands* section of `CLAUDE.md`.
## Purpose
This repository serves as:
- A personal reference for algorithm implementations
- Practice for solving algorithmic problems
- Preparation for technical interviews
- A learning resource for understanding common algorithmic patterns

## License
This project is available for personal use and learning purposes.
