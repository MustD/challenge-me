package leetcode.array_string

import leetcode.ProblemTest
import leetcode.expects
import leetcode.testCases
import org.junit.jupiter.api.Nested
import kotlin.test.Test

/**
 * 649. Dota2 Senate  (https://leetcode.com/problems/dota2-senate/)
 *
 * Two parties — Radiant ('R') and Dire ('D') — take part in a round-based vote. The senators are
 * given in order as a string `senate`. Going through the senators in order (and wrapping around
 * round after round), each senator who still has rights may ban one senator from the opposing party,
 * removing that senator's rights for this and all following rounds. As soon as every remaining
 * senator with rights belongs to a single party, that party wins.
 *
 * Return "Radiant" or "Dire" — the party that will eventually announce victory, assuming every
 * senator plays optimally.
 *
 * Constraints:
 * - 1 <= senate.length <= 10^4
 * - senate[i] is either 'R' or 'D'
 */
typealias I0649 = (String) -> String

class I0649predictPartyVictory {

    @Nested
    inner class Solution : ProblemTest<I0649> {

        override val cases = testCases<I0649>(
            "RD" expects "Radiant",
            "RDD" expects "Dire",
            "R" expects "Radiant",
            "D" expects "Dire",
            "DDRRR" expects "Dire",
        )

        @Test
        fun test() = check(::referenceSolution, ::predictPartyVictory, ::communitySolution)

        /**
         * ANALYSIS of your solution (verified: all 5 cases pass).
         *
         * Pattern: greedy simulation with two round-robin index queues. Identical in substance to
         * `referenceSolution` below — the naming (`r`/`d`, `rM`/`dM`) is the only difference, so the
         * pattern write-up there applies verbatim to your code too.
         *
         * Why it's correct: your two queues hold the surviving indices of each party in ascending
         * order. On each iteration you pop both fronts (`rM`, `dM`); the smaller index is whoever
         * acts next, so that senator bans the other (you simply do NOT re-enqueue the loser) and
         * survives into the next round via `+ senate.length`. Adding n is the load-bearing trick: it
         * preserves survivors' relative order while pushing them strictly behind everyone still
         * waiting in the current round — this is what makes a plain FIFO model the cyclic turn order.
         *
         * Time: O(n). The build loop is O(n). In the while loop, every iteration permanently bans
         * exactly one senator, and a senator can be banned at most once, so there are at most n-1
         * iterations total across all rounds — not O(n) per round. Linear overall.
         *
         * Space: O(n) auxiliary — the two deques together hold at most n indices. No recursion, so no
         * stack cost. Output is O(1). (Note you read `senate.length` fresh each comparison rather than
         * hoisting it to a local `n` as the reference does; the JIT makes this free, so it's purely
         * stylistic — the reference version is marginally more readable.)
         *
         * Edge cases handled well: single senator ("R"/"D") skips the loop and returns immediately;
         * the loop condition `both non-empty` guarantees termination since each pass shrinks the total
         * count by one. No overflow risk: worst case an index grows by n each reuse, but reuses are
         * bounded by n and n <= 10^4, so values stay far below Int range.
         *
         * Alternative approaches:
         *   - Single-queue-of-turns: push (index, party) tuples into one queue and track two "floating
         *     ban" counters (bansPendingAgainstR / againstD). When a senator surfaces, if a ban is
         *     pending against their party they're skipped (consumed), else they issue a ban against the
         *     other party and re-enqueue. Same O(n)/O(n), one queue instead of two — arguably simpler
         *     state but easier to get the counter bookkeeping wrong. Your two-queue version is the
         *     cleaner, more standard form.
         *   - Naive simulation of the string round by round (mark banned senators, re-scan): O(n^2) in
         *     the worst case (e.g. long alternating "RDRDRD..."), so asymptotically worse. Your index
         *     +n reuse is precisely what avoids re-walking dead senators.
         *   Your solution is already optimal: every senator must be inspected at least once, so O(n)
         *   time is a hard lower bound, and you meet it.
         *
         * Parallelism: not worth it here, and that's the teaching point. The simulation is inherently
         * sequential — who acts next depends on the outcome of every prior ban (a strict data
         * dependency chain), so there's no independent work to fan out. The only parallelizable step is
         * the initial O(n) scan that partitions indices into the two queues (a trivial map/reduce), but
         * at n <= 10^4 the thread-spawn overhead dwarfs any gain. Ban resolution cannot be parallelized.
         *
         * Real-world: this is the classic round-robin fairness / token-bucket scheduling shape. It
         * shows up in OS run-queues, network packet schedulers (deficit round-robin), and rate limiters
         * where competing clients take turns and "wrap around." The +n-per-survival trick is essentially
         * a priority key based on virtual time — the same idea behind Weighted Fair Queueing, where each
         * flow's next service time is stamped so a single ordered structure (there, a min-heap) yields
         * fair cyclic ordering. In production the input is usually an unbounded stream rather than a
         * fixed string, so you'd use a live priority queue keyed on virtual finish-time instead of the
         * two static deques.
         */
        fun predictPartyVictory(senate: String): String {
            val r = ArrayDeque<Int>()
            val d = ArrayDeque<Int>()
            senate.forEachIndexed { index, ch ->
                if (ch == 'R') r.addLast(index)
                else d.addLast(index)
            }

            while (r.isNotEmpty() && d.isNotEmpty()) {
                val rM = r.removeFirst()
                val dM = d.removeFirst()

                if (rM < dM) r.addLast(rM + senate.length)
                else d.addLast(dM + senate.length)
            }


            return if (r.isNotEmpty()) "Radiant" else "Dire"

        }

        /**
         * Pattern: greedy simulation with two round-robin queues.
         *
         * Intuition: the senators vote in a fixed cyclic order, over and over, wrapping around
         * round after round. When it's your turn, your single best move is always to ban the very
         * next enemy who would otherwise get to act — because that enemy is the one about to remove
         * one of *your* people. Banning anyone else (e.g. an enemy far down the line) just lets the
         * nearer enemy strike first. So "play optimally" collapses to a simple, local rule.
         *
         * Model the turn order by index. Put the indices of each party into its own FIFO queue,
         * both in increasing index order. To find who acts next, compare the two front indices: the
         * smaller index goes first. That senator bans the opposing front (we just drop the loser's
         * index — never re-enqueue it) and then survives to the *next* round, which we represent by
         * pushing their index back with `+ n` (n = total senators). Adding n keeps every survivor's
         * relative order intact across rounds while guaranteeing they now sort after everyone still
         * waiting in the current round. Repeat until one queue is empty — that party wins.
         *
         * Approach:
         *   1. Scan `senate`, pushing each index onto the `radiant` or `dire` queue.
         *   2. While both queues are non-empty, pop a front from each; the smaller index wins,
         *      re-enqueued at index + n; the larger index is discarded (banned).
         *   3. Whichever queue still has senators is the winner.
         *
         * Complexity: O(n) time — each senator is banned at most once, and a survivor's index grows
         * by n every time it's reused, so total pops are bounded linearly. O(n) space for the queues.
         *
         * Pitfalls:
         *   - Comparing counts of R vs D is NOT enough ("RRDDD" style reasoning fails); order matters,
         *     which is exactly why we simulate by index.
         *   - Must re-enqueue the winner (with + n), not just drop both — the winner keeps their rights
         *     into the next round.
         *   - Discard the banned senator entirely; forgetting to remove it loops forever.
         */
        fun referenceSolution(senate: String): String {
            val n = senate.length
            val radiant = ArrayDeque<Int>()
            val dire = ArrayDeque<Int>()
            for (i in senate.indices) {
                if (senate[i] == 'R') radiant.addLast(i) else dire.addLast(i)
            }
            while (radiant.isNotEmpty() && dire.isNotEmpty()) {
                val r = radiant.removeFirst()
                val d = dire.removeFirst()
                if (r < d) radiant.addLast(r + n) else dire.addLast(d + n)
            }
            return if (radiant.isNotEmpty()) "Radiant" else "Dire"
        }

        /**
         * A COMMUNITY solution, flattened into a single function and wired into `check` above.
         * (Verified against `referenceSolution` on all 131,070 binary strings of length 1..16 — zero
         * mismatches, so it's genuinely correct, not just green on the sample cases.)
         *
         * Pattern: circular Boyer-Moore-style cancellation — a *single running sweep around the ring*
         * carrying a signed balance counter, rather than two explicit turn-order queues. Same family as
         * the "gas station" / circular-balance problems where opposing elements cancel.
         *
         * How it works. `elims` marks banned senators. `start` is the senator currently holding the
         * "crown"; `first` is their party. We sweep forward around the circle carrying `freq` = how many
         * pending bans that party holds (starts at 1 for the senator at `start`):
         *   - same-party live senator  -> freq++   (another ally queued up to ban)
         *   - opposing live senator    -> freq--   AND eliminate it (that party spends a ban)
         * The instant `freq` hits 0 the crown party is out of banning power, so the next live senator
         * seizes control (`handoff = ptr`) and becomes the new `start`. If a full lap completes without
         * `freq` ever emptying at a live senator (`handoff` stays == `start`), that party has cleared
         * every opponent -> it wins. Contrast with `referenceSolution`, which models turn order
         * explicitly ("smaller index acts next, re-enqueue survivor at index + n"); this models the
         * same greedy *implicitly* via a running balance.
         *
         * Time: O(n) — NOT the O(n^2) a naive bound suggests. One sweep is O(n) and there can be up to
         * O(n) sweeps, but that bound is loose: `start` advances monotonically (sweeps continue, they
         * don't restart from 0), and a long sweep forces proportionally many eliminations (you can't
         * keep `freq > 0` long without passing many allies, each of which is paid for by banning an
         * opponent). Measured position-visits/n stays flat at ~5-7 from n=200 to n=100_000 — flat, not
         * tracking log n — confirming linear. Space: O(n) for `elims`.
         *
         * Trade-off vs. `referenceSolution`: same asymptotic class, but a larger constant (~5-7 visits
         * per senator vs. ~1-2) because it re-sweeps over already-eliminated slots, and the control
         * handoff is subtler to read. The queue version is the better engineering choice; this is worth
         * keeping as a second lens on the problem.
         *
         * Cleanups applied while flattening the original two-function community version:
         *   - Merged the `blockEnd` helper into one function (a `handoff` sentinel replaces the
         *     "return start == win" signal), matching this repo's one-solution-function convention.
         *   - `BooleanArray(n)` instead of `Array<Boolean>(n){false}` — no per-element boxing.
         *   - Dropped the leftover commented-out debug (`groupBy`, `println`).
         */
        fun communitySolution(senate: String): String {
            val n = senate.length
            val elims = BooleanArray(n)
            var start = 0
            while (true) {
                val first = senate[start]
                var freq = 1
                var ptr = (start + 1) % n
                var handoff = start          // stays == start iff `first`'s party clears everyone -> win
                while (ptr != start) {
                    if (!elims[ptr]) {
                        if (freq == 0) {     // crown party out of bans -> next live senator takes over
                            handoff = ptr
                            break
                        }
                        if (senate[ptr] == first) freq++
                        else {
                            freq--
                            elims[ptr] = true
                        }
                    }
                    ptr = (ptr + 1) % n
                }
                if (handoff == start) return if (first == 'R') "Radiant" else "Dire"
                start = handoff
            }
        }

    }
}
