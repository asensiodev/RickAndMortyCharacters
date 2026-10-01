plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

val ktlintCli by configurations.creating
val detektCli by configurations.creating

dependencies {
    ktlintCli(variantOf(libs.ktlint.cli) { classifier("all") })
    detektCli(variantOf(libs.detekt.cli) { classifier("all") })
}

val kotlinFiles = fileTree(rootDir) {
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/.gradle/**", "**/.git/**", "**/.kotlin/**")
}

val ktlintCheck by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Checks Kotlin and Kotlin DSL formatting without modifying source."
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir
    inputs.files(kotlinFiles)
    inputs.file(".editorconfig")
    args(
        "--reporter=plain",
        "--reporter=checkstyle,output=${layout.buildDirectory.get()}/reports/ktlint/ktlint.xml",
        "**/*.kt",
        "**/*.kts",
        "!**/build/**",
        "!**/.gradle/**",
        "!**/.git/**",
        "!**/.kotlin/**",
    )
}

val detekt by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Analyzes Kotlin source without type resolution or source modifications."
    classpath = detektCli
    mainClass.set("dev.detekt.cli.Main")
    workingDir = rootDir
    inputs.files(kotlinFiles)
    inputs.file("config/detekt.yml")
    args(
        "--input", rootDir.absolutePath,
        "--excludes", "**/build/**,**/.gradle/**,**/.git/**,**/.kotlin/**",
        "--config", "config/detekt.yml",
        "--build-upon-default-config",
        "--report", "html:${layout.buildDirectory.get()}/reports/detekt/detekt.html",
        "--report", "checkstyle:${layout.buildDirectory.get()}/reports/detekt/detekt.xml",
    )
}

val androidModules =
    listOf(
        ":app",
        ":feature:home",
        ":feature:details",
        ":data:characters",
        ":core:designsystem",
    )

val konsistCheck by tasks.registering {
    group = "verification"
    description = "Runs the JVM production architecture checks."
    dependsOn(":domain:characters:konsistCheck")
}

tasks.register("qualityCheck") {
    group = "verification"
    description =
        "Runs formatting, static analysis, architecture checks, Android lint, JVM tests and debug assembly."
    dependsOn(
        ktlintCheck,
        detekt,
        konsistCheck,
        ":domain:characters:test",
        ":core:testing:check",
        ":app:assembleDebug",
    )
    dependsOn(androidModules.map { "$it:lintDebug" })
    dependsOn(androidModules.map { "$it:testDebugUnitTest" })
}

tasks.register<Exec>("installGitHooks") {
    group = "verification"
    description = "Explicitly installs the tracked hook for this clone, preserving custom hooks."
    workingDir = rootDir
    commandLine("sh", "scripts/install-git-hooks.sh")
}
