package database.isolation

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Tiny harness shared by every isolation demo in this package.
 *
 * The demos need something that a connection POOL deliberately hides from you: two (or more)
 * connections whose transactions you can interleave STATEMENT BY STATEMENT. Isolation anomalies
 * only exist in the gaps between statements of concurrent transactions, so the whole point here is
 * to open the gaps manually and look inside them.
 *
 * Everything below is plain JDBC on purpose — no pool, no ORM, no coroutines. One [Session] ==
 * one physical connection == one backend process in Postgres == at most one transaction at a time.
 */

// --------------------------------------------------------------------------------------------
// Connection settings — matched to docker-compose.yml at the repo root (fixed host port 5433).
// --------------------------------------------------------------------------------------------

val JDBC_URL: String = System.getenv("ISOLATION_JDBC_URL") ?: "jdbc:postgresql://localhost:5433/isolation"
val DB_USER: String = System.getenv("ISOLATION_DB_USER") ?: "demo"
val DB_PASSWORD: String = System.getenv("ISOLATION_DB_PASSWORD") ?: "demo"

/**
 * The four SQL-standard isolation levels, plus what PostgreSQL actually does with each one.
 *
 * The standard defines levels by which ANOMALIES they forbid; Postgres implements them with MVCC
 * (Multi-Version Concurrency Control) — readers never block writers and writers never block
 * readers, because every transaction reads from a SNAPSHOT of committed data. That implementation
 * happens to be STRICTER than the standard requires, which is why two of the four levels collapse:
 *
 *   READ UNCOMMITTED -> silently behaves as READ COMMITTED. Postgres has no way to read a row
 *                       version that was never committed, so dirty reads are simply impossible.
 *   READ COMMITTED   -> the DEFAULT. A fresh snapshot is taken at the start of EVERY statement.
 *   REPEATABLE READ  -> one snapshot for the WHOLE transaction (this is Snapshot Isolation). It
 *                       therefore also blocks phantoms, which the standard allows at this level.
 *   SERIALIZABLE     -> Snapshot Isolation + SSI (Serializable Snapshot Isolation): Postgres tracks
 *                       read/write dependencies between transactions and aborts one of them if the
 *                       result could not have been produced by running them one after another.
 *
 * Anomaly matrix as implemented by PostgreSQL (✗ = cannot happen, ✓ = can happen):
 *
 *   anomaly              | READ UNCOMMITTED | READ COMMITTED | REPEATABLE READ | SERIALIZABLE
 *   ---------------------+------------------+----------------+-----------------+-------------
 *   dirty read           |        ✗         |       ✗        |        ✗        |      ✗
 *   non-repeatable read  |        ✓         |       ✓        |        ✗        |      ✗
 *   phantom read         |        ✓         |       ✓        |        ✗        |      ✗
 *   lost update          |        ✓         |       ✓        |     ✗ (40001)   |   ✗ (40001)
 *   write skew           |        ✓         |       ✓        |        ✓        |   ✗ (40001)
 *
 * The price of the two strict levels is SERIALIZATION FAILURES (SQLSTATE 40001): instead of
 * blocking or corrupting data, Postgres aborts a transaction and expects the APPLICATION to retry
 * it. Any code that runs at REPEATABLE READ or SERIALIZABLE must therefore be wrapped in a retry
 * loop — see [retrying].
 */
enum class Level(val jdbc: Int, val sql: String) {
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED, "READ UNCOMMITTED"),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED, "READ COMMITTED"),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ, "REPEATABLE READ"),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE, "SERIALIZABLE"),
}

/** SQLSTATE 40001 — "could not serialize access…". The retryable one. */
const val SQLSTATE_SERIALIZATION_FAILURE = "40001"

/** SQLSTATE 40P01 — deadlock detected; Postgres already rolled one victim back. Also retryable. */
const val SQLSTATE_DEADLOCK_DETECTED = "40P01"

/**
 * One named connection with its own transaction, plus narration.
 *
 * `autoCommit = false` is what makes a transaction explicit in JDBC: the driver issues `BEGIN`
 * lazily before the first statement and nothing is durable until [commit]. Note the ordering rule —
 * the isolation level must be set BEFORE the transaction starts; changing it mid-transaction is an
 * error in Postgres ("SET TRANSACTION ISOLATION LEVEL must be called before any query").
 */
class Session(val name: String, val level: Level) : AutoCloseable {

    val connection: Connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD).apply {
        autoCommit = false
        transactionIsolation = level.jdbc
    }

    /**
     * What the server reports for this transaction. Careful — it is NOT proof of behaviour: ask for
     * READ UNCOMMITTED and Postgres echoes "read uncommitted" back while actually running you at
     * READ COMMITTED. Only an experiment (see A1) shows which semantics you really got.
     */
    fun effectiveLevel(): String = queryOne("SHOW transaction_isolation") ?: "?"

    fun log(message: String) = println("   %-6s | %s".format(name, message))

    /** Runs a statement that changes rows; returns the affected row count. */
    fun exec(sql: String): Int = connection.createStatement().use { st ->
        val rows = st.executeUpdate(sql)
        log("$sql   -> $rows row(s)")
        rows
    }

    /** First column of the first row, as text (null when the query returned nothing). */
    fun queryOne(sql: String): String? = connection.createStatement().use { st ->
        st.executeQuery(sql).use { rs -> if (rs.next()) rs.getString(1) else null }
    }

    /** First column of every row, as text — handy for showing a whole result set changing shape. */
    fun queryAll(sql: String): List<String> = connection.createStatement().use { st ->
        st.executeQuery(sql).use { rs -> buildList { while (rs.next()) add(rs.getString(1)) } }
    }

    /** [queryOne] + narration, so the demo transcript shows what the transaction observed. */
    fun read(label: String, sql: String): String? = queryOne(sql).also { log("$label = $it        ($sql)") }

    fun begin() = log("BEGIN  (asked for ${level.sql}, server reports ${effectiveLevel()})")
    fun commit() {
        connection.commit(); log("COMMIT")
    }

    fun rollback() {
        connection.rollback(); log("ROLLBACK")
    }

    override fun close() {
        // Roll back anything still open so a failed demo cannot leave locks behind for the next one.
        runCatching { if (!connection.isClosed) connection.rollback() }
        runCatching { connection.close() }
    }
}

