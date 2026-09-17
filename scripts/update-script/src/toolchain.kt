/*
 * 1/3 — the Kotlin Toolchain itself.
 *
 * `./kotlin update` rewrites the `kotlin` / `kotlin.bat` wrappers (the version they pin plus
 * the SHA256 of the distribution they provision). All this pass adds is a before/after read of
 * the pinned version, and the warning about the local kotlin.bat patch being regenerated away.
 */

import kotlinx.io.files.Path

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

    if (exec(root, "./kotlin update") != 0) {
        warn("kotlin update failed — leaving the wrappers untouched")
    }

    val new = cliVersion(root)
    if (new == current) return println("already on the latest release")

    updated("updated: $current -> $new")
    warn("NOTE: 'kotlin update' regenerates BOTH wrappers from the upstream templates, so any")
    warn("local patch is lost — in this repo that is the kotlin.bat fix for the 'Terminate batch")
    warn("job (Y/N)?' prompt (commit bd46be3). Check 'git diff kotlin.bat' and re-apply it.")
}
