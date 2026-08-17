# `other.concurrency` — worked concurrency lessons

A guided, runnable tour of JVM and Kotlin concurrency. Each file is **one self-contained lesson**: a KDoc block
explaining the core idea, a demo of the hazard, the fix (es), and JUnit tests you can run. Read top to bottom in numeric
order — later lessons assume the vocabulary of earlier ones.

Unlike the `leetcode.*` packages, these do **not** use the `ProblemTest` DSL. They are plain JUnit 5 +
`kotlin.test` tests (same style as the rest of `other/`).

## The determinism convention (read this first)

Concurrency tests are worthless if they're flaky. Every lesson follows one rule:

- **Correct/fixed code asserts EXACT results, deterministically, on every run.** (A `CountDownLatch`
  "start gun" is used throughout to release all threads at once and maximize contention, so a broken fix would reliably
  fail.)
- **Buggy demos assert only loose, always-true invariants** (e.g. `result <= expected`) and `println`
  the usual wrong behavior — never a flaky "this is always wrong".
- **No test ever hangs.** Blocking demos use latches, `join(timeout)`, `tryLock` timeouts, or
  `withTimeout`/`withTimeoutOrNull`, and clean up stuck threads.

## Lessons

### JVM primitives (`java.util.concurrent`, threads)

| #  | File                       | Teaches                                                                                  |
|----|----------------------------|------------------------------------------------------------------------------------------|
| 01 | `L01RaceConditions`        | Lost-update race; `synchronized` and `AtomicInteger` fixes                               |
| 02 | `L02LocksAndConditions`    | `ReentrantLock`/`withLock`, `tryLock` timeout, `Condition` await/signal, `ReadWriteLock` |
| 03 | `L03Deadlock`              | Two-lock deadlock and the lock-ordering fix                                              |
| 04 | `L04Atomics`               | Compare-and-swap, CAS retry loops, a lock-free stack, `LongAdder`                        |
| 05 | `L05VolatileVisibility`    | Visibility vs atomicity, `@Volatile`, happens-before                                     |
| 06 | `L06ProducerConsumer`      | Bounded buffer with `ArrayBlockingQueue`, and the hand-rolled `wait`/`notifyAll` version |
| 07 | `L07Executors`             | Thread pools: `ExecutorService`, `Callable`, `Future`, `invokeAll`                       |
| 08 | `L08Coordination`          | `CountDownLatch`, `CyclicBarrier`, `Semaphore`                                           |
| 09 | `L09ConcurrentCollections` | `ConcurrentHashMap` atomic ops, `CopyOnWriteArrayList`, `ThreadLocal`                    |
| 10 | `L10CompletableFuture`     | Async composition: `thenApply`/`thenCompose`/`thenCombine`/`allOf`/`exceptionally`       |

### Kotlin coroutines (`kotlinx.coroutines`)

| #   | File                 | Teaches                                                                               |
|-----|----------------------|---------------------------------------------------------------------------------------|
| K01 | `K01CoroutineBasics` | `launch`, `async`/`await`, structured concurrency                                     |
| K02 | `K02Dispatchers`     | `Dispatchers.Default`/`IO`, `withContext`                                             |
| K03 | `K03Cancellation`    | Cooperative cancellation, `withTimeout`/`withTimeoutOrNull`, `NonCancellable` cleanup |
| K04 | `K04CoroutineMutex`  | Races across coroutines; `Mutex`, atomics, and confinement                            |
| K05 | `K05Channels`        | Channels, `produce`, fan-out / fan-in (the coroutine analogue of L06)                 |
| K06 | `K06Flow`            | Cold streams: `flow { }`, operators, `flowOn`                                         |

## Running

```bash
# One lesson:
mise run test-one other.concurrency.L01RaceConditions

# Everything in this package:
mise run test-one other.concurrency
```

These lessons are **excluded from the default test run** (`mise run test` passes
`--exclude-classes "other.concurrency.*"`, carried over from the Gradle build's
`exclude("other/concurrency/*")`). They are meant to be run deliberately, one at a time, and observed —
that's the point of a lesson. Run them explicitly with the commands above.

Unlike the `leetcode.*` problems, these are plain top-level JUnit classes rather than
`@Nested inner class Solution`, so an exact `--include-classes "other.concurrency.L01RaceConditions"`
matches without a trailing wildcard.

Coroutine lessons depend on `kotlinx-coroutines-core` / `-test` (declared in `module.yaml` under
`test-dependencies`).

## Suggested reading order for someone learning

1. **01 → 05** build the mental model: why races happen (atomicity) and why threads don't see each other's writes
   (visibility). Everything else is tools that solve one or both.
2. **06 → 10** are the higher-level toolkit you actually reach for: queues, pools, coordinators, concurrent collections,
   and async composition — instead of hand-managing threads and locks.
3. **K01 → K06** re-tell the same story in coroutines: the same races still exist (K04), but the idioms (structured
   concurrency, channels, flows) are higher-level and read as sequential code.
