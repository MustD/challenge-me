package database.isolation

/**
 * ANOMALY 3 — PHANTOM READ (standard name: P3).
 *
 * WHAT IT IS
 * ----------
 * The same RANGE query, run twice inside one transaction, returns a different SET OF ROWS because
 * another transaction inserted (or deleted) rows matching the predicate.
 *
 *     T1: BEGIN; SELECT count(*) FROM accounts WHERE balance >= 100;   -- 2
 *     T2:        INSERT INTO accounts VALUES (3,'carol',500); COMMIT;
 *     T1:        SELECT count(*) FROM accounts WHERE balance >= 100;   -- 3   <- a phantom appeared
 *
 * PHANTOM vs NON-REPEATABLE READ
 * ------------------------------
 * Non-repeatable read = an EXISTING row changed value. Phantom = the SET of matching rows changed
 * membership. The distinction matters because they need different defences in a lock-based engine:
 * protecting an existing row only needs a row lock, but preventing a row from APPEARING needs a
 * lock on something that does not exist yet — a predicate/range lock (in MySQL/InnoDB: gap locks).
 *
 * WHY POSTGRES BEATS THE STANDARD HERE
 * ------------------------------------
 * The SQL standard allows phantoms at REPEATABLE READ; that permission exists for lock-based
 * implementations. Postgres implements REPEATABLE READ as SNAPSHOT ISOLATION, and a snapshot is a
 * view of the ENTIRE database at one instant — rows committed after it are invisible whether they
 * are updates or inserts. So a Postgres REPEATABLE READ transaction sees no phantoms at all.
 *
 * That is *read* phantoms only. A transaction can still make a decision based on the ABSENCE of
 * rows it cannot see and then write a conflicting row — that is write skew, anomaly A5 below, and
 * it is the one thing REPEATABLE READ does not stop.
 */
fun phantomReadDemo() {
    section("A3 — PHANTOM READ: allowed at READ COMMITTED, prevented at REPEATABLE READ (Postgres is stricter than the standard)")

    for (level in listOf(Level.READ_COMMITTED, Level.REPEATABLE_READ)) {
        resetSchema()
        scenario("reader runs at ${level.sql}")

        session("T1", level) { t1 ->
            session("T2", Level.READ_COMMITTED) { t2 ->
                t1.begin()
                val first = t1.read("rows with balance >= 100", "SELECT count(*) FROM accounts WHERE balance >= 100")

                // A new row appears that MATCHES T1's predicate.
                t2.begin()
                t2.exec("INSERT INTO accounts VALUES (3, 'carol', 500)")
                t2.commit()

                val second = t1.read("rows with balance >= 100", "SELECT count(*) FROM accounts WHERE balance >= 100")
                val owners = t1.queryAll("SELECT owner FROM accounts WHERE balance >= 100 ORDER BY id")
                t1.log("owners seen by T1: $owners")
                t1.commit()

                verdict(
                    anomalyHappened = first != second,
                    description = "count went $first -> $second; the row really is committed " +
                            "(a fresh connection counts ${observe("SELECT count(*) FROM accounts WHERE balance >= 100")}).",
                )
            }
        }
    }
}
