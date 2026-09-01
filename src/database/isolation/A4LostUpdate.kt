package database.isolation

/**
 * ANOMALY 4 — LOST UPDATE (standard name: P4).
 *
 * WHAT IT IS
 * ----------
 * Two transactions READ the same row, each computes a new value from what it read, and both WRITE.
 * The second write is based on a value that is already stale, so the first update disappears:
 *
 *     T1: SELECT balance -> 100
 *     T2: SELECT balance -> 100
 *     T1: UPDATE SET balance = 90;  COMMIT       -- withdrew 10
 *     T2: UPDATE SET balance = 90;  COMMIT       -- withdrew 10 -> but the balance is still 90
 *                                                  20 withdrawn, 10 debited. The bank lost money.
 *
 * THE KEY DETAIL: WHERE THE ARITHMETIC HAPPENS
 * --------------------------------------------
 * This anomaly needs a READ-MODIFY-WRITE cycle that goes THROUGH YOUR APPLICATION. The blind
 * in-database form is already safe at READ COMMITTED:
 *
 *     UPDATE accounts SET balance = balance - 10 WHERE id = 1     -- safe
 *
 * because when a READ COMMITTED `UPDATE` finds a row that a concurrent transaction has modified, it
 * WAITS for that transaction, then RE-EVALUATES the row against the newest committed version
 * ("EvalPlanQual" re-check) and applies the arithmetic to the fresh value. Compute the new value in
 * Kotlin, though, and the database has no idea the write was derived from a stale read.
 *
 * THREE WAYS OUT (all three are shown below)
 * ------------------------------------------
 *   1. PESSIMISTIC: `SELECT ... FOR UPDATE` — take the row lock at READ time. The second reader
 *      BLOCKS until the first commits, then reads the fresh value. Simple, predictable, and it
 *      serialises the hot row (contention becomes latency, not errors).
 *   2. OPTIMISTIC via isolation: run at REPEATABLE READ. Postgres detects that the row changed
 *      after the transaction's snapshot and aborts with SQLSTATE 40001 — no lost update, but the
 *      application must RETRY. (This is "first updater wins".)
 *   3. OPTIMISTIC via version column: `UPDATE ... WHERE id = ? AND version = ?`, then check the
 *      affected row count. This is what JPA's `@Version` does, and it works at any isolation level
 *      and across any number of services.
 */
fun lostUpdateDemo() {
    section("A4 — LOST UPDATE: two withdrawals of 10 from a balance of 100")

    // ---------------------------------------------------------------------------------------
    // 1. READ COMMITTED, arithmetic in the application -> the update is lost.
    // ---------------------------------------------------------------------------------------
    resetSchema()
    scenario("READ COMMITTED, plain SELECT then UPDATE (read-modify-write in Kotlin)")
    session("T1", Level.READ_COMMITTED) { t1 ->
        session("T2", Level.READ_COMMITTED) { t2 ->
            t1.begin()
            t2.begin()

            // Both transactions read the SAME value — a plain SELECT takes no locks under MVCC.
            val seenByT1 = t1.read("balance", "SELECT balance FROM accounts WHERE id = 1")!!.toInt()
            val seenByT2 = t2.read("balance", "SELECT balance FROM accounts WHERE id = 1")!!.toInt()

            t1.exec("UPDATE accounts SET balance = ${seenByT1 - 10} WHERE id = 1")
            t1.commit()

            // T2's UPDATE does not block (T1 is already gone) and does not fail: at READ COMMITTED
            // T2 is simply allowed to overwrite the row with a value computed from a stale read.
            t2.exec("UPDATE accounts SET balance = ${seenByT2 - 10} WHERE id = 1")
            t2.commit()

            val final = observe("SELECT balance FROM accounts WHERE id = 1")
            verdict(final != "80", "expected 80 after two withdrawals of 10, final balance is $final")
        }
    }

    // ---------------------------------------------------------------------------------------
    // 2. Same interleaving at REPEATABLE READ -> Postgres refuses the second write.
    // ---------------------------------------------------------------------------------------
    resetSchema()
    scenario("REPEATABLE READ, same interleaving — 'first updater wins', loser gets 40001")
    session("T1", Level.REPEATABLE_READ) { t1 ->
        session("T2", Level.REPEATABLE_READ) { t2 ->
            t1.begin()
            t2.begin()
            val seenByT1 = t1.read("balance", "SELECT balance FROM accounts WHERE id = 1")!!.toInt()
            val seenByT2 = t2.read("balance", "SELECT balance FROM accounts WHERE id = 1")!!.toInt()

            t1.exec("UPDATE accounts SET balance = ${seenByT1 - 10} WHERE id = 1")
            t1.commit()

            // The row changed after T2's snapshot was taken, so T2 cannot be allowed to write it.
            t2.expectingError("T2's UPDATE") {
                t2.exec("UPDATE accounts SET balance = ${seenByT2 - 10} WHERE id = 1")
            }

            val final = observe("SELECT balance FROM accounts WHERE id = 1")
            verdict(false, "T2 was aborted instead of silently losing T1's write; balance is $final and T2 must retry")
        }
    }

    // ---------------------------------------------------------------------------------------
    // 3. READ COMMITTED + SELECT ... FOR UPDATE -> the second reader waits, then reads fresh.
    // ---------------------------------------------------------------------------------------
    resetSchema()
    scenario("READ COMMITTED + SELECT ... FOR UPDATE — pessimistic locking, no error, no loss")
    session("T1", Level.READ_COMMITTED) { t1 ->
        session("T2", Level.READ_COMMITTED) { t2 ->
            t1.begin()
            t2.begin()

            // FOR UPDATE takes an exclusive row lock, held until T1 ends.
            val seenByT1 = t1.read("balance (locked)", "SELECT balance FROM accounts WHERE id = 1 FOR UPDATE")!!.toInt()

            // T2's identical statement BLOCKS, so it has to run on another thread.
            val t2Work = background {
                val seenByT2 =
                    t2.read("balance (locked)", "SELECT balance FROM accounts WHERE id = 1 FOR UPDATE")!!.toInt()
                t2.exec("UPDATE accounts SET balance = ${seenByT2 - 10} WHERE id = 1")
                t2.commit()
            }
            note(
                if (t2Work.stillBlockedAfter(millis = 500)) "T2's SELECT ... FOR UPDATE is parked on T1's row lock — this wait IS the safety mechanism."
                else "T2 was not blocked (unexpected)",
            )

            t1.exec("UPDATE accounts SET balance = ${seenByT1 - 10} WHERE id = 1")
            t1.commit()      // releasing the lock lets T2 continue — and it re-reads the new value
            t2Work.awaitDone()

            val final = observe("SELECT balance FROM accounts WHERE id = 1")
            verdict(final != "80", "final balance is $final — both withdrawals applied, no retry needed")
        }
    }
}
