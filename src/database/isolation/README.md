# Transaction isolation — worked examples on PostgreSQL

Runnable demonstrations of the classic concurrency anomalies, each one interleaved statement by statement across two
real connections so you can watch the anomaly appear and then disappear when the isolation level changes.

## Run it

```bash
mise run db-up      # docker compose up -d --wait   (Postgres 17 on fixed host port 5433)
mise run isolation  # ./kotlin run --main-class org.example.database.isolation.IsolationDemosKt
mise run db-down    # docker compose down          (add -v by hand to wipe the volume)
```

`docker compose logs -f postgres` shows the same run from the server's side — the compose file turns on
`log_statement=all` and `log_lock_waits`, so every statement appears with its backend PID.

Connection settings live in `Db.kt` and can be overridden with `ISOLATION_JDBC_URL`,
`ISOLATION_DB_USER`, `ISOLATION_DB_PASSWORD` if you want to point the demos at another server.

## The files

| File                               | Anomaly                  | Shown at                                                         | Prevented by                                             |
|------------------------------------|--------------------------|------------------------------------------------------------------|----------------------------------------------------------|
| `Db.kt`                            | —                        | the shared harness: `Session`, `Level`, `retrying`, schema reset |                                                          |
| `A1DirtyRead.kt`                   | dirty read (P1)          | nowhere — MVCC makes it impossible                               | (nothing to fix)                                         |
| `A2NonRepeatableRead.kt`           | non-repeatable read (P2) | READ COMMITTED                                                   | REPEATABLE READ                                          |
| `A3PhantomRead.kt`                 | phantom read (P3)        | READ COMMITTED                                                   | REPEATABLE READ (Postgres is stricter than the standard) |
| `A4LostUpdate.kt`                  | lost update (P4)         | READ COMMITTED                                                   | `SELECT … FOR UPDATE`, or REPEATABLE READ + retry        |
| `A5WriteSkew.kt`                   | write skew (A5B)         | REPEATABLE READ                                                  | SERIALIZABLE + retry                                     |
| `A6RetryOnSerializationFailure.kt` | —                        | how to retry a 40001 correctly                                   |                                                          |
| `IsolationDemos.kt`                | —                        | `main`, plus guidance on choosing a level                        |                                                          |

The theory for each anomaly is in the KDoc at the top of its file; this table is only the index.

## The one-paragraph summary

PostgreSQL implements isolation with MVCC: every transaction reads from a **snapshot** of committed data, so readers
never block writers and writers never block readers. READ COMMITTED takes a fresh snapshot per **statement**; REPEATABLE
READ takes one per **transaction** (this is Snapshot Isolation, which also rules out phantoms); SERIALIZABLE adds SSI,
which tracks read/write dependencies and aborts transactions whose combined effect no serial order could have produced.
READ UNCOMMITTED exists only as a spelling of READ COMMITTED. The stricter levels never block extra — they **fail**
extra, with `SQLSTATE 40001`, which is why a retry loop is mandatory above READ COMMITTED.
