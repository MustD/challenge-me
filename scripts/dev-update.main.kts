#!/usr/bin/env kotlin

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.11.0")

/*
 * `mise run dev-update` — one pass over everything in this repo that carries a pinned version.
 *
 *     mise run dev-update                  # update everything the script owns
 *     mise run dev-update -- --dry-run     # list what would change, write nothing
 *
 *   1. Kotlin Toolchain  — `./kotlin update` rewrites the `kotlin` / `kotlin.bat` wrappers
 *                          (version + SHA256 of the distribution they provision).
 *   2. Dependencies      — the Kotlin Toolchain has no built-in "check for newer versions"
 *                          command (see `./kotlin --help`), so we read the coordinates out of
 *                          module.yaml and ask Maven Central's maven-metadata.xml directly.
 *                          module.yaml is rewritten in place; when the Kotlin version moves, the
 *                          `[tools].kotlin` pin in mise.toml moves with it, so the compiler that
 *                          runs this script stays on the same release as the module.
 *   3. Tools             — everything else that is pinned: mise-managed tools, the mise binary
 *                          itself, the JDK, the Postgres image used by the isolation demos.
 *                          Report-only, always: none of those are this script's to rewrite.
 *
 * HOW THIS IS RUN
 * ---------------
 * By the standalone Kotlin compiler (`kotlin` / `kotlinc`), pinned as `[tools].kotlin` in
 * mise.toml — NOT by the Kotlin Toolchain (`./kotlin`) that builds the module. The Toolchain
 * cannot run a `.kts` from inside a project: `kotlin run <script>.main.kts` silently ignores the
 * path and runs the module's `Main.kt` instead. Keeping this a script is the point — a dev tool
 * has no business in `src/`, which is otherwise only the isolation lessons and Hello World.
 *
 * A script is also why everything below is ordered "values, then functions, then the driver at
 * the very bottom": a `.kts` body executes top-to-bottom, so a top-level `val` must be declared
 * before the first line that reads it.
 *
 * The `@file:DependsOn` above is resolved from Maven Central by the `.main.kts` script host on
 * the first run and cached afterwards; the mise task quiets the resolver's logging via JAVA_OPTS.
 *
 * Kotlin over Java wherever the stdlib has an equivalent: `kotlin.io.path` (`Path`, `/`,
 * `readText`) rather than `Path.of`/`Files`, `kotlin.time.Duration` rather than
 * `java.time.Duration`, and coroutines rather than an `Executors` pool of virtual threads.
 * Two things have no Kotlin counterpart and stay on the JDK: `ProcessBuilder`, and `java.net`
 * for HTTP — the Kotlin stdlib ships no HTTP client, so the network calls are plain blocking
 * `URLConnection` reads pushed onto `Dispatchers.IO`.
 */

import kotlinx.coroutines.*
import java.io.IOException
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

// ------------------------------------------------------------------------------------------
// Terminal helpers.
// ------------------------------------------------------------------------------------------

val RESET = "\u001B[0m"

fun color(code: String, msg: String) = "\u001B[${code}m$msg$RESET"

fun bold(msg: String) = println("\n" + color("1", msg))
fun dim(msg: String) = println(color("2", msg))
fun warn(msg: String) = println(color("33", msg))
fun updated(msg: String) = println(color("32", msg))

val USAGE = """
    Usage: mise run dev-update [-- --dry-run]

      -n, --dry-run      only list the available updates; write nothing
      -h, --help         show this message

    With no flags the script updates everything it owns: the ./kotlin wrappers, the pinned
    versions in module.yaml, and the matching [tools].kotlin pin in mise.toml.
""".trimIndent()

// ------------------------------------------------------------------------------------------
// Process helpers
// ------------------------------------------------------------------------------------------

/** Runs a command with its output attached to this terminal; returns the exit code (127 if missing). */
fun exec(root: Path, vararg command: String): Int = try {
    ProcessBuilder(*command).directory(root.toFile()).inheritIO().start().waitFor()
} catch (_: IOException) {
    127
}

/** Runs a command and returns its merged stdout+stderr; empty when the binary is not installed. */
fun capture(root: Path, vararg command: String): String = try {
    val process = ProcessBuilder(*command).directory(root.toFile()).redirectErrorStream(true).start()
    process.inputStream.bufferedReader().use { it.readText() }.also { process.waitFor() }.trim()
} catch (_: IOException) {
    ""
}

