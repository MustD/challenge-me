/*
 * Terminal, process, filesystem and HTTP helpers — everything the update passes need from the
 * outside world.
 *
 * On Kotlin/Native "the outside world" means libc: there is no ProcessBuilder, no
 * java.nio.file and no HTTP client. Processes go through posix `system`/`popen`, files through
 * kotlinx-io, and HTTP through a `curl` subprocess.
 */

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import platform.posix.fgets
import platform.posix.getenv
import platform.posix.pclose
import platform.posix.popen
import platform.posix.system
import kotlin.time.Duration.Companion.seconds

// ------------------------------------------------------------------------------------------
// Terminal helpers.
// ------------------------------------------------------------------------------------------

const val ESC = ""
const val RESET = "$ESC[0m"

fun color(code: String, msg: String) = "$ESC[${code}m$msg$RESET"

fun bold(msg: String) = println("\n" + color("1", msg))
fun dim(msg: String) = println(color("2", msg))
fun warn(msg: String) = println(color("33", msg))
fun updated(msg: String) = println(color("32", msg))

fun row(label: String, value: String) = println("  ${label.padEnd(22)} $value")

// ------------------------------------------------------------------------------------------
// Filesystem helpers — kotlinx-io in place of kotlin.io.path.
// ------------------------------------------------------------------------------------------

operator fun Path.div(child: String) = Path(this, child)

fun Path.exists() = SystemFileSystem.exists(this)

fun Path.readText() = SystemFileSystem.source(this).buffered().use { it.readString() }

fun Path.writeText(text: String) = SystemFileSystem.sink(this).buffered().use { it.writeString(text) }

// ------------------------------------------------------------------------------------------
// Process helpers.
// ------------------------------------------------------------------------------------------

private const val BUFFER_BYTES = 8 * 1024

/**
 * `system`/`popen` hand back a wait(2) status, not an exit code: the code is the second byte.
 * 127 is what a shell reports for "command not found", which is also how a failed spawn
 * (`system` returning -1) is reported here — the JVM script mapped an IOException to the same.
 */
private fun exitCode(status: Int) = if (status == -1) 127 else (status shr 8) and 0xFF

/** `cd`s into [root] first: unlike ProcessBuilder, libc has no per-process working directory. */
private fun inDir(root: Path, command: String) = "cd '$root' && $command"

/** Runs a command with its output attached to this terminal; returns the exit code (127 if missing). */
fun exec(root: Path, command: String): Int = exitCode(system(inDir(root, command)))

/** Runs a command and returns its merged stdout+stderr; empty when the binary is not installed. */
fun capture(root: Path, command: String): String = run(inDir(root, command)).second

/** Merged stdout+stderr *and* the exit code; the code only matters to [fetch]. */
private fun run(command: String): Pair<Int, String> {
    val pipe = popen("$command 2>&1", "r") ?: return 127 to ""
    val output = buildString {
        memScoped {
            val buffer = allocArray<ByteVar>(BUFFER_BYTES)
            while (fgets(buffer, BUFFER_BYTES, pipe) != null) append(buffer.toKString())
        }
    }
    return exitCode(pclose(pipe)) to output.trim()
}

/** `getenv`, as a nullable Kotlin String. */
fun env(name: String): String? = getenv(name)?.toKString()

// ------------------------------------------------------------------------------------------
// HTTP.
// ------------------------------------------------------------------------------------------

val CONNECT_TIMEOUT = 10.seconds
val READ_TIMEOUT = 30.seconds

/**
 * Blocking GET; `null` on any transport error, non-OK status, or missing `curl`.
 *
 * Kotlin/Native ships no HTTP client (the JVM script used `URLConnection`), and the only real
 * alternative — a Ktor client engine — would add a libcurl/OpenSSL link dependency for three
 * one-shot GETs. Spawning `curl` costs a process and stays honest about what it is. `-f` turns
 * a 404 into a non-zero exit, which is what the old IOException-on-404 amounted to.
 */
fun fetch(url: String): String? {
    val curl = "curl -fsS" +
            " --connect-timeout ${CONNECT_TIMEOUT.inWholeSeconds}" +
            " --max-time ${READ_TIMEOUT.inWholeSeconds}" +
            " '$url'"
    val (code, body) = run(curl)
    return body.takeIf { code == 0 }
}
