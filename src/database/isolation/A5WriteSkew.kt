package org.example.database.isolation

/**
 * ANOMALY 5 — WRITE SKEW (standard name: A5B).
 *
 * THE CLASSIC EXAMPLE — the hospital on-call roster
 * -------------------------------------------------
 * Invariant: AT LEAST ONE doctor must be on call. Alice and Bob are both on call, and both decide
 * to go home at the same moment:
 *
 *     T1: SELECT count(*) FROM doctors WHERE on_call;   -- 2, fine, I can leave
 *     T2: SELECT count(*) FROM doctors WHERE on_call;   -- 2, fine, I can leave
 *     T1: UPDATE doctors SET on_call = false WHERE name = 'alice';   COMMIT
 *     T2: UPDATE doctors SET on_call = false WHERE name = 'bob';     COMMIT
 *     -> nobody is on call. Each transaction was correct in isolation; together they broke the rule.
 *
 * WHY SNAPSHOT ISOLATION CANNOT CATCH IT
 * --------------------------------------
 * Write skew is the signature weakness of Snapshot Isolation, i.e. of Postgres REPEATABLE READ.
 * Both transactions read the same rows, then write DIFFERENT rows — there is no update conflict on
 * any single row, so the "first updater wins" check from A4 never fires. The conflict is between
 * one transaction's WRITE and the other's READ PREDICATE, and a snapshot alone cannot see that.
 *
 * Same shape, other disguises: double-booking a meeting room after checking for overlaps, letting a
 * balance go negative across two accounts that are checked together, allocating the last item of
 * stock from two carts, or two rows claiming the same "primary" flag. Any
 * READ-A-CONDITION-then-WRITE-SOMETHING-ELSE pair is a candidate.
 *
 * WHAT SERIALIZABLE DOES INSTEAD
 * ------------------------------
 * SSI (Serializable Snapshot Isolation) keeps snapshot reads — still no read locks, still no
 * blocking — but records SIREAD "predicate locks" on what each transaction read, then looks for the
 * dangerous structure that makes a cycle in the dependency graph (rw-antidependencies in and out of
 * one transaction). When it finds one, it aborts a participant with SQLSTATE 40001 at COMMIT time.
 * You pay with retries, not with locks — which also means SERIALIZABLE is only correct if EVERY
 * transaction touching that data runs at SERIALIZABLE.
 *
 * CHEAPER ALTERNATIVES if you don't want SERIALIZABLE globally:
 *   - `SELECT ... FOR UPDATE` on the rows you read (materialises the conflict onto real row locks);
 *   - fold the check into the write: `UPDATE ... WHERE (SELECT count(*) FROM doctors WHERE on_call) > 1`;
 *   - a real constraint (unique index, exclusion constraint for overlapping ranges) — the database
 *     then enforces the invariant no matter how the application misbehaves.
 */
fun writeSkewDemo() {
    section("A5 — WRITE SKEW: allowed at REPEATABLE READ, prevented at SERIALIZABLE")

    for (level in listOf(Level.REPEATABLE_READ, Level.SERIALIZABLE)) {
        resetSchema()
        scenario("both doctors run at ${level.sql}")

        session("alice", level) { t1 ->
            session("bob", level) { t2 ->
                t1.begin()
                t2.begin()

                // Both read the same predicate and reach the same (individually correct) conclusion.
                val seenByT1 = t1.read("doctors on call", "SELECT count(*) FROM doctors WHERE on_call")!!.toInt()
                val seenByT2 = t2.read("doctors on call", "SELECT count(*) FROM doctors WHERE on_call")!!.toInt()
                t1.log("sees $seenByT1 on call -> safe to go off call")
                t2.log("sees $seenByT2 on call -> safe to go off call")

                // They write DIFFERENT rows, so nothing blocks and no update conflict exists.
                t1.exec("UPDATE doctors SET on_call = false WHERE name = 'alice'")
                t2.exec("UPDATE doctors SET on_call = false WHERE name = 'bob'")

                t1.commit()
                // At SERIALIZABLE the conflict surfaces HERE, at commit time — the reads T2 made are
                // no longer consistent with the state T1 committed.
                t2.expectingError("bob's COMMIT") { t2.commit() }

                val onCall = observe("SELECT count(*) FROM doctors WHERE on_call")!!.toInt()
                verdict(
                    anomalyHappened = onCall == 0,
                    description = "$onCall doctor(s) left on call — the invariant is " +
                            if (onCall == 0) "BROKEN (write skew)" else "intact (bob's transaction was rolled back and must retry)",
                )
            }
        }
    }
}