/** Opens a session, runs [body], and always closes the connection. */
inline fun <T> session(name: String, level: Level, body: (Session) -> T): T =
    Session(name, level).use(body)

/**
 * Runs [block] expecting the server to REJECT it, and narrates whichever way it goes.
 *
 * Serialization failures are the normal, healthy outcome at the strict levels — this helper keeps
 * the demos honest by making it obvious when the error did *not* happen.
 */
inline fun Session.expectingError(what: String, block: () -> Unit) {
    try {
        block()
        log("!! $what SUCCEEDED — no error was raised")
    } catch (e: SQLException) {
        val reason = when (e.sqlState) {
            SQLSTATE_SERIALIZATION_FAILURE -> "serialization failure — Postgres refused to allow the anomaly"
            SQLSTATE_DEADLOCK_DETECTED -> "deadlock detected — this transaction was chosen as the victim"
            else -> "SQL error"
        }
        log("!! $what REJECTED [SQLSTATE ${e.sqlState}] $reason")
        log("   ${e.message?.lineSequence()?.first()}")
        // After ANY error the transaction is in an aborted state: every further statement fails
        // with 25P02 until it is rolled back. Rolling back here is what a retry loop would do.
        rollback()
    }
}

/**
 * The retry loop every REPEATABLE READ / SERIALIZABLE workload needs.
 *
 * A serialization failure is not a bug and not something to "handle" — it is the database telling
 * you "run this again and you'll get a consistent result". The transaction must be retried from the
 * very beginning (a fresh snapshot), which is why [body] receives a brand-new [Session].
 */
fun <T> retrying(name: String, level: Level, attempts: Int = 5, body: (Session) -> T): T {
    repeat(attempts) { attempt ->
        Session("$name#${attempt + 1}", level).use { s ->
            try {
                val result = body(s)
                s.commit()
                return result
            } catch (e: SQLException) {
                if (e.sqlState != SQLSTATE_SERIALIZATION_FAILURE && e.sqlState != SQLSTATE_DEADLOCK_DETECTED) throw e
                s.log("retryable [${e.sqlState}] — attempt ${attempt + 1} of $attempts, starting over")
                runCatching { s.rollback() }
            }
        }
    }
    error("$name still failing after $attempts attempts")
}

// --------------------------------------------------------------------------------------------
// Background execution — needed only where a statement is expected to BLOCK on a row lock.
// --------------------------------------------------------------------------------------------

private val pool = Executors.newCachedThreadPool { r -> Thread(r, "isolation-demo").apply { isDaemon = true } }

/** Runs [block] on another thread so the demo can observe a statement that blocks. */
fun background(block: () -> Unit): Future<*> = pool.submit(block)

/**
 * Waits [millis] and reports whether the background statement is STILL waiting.
 *
 * Blocking is itself the observation in the lock demos: "this UPDATE is parked on a row lock held
 * by the other transaction" is the lesson, so we assert it explicitly rather than just sleeping.
 */
fun Future<*>.stillBlockedAfter(millis: Long): Boolean {
    Thread.sleep(millis)
    return !isDone
}

fun Future<*>.awaitDone(seconds: Long = 10) = get(seconds, TimeUnit.SECONDS)

// --------------------------------------------------------------------------------------------
// Schema — recreated from scratch before every demo so each one starts from a known state.
// --------------------------------------------------------------------------------------------

/**
 * `accounts` drives the money-transfer anomalies, `doctors` drives the write-skew one.
 *
 * DDL runs in autocommit on its own connection: in Postgres DDL is transactional, and leaving an
 * uncommitted `CREATE TABLE` open would block every demo session on a table lock.
 */
fun resetSchema() {
    DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD).use { c ->
        c.autoCommit = true
        c.createStatement().use { st ->
            st.execute("DROP TABLE IF EXISTS accounts, doctors")
            st.execute("CREATE TABLE accounts (id int PRIMARY KEY, owner text NOT NULL, balance int NOT NULL)")
            st.execute("INSERT INTO accounts VALUES (1, 'alice', 100), (2, 'bob', 200)")
            st.execute("CREATE TABLE doctors (id int PRIMARY KEY, name text NOT NULL, on_call boolean NOT NULL)")
            st.execute("INSERT INTO doctors VALUES (1, 'alice', true), (2, 'bob', true)")
        }
    }
}

/** Reads a value on a throwaway autocommit connection — "what does the world see now?". */
fun observe(sql: String): String? =
    DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD).use { c ->
        c.createStatement().use { st -> st.executeQuery(sql).use { rs -> if (rs.next()) rs.getString(1) else null } }
    }

// --------------------------------------------------------------------------------------------
// Console formatting.
// --------------------------------------------------------------------------------------------

fun section(title: String) {
    println()
    println("=".repeat(100))
    println("  $title")
    println("=".repeat(100))
}

fun scenario(title: String) {
    println()
    println("-- $title ".padEnd(100, '-'))
}

fun note(text: String) = println("        $text")

fun verdict(anomalyHappened: Boolean, description: String) =
    println("   ${if (anomalyHappened) "ANOMALY  ->" else "PREVENTED ->"} $description")
