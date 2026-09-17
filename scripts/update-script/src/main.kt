/*
 * `mise run dev-update` — one pass over everything in this repo that carries a pinned version.
 *
 *     mise run dev-update                  # update everything the tool owns
 *     mise run dev-update -- --dry-run     # list what would change, write nothing
 *     mise run dev-update-build            # rebuild the binary after editing this module
 *
 *   1. Kotlin Toolchain  — toolchain.kt: `./kotlin update` rewrites the `kotlin` / `kotlin.bat`
 *                          wrappers (version + SHA256 of the distribution they provision).
 *   2. Dependencies      — dependencies.kt: the coordinates in every module.yaml, checked
 *                          against Maven Central and rewritten in place.
 *   3. Tools             — tools.kt: mise-managed tools, mise itself, the JDK, the Postgres
 *                          image. Report-only.
 *
 * HOW THIS IS RUN
 * ---------------
 * As a native binary built by the Kotlin Toolchain: `./kotlin build -m update-script` links
 * build/tasks/_update-script_linkLinuxX64Debug/update-script.kexe, and the `dev-update` mise
 * task builds it if needed and runs it. This replaces the former scripts/dev-update.main.kts,
 * which the Toolchain could not run at all (`./kotlin run <script>.main.kts` from inside a
 * project silently ignores the path and runs the module's Main.kt), so the script needed a
 * second, standalone Kotlin compiler pinned in mise.toml just for itself.
 *
 * Being a module rather than a script also means it compiles with the rest of the project and
 * its dependencies are resolved by the Toolchain instead of a `@file:DependsOn` fetched at
 * first run. See module.yaml for what the move off the JVM changed.
 */

import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.system.exitProcess

val USAGE = """
    Usage: mise run dev-update [-- --dry-run]

      -n, --dry-run      only list the available updates; write nothing
      -h, --help         show this message

    With no flags the tool updates everything it owns: the ./kotlin wrappers, the pinned
    versions in every module.yaml, and the matching [tools].kotlin pin in mise.toml.
""".trimIndent()

/**
 * The repo root: `MISE_PROJECT_ROOT` when launched through mise, otherwise the nearest ancestor
 * of the working directory that holds both a `module.yaml` and the `./kotlin` wrapper — the
 * wrapper is what tells the root apart from this tool's own module directory.
 */
fun repoRoot(): Path {
    env("MISE_PROJECT_ROOT")?.let { return Path(it) }
    var dir: Path? = SystemFileSystem.resolve(Path("."))
    while (dir != null) {
        if ((dir / "module.yaml").exists() && (dir / "kotlin").exists()) return dir
        dir = dir.parent
    }
    println("cannot locate the repo root (no module.yaml + kotlin wrapper above the working directory)")
    exitProcess(2)
}

fun main(args: Array<String>) {
    var dryRun = false
    var help = false

    for (arg in args) when (arg) {
        "-n", "--dry-run" -> dryRun = true
        "-h", "--help" -> help = true
        else -> {
            println("unknown option: $arg")
            println(USAGE)
            exitProcess(2)
        }
    }

    if (help) return println(USAGE)

    runBlocking {
        val root = repoRoot()
        val modules = modulesIn(root)
        val pins = modules.flatMap { it.pins }
        // Fire every Maven Central lookup first: they travel while './kotlin update' downloads.
        val latest = latestStableAsync(pins)

        updateToolchain(root, dryRun)
        updateDependencies(root, modules, latest.await(), dryRun)
        reportTools(root)
        bold(if (dryRun) "Done (dry run — nothing was written)." else "Done.")
    }
}
