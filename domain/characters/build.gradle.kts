import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.konsist)
    testImplementation(libs.kotlinx.coroutines.core)
}

val productionSourceRoots = listOf(
    "app",
    "feature/home",
    "feature/details",
    "domain/characters",
    "data/characters",
    "core/designsystem",
).map { rootProject.file("$it/src/main/kotlin") }

val konsistCheck by tasks.registering(Test::class) {
    group = "verification"
    description = "Checks production implementation visibility and read-only screen state."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/ArchitectureRulesTest.class")
    workingDir = rootProject.projectDir
    systemProperty(
        "architecture.sourceRoots",
        productionSourceRoots.joinToString(File.pathSeparator),
    )
    inputs.files(productionSourceRoots.map { fileTree(it) { include("**/*.kt") } })
        .withPropertyName("productionKotlinSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.test {
    exclude("**/ArchitectureRulesTest.class")
    failOnNoDiscoveredTests = false
}

tasks.check {
    dependsOn(konsistCheck)
}
