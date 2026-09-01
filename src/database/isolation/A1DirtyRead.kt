package database.isolation

/**
 * ANOMALY 1 — DIRTY READ (standard name: P1).
 *
 * WHAT IT IS
 * ----------
 * T2 reads a row version written by T1 while T1 is still open. If T1 then rolls back, T2 has acted
 * on data that never existed — money that was never transferred, an order that was never placed.
 *
 *     T1: BEGIN; UPDATE accounts SET balance = 999 WHERE id = 1;      -- not committed
 *     T2:                                          SELECT balance ... -- 999?  <- dirty read
 *     T1: ROLLBACK;                                                   -- the 999 never existed
 *
 * WHY YOU CANNOT REPRODUCE IT IN POSTGRES
 * ---------------------------------------
 * The standard permits dirty reads at READ UNCOMMITTED, but Postgres cannot produce one even if you
 * ask for that level. MVCC is why: `UPDATE` does not overwrite the row in place — it writes a NEW
 * row version stamped with the writing transaction's id (xmin) and marks the old version dead as of
 * that id (xmax). Visibility is then decided per reader: a row version is visible only if the
 * transaction that produced it has COMMITTED (and committed before the reader's snapshot). An
 * uncommitted version is invisible to everyone but its own author, so there is nothing "dirty" to
 * read. Rollback is nearly free for the same reason — the new versions are simply never made
 * visible, and VACUUM reclaims them later.
 *
 * Consequence: in Postgres, READ UNCOMMITTED is an ALIAS for READ COMMITTED. The server accepts the
 * request without complaint and quietly gives you the stronger level. Note the trap in the output
 * below: `SHOW transaction_isolation` still ECHOES "read uncommitted", so the setting looks honoured
 * — only the experiment reveals that the semantics are READ COMMITTED. (Other engines differ: SQL
 * Server and MySQL/InnoDB *do* implement READ UNCOMMITTED for real, so the same code is unsafe
 * there.)
 */
fun dirtyReadDemo() {
    section("A1 — DIRTY READ: impossible in PostgreSQL, even at READ UNCOMMITTED")
    resetSchema()

    session("T1", Level.READ_COMMITTED) { t1 ->
        session("T2", Level.READ_UNCOMMITTED) { t2 ->
            t1.begin()
            t2.begin()
            note(
                "T2 asked for READ UNCOMMITTED and the server echoes '${t2.effectiveLevel()}' back — " +
                        "but the reads below are the only proof of what it actually does.",
            )

            // T1 makes an uncommitted change. Only T1 can see it.
            t1.exec("UPDATE accounts SET balance = 999 WHERE id = 1")
            t1.read("own uncommitted balance", "SELECT balance FROM accounts WHERE id = 1")

            // T2 tries to peek at it.
            val seenByT2 = t2.read("balance seen by T2", "SELECT balance FROM accounts WHERE id = 1")

            // ...and T1 throws its change away.
            t1.rollback()
            val afterRollback = t2.read("balance after T1 rolled back", "SELECT balance FROM accounts WHERE id = 1")

            verdict(
                anomalyHappened = seenByT2 == "999",
                description = "T2 saw $seenByT2 while T1's uncommitted value was 999 " +
                        "(still $afterRollback after the rollback) — no dirty read is possible under MVCC.",
            )
            t2.commit()
        }
    }
}