/**
 * The repo root: `MISE_PROJECT_ROOT` when launched through mise, otherwise the nearest ancestor
 * of the working directory that holds a `module.yaml`.
 */
fun repoRoot(): Path {
    System.getenv("MISE_PROJECT_ROOT")?.let { return Path(it) }
    var dir: Path? = Path("").toAbsolutePath()
    while (dir != null) {
        if ((dir / "module.yaml").exists()) return dir
        dir = dir.parent
    }
    System.err.println("cannot locate the repo root (no module.yaml found above the working directory)")
    exitProcess(2)
}

// ------------------------------------------------------------------------------------------
// 1. Kotlin Toolchain
// ------------------------------------------------------------------------------------------

/** The `kotlin_cli_version=` line the `./kotlin` wrapper pins itself to. */
fun cliVersion(root: Path): String =
    (root / "kotlin").readText()
        .lineSequence()
        .firstOrNull { it.startsWith("kotlin_cli_version=") }
        ?.substringAfter('=')
        .orEmpty()

fun updateToolchain(root: Path, dryRun: Boolean) {
    bold("1/3  Kotlin Toolchain")
    val current = cliVersion(root)
    println("current: $current")

    // `./kotlin update` has no check-only mode: it either rewrites the wrappers or does nothing,
    // and it never reports what it would have done — so under --dry-run we must not run it.
    if (dryRun) return dim("skipped (--dry-run) — './kotlin update' cannot report without rewriting")

    if (exec(root, "./kotlin", "update") != 0) {
        warn("kotlin update failed — leaving the wrappers untouched")
    }

    val new = cliVersion(root)
    if (new == current) return println("already on the latest release")

    updated("updated: $current -> $new")
    warn("NOTE: 'kotlin update' regenerates BOTH wrappers from the upstream templates, so any")
    warn("local patch is lost — in this repo that is the kotlin.bat fix for the 'Terminate batch")
    warn("job (Y/N)?' prompt (commit bd46be3). Check 'git diff kotlin.bat' and re-apply it.")
}

// ------------------------------------------------------------------------------------------
// 2. Dependencies declared in module.yaml
// ------------------------------------------------------------------------------------------

/**
 * One pinned Maven coordinate found in module.yaml.
 *
 * [pattern] is the exact text to swap when rewriting; it carries the literal `VERSION`
 * placeholder so the same field works for a coordinate (`group:artifact:VERSION`) and for a
 * settings key (`junitPlatformVersion: VERSION`).
 */
data class Pin(val group: String, val artifact: String, val current: String, val pattern: String) {
    val coordinate get() = "$group:$artifact"
}

/** `- group:artifact:version` entries under `dependencies:` / `test-dependencies:`. */
val COORDINATE = Regex("""^\s*-\s+([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):([0-9][A-Za-z0-9_.+-]*)""")

val VERSION_TAG = Regex("""<version>([^<]*)</version>""")
val PRERELEASE = Regex("""alpha|beta|rc|-m[0-9]|eap|dev|snapshot|pre|cr[0-9]""", RegexOption.IGNORE_CASE)

val CONNECT_TIMEOUT = 10.seconds
val READ_TIMEOUT = 30.seconds

/** The artifact whose version is mirrored by `[tools].kotlin` in mise.toml. */
val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib"

/**
 * `sort -V` semantics: compare dot/dash-separated parts, numerically where both sides are
 * numeric and lexicographically otherwise, with the shorter version losing on a common prefix.
 * Needed because plain string ordering puts `42.7.9` above `42.7.13`.
 */
val VERSION_ORDER = Comparator<String> { a, b ->
    val left = a.split('.', '-', '_', '+')
    val right = b.split('.', '-', '_', '+')
    for (i in 0 until maxOf(left.size, right.size)) {
        val x = left.getOrNull(i) ?: return@Comparator -1
        val y = right.getOrNull(i) ?: return@Comparator 1
        val cmp = x.toLongOrNull()?.let { xn -> y.toLongOrNull()?.let { yn -> xn.compareTo(yn) } }
            ?: x.compareTo(y)
        if (cmp != 0) return@Comparator cmp
    }
    0
}

