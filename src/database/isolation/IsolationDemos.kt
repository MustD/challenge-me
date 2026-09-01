package database.isolation

import java.sql.DriverManager
import java.sql.SQLException
import kotlin.system.exitProcess

/**
 * Entry point — runs every isolation demo in this package against the Postgres started by
 * `docker-compose.yml` at the repo root.
 *
 *     docker compose up -d --wait                       # or: mise run db-up
 *     mise run isolation                                # or: ./kotlin run --main-class org.example.database.isolation.IsolationDemosKt
 *
 * Each demo recreates its own tables, so they can be run in any order and repeated freely.
 *
 * READING THE OUTPUT
 * ------------------
 * Every line is prefixed with the transaction that issued it, in the order the statements really
 * reached the server. `docker compose logs -f postgres` shows the same story from the server's
 * side (the compose file enables `log_statement=all`), with one backend PID per session.
 *
 * PICKING A LEVEL IN REAL LIFE
 * ----------------------------
 *   READ COMMITTED  — the default, and the right answer for most CRUD. Safe for blind writes
 *                     (`SET x = x + 1`) and single-statement reads; NOT safe for read-then-write or
 *                     for multi-statement reads that must agree with each other.
 *   REPEATABLE READ — reports, exports, "read a consistent view of several tables", and anything
 *                     that must not see data shift under it. Also stops lost updates. Needs retries.
 *   SERIALIZABLE    — invariants that span rows the transaction does not write (write skew): "at
 *                     least one on call", "no overlapping bookings", "stock never negative". Needs
 *                     retries, and only helps if ALL transactions on that data use it.
 *
 * And the escape hatches that do not need a stronger level at all: do the arithmetic in SQL,
 * `SELECT ... FOR UPDATE` the rows you are about to base a decision on, add a `version` column, or
 * push the invariant into a CONSTRAINT (unique / exclusion / check) so the database enforces it.
 */
fun main() {
    requireDatabase()

    dirtyReadDemo()               // A1 — impossible under MVCC
    nonRepeatableReadDemo()       // A2 — READ COMMITTED vs REPEATABLE READ
    phantomReadDemo()             // A3 — snapshot isolation beats the standard
    lostUpdateDemo()              // A4 — read-modify-write, FOR UPDATE, 40001
    writeSkewDemo()               // A5 — the anomaly only SERIALIZABLE stops
    retryOnSerializationFailureDemo()  // A6 — the retry loop those levels require

    section("done — see the KDoc at the top of each A*.kt file for the theory behind that anomaly")
}

/** Fails fast with a useful message instead of a stack trace when the container is not running. */
private fun requireDatabase() {
    try {
        DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD).use { c ->
            val version = c.createStatement().use { st ->
                st.executeQuery("SHOW server_version").use { rs -> if (rs.next()) rs.getString(1) else "?" }
            }
            val default = c.createStatement().use { st ->
                st.executeQuery("SHOW default_transaction_isolation")
                    .use { rs -> if (rs.next()) rs.getString(1) else "?" }
            }
            println("Connected to PostgreSQL $version at ${JDBC_URL} (default_transaction_isolation = $default)")
        }
    } catch (e: SQLException) {
        System.err.println(
            """
            Cannot connect to ${JDBC_URL} — ${e.message?.lineSequence()?.first()}

            Start the database first:
                docker compose up -d --wait        (or: mise run db-up)

            Override the target with ISOLATION_JDBC_URL / ISOLATION_DB_USER / ISOLATION_DB_PASSWORD.
            """.trimIndent(),
        )
        exitProcess(1)
    }
}
