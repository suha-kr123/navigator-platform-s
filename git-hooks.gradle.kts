import java.util.Locale

fun isLinuxOrMac(): Boolean {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return os.contains("linux") || os.contains("mac")
}

val copyGitHooks by tasks.registering(Copy::class) {
    description = "Copies git hooks from scripts/git-hooks to .git/hooks"
    from("scripts/git-hooks") {
        include("pre-commit.sh")
        rename("pre-commit.sh", "pre-commit")
    }
    into(".git/hooks")
    onlyIf { isLinuxOrMac() }
}

val makeHooksExecutable by tasks.registering(Exec::class) {
    description = "Makes git hooks executable"
    group = "git hooks"
    commandLine("chmod", "+x", ".git/hooks/pre-commit")
    onlyIf { isLinuxOrMac() }
    dependsOn(copyGitHooks)
}

tasks.register("installGitHooks") {
    description = "Installs git hooks"
    dependsOn(makeHooksExecutable)
}

tasks.named("clean").configure {
    dependsOn("installGitHooks")
}
