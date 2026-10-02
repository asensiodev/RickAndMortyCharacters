## Context

The user selected screenshot testing after episode cards and a slightly larger square portrait. Existing Compose tests cover callbacks, recovery and scroll; Paparazzi is intended to detect visual differences, rather than replace those tests. C12 remains the final reproducible delivery increment.

Starting catalogue: AGP 9.2.1, Kotlin 2.4.20, Compose BOM 2026.09.00, compile/target SDK 37 and Java 17. The first smoke used alpha05 and failed before rendering. The approved retry uses published alpha05.1 with declared AGP 9 support and the Java 21 build runtime; executed evidence below supersedes the earlier pending assessment. Do not downgrade the production toolchain or use an unpublished dependency to conceal a compatibility failure.

## Decisions

### Use existing rendering boundaries

Use JVM tests in `:feature:home` and `:feature:details`, rendering their real plain composables. Component tests cover focused geometry; screen tests cover composition of controls, feedback and overlays. Avoid repositories, ViewModels, Hilt, Activity navigation, live network and automatic discovery of every Preview. Keep fixtures test-only and local to their consumer; extract helpers only when there is actual repeated need.

### Initial bounded matrix

One ordinary portrait profile with default font scale, English text and the accepted dark theme. Start with a 412dp-wide phone profile; record the actual height, density, renderer/API and fonts. Component bounds must be explicit and screen compositions must use the same documented profile.

| Owner | Snapshot | Protected visual behavior |
|---|---|---|
| Home | Character card with long name/species | Accepted name wrapping, species ellipsis, status reservation and stable portrait bounds |
| Home | Unknown-status card with failed portrait | Neutral fallback, readable metadata and informational badge |
| Home | Card skeleton at a fixed animation frame | Accepted card/placeholder geometry |
| Home | No-match screen with query/status | Search/chips, feedback and shortcuts remain integrated |
| Home | Loaded results with append error/counter | Cards remain visible; footer Retry and counter clearance |
| Detail | Content top with controlled portrait | 288dp square portrait, fixed Back and identity/fact hierarchy |
| Detail | Episode section with a long title | Text-card width, title/date wrapping and partial next-card affordance |
| Detail | Episode error | Local message/Retry styling without replacing the character screen |
| Detail | Episode loading | Section-local placeholder geometry at a fixed frame |

Avoid a Cartesian combination of status, size and state. Add a baseline only for a concrete visual contract. Narrow/enlarged-text, light theme, full TalkBack and dynamic parallax snapshots are outside this increment.

### Determinism and real output

Use controlled Coil responses with known local image content; distinguish pending, successful and failed image outcomes. Render actual production branches, without globally forcing LocalInspectionMode or adding production branches for screenshots. Control animations using the supported renderer/test APIs and capture at a fixed frame. Use fixed data, locale and typography; no timestamps, external services or arbitrary sleeps. Prove repeated verification does not drift. Screenshots are layoutlib output, not evidence of device motion, performance or R8 behavior.

### Review and verification

Official Paparazzi documentation distinguishes record tasks, which create source-controlled goldens, from verify tasks, which compare them and emit failure diffs. Use native plugin facilities rather than custom image comparison or report generation. Review every initial golden against the accepted UI before treating it as expected output. Store the small initial set in Git; revisit LFS only if measured asset volume justifies it, preserving existing hooks.

Run an intentional temporary token/layout perturbation after the first baseline: observe visual verification failure and a meaningful diff, restore production source, then verify success without rerecording. This demonstrates regression detection; dependency/configuration failures are not behavioral RED. Do not fabricate a missing-product-behavior RED for introducing a test tool.

Existing `qualityCheck` and CI must select verification, never recording. Document the discovered variant-specific tasks and report paths; do not claim CI passed until the actual job does. A missing baseline must not silently count as success or trigger automatic acceptance. A future baseline update needs a visible reason and human review.

## Verification and acceptance

First run the compatibility smoke, then each bounded fixture, repeated verification and intentional visual failure/recovery. Run affected existing JVM/instrumented tests only when their inputs or production behavior change, then the documented quality gate and release assembly. Record actual task names, commands, versions, host/render configuration, snapshot count, reports, outcome and limitations here. Human acceptance precedes archival and C12. No commit or push is authorized for this draft.

