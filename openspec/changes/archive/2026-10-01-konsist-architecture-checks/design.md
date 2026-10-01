## Context and scope

C05 is accepted and archived. Read [architecture](../../../../docs/ARCHITECTURE.md), [development](../../../../docs/DEVELOPMENT.md) and the [accepted quality contract](../../../specs/shared-quality-checks/spec.md). C05A adds only the mandatory architecture checks before pagination. It changes no rendered behavior.

## Selected rules

| Rule | Production scope | Protected contract |
|---|---|---|
| Data implementation visibility | Top-level classes, interfaces and objects in `data.characters.remote` and `data.characters.repository`, including subpackages | Declarations are internal or private; DTOs, transport and repository implementation do not become public APIs |
| ViewModel visibility | Feature classes deriving from AndroidX `ViewModel` | ViewModels are internal; routes remain the feature entry points |
| Read-only screen state | Each selected ViewModel's directly declared `state` property | A visible `val` with an explicit `StateFlow<…>` type; outward properties declare their types and do not expose mutable flow owners |

The data DI package is deliberately outside the first selector: `CharactersBindings` is a public module replaced by app instrumentation, while its implementation-bearing binding remains internal. Nested DTO members and sealed result variants are not subject to a blanket internal-class rule. Do not duplicate the Gradle dependency graph or ktlint/Detekt naming checks.

Both ViewModels currently infer their correct read-only state type from `asStateFlow()`. Konsist inspects source declarations and cannot generally resolve inferred types. Add explicit `StateFlow<HomeUiState>` and `StateFlow<DetailsUiState>` signatures rather than parsing initializer text or requiring a specific private-field name. Visible ViewModel properties also declare `val` types, so exposing an inferred mutable owner cannot bypass the check. Declared `MutableStateFlow`/`MutableSharedFlow` properties must be private. The assertions identify flow declarations through Konsist and the existing coroutines types, rather than comparing generic type text. This rule protects the screen-state property; it is not a proof of deep immutability or all possible coroutine behavior. Kotlin compilation and the existing behavioral tests retain those responsibilities. The guard checks directly declared properties, not every possible function return or mutation of nested objects.

Each rule must fail if its expected source/declaration scope is missing or empty, rather than passing vacuously. Review the actual selected declarations during implementation. Production source selection excludes tests, IDE build output and generated sources; test-only Hilt replacements are not runtime violations.

## Dependency and test host

Use `com.lemonappdev:konsist:0.17.3`, the candidate pinned version listed by the official project, with the existing JUnit 4 and coroutines-core versions as test dependencies. Coroutines types let Konsist identify qualified flow declarations; production domain dependencies stay unchanged. Resolution, parsing of the implemented sources and execution with Kotlin 2.4.20/JDK 17 are now verified by the runs below. This does not guarantee support for every future Kotlin syntax feature. No production dependency on Konsist is added; hosting source-inspection tests in domain's test source set does not couple domain production code to Android or other modules.

Use one architecture suite in `:domain:characters/src/test/kotlin/…/architecture`. It scans explicit production source roots from the repository, rather than importing feature/data classes or depending on their compiled artifacts. Keep rules as direct, readable Konsist assertions without a custom rule DSL, baseline or broad suppression list.

## Task ownership and inputs

Expose a root `konsistCheck` backed by a dedicated JVM Test task in `:domain:characters`, using its existing test sources/runtime classpath. Select the architecture suite once; exclude it from the ordinary domain `test` task to prevent duplicate execution, and include it in the module's `check` lifecycle. Future domain behavior tests retain the ordinary test task. Gradle 9 treats an intentionally empty test task as a failure once test sources exist, so only the ordinary domain task sets `failOnNoDiscoveredTests = false`; the dedicated architecture task retains its strict discovery behavior. The empty ordinary task is not behavior-test coverage.

Give the scanner a deterministic repository root, independent of the invoking shell directory, and explicit `src/main/kotlin` roots for the existing modules. Declare all scanned Kotlin files as task inputs, including additions/deletions. A source change in an Android module must invalidate the JVM architecture task, even when the domain test classes have not changed. Reuse normal Gradle XML/HTML test reports.

The root `qualityCheck` depends on `konsistCheck`; GitHub Actions already calls the aggregate gate. Preserve existing hook installation and its `ktlintCheck detekt` command. Architecture tests are shared full-gate checks, not another automatic hook installation step.

