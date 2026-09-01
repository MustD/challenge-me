package database.isolation

/**
 * PATTERN — RETRYING A SERIALIZATION FAILURE (SQLSTATE 40001).
 *
 * WHY THIS FILE EXISTS
 * --------------------
 * REPEATABLE READ and SERIALIZABLE do not make anomalies impossible by magic; they convert them
 * into ERRORS. A system running at those levels is only correct if the application retries, so the
 * retry loop is part of the isolation strategy, not an afterthought.
 *
 * THE RULES OF A CORRECT RETRY
 * ----------------------------
 *   1. Retry the WHOLE transaction from BEGIN. A retry must take a NEW snapshot — that is the
 *      entire point. Re-issuing only the failed statement on the aborted transaction cannot work:
 *      after any error the transaction is poisoned and every statement fails with 25P02 until
 *      ROLLBACK.
 *   2. Re-read inside the retry, and re-decide. Below, attempt #2 sees a different world (one
 *      doctor already went off call) and correctly DECLINES to do the write it planned. Replaying a
 *      cached decision would simply reproduce the anomaly the abort prevented.
 *   3. Retry only 40001 (serialization failure) and 40P01 (deadlock victim). A unique-violation or
 *      a syntax error will fail identically forever.
 *   4. Keep side effects OUT of the transaction body — no emails, no HTTP calls, no in-memory
 *      counters. The body can and will run several times; only the database rolls back.
 *   5. Bound the attempts and back off. Under heavy conflict, unbounded retries turn a correctness
 *      mechanism into a livelock.
 *
 * See [retrying] in `Db.kt` for the implementation this demo uses.
 */
fun retryOnSerializationFailureDemo() {
    section("A6 — RETRY LOOP: the same write skew, resolved correctly by retrying at SERIALIZABLE")
    resetSchema()

    var attempt = 0
    val outcome = retrying("bob", Level.SERIALIZABLE) { bob ->
        attempt++
        bob.log("--- attempt $attempt: fresh transaction, fresh snapshot ---")
        val onCall = bob.read("doctors on call", "SELECT count(*) FROM doctors WHERE on_call")!!.toInt()

        if (attempt == 1) {
            // Alice slips in and commits AFTER bob's snapshot was taken. This is what makes
            // attempt #1 unserializable — bob's read is invalidated by alice's write.
            session("alice", Level.SERIALIZABLE) { alice ->
                alice.begin()
                alice.read("doctors on call", "SELECT count(*) FROM doctors WHERE on_call")
                alice.exec("UPDATE doctors SET on_call = false WHERE name = 'alice'")
                alice.commit()
            }
        }

        if (onCall > 1) {
            bob.exec("UPDATE doctors SET on_call = false WHERE name = 'bob'")
            "bob went off call"                       // attempt #1 gets this far, then COMMIT throws 40001
        } else {
            bob.log("only $onCall doctor on call — bob stays")
            "bob stayed on call"                      // attempt #2 re-decides on fresh data
        }
    }

    val onCall = observe("SELECT count(*) FROM doctors WHERE on_call")!!.toInt()
    println("   RESULT -> $outcome after $attempt attempt(s); $onCall doctor(s) on call — invariant held.")
    note("Attempt #1 was not 'wrong code' — it was a correct transaction that the server refused to serialize.")
    note("The retry is what turns an abort into a correct outcome, and it had to RE-READ to get there.")
}