/** Versions that live in `settings:` rather than in a coordinate. */
fun settingsPin(yaml: String, key: String, group: String, artifact: String): Pin? {
    // `[^ #\n]` and not `[^ #]`: in MULTILINE mode a negated class still matches the line break,
    // so `[^ #]*` would swallow the newline and compare `2.4.10\n` against `2.4.10`.
    val version = Regex("""^\s*$key:\s*([0-9][^ #\n]*)""", RegexOption.MULTILINE)
        .find(yaml)?.groupValues?.get(1) ?: return null
    return Pin(group, artifact, version, "$key: VERSION")
}

/** Every pinned version declared in module.yaml, in the order they are reported. */
fun pinsIn(yaml: String): List<Pin> = buildList {
    yaml.lineSequence().forEach { line ->
        COORDINATE.find(line)?.destructured?.let { (group, artifact, version) ->
            add(Pin(group, artifact, version, "$group:$artifact:VERSION"))
        }
    }
    // `version:` matches the Kotlin version first — the JDK's `version: 25` sits further down.
    settingsPin(yaml, "version", "org.jetbrains.kotlin", "kotlin-stdlib")?.let(::add)
    settingsPin(yaml, "junitPlatformVersion", "org.junit.platform", "junit-platform-launcher")?.let(::add)
}

/** Blocking GET; `null` on any transport error or non-OK status (a 404 arrives as an IOException). */
fun fetch(url: String): String? = try {
    URI(url).toURL().openConnection().run {
        connectTimeout = CONNECT_TIMEOUT.inWholeMilliseconds.toInt()
        readTimeout = READ_TIMEOUT.inWholeMilliseconds.toInt()
        getInputStream().bufferedReader().use { it.readText() }
    }
} catch (_: IOException) {
    null
}

/** Latest non-prerelease version of a Maven coordinate, or `null` if it cannot be resolved. */
fun latestStable(pin: Pin): String? {
    val group = pin.group.replace('.', '/')
    val body = fetch("https://repo1.maven.org/maven2/$group/${pin.artifact}/maven-metadata.xml") ?: return null
    return VERSION_TAG.findAll(body)
        .map { it.groupValues[1] }
        .filterNot { PRERELEASE.containsMatchIn(it) }
        .maxWithOrNull(VERSION_ORDER)
}

/**
 * One Maven Central request per pin, all in flight at once, so the whole pass costs about as
 * much as its slowest single request; results come back in [pins] order.
 *
 * Returned as a [Deferred] rather than awaited here so the caller can fire the requests off and
 * then get on with the toolchain update while they are still in flight. The requests are
 * blocking socket reads, hence `Dispatchers.IO` — the children inherit that dispatcher.
 */
fun CoroutineScope.latestStableAsync(pins: List<Pin>): Deferred<List<String?>> =
    async(Dispatchers.IO) { pins.map { pin -> async { latestStable(pin) } }.awaitAll() }

/**
 * Mirrors a new Kotlin version into `[tools].kotlin` in mise.toml, which pins the standalone
 * compiler that runs this very script; CLAUDE.md requires the two to stay matched.
 */
fun syncMiseKotlin(root: Path, from: String, to: String) {
    val miseToml = root / "mise.toml"
    val text = miseToml.readText()
    val line = Regex("""^kotlin = \{.*$""", RegexOption.MULTILINE).find(text)?.value
    if (line == null || !line.contains("\"$from\"")) {
        return warn("  !  mise.toml [tools].kotlin is not on $from — bump it by hand to $to")
    }
    miseToml.writeText(text.replace(line, line.replace("\"$from\"", "\"$to\"")))
    updated("  ^  mise.toml [tools].kotlin  $from -> $to")
}