## Sources

- [Official Paparazzi documentation](https://cashapp.github.io/paparazzi/)
- [Official README: Compose snapshots, record/verify tasks and LocalInspectionMode](https://github.com/cashapp/paparazzi)

Preparation read current project configuration and these sources; no Paparazzi dependency was installed, build was run or baseline generated during preparation.

## Compatibility attempt — 2026-10-02

The user authorized implementation with no commit or push. Tested the published `2.0.0-alpha05` plugin using a temporary catalogue pin, root plugin declaration, Home plugin application and a single JVM `PaparazziCompatibilityTest` rendering `Text` inside the real `RickAndMortyTheme`.

The project uses Gradle 9.4.1, AGP 9.2.1, Kotlin 2.4.20 and Compose BOM 2026.09.00. `gradle/gradle-daemon-jvm.properties` pins JDK 17; the installed system JVM is Amazon Corretto 17.0.20 on macOS arm64. Published plugin [Gradle module metadata](https://repo.maven.apache.org/maven2/app/cash/paparazzi/paparazzi-gradle-plugin/2.0.0-alpha05/paparazzi-gradle-plugin-2.0.0-alpha05.module) declares `org.gradle.jvm.version=21` for both API and runtime variants. The [official alpha05 release](https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha05) explicitly supports pre-AGP 9 consumers; the [changelog](https://github.com/cashapp/paparazzi/blob/master/CHANGELOG.md) records the Java 21 minimum since alpha04.

Managed workflow `7ee4ec3c0f16c2428b98b2e8149eff5f` attempted:

```sh
./gradlew :feature:home:recordPaparazziDebug --tests '*PaparazziCompatibilityTest*' --console=plain --no-scan
```

The initial sandbox run could not access the Gradle cache lock. The escalated attempt reached Gradle and exited 1 after 4.12 seconds, during root project configuration: it could not resolve `app.cash.paparazzi:paparazzi-gradle-plugin:2.0.0-alpha05` on the buildscript classpath. No task or Compose renderer executed. The managed summary truncates the underlying resolution reason, so it does not independently prove an AGP runtime failure; JVM 21 is independently confirmed by published metadata, and AGP support is a declared upstream limit. This is a configuration/dependency failure, not a visual RED.

Removed all temporary plugin declarations, the catalogue pin and the compatibility test, preserving the production toolchain and unrelated working-tree changes. No renderer/API/font profile has been demonstrated, no goldens or diffs exist, and qualityCheck/CI were not modified. Tasks 1.2 onward remain incomplete. Continuing requires a compatible published Paparazzi release or a separately approved change to the toolchain/testing choice; O03 is not accepted or archived.

Restoration verification in the same managed workflow ran `./gradlew :feature:home:testDebugUnitTest --console=plain --no-scan`: exit 0 in 2.73 seconds, with all 34 tasks UP-TO-DATE. Existing successful test outputs were reused; this is not a new execution of the Home suite. `openspec validate add-paparazzi-visual-regressions --strict` passed. Full quality/release and remote CI were not rerun for this removed trial. Workflow finish succeeded and deleted only wrapper-owned logs. No commit or push occurred.

The managed verification questions were “Does Paparazzi 2.0.0-alpha05 configure and render the Compose smoke test with AGP 9.2.1, Gradle 9.4.1 and the current Java 17 runtime?” (no: configuration failed before rendering) and “¿Pasan los tests de Home tras retirar la prueba?” (yes: the task succeeded using unchanged UP-TO-DATE outputs).


## Tooling reassessment — 2026-10-02

The previous smoke remains valid evidence for **alpha05 on JDK 17**, but does not establish that all published Paparazzi versions lack AGP 9 support. The follow-up check found [Paparazzi 2.0.0-alpha05.1, released 2026-09-28](https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha05.1), whose release notes add AGP 9.0 support. Its [published module metadata](https://repo.maven.apache.org/maven2/app/cash/paparazzi/paparazzi-gradle-plugin/2.0.0-alpha05.1/paparazzi-gradle-plugin-2.0.0-alpha05.1.module) declares JVM 21 for API/runtime variants. This corrects the earlier broad compatibility conclusion; AGP 9.2.1 plus the project's exact Kotlin/Compose versions still require a real smoke.

| Candidate | Toolchain implications | Assessment |
|---|---|---|
| Paparazzi 2.0.0-alpha05.1 | Build daemon and CI JDK 21; retain AGP 9.2.1 and Java/Kotlin bytecode targets 17 | Recommended first candidate; published AGP 9 support, but exact rendering not verified |
| Google Compose Preview Screenshot Testing 0.0.1-alpha16, standalone plugin | Published requirements allow current AGP/JDK; dedicated screenshotTest source set and PreviewTest fixtures | Experimental alpha; standalone setup is deprecated, so adopting it introduces a later migration |
| Google screenshot engine with native AGP test suites | Requires AGP 9.5.0-alpha03 or newer; JDK 17+ | Experimental engine and AGP upgrade; unnecessary toolchain scope for the current showcase |

### Rationale for the alpha test dependency

Paparazzi 2.0.0-alpha05.1 is a proposed test/build dependency, not an alpha application runtime dependency. Its declared AGP 9 support justifies evaluating it while retaining the current AGP and production targets; being an MVP is not, by itself, a reason to accept instability. The accepted exposure would be build/CI failures, renderer drift or misleading visual results: pin the published version, keep its runtime in tests, require rendering and repeated verification plus the quality/release gates, and review baseline updates explicitly. Adoption remains conditional on those checks; if they fail, defer O03 while retaining the existing interaction tests, and reassess when a compatible stable release is available.

[Google's documentation](https://developer.android.com/studio/preview/compose-screenshot-testing) still labels Compose Preview Screenshot Testing experimental; it is not stable. The standalone route documents AGP 8.5+ and JDK 17+, uses `@PreviewTest` in `src/screenshotTest`, separates `updateDebugScreenshotTest` from `validateDebugScreenshotTest`, and provides HTML diffs. Its [replacement through AGP test suites](https://developer.android.com/studio/preview/compose-screenshot-testing-with-testsuites) requires AGP 9.5.0-alpha03+ and engine alpha16+. Published prerequisites are not evidence that either route works with this project's exact dependencies.

Changing the **build runtime** to JDK 21 is distinct from changing Java/Kotlin source or bytecode targets. The candidate preserves target 17, minSdk 26 and the product's runtime/API scope. A coordinated change would update daemon criteria, local setup and CI JDK selection, followed by a minimal Compose render, JVM checks, qualityCheck and release assembly. No AGP increase is proposed solely to use Paparazzi. JDK 21 alone would not fix the old alpha05's declared AGP limit; the candidate also updates Paparazzi to alpha05.1.

Keep the same nine visual contracts and review/verification policy regardless of tool. Google would require revising the Paparazzi-specific requirement, design fixtures/tasks and development commands before implementation. This turn records the alternatives and recommendation only: no JDK, AGP, plugin, production source, baseline or CI change was made, and no Gradle task was launched. Runtime/tool selection and compatibility smoke remain pending. No commit or push.


## Approved implementation — 2026-10-02

The user approved the retry. Paparazzi **2.0.0-alpha05.1** is pinned and applied only to Home and Details. Gradle daemon criteria now select Java **21**; local execution uses the existing Zulu 21.0.11 installation on macOS arm64. AGP 9.2.1, Gradle 9.4.1, Kotlin 2.4.20, Compose BOM 2026.09.00, minSdk 26 and production JVM targets **17** remain unchanged. JVM-only module toolchains retain JDK 17. CI installs Temurin 17 then 21, with 21 as the build runtime.

The approved alpha is test/build tooling, justified by published AGP 9 support and verified rendering with this project's toolchain. It does not make the application runtime alpha. Pinning the version, explicit human review of baseline updates and a failing verification gate bound the risk of renderer/tooling regressions. Reassess if those checks become unreliable; MVP status alone does not justify instability.

### Rendering and synchronization

Nine PNGs cover the matrix: Home 5, Detail 4. `DeviceConfig` fixes 412 × 891 px/dp at mdpi, 160 dpi, portrait, English, dark mode, font scale 1 and no software button strip; component scenes use 412 × 480 with 16dp padding, Home cards 184dp wide and real Detail episode cards 240dp wide/minimum height 148dp. Theme typography uses the existing Material/platform fonts. Paparazzi resources report compile/target API 37; its pinned dependencies resolve layoutlib 16.2.3. This is layoutlib output, not a Pixel emulator capture.

Each feature uses a local Coil interceptor with a fixed 300 × 300 blue image; Home additionally returns a controlled image failure. No live HTTP, production inspection branches or ViewModels participate. The later local-portrait refinement below supersedes this initial blue success fixture. Initial single-frame output left asynchronous image painters in Loading despite successful test execution. The released renderer's native `gif` API now advances virtual frames from 0 to 1000 ms at 10 fps. A small private `SnapshotHandler` forwards only the final frame to the official `HtmlReportWriter` or `SnapshotVerifier`, producing ordinary PNGs and native comparison reports. It adds no custom image comparison/report implementation and uses no sleeps. Review confirmed blue success portraits and the real failed-image icon, as well as loading geometry, wrapping/ellipsis, badges, no-match controls, append Retry/counter clearance and episode states. The temporary minimal Text smoke baseline was deleted.

Goldens live in each feature's `src/test/snapshots/images/` (nine PNGs totaling 101,828 bytes, no LFS needed). Generated HTML/diff artifacts remain in ignored build directories. `qualityCheck` depends on both native `verifyPaparazziDebug` tasks alongside the existing JVM tests; CI runs the same gate and uploads Paparazzi failure artifacts, never records. The configured Ubuntu/Temurin job has not run against these macOS/Zulu goldens. Cross-host equivalence remains pending that run.

### Commands and acceptance

Use `:feature:home:recordPaparazziDebug :feature:details:recordPaparazziDebug --tests '*ScreenshotTest*'` only for an intentional baseline update, followed by image review. Normal verification uses both `verifyPaparazziDebug` tasks (or `qualityCheck`). Reports are `feature/{home,details}/build/reports/paparazzi/debug/index.html`; native failed comparisons are under each feature's `build/paparazzi/failures/`.

All Gradle commands in this retry run through the managed wrapper, workflow `baa66bbe829eace75f686042ae2f0ccf`, owned by the read-only Solver; repository edits remain in the parent. The execution ledger below records actual outcomes. Human acceptance, remote CI, final native C11 journeys/device/parallax verification and C12 delivery remain pending. No archival, commit or push occurred.


### Managed execution ledger

All commands below are nested `./gradlew` invocations launched by `gradle_run.py run --workflow baa66bbe829eace75f686042ae2f0ccf --scope targeted --question ... --`; the wrapper adds `--console=plain --no-scan`. No full build logs were exposed.

| Verification question | Nested command | Observed result |
|---|---|---|
| ¿Renderiza Compose con Paparazzi y Java 21? | `:feature:home:recordPaparazziDebug --tests '*HomeScreenshotTest*'` (temporary Text smoke) | PASS, 53 s; one real Compose test executed, temporary golden removed later |
| ¿Se generan las nueve capturas previstas? | `:feature:home:recordPaparazziDebug :feature:details:recordPaparazziDebug --tests '*ScreenshotTest*'` | PASS, 9 s, nine tests; visual review found incorrect component bounds and async image states, so these initial outputs were not accepted |
| ¿Renderizan las nueve escenas en su frame fijo? | Same record command after fixing component dimensions and frame offset | PASS, 7 s; geometry fixed, single-frame Detail/card painters still showed Loading |
| ¿Muestra Detail el retrato controlado ya cargado? | `:feature:details:recordPaparazziDebug --tests '*DetailsScreenshotTest*loaded*'` | Task PASS, 5 s; visual answer NO: single-frame painter remained Loading; temporary cache workaround removed |
| ¿Carga el retrato al avanzar los frames virtuales? | `:feature:details:recordPaparazziDebug --tests '*DetailsScreenshotTest*'` | PASS, 5 s, four tests; visual review confirmed loaded portrait |
| ¿Carga Home los retratos y muestra su fallback controlado? | `:feature:home:recordPaparazziDebug --tests '*HomeScreenshotTest*'` | PASS, 6 s, five tests; visual review confirmed blue success and failed-image icon |
| ¿Pasan las nueve capturas sin deriva al repetir la verificación? | `:feature:home:verifyPaparazziDebug :feature:details:verifyPaparazziDebug --tests '*ScreenshotTest*'`, then same command with `--rerun-tasks` | PASS, 6 s and 9 s; second run executes all 57 selected tasks; nine goldens remain byte-identical by SHA-256 |
| ¿Pasan los controles estáticos con los nuevos fixtures? | `ktlintCheck detekt` | PASS, 4 s; both checks executed |
| ¿Falla la verificación cuando falta una baseline? | `:feature:details:verifyPaparazziDebug --tests '*DetailsScreenshotTest*loaded*'`, with the loaded Detail golden temporarily moved outside the repository | Expected FAIL, 4 s, one test/one failure: native AssertionError and difference report (22.280849%); no replacement accepted. Restored the identical original PNG without recording |
| ¿Detecta una regresión visual y vuelve a pasar al restaurarla? | Loaded Detail verify with temporary `portraitMaxWidth` 288 → 240dp, then both feature verify tasks after restoring 288dp | Expected FAIL, 5 s: native AssertionError, 6.563630% difference; reviewed diff shows the smaller square and 48dp vertical reflow. Original token and goldens restored; GREEN PASS, 1 s, Details test restored FROM-CACHE and verify tasks UP-TO-DATE. This recovery reuses the already-proven original input/output, not a new test execution; the subsequent unfiltered full gate verifies the complete suites |
| ¿Pasan el gate completo y la compilación de release? | `qualityCheck :app:assembleRelease` (scope broad, no test filter) | First attempt FAIL, 46 s, internal Android Lint failure at `:app:lintAnalyzeDebugAndroidTest`; all 96 JVM/architecture tests report zero failures/errors/skips. The bounded diagnostic did not identify a root cause; this is not a screenshot regression |
| ¿Cuál es la causa del crash de lint con Java 21? | `:app:lintAnalyzeDebugAndroidTest --stacktrace` | PASS, 3 s, affected task executed without source changes; failure not reproduced and root cause remains unknown. No detector was disabled, no lint error was suppressed, no AGP/toolchain workaround was applied. Aggregate gate retried after this focused recovery |
| ¿Pasan el gate completo y la compilación de release? | `qualityCheck :app:assembleRelease` (same broad command after successful isolated lint) | PASS, 6 s, 462 tasks: 22 executed, 440 UP-TO-DATE. Gate and release complete; existing passing test outputs from the first aggregate attempt are reused |

Final XML evidence: Home 31 tests (5 screenshots), Details 15 (4 screenshots), data 47 and Konsist 3: **96 total**, zero failures/errors/skips. Modules with empty test tasks are not counted as coverage. All nine golden hashes match their original SHA-256 values after both intentional failures; the temporary portrait token is restored exactly. `openspec validate add-paparazzi-visual-regressions --strict` and `git diff --check` pass. No production behavior change remains, so native suites were not rerun for this test-only increment. Full delivery/device/navigation/parallax acceptance remains separate.

Managed workflow finish succeeded and removed only wrapper-owned logs. Task-owned temporary QA files were removed separately; source goldens and native build reports remain. No staging, commit, push or archival was performed.


### No-match suggestion refinement — 2026-10-02

The user requested fixing Summer wrapping below the other suggestions in the ordinary phone golden. The existing FlowRow remains; only the suggestion OutlinedButtons now use named spacing tokens for content padding (16dp horizontal, 8dp vertical). This keeps all four suggestions on one centered row at 412dp/default font scale, while allowing width-driven wrapping on constrained layouts and retaining Material touch targets. No font-scale breakpoint, new screenshot fixture or global feedback-padding change is added. The no-match golden alone was explicitly rerecorded and visually reviewed; the other eight goldens are preserved.

Managed incidental validation uses workflow `acd507cefed46bcd47f91c4d393a11d4`:

- “¿Caben las cuatro sugerencias en la fila de 412dp?” — wrapper launched `./gradlew :feature:home:recordPaparazziDebug --tests '*HomeScreenshotTest*constrained*'`; PASS in 6.667 s, one test executed. Visual review confirms Rick, Morty, Beth and Summer on one row.
- “¿Pasan las capturas, el gate completo y release tras el ajuste?” — wrapper launched `./gradlew qualityCheck :app:assembleRelease`; PASS in 12.316 s, 462 tasks (55 executed, 407 UP-TO-DATE). Screenshot XML: Home 5 and Details 4, zero failures/errors/skips. Strict OpenSpec validation and diff whitespace check pass. No commit, push or archival; native device review remains pending.


### Local portrait fixture — 2026-10-02

The user selected a local photo instead of the solid-blue success fixture. A fixed 300 × 300 Rick Sanchez JPEG (39,323 bytes) is stored once in the existing test-only `:core:testing` module at `src/main/resources/fixtures/rick-sanchez.jpeg`, shared through both features' existing test dependency. Source: [character 1 response](https://rickandmortyapi.com/api/character/1), [portrait](https://rickandmortyapi.com/api/character/avatar/1.jpeg), fetched once on 2026-10-02. SHA-256: `8332527a300dad67131df8fc84679fd0385d6f43ab45bdb84e631607a54ca6f8`. Tests read the bundled resource, decode it with BitmapFactory and use Coil's public `Bitmap.asImage()` conversion; no network call or image dependency is introduced in tests or production. The same sample portrait intentionally serves every successful fixture, independent of synthetic character metadata. Real failure and skeleton branches are unchanged.

Explicit recording changed exactly three goldens: Home long-name card, Home loaded/append-error screen and Detail content. The remaining six are byte-identical. Visual review confirms the actual photo, crop and rounded portrait bounds at all three scenes. The matrix stays at nine; no additional screenshot framework/helper is added.

Managed incidental workflow `32b833e17c835b877ae899e0547c6041`:

- “¿Renderizan las nueve capturas con el retrato local?” — `./gradlew :feature:home:recordPaparazziDebug :feature:details:recordPaparazziDebug --tests '*ScreenshotTest*'`. Initial attempts failed compilation (1.681 s: missing `shareable` constructor argument; 1.347 s: BitmapImage constructor is internal). Focused inspection of Coil 3.6.3's released `Image.android.kt`/`Image.kt` confirmed the public `Bitmap.asImage()` factory. Corrected call PASS in 6.279 s; all nine screenshot tests executed, with the three expected visual updates. Compilation failures are not behavioral RED.
- “¿Pasan las capturas con foto local, el gate completo y release?” — `./gradlew qualityCheck :app:assembleRelease`; PASS in 7.164 s, 463 tasks (16 executed, 447 UP-TO-DATE). Home 5 and Details 4 screenshot tests report zero failures/errors/skips. Release task reuses unchanged production outputs. Strict OpenSpec and whitespace validation pass. Remote CI/human acceptance remain pending; no commit, push or archival.

Workflow finish removed only wrapper-owned logs; the temporary before/after hash record was removed separately. The JPEG is a test-only resource, and the source PNGs remain reviewable.


### Single-JDK development setup — 2026-10-02

The user authorized removing the separate JDK 17 installation requirement. Gradle/Paparazzi and the two pure JVM modules now use JDK 21; Java/Kotlin output targets remain 17. CI installs only JDK 21 and README reflects that requirement. No production behavior, dependency, golden image or C12 artifact is changed.

Managed workflow `1f31c7b2209dfd1d6803e91b172a6a42` verified with JDK auto-detection and auto-download disabled and the installations path restricted to the existing Zulu 21.0.11 installation:

- “¿Pasan los checks de los módulos JVM usando únicamente la toolchain JDK 21 y bytecode Java 17?” — `./gradlew :domain:characters:check :core:testing:check`; PASS in 4.233 s. The initial sandbox attempt could not access the Gradle cache lock; the escalated run passed. Compiled Kotlin main class files in domain (20) and core/testing (1) use class-file major version 61 (Java 17).
- “¿Pasan qualityCheck, Paparazzi y assembleRelease sin detectar ni descargar JDK 17, usando únicamente la toolchain JDK 21?” — `./gradlew qualityCheck :app:assembleRelease`; PASS in 4.243 s. Successful cached/up-to-date outputs may be reused; this does not claim every test reran. No baseline recording occurred.

Both commands supplied `-Porg.gradle.java.installations.auto-detect=false`, `-Porg.gradle.java.installations.auto-download=false` and `-Porg.gradle.java.installations.paths=<JDK 21 Home>`. Strict OpenSpec validation passed for all 16 items. Managed finish removed only wrapper-owned logs. Remote CI and device checks were not rerun for this local configuration change; C12 remains separate.