## Approved test boundaries

1. **Production source → Konsist assertions:** the suite selects real implementation declarations and reports their qualified names/files when a rule fails. Include nonempty-scope guards and explicit state-type checks.
2. **Root task → architecture test execution:** invoking `konsistCheck` executes the suite, fails on a violation and reacts to inspected source changes outside domain. Invoking `qualityCheck` includes the same task.

Use GIVEN / WHEN / THEN test names with JVM backticks/spaces. A Konsist scope may be the descriptive `lateinit var` subject, separated from other setup where relevant. No repository mock or runtime test Activity is needed.

## TDD and completion evidence

Work one rule at a time. For visibility RED, temporarily remove `internal` from the existing `CharacterDto` declaration: its primitive fields make the mutation compilable. Observe the rule's failure, then restore that exact edit and verify GREEN. For state RED, temporarily expose an explicitly typed `MutableStateFlow` backed by the existing mutable owner, keeping Kotlin compilation valid; verify the state rule fails and restore the read-only signature. A compiler/dependency error is not behavioral RED. Temporary mutations must never remain in the delivered source or overwrite unrelated changes.

Also exercise default-public visibility, missing state type and empty selector handling at the source-assertion boundary where practical, without creating a production fixture or a general mutation harness. Demonstrate that the aggregate gate rejects a representative compilable violation. Restore source before completion and run affected JVM tests, the full quality gate and release assembly through the managed Gradle workflow. Existing UI behavior is unchanged, so no new instrumented suite is planned for this increment.

Record actual dependency resolution, selected declarations, failure/recovery commands, source-input invalidation and passing reports here after implementation. A workflow inspection or local passing gate is not evidence of a successful remote CI run. Complete human acceptance before archival/C06; commit and push require separate explicit instructions.

## Preparation and sources

Prepared with Codex on 2026-10-01 after C05 acceptance. Source review identified the legitimate public DI replacement boundary and the two inferred, already read-only ViewModel state properties. No new library was installed, rule executed or build run during preparation.

