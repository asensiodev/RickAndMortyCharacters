## Context

C02 follows the accepted six-module Android foundation. The build uses AGP built-in Kotlin and fixed versions. Checks must work with that toolchain and cover both Android Kotlin and pure JVM domain without analyzing generated outputs.

## Goals / Non-Goals

Establish repeatable checks and explicit hook installation. Character behavior, instrumentation journeys, coverage thresholds and deployment belong to later changes. Do not add demonstration tests or empty test infrastructure merely to report coverage.

## Decisions

1. **One full local/CI gate.** The root `qualityCheck` task aggregates ktlint checks, Detekt, Android debug lint, Android debug JVM test tasks, domain JVM tests and app debug assembly. Include all applicable modules, not Gradle namespace containers. Treat absent tests as absent coverage. No format task is invoked by the gate.
2. **Pinned isolated CLI tooling.** Root `JavaExec` tasks run the official ktlint 1.8.0 and Detekt 2.0.0-alpha.6 CLI artifacts from separate configurations. This avoids Android source-set discovery through analyzer plugins with AGP built-in Kotlin. Detekt alpha.6 embeds Kotlin 2.4.10; its CLI successfully analyzes the current Kotlin 2.4.20 project source without type resolution. This is observed source compatibility, not a guarantee for every future language feature. The analyzer is build tooling and does not enter the APK. One formatter configuration includes Kotlin DSL scripts and allows annotated Compose function names. Do not hide incompatibility through blanket exclusions, a generated baseline or disabled failures. Keep direct build files; there is no separate convention-plugin project.
3. **Explicit lightweight hook.** Track `.githooks/pre-commit`. `installGitHooks` configures this clone's local hook path, never global Git. If a different custom hooks path or existing local pre-commit is present, stop with actionable guidance instead of overwriting it. Repeat installation is safe. The hook runs formatting checks and Detekt only; full tests/build/lint run through `qualityCheck` and CI. Checks inspect the working tree, so unstaged Kotlin edits can block a commit; document this rather than manipulating the index or auto-stashing. Never format, stage, stash or commit from the hook. Resolve the repository root so commits from subdirectories work.
4. **CI runs the same gate.** Use pull_request, push to main and workflow_dispatch, a supported Linux runner, JDK 17 and explicitly prepared command-line tools and installed required Android SDK packages. Pin action revisions and give the job read-only repository permissions. Cancel obsolete runs within the same PR/ref. Use normal Gradle caching without signing/deployment credentials; report upload must not mask check failures. Add instrumented jobs only when the deterministic journey exists.
5. **Proportional build properties.** Enable `org.gradle.caching`, `org.gradle.parallel` and `org.gradle.tooling.parallel`; keep the 2 GB heap. Do not copy unrelated Firebase/KSP settings, explicitly restate tool defaults, disable JVM class sharing or introduce configuration-on-demand. No measured speedup is claimed. See [Gradle build cache](https://docs.gradle.org/current/userguide/build_cache.html) and [parallel execution/tooling](https://docs.gradle.org/current/userguide/performance.html#parallel_execution).

## Validation and test boundary

Validate configuration with task outcomes and controlled failures, rather than claiming a TDD cycle. In an isolated checkout/copy, introduce a formatting violation and a supported Detekt violation independently; observe the intended check fail, remove it and observe recovery. Exercise an actually failing temporary JVM test through the gate to confirm failure propagation, then remove that fixture. These probes must never be committed as product tests.

Verify hook installation twice, protection of custom hooks and failure propagation. Compare index/worktree state before and after a hook run; no automatic source/index mutation is allowed. Validate YAML and workflow commands locally, then record a real GitHub Actions run after publication when authorized. Local validation is not remote CI evidence.

## Runtime and verification boundaries

The tracked daemon criteria now select JDK 17, matching README and CI. The previous local JDK 25 configuration was backed up outside the repository before replacement, as part of the authorized C02 implementation. Local checks use installed Corretto 17; CI selects Temurin 17. This policy requires a locally installed JDK 17 rather than silently downloading one through generated platform URLs.

Working-tree hook checks can detect unstaged violations. Hook safety was validated in temporary clones: custom local/inherited hook protection, repeat installation, failure propagation, nested working directories and unchanged source/index. The actual pre-commit also passed its Gradle checks without changing source/index hashes. No auxiliary hook verification script or task is retained; installation remains available through `installGitHooks`.

The published Quality workflow passed on 2026-09-30. Product tests are not present; current JVM tasks have no source. Temporary failing fixtures validate gate propagation and are excluded from the project.

## Assistance and evidence

2026-09-30: Codex prepared this proposal from the accepted backlog and existing build configuration. The reviewer authorized complete implementation before review. Three build properties, isolated analyzer tasks, the shared gate, hook installation and CI were implemented. Final scope review authorized publication after removing the auxiliary hook check and scheduling mandatory Konsist checks in C05A.

Managed `gradle-run` validation asked: “Does debug assembly and the domain build pass with build caching and parallel execution enabled?” `./gradlew :app:assembleDebug :domain:characters:build --warning-mode all` passed: 101 actionable tasks, 7 executed and 94 up-to-date; no warning/failure fingerprints. The existing local daemon selection is JDK 25; this run does not verify a JDK 17 daemon. Compilation remains targeted to Java 17. No behavioral tests were executed or cache-speed benchmark performed. Workflow finish removed only wrapper-owned logs.

### C02 implementation checks

All agent Gradle commands used the managed `gradle-run` wrapper. A dedicated diagnostic owner executed the checks; the parent owned source/configuration changes. Temporary probes ran in an isolated source copy and were removed afterward. These are configuration and failure-propagation checks, not a fabricated TDD cycle or product test coverage.

| Check | Observed result |
|---|---|
| Formatter accepts current source/scripts | `./gradlew ktlintCheck`: passed; no findings in the Checkstyle report |
| Analyzer parses/checks current source/scripts | `./gradlew detekt`: passed; no findings in the Checkstyle report |
| Gate rejects malformed formatting | Isolated `qualityCheck` failed at `ktlintCheck` for missing spacing around `=`; fixture removed |
| Gate rejects static-analysis violation | Isolated `qualityCheck` failed at `detekt` for `TooGenericExceptionThrown`; fixture removed |
| Gate propagates an executed JVM failure | Isolated `qualityCheck` failed at `:domain:characters:test`; JUnit XML recorded one test/one failure with the expected assertion. Temporary JUnit dependency/test removed |
| Android compatibility check | `./gradlew :app:lintDebug`: passed after removing the redundant API 27-only `windowLightNavigationBar` item from the minSdk 26 base theme. `MainActivity` continues to set dark system-bar styling |
| Hook installation and actual execution | `installGitHooks` passed after previous installation in a temporary clone. The actual installed pre-commit passed ktlint/Detekt; hashes confirmed no versionable source or index changes |
| Complete gate after fixture removal | `qualityCheck --warning-mode all` passed in the isolated copy and real project on JDK 17. The project run processed 220 tasks: 27 executed, 12 from cache and 181 up-to-date. All five applicable Android lint tasks completed without errors; no product tests exist yet |
| Workflow configuration | actionlint 1.7.12 passed after verifying its official archive checksum. Action pins were resolved to commit SHAs; the Gradle v5 annotated tag was dereferenced. Runner documentation confirms required Android SDK packages. The subsequent published run is recorded below |

An initial formatter failure identified wrapping in the module list and was corrected. The first format probe also triggered Detekt's constant rule; replacing it with an unformatted constant isolated the formatter failure. Android Lint initially blocked the JVM-failure probe because of the real base-theme API mismatch; the source fix was checked before resuming the probe. No failing fixture or analyzer baseline is retained.

Changes affect root Gradle tasks/catalogue/properties/daemon criteria, formatter/analyzer configuration, the pre-commit and installation script, the Quality workflow, the base theme resource and setup/OpenSpec documentation. No production Kotlin class or permanent JVM test class was added or modified. The auxiliary hook-safety script and task were removed during final scope review; the installation task and actual pre-commit remain.

Official tool references: [ktlint CLI artifact](https://github.com/ktlint/ktlint/blob/1.8.0/ktlint-cli/build.gradle.kts), [Detekt alpha.6 release](https://github.com/detekt/detekt/releases/tag/v2.0.0-alpha.6), [AGP built-in Kotlin integration issue](https://github.com/JLLeitschuh/ktlint-gradle/issues/1008), [Ubuntu runner software](https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2404-Readme.md).

Final scope review makes Konsist mandatory in C05A after the initial Home/Detail implementations; it is not implemented by C02. Final aggregate validation after removing the auxiliary hook check is recorded below.

Final validation: managed `gradle-run` asked “¿Pasa qualityCheck tras retirar la comprobación auxiliar de hooks?”; `./gradlew qualityCheck` passed with 219 tasks (7 executed, 212 up-to-date). The remaining gate still runs formatting, static analysis, Android lint, JVM test tasks and debug assembly. No product tests exist yet. The final diff and spec were reviewed before publication.

The first remote run stopped before Gradle because `sdkmanager` was absent from PATH. CI now explicitly prepares pinned Android command-line tools through the setup action before installing the required SDK packages; it no longer relies on the runner exposing that command.

Remote validation: [Quality run 36769336718](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/36769336718) passed on commit `a61fb9df2ffd8a948d39c3c04b33c1a6a7b69312` with Temurin 17 on Ubuntu 24.04. SDK preparation, `./gradlew qualityCheck` and report upload succeeded. C02 is accepted and archived; product tests remain absent until their owning capabilities are implemented.
