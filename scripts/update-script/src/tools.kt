/*
 * 3/3 — everything else that is pinned: mise-managed tools, the mise binary itself, the JDK,
 * the Postgres image used by the isolation demos. Report-only, always: none of those are this
 * tool's to rewrite.
 */

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.io.files.Path

suspend fun reportTools(root: Path) = coroutineScope {
    // Both are blocking process spawns; `mise outdated` is the slow one — run them together.
    // Default, not IO: `Dispatchers.IO` is `internal` on Kotlin/Native (see latestStableAsync).
    val outdatedTools = async(Dispatchers.Default) { capture(root, "mise outdated") }
    val miseVersion = async(Dispatchers.Default) { capture(root, "mise --version") }

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
