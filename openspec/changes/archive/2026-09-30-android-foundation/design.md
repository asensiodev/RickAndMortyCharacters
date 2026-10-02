## Context

At proposal creation, planning and the visual handoff were complete and no Android code existed. The foundation is now implemented and validated; the reviewer accepted progression to C02. This is C01 in the [ordered queue](../../../../docs/BACKLOG.md#c01--android-foundation). [ARCHITECTURE](../../../../README.md#architecture) owns the module graph; [Development process](../../../../docs/DEVELOPMENT_PROCESS.md) owns the shared process.

## Goals / Non-Goals

**Goals:** establish one buildable six-module project, a minimal Compose shell and reproducible setup evidence.

**Non-Goals:** character screens, remote requests, caching, navigation behavior, the final visual theme and CI/hooks. Their owning changes remain in the backlog. Configuration is validated through build/dependency/startup checks rather than artificial behavior tests.

## Decisions

1. **Use the selected module graph.** `app` uses the Android application plugin; home, details, data and design system use Android library plugins; domain uses Kotlin/JVM. Enable Compose only in UI modules. The Android data module supports the planned app-cache integration. A single module would reduce build files but would not enforce the selected feature/data boundaries at compile time.
2. **Use direct Gradle Kotlin DSL and a version catalogue.** Six modules do not yet justify a separate build-logic module. Keep repeated configuration small; extract conventions only when real repetition makes them useful. Pin the wrapper and verify its distribution checksum.
3. **Resolve toolchain compatibility before generating source.** Verify the installed JDK/SDK and official AGP, Gradle, Kotlin and Compose compatibility guidance; choose supported versions together and record SDK levels and JVM targets. Use fixed versions, not dynamic selectors. The accepted versions and commands are recorded in [README](../../../../README.md#development-setup).
4. **Keep the shell minimal.** Add one activity and a basic Compose surface with system insets and string resources. The feature modules reserve ownership without fake repositories, demonstration screens or speculative domain models. Domain/data can initially contain build configuration only. Declare the selected project edges without introducing artificial classes merely to exercise them.
5. **Add libraries when their behavior lands.** Compose/Material 3 and the Android entry-point dependencies belong here. Introduce DI, networking, images, pagination and navigation dependencies through the changes that first use them; confirm Navigation 3 compatibility before implementing C05. This keeps the scaffold independently reviewable.

## Risks / Trade-offs

- Toolchain incompatibility → resolve versions as one set, record compatibility sources and verify a clean build before claiming the scaffold works.
- Additional module configuration → keep the existing six-module graph and avoid extra convention/network/api-impl modules.
- A desktop/device can ignore orientation requests → validate on the supported portrait phone setup and describe that boundary accurately.
- Device/emulator unavailable → record startup as pending; successful assembly alone does not establish runtime behavior.

## Migration Plan

Add build/source files to the documentation-only repository, update setup instructions from validation and review the diff. No user data migration or deployment is involved. Keep the prepared change active until implementation, validation and human review are complete; archive through OpenSpec afterward.

## Open Questions

Resolved during implementation: compatible versions are pinned in the catalogue/wrapper, compilation uses Android 37.0 with target 36/minimum 26 and Java 17, and the application ID/namespace is `com.asensiodev.rickandmortycharacters`. Minimum 26 is the initial support boundary; it avoids extending this iteration to legacy Android versions. No technical question blocks C01; the reviewer accepted progression to C02.

## Assistance and validation record

2026-09-30: Codex assisted with the documentation audit, C01 proposal and Android scaffold. The local OpenSpec 1.3.1 CLI supplied the spec-driven workflow. The human reviewer authorized implementation of the complete foundation as one change. A dedicated Gradle diagnostic agent owned compact-wrapper execution; the primary agent owned source edits, dependency-result inspection and device checks. The reviewer authorized commit/push, then progression to the next change.

Compatibility was checked against [AGP 9.2 release notes](https://developer.android.com/build/releases/agp-9-2-0-release-notes), [Kotlin's Gradle compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html), [built-in Kotlin guidance](https://developer.android.com/build/migrate-to-built-in-kotlin) and [Compose BOM guidance](https://developer.android.com/develop/ui/compose/bom). The build initially rejected compile SDK 36: the resolved Compose 1.12.1 AAR metadata requires 37. The five Android modules now select `release(37)` with `minorApiLevel = 0`, matching the installed SDK package `platforms;android-37.0`. Target/minimum SDK remain separate decisions. See [Android SDK configuration](https://developer.android.com/build).

The Gradle wrapper was generated through the native `wrapper` task. Its JAR and cached distribution matched the official [wrapper checksum](https://services.gradle.org/distributions/gradle-9.4.1-wrapper.jar.sha256) and [distribution checksum](https://services.gradle.org/distributions/gradle-9.4.1-bin.zip.sha256); the latter is pinned in wrapper properties.

Gradle commands were executed through the `gradle-run` compact-output wrapper, with the daemon explicitly fixed to Corretto 17. The host launcher was Android Studio's JBR 25. Machine-specific JDK overrides are omitted from the portable commands below; README explains selecting JDK 17. Complete build logs were not exposed, and workflow finish removed only wrapper-owned logs.

| Verification question | Executed check and observed result |
|---|---|
| Does the pinned wrapper generate correctly? | Native `wrapper --gradle-version 9.4.1 --distribution-type bin --gradle-distribution-sha256-sum <official SHA>` in an isolated bootstrap project: passed; JAR/distribution hashes verified |
| Does the six-module base produce an installable debug APK? | `./gradlew :app:assembleDebug :domain:characters:build`: passed after the compile SDK correction; 101 tasks processed |
| Do dependencies follow the graph and keep domain pure? | `./gradlew foundationDependencyReport --init-script <temporary report script>`: passed; structured Gradle result inspected for all six configured modules and every selected project edge. Domain has only Kotlin stdlib 2.4.20 and JetBrains annotations 13.0 on its compile classpath, with Kotlin/JVM and no Android/Compose plugins |
| Is the build independent of project outputs and local SDK files? | The same assembly/domain command in a fresh copy of all versionable sources, excluding `.gradle`, build outputs and `local.properties`: passed, all 101 tasks executed; SDK resolved through `ANDROID_HOME`. The existing user-level dependency/distribution cache was available; this was not a cold network-download test |
| Does the app start and respect phone orientation? | `adb install -r <debug APK>` succeeded; `adb shell am start -W -n com.asensiodev.rickandmortycharacters/.MainActivity` returned `Status: ok` with a cold launch on Pixel 9a / Android 17 / API 37. The process stayed alive; a native screenshot showed the resource-based name, readable system bars and unobstructed content. A 90-degree user rotation preference retained `ROTATION_0` with portrait requested; original emulator preferences were restored |
| Is planning consistent with the implementation? | Strict OpenSpec validation, task status, local Markdown links, versionable-document reference audit and whitespace checks passed |

The dependency report used a temporary init script outside the repository. Its first attempt included Gradle namespace-container projects without an `implementation` configuration; filtering to configured modules corrected the report. Aggregate deprecation notices belonged to that temporary diagnostic task; normal builds had no warning fingerprints. No report task or architectural test was added to production build files.

This change contains one production class, `MainActivity`, which enables edge-to-edge system bars and renders a small Material 3 shell with safe-area padding, resource text and a preview. The remaining modules have build configuration only; their empty source manifests were removed. Only `app/src/main/AndroidManifest.xml` is maintained because it declares the application and launcher activity. No behavioral tests exist yet, so domain `build` success is compilation/configuration evidence rather than executed test coverage. Device smoke checks do not validate final character-screen behavior, broad Android-version compatibility, performance or release/R8 behavior. Quality gates and hooks are the next change; C01 was accepted for archival before preparing C02.

A final cleanup removed the four empty library manifests. `./gradlew :app:assembleDebug` passed afterward through the compact Gradle wrapper (101 tasks, 11 executed; no warning/failure fingerprints). AGP supplies the empty library manifests during the build.

Application identity was updated to `com.asensiodev.rickandmortycharacters`, keeping owner and product segments explicit. All Android namespaces, the `MainActivity` package/path and launch commands were aligned. `./gradlew :app:assembleDebug` passed afterward (101 tasks, 42 executed; no warning/failure fingerprints), and APK inspection confirmed the new ID and launcher. Streaming installation reported insufficient internal space; conventional `adb install --no-streaming -r <debug APK>` succeeded. Cold launch returned `Status: ok`; portrait was rechecked and rotation preferences restored. Only the prior task-created shell installation was removed.

Acceptance: the reviewer requested commit and push of the foundation, then authorized progression to the next task. Commit `61e0f9f` records the accepted foundation. No behavioral capabilities or quality checks were accepted as implemented.
