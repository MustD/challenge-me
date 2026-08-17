package leetcode.bit_manipulation

import leetcode.expects
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 136. Single Number  (https://leetcode.com/problems/single-number/)
 *
 * Given a non-empty array of integers `nums`, every element appears exactly twice except for one
 * element which appears only once. Find and return that single element.
 *
 * You must implement a solution with linear runtime complexity and use only constant extra space.
 *
 * Constraints:
 * - 1 <= nums.size <= 3 * 10^4
 * - -3 * 10^4 <= nums[i] <= 3 * 10^4
 * - Each element in the array appears twice except for one element which appears only once.
 * - Required: O(n) time, O(1) extra space (so no HashSet/HashMap in the intended solution).
 */
typealias I0136 = (IntArray) -> Int

class I0136singleNumber {

    @Nested
    inner class Solution : leetcode.ProblemTest<I0136> {

        override val cases = leetcode.testCases<I0136>(
            "[2,2,1]" expects 1,
            "[4,1,2,1,2]" expects 4,
            "[1]" expects 1,
            // edge cases implied by the constraints
            "[0,1,0]" expects 1,
            "[-1,-1,-30000]" expects -30000,
            "[7,3,3,7,-5]" expects -5,
        )

        @Test
        fun test() = check(::singleNumber)

        /**
         * ### Pattern: XOR cancellation (bit-manipulation invariant)
         *
         * The array, viewed under `xor`, is an *abelian group* `(Z/2)^32`: `xor` is commutative and
         * associative, `0` is the identity, and **every element is its own inverse** (`x xor x == 0`).
         * Folding the whole array therefore makes each duplicated pair annihilate itself *regardless of
         * where the two copies sit*, and the survivor is the lone element. That is the entire proof —
         * no ordering assumption, no bookkeeping.
         *
         * ### Time: `O(n)`
         * One pass. `IntArray.reduce` walks indices `1..lastIndex` doing exactly one `IXOR` bytecode per
         * element — a single-cycle ALU op, no branches, no hashing. `n - 1` XORs total.
         *
         * ### Space: `O(1)` — genuinely, not "O(1) if you squint"
         * `reduce` on `IntArray` is `inline`, so the lambda is *not* allocated as an object and the
         * accumulator stays an unboxed primitive `int` in a local slot/register. No `Integer` boxing,
         * no iterator, no auxiliary structure. Constant 4 bytes of working state. Iterative, so zero
         * recursion stack. This is what satisfies the problem's "constant extra space" requirement —
         * the same code written over `List<Int>` would box and quietly cost more.
         *
         * ### Correctness notes
         * - **Negatives are free.** `xor` is a bitwise op over the full two's-complement 32-bit word,
         *   sign bit included; `-30000 xor -1 xor -1 == -30000`. No special-casing needed.
         * - **Single-element input works.** `reduce` seeds the accumulator with `nums[0]` and never
         *   invokes the lambda, returning `nums[0]`.
         * - **No overflow risk.** XOR cannot overflow. Contrast with the arithmetic trick
         *   `2 * sum(distinct) - sum(all)`, which can blow past `Int.MAX_VALUE` on large inputs.
         * - **`reduce` vs `fold(0)`.** `reduce` throws `UnsupportedOperationException` on an empty array;
         *   safe here only because the constraints guarantee `nums.size >= 1`. `fold(0) { a, b -> a xor b }`
         *   is equivalent, and would return `0` on empty input instead of throwing — a defensible choice
         *   if the non-empty guarantee were not given.
         * - **Fragile to its premise (the real teaching point).** If the "exactly twice" invariant is
         *   violated, this returns a meaningless number instead of failing loudly. It is *correct*, but
         *   it has no error-detection capability at all.
         *
         * ### Alternatives and their trade-offs
         * | Approach | Time | Space | Verdict |
         * |---|---|---|---|
         * | **XOR fold (this)** | `O(n)` | `O(1)` | Optimal on both axes |
         * | `HashSet` add/remove toggle | `O(n)` avg | `O(n)` | Violates the space constraint; also worst-case `O(n^2)` on adversarial hashing |
         * | `HashMap` counting | `O(n)` | `O(n)` | Same, plus generalizes to "appears k times" |
         * | Sort, then scan in pairs | `O(n log n)` | `O(1)`–`O(log n)` | Beats hashing on memory, loses on time; mutates the input |
         * | `2 * sum(distinct) - sum(all)` | `O(n)` | `O(n)` | Needs a set anyway *and* risks overflow — strictly worse |
         *
         * **This solution is optimal and no better one exists.** `Ω(n)` time is a hard lower bound: any
         * element you never read could be the singleton, so every element must be inspected. `O(1)` space
         * is the floor. Nothing to improve asymptotically or constant-factor-wise.
         *
         * Worth knowing the family — the same `xor` algebra scales up:
         * - **137. Single Number II** (all thrice but one): pair-XOR fails, since three copies leave one
         *   behind. Use per-bit counting mod 3, or the two-mask `ones`/`twos` state machine.
         * - **260. Single Number III** (two singletons): XOR everything to get `a xor b`, isolate any set
         *   bit (`x and -x`), partition the array on that bit, and XOR each half independently.
         *
         * ### Parallelism / SIMD — one of the rare problems where it genuinely applies
         * XOR is an associative, commutative monoid with identity `0`, which makes this a *textbook*
         * parallel reduction — the correctness precondition most LeetCode problems fail. Split into `k`
         * chunks, XOR each independently, then XOR the `k` partials; the answer is bit-identical to the
         * sequential one (unlike floating-point sums, which are order-dependent). In JVM terms:
         * `Arrays.stream(nums).parallel().reduce(0) { a, b -> a xor b }` is *correct here* purely because
         * of that algebra. SIMD applies too: one AVX2 `VPXOR` XORs 8 `int`s per instruction, and HotSpot's
         * superword pass can auto-vectorize this reduction shape.
         *
         * **And yet you should not do it at this size.** With `n <= 3 * 10^4` the array is ~120 KB and the
         * work is ~30k single-cycle ops — tens of microseconds, entirely memory-bandwidth bound. Fork/join
         * dispatch alone costs tens of microseconds, so the parallel version reliably *loses*. The
         * crossover for a per-element op this cheap is roughly `10^7`+ elements, and even then you cap out
         * at memory bandwidth rather than core count — Amdahl's law bites at the bandwidth ceiling, not the
         * arithmetic. Correct lesson: parallelism here is *legal but not profitable*, and knowing which of
         * those two is missing is the skill.
         *
         * ### Where this shows up in production
         * - **RAID 5 / erasure coding.** Parity is literally `XOR` of the data drives; a lost drive is
         *   reconstructed by XORing the survivors. Same self-inverse property, same one-pass fold.
         * - **Data reconciliation at scale.** To find the one record present in ledger A but not B, XOR all
         *   IDs on each side and XOR the results — `O(1)` memory over billions of rows, where a `HashSet`
         *   would need gigabytes. Extended, this becomes **Invertible Bloom Lookup Tables (IBLT)**, which
         *   keep a XOR of keys per bucket to recover set differences between distributed replicas — used in
         *   database anti-entropy and Bitcoin's Erlay block relay.
         * - **Streaming and distributed shapes.** State is 4 bytes, so it works on an unbounded stream where
         *   a hash set cannot; and because it is mergeable and commutative it behaves like a CRDT — each
         *   shard folds locally, a coordinator folds the partials, order and retries don't matter.
         * - **How the constraint differs from the interview.** Real data almost never honors "exactly
         *   twice". Production code usually reaches for the counting `HashMap` (or `groupingBy().eachCount()`)
         *   *despite* being asymptotically worse on space, because it tells you when your assumption broke
         *   and what the actual multiplicities were. The `O(1)` trick is only valid under an invariant that
         *   something else must be enforcing — checksums and parity blocks are exactly the places where the
         *   system does enforce it, which is why that is where XOR folds actually live.
         */
        fun singleNumber(nums: IntArray): Int {
            return nums.reduce { acc, i -> acc xor i }
        }

    }
}