Official references: [Konsist dependency](https://github.com/LemonAppDev/konsist), [scope selection](https://docs.konsist.lemonappdev.com/writing-tests/koscope), [property assertions](https://docs.konsist.lemonappdev.com/veryfying-codebase/verify-properties), [type-inference limitation](https://docs.konsist.lemonappdev.com/features/compiler-type-inference). These informed the integration; local execution evidence follows.


## Implementation and validation record

Scope/test boundaries were approved on 2026-10-01. Codex implemented the source assertions and Gradle wiring, with a read-only Gradle Solver owning the managed executions. Human completion review accepted C05A on 2026-10-01. No commit or push was made.

### Changed responsibilities

- `ArchitectureRulesTest`: three source-level tests cover scoped data visibility, internal feature ViewModels and explicitly read-only state. Missing main roots or empty data/feature selectors fail; exposed properties cannot hide an inferred mutable owner. The scanner uses absolute production roots passed by Gradle and excludes tests/generated output.
- `HomeViewModel` and `DetailsViewModel`: import `StateFlow` and explicitly declare their existing state types; actions, requests, cancellation and rendering behavior remain unchanged. Both mutable owners remain private.
- Domain Gradle configuration: test-only Konsist/JUnit/coroutines dependencies, one dedicated architecture Test task, scanned-source inputs, ordinary-test exclusion and module `check` wiring. Root Gradle configuration exposes `konsistCheck` and includes it in `qualityCheck`.
- Version catalogue and technical documents: pin Konsist 0.17.3, document the command/report and record implemented status. No production module/dependency, hook change or additional workflow is introduced.

The selected rules inspect **8 top-level data types** in remote/repository and **2 feature ViewModels**, with source scopes drawn from **43 main Kotlin files across the six modules**. DI is excluded deliberately. These counts describe the current bounded source rules, not general architectural completeness.

### Observed commands and outcomes

Every Gradle invocation used `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run` with workflow `d263906dbfcb00326b531c37e951c537`. The nested commands and their verification questions are recorded below. Source mutations were temporary and have all been restored; no production fixture or mutation framework remains.

| Run | Verification question | Nested Gradle command | Observed outcome |
|---|---|---|---|
| 0001 | Does the architecture suite reject a compilable default-public CharacterDto while the Android data source still compiles? | `:data:characters:compileDebugKotlin konsistCheck` | Behavioral RED: data compiles; source assertion identifies public/default `CharacterDto` |
| 0002 | Does restoring internal CharacterDto recover the architecture check with compilable data sources? | `:data:characters:compileDebugKotlin konsistCheck` | GREEN: restored DTO passes |
| 0003 | ViewModel visibility RED? | `:feature:home:compileDebugKotlin konsistCheck` | Behavioral RED: compilable public HomeViewModel is rejected; state/process temporarily internal during this mutation |
| 0004 | ViewModel visibility GREEN? | `:feature:home:compileDebugKotlin konsistCheck` | GREEN: original internal ViewModel and member signatures restored |
| 0005 | State contract RED? | `konsistCheck` | Behavioral RED: inferred state lacks the required explicit source type |
| 0006 | State contract GREEN? | `konsistCheck :feature:home:testDebugUnitTest :feature:details:testDebugUnitTest` | Failed GREEN attempt: Konsist `type.name` includes generic arguments; direct pinned-source inspection identifies the faulty comparison |
| 0007 | State contract GREEN? | same as 0006 | GREEN: supported qualified `hasSourceDeclarationOf` assertion; 3 architecture + 14 ViewModel tests pass |
| 0008 | Mutable state RED? | `:feature:home:compileDebugKotlin konsistCheck` | Behavioral RED: compilable explicit `MutableStateFlow` state rejected |
| 0009 | Recovery and module checks? | `konsistCheck :domain:characters:check ktlintCheck detekt` | Architecture GREEN after restoration; aggregate attempt fails because the ordinary domain task discovers no tests after suite exclusion |
| 0010 | Recovery and module checks? | same as 0009 | GREEN after explicitly allowing that ordinary empty task; shared root/module selection references one architecture task; formatting and analysis pass |
| 0011 | Added source invalidates checks? | `konsistCheck :data:characters:compileDebugKotlin --no-build-cache` | GREEN: moving existing PageInfoDto to a temporary separate file reruns the architecture task while domain test compilation remains up-to-date; data compiles |
| 0012 | Removed scope fails closed? | `konsistCheck --no-build-cache` | Source-scope validation: temporarily excluding the repository file reruns tests and reports `Missing data repository declarations`; this is not a compilable-production RED claim |
| 0013 | Shared gate rejects mutable owner? | `qualityCheck` | Behavioral RED through the aggregate gate: compilable exposed inferred `mutableState` is identified; other gate tasks may stop after this failure |
| 0014 | Final quality gate and release? | `qualityCheck :app:assembleRelease` | GREEN with all original sources restored except the two approved explicit state types |

The generic-name correction and empty ordinary-test setup failure are not additional behavioral RED slices. Formatting was applied only to the changed root/domain build files and the new architecture suite. Source review confirmed final DTO grouping, repository path, internal ViewModels and private mutable owners, without temporary files.

### Completion evidence

- Final gate/release result: **34 passing JVM tests**, no failures/errors/skips: 17 repository, 14 ViewModel and 3 architecture. Run 0014 executes the repository tests and reuses matching prior passing architecture/ViewModel results through cache/up-to-date checks; it does not reexecute all 34. Earlier rule/VM runs and source-input probes provide actual execution evidence.
- Source additions and removal outside the host module invalidate the architecture task. The ordinary domain test task contains no behavior suite yet and is not counted. The dedicated task's XML/HTML reports live in `domain/characters/build/test-results/konsistCheck/` and `domain/characters/build/reports/tests/konsistCheck/`.
- Root `qualityCheck` includes architecture checks alongside formatting, Detekt, Android debug lint, existing JVM tests and debug assembly. Release assembly produces `app/build/outputs/apk/release/app-release-unsigned.apk`. Inspection of the domain main JAR finds no architecture-test/Konsist classes; the dependency is test-only.
- Existing hook installation and the Quality workflow are unchanged. The workflow invokes the extended aggregate gate, but no remote C05A run has occurred. Existing 20 instrumented C05 tests remain prior evidence; no instrumentation was rerun for these signature-only runtime changes.
- Strict OpenSpec validation, local-document targets and `git diff --check` are checked after documentation updates. Human acceptance is complete and all C05A tasks are checked; the change is archived and its requirements are merged into the architecture-checks capability specification.

The managed Gradle workflow is finished; cleanup removed only its own temporary logs.
