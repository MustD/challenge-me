/*
 * 2/3 — the versions pinned in the project's module.yaml files.
 *
 * The Kotlin Toolchain has no built-in "check for newer versions" command (see `./kotlin
 * --help`), so the coordinates are read out of each module.yaml and asked about directly at
 * Maven Central's maven-metadata.xml. Each module.yaml is rewritten in place; when the root
 * module's Kotlin version moves, the `[tools].kotlin` pin in mise.toml moves with it.
 *
 * Every module listed in project.yaml is covered, this update tool's own module included —
 * otherwise its dependencies would be the one corner of the repo that silently rots.
 */

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.io.files.Path

/**
 * One pinned Maven coordinate found in a module.yaml.
 *
 * [pattern] is the exact text to swap when rewriting; it carries the literal `VERSION`
 * placeholder so the same field works for a coordinate (`group:artifact:VERSION`) and for a
 * settings key (`junitPlatformVersion: VERSION`).
 */
data class Pin(val group: String, val artifact: String, val current: String, val pattern: String) {
    val coordinate get() = "$group:$artifact"
}

/** One module.yaml and everything pinned inside it. [label] is its path relative to the repo root. */
data class Module(val label: String, val file: Path, val pins: List<Pin>)

/** `- group:artifact:version` entries under `dependencies:` / `test-dependencies:`. */
val COORDINATE = Regex("""^\s*-\s+([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):([0-9][A-Za-z0-9_.+-]*)""")

/** `- some/module/dir` entries under `modules:` in project.yaml. */
val MODULE_ENTRY = Regex("""^\s*-\s+(\S+)\s*$""")

val VERSION_TAG = Regex("""<version>([^<]*)</version>""")
val PRERELEASE = Regex("""alpha|beta|rc|-m[0-9]|eap|dev|snapshot|pre|cr[0-9]""", RegexOption.IGNORE_CASE)

/** The artifact whose version is mirrored by `[tools].kotlin` in mise.toml. */
const val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib"

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

/** Every pinned version declared in one module.yaml, in the order they are reported. */
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

/** The root module plus every module listed under `modules:` in project.yaml. */
fun modulesIn(root: Path): List<Module> = buildList {
    fun module(label: String, dir: Path) {
        val file = dir / "module.yaml"
        if (file.exists()) add(Module(label, file, pinsIn(file.readText())))
    }
    module("module.yaml", root)
    val projectYaml = root / "project.yaml"
    if (!projectYaml.exists()) return@buildList
    projectYaml.readText().lineSequence()
        .mapNotNull { MODULE_ENTRY.find(it)?.groupValues?.get(1) }
        .forEach { path -> module("$path/module.yaml", Path(root, *path.split('/').toTypedArray())) }
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
 * then get on with the toolchain update while they are still in flight. Each request is a
 * blocking `curl` spawn, so it needs a real thread: `Dispatchers.Default` is the only
 * multi-threaded dispatcher Kotlin/Native exposes (`Dispatchers.IO` is `internal` there), which
 * caps the requests in flight at the CPU count rather than running literally all of them at
 * once — immaterial for a handful of pins. The children inherit the dispatcher.
 */
fun CoroutineScope.latestStableAsync(pins: List<Pin>): Deferred<List<String?>> =
    async(Dispatchers.Default) { pins.map { pin -> async { latestStable(pin) } }.awaitAll() }

fun updateDependencies(root: Path, modules: List<Module>, latest: List<String?>, dryRun: Boolean) {
    bold("2/3  Dependencies (module.yaml vs Maven Central)")

    var index = 0
    var outdated = 0
    modules.forEach { module ->
        println("${module.label}:")
        var yaml = module.file.readText()
        var kotlinBump: Pair<String, String>? = null
        var rewrites = 0

        module.pins.forEach { pin ->
            val newest = latest[index++]
            when {
                newest == null -> warn("  ?  ${pin.coordinate}  ${pin.current}  (could not reach Maven Central)")
                newest == pin.current -> println("  ok ${pin.coordinate}  ${pin.current}")
                else -> {
                    rewrites++
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

        outdated += rewrites
        if (rewrites > 0 && !dryRun) {
            // One write from the in-memory string, rather than a `sed -i` per coordinate.
            module.file.writeText(yaml)
            updated("  ${module.label} rewritten")
            // Only the root module's Kotlin version is mirrored into mise.toml; the update
            // tool's own module is free to sit on a different compiler release.
            if (module.label == "module.yaml") kotlinBump?.let { (from, to) -> syncMiseKotlin(root, from, to) }
        }
    }

    when {
        outdated == 0 -> println("  all declared dependencies are on their latest stable release")
        dryRun -> dim("  dry run; drop --dry-run to rewrite the module.yaml files")
        else -> updated("  run 'mise run test' before committing")
    }
    dim("  reminder: junit-jupiter and settings.jvm.test.junitPlatformVersion must stay aligned")
}

/**
 * Mirrors a new Kotlin version into `[tools].kotlin` in mise.toml, which pins the standalone
 * compiler used for one-off `.kts` scripts; CLAUDE.md requires the two to stay matched.
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
