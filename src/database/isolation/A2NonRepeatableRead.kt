package org.example.database.isolation

/**
 * ANOMALY 2 — NON-REPEATABLE READ (standard name: P2, also "fuzzy read").
 *
 * WHAT IT IS
 * ----------
 * The same query, run twice inside one transaction, returns DIFFERENT values for the SAME row,
 * because another transaction committed an UPDATE in between.
 *
 *     T1: BEGIN; SELECT balance FROM accounts WHERE id = 1;   -- 100
 *     T2:        UPDATE ... SET balance = 50; COMMIT;
 *     T1:        SELECT balance FROM accounts WHERE id = 1;   -- 50   <- not repeatable
 *
 * WHY IT HAPPENS AT READ COMMITTED
 * --------------------------------
 * At READ COMMITTED, Postgres takes a FRESH SNAPSHOT at the start of every STATEMENT. That is
 * exactly the definition of the level: each statement sees all data committed before that statement
 * began, so two statements in one transaction can straddle someone else's commit.
 *
 * WHY REPEATABLE READ FIXES IT
 * ----------------------------
 * At REPEATABLE READ the snapshot is taken ONCE, at the first statement of the transaction, and
 * reused for the entire transaction. Later commits by others are simply invisible — the transaction
 * keeps reading a consistent point-in-time view of the whole database.
 *
 * WHY YOU SHOULD CARE
 * -------------------
 * Any multi-statement read that must be self-consistent is at risk: "sum the line items, then read
 * the order total", a report that queries five tables, or the classic read-then-decide-then-write.
 * Under READ COMMITTED each of those statements can see a different world.
 *
 * Note the snapshot timing rule: the transaction's snapshot is taken at its FIRST STATEMENT, not at
 * `BEGIN`. Below, T1 deliberately reads before T2 updates so the snapshot is pinned early.
 */
fun nonRepeatableReadDemo() {
    section("A2 — NON-REPEATABLE READ: allowed at READ COMMITTED, prevented at REPEATABLE READ")

    for (level in listOf(Level.READ_COMMITTED, Level.REPEATABLE_READ)) {
        resetSchema()
        scenario("reader runs at ${level.sql}")

        session("T1", level) { t1 ->
            session("T2", Level.READ_COMMITTED) { t2 ->
                t1.begin()
                // This first read pins T1's snapshot (it matters at REPEATABLE READ).
                val first = t1.read("first read of alice's balance", "SELECT balance FROM accounts WHERE id = 1")

                // Someone else changes the row and COMMITS — no dirty read involved, this is real data now.
                t2.begin()
                t2.exec("UPDATE accounts SET balance = 50 WHERE id = 1")
                t2.commit()

                val second = t1.read("second read of alice's balance", "SELECT balance FROM accounts WHERE id = 1")
                t1.commit()

                verdict(
                    anomalyHappened = first != second,
                    description = when (level) {
                        Level.READ_COMMITTED ->
                            "$first then $second — a new snapshot per statement lets T2's commit leak into T1."

                        else ->
                            "$first then $second — one snapshot for the whole transaction; T1 never sees T2's commit " +
                                    "(the committed value in the database is now ${observe("SELECT balance FROM accounts WHERE id = 1")})."
                    },
                )
            }
        }
    }
}