fun updateDependencies(root: Path, pins: List<Pin>, latest: List<String?>, dryRun: Boolean) {
    bold("2/3  Dependencies (module.yaml vs Maven Central)")

    val moduleYaml = root / "module.yaml"
    var yaml = moduleYaml.readText()
    var kotlinBump: Pair<String, String>? = null

    var outdated = 0
    pins.forEachIndexed { i, pin ->
        val newest = latest[i]
        when {
            newest == null -> warn("  ?  ${pin.coordinate}  ${pin.current}  (could not reach Maven Central)")
            newest == pin.current -> println("  ok ${pin.coordinate}  ${pin.current}")
            else -> {
                outdated++
                val arrow = "${pin.current} -> $newest"
                updated(if (dryRun) "  ^  ${pin.coordinate}  $arrow  (dry run)" else "  ^  ${pin.coordinate}  $arrow")
                yaml = yaml.replace(
                    pin.pattern.replace("VERSION", pin.current),
                    pin.pattern.replace("VERSION", newest),
                )
                if (pin.coordinate == KOTLIN_STDLIB) kotlinBump = pin.current to newest
            }
        }
    }

    when {
        outdated == 0 -> println("  all declared dependencies are on their latest stable release")
        dryRun -> dim("  dry run; drop --dry-run to rewrite module.yaml")
        else -> {
            // One write from the in-memory string, rather than a `sed -i` per coordinate.
            moduleYaml.writeText(yaml)
            updated("  module.yaml rewritten — run 'mise run test' before committing")
            kotlinBump?.let { (from, to) -> syncMiseKotlin(root, from, to) }
        }
    }
    dim("  reminder: junit-jupiter and settings.jvm.test.junitPlatformVersion must stay aligned")
}

// ------------------------------------------------------------------------------------------
// 3. Other pinned tools — report only; these are not this script's to rewrite.
// ------------------------------------------------------------------------------------------

fun row(label: String, value: String) = println("  ${label.padEnd(22)} $value")

suspend fun reportTools(root: Path) = coroutineScope {
    // Both are blocking process spawns; `mise outdated` is the slow one — run them together.
    val outdatedTools = async(Dispatchers.IO) { capture(root, "mise", "outdated") }
    val miseVersion = async(Dispatchers.IO) { capture(root, "mise", "--version") }

    bold("3/3  Tools")

    println("mise-managed tools:")
    outdatedTools.await().lines().forEach { if (it.isNotBlank()) println("  $it") }
    dim("  bump with 'mise upgrade' (respects mise.toml) or by editing [tools] in mise.toml")

    val yaml = (root / "module.yaml").readText()
    // The LAST `version:` in module.yaml is the JDK's; the first is Kotlin's (see pinsIn).
    val jdk = Regex("""^\s*version:\s*([0-9]+)\s*$""", RegexOption.MULTILINE)
        .findAll(yaml).lastOrNull()?.groupValues?.get(1) ?: "?"
    val image = Regex("""^\s*image:\s*(.+)$""", RegexOption.MULTILINE)
        .find((root / "docker-compose.yml").readText())?.groupValues?.get(1)?.trim() ?: "?"
    val mise = miseVersion.await()

    println()
    println("other pinned versions in this repo:")
    row("kotlin wrapper", "${cliVersion(root)} (kotlin, kotlin.bat)")
    row("JDK (module.yaml)", "$jdk — keep in sync with [tools].java in mise.toml")
    row("postgres image", "$image (docker-compose.yml; refresh with 'docker compose pull')")
    row("mise itself", mise.lineSequence().firstOrNull().orEmpty())
    mise.lineSequence().filter { it.contains("available", ignoreCase = true) }.forEach { println("  $it") }
}

// ------------------------------------------------------------------------------------------
// Driver — must stay last: a .kts body runs top-to-bottom.
// ------------------------------------------------------------------------------------------

var dryRun = false
var help = false

for (arg in args) when (arg) {
    "-n", "--dry-run" -> dryRun = true
    "-h", "--help" -> help = true
    else -> {
        System.err.println("unknown option: $arg")
        System.err.println(USAGE)
        exitProcess(2)
    }
}

if (help) {
    println(USAGE)
} else runBlocking {
    val root = repoRoot()
    val pins = pinsIn((root / "module.yaml").readText())
    // Fire every Maven Central lookup first: they travel while './kotlin update' downloads.
    val latest = latestStableAsync(pins)

    updateToolchain(root, dryRun)
    updateDependencies(root, pins, latest.await(), dryRun)
    reportTools(root)
    bold(if (dryRun) "Done (dry run — nothing was written)." else "Done.")
}
