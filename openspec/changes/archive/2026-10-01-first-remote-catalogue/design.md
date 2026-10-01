## Context

C03 is accepted and archived. The app has the selected theme, Home-owned card/skeleton components and a shared Coil loader, but its runtime entry point is still the shell. C04 is the next vertical slice from a remote first page to Home. [Architecture](../../../../docs/ARCHITECTURE.md) owns the module boundaries; [UI/UX](../../../../docs/UI_UX.md) owns the final appearance and interaction contracts.

## Goals / Non-Goals

**Goals:** first-page remote data, repository-backed Home state, native loading/content/empty/error/retry, production DI and observable HTTP/state/screen tests.

**Non-Goals for this change:** detail/navigation (C05), automatic append and floating count (C06), name search (C07), status chips (C08), HTTP disk cache (C09), connectivity monitoring (S01) and screenshot tooling (O03). These are product increments rather than inactive demo controls. Home starts directly with the results area in C04; subsequent changes add working search/chips in the documented positions. Image caching from C03 remains active.

## Decisions

### Contracts and transport

Use a pure `CharactersRepository` suspending page operation and a `CharacterPage` result containing summary items, total count and nullable next-page number. Character summaries contain only fields needed by the card. C05 adds detail behavior when it has a consumer. Domain owns a typed success/failure result; exceptions and response bodies stay inside data. A small failure classification distinguishes network, HTTP/service and invalid response outcomes without presenting raw exception messages to users.

Use Retrofit with the official kotlinx.serialization converter and OkHttp in data. Decode internal DTOs with unknown JSON fields ignored; map to domain at the repository boundary. API base URL and client configuration belong to the data DI configuration, not Home. Use the documented `character` endpoint and the first page; preserve metadata for subsequent pagination without requesting it yet. Do not create a network module or a second API client per screen.

A valid empty successful payload becomes Empty. Transport failures, malformed/absent required response data and an unrecognized 404 become Error. The documented API has context-sensitive 404 behavior; C07 implements recognized filtered no matches and C06 recognized append completion. C04 must not guess that an unfiltered HTTP failure means an empty catalogue. Cancellation is rethrown before normal failure translation. Retrofit suspending requests provide asynchronous transport; avoid wrapping them in an additional fire-and-forget scope or blocking the main thread.

### Dependency composition

Introduce Hilt in app/Home/data using compatible pinned runtime/compiler tooling. The application is the Hilt root, the activity is an entry point, and data contributes repository/client bindings. Home consumes the domain repository through its ViewModel constructor. Keep DTOs and concrete remote implementation internal; expose only the bindings needed for composition. Verify AGP/Kotlin/KSP/Hilt compatibility before pinning versions. No service locator, generic DI wrapper or additional Gradle module is needed.

### Screen state and request lifetime

`HomeViewModel` owns the first-page request in `viewModelScope`, private mutable state and a public read-only `StateFlow<HomeUiState>`. Model Loading, Content, Empty and Error explicitly; Content uses presentation mappings into the accepted `CharacterCardUiModel`. Do not add a pass-through use case or general reducer framework.

`process(HomeAction)` handles initial Load and Retry. Initial Load is idempotent for this ViewModel instance; retries are accepted only after a failure. Guard a pending operation to prevent duplicate jobs, including consecutive taps before recomposition. Cancellation is not displayed as Error. Clearing the ViewModel cancels its request. Subscriber/recomposition changes do not own or restart that request.

`HomeRoute` obtains the Hilt ViewModel, performs idempotent initialization with `LaunchedEffect(viewModel)`, collects using `collectAsStateWithLifecycle()` and forwards state/actions to `HomeContent`. `HomeContent` takes immutable state, the shared image loader, Retry/selection callbacks and a root modifier. It owns composition-specific grid scroll/layout objects and no repository or navigation state. Selection forwards the supplied ID; C05 connects it to the real detail destination. Do not add a fake destination to demonstrate that callback.

### UI ownership and tokens

Reuse C03 cards, skeletons, portrait states and theme. Compose the grid lazily with stable character keys/content types and system-safe content padding. Prefer two columns for ordinary portrait widths, reducing to one when width/font size requires it. Empty/error content remains scrollable for small screens and large text. Search/chips/counter are not simulated before their capabilities exist.

New spacing, dimensions and responsive thresholds use named tokens/constants in their owning files. Shared theme/dimension primitives belong in the design system; Home-specific layout policy belongs in Home. Keep strings in English resources and the neutral image fallback local to portraits. Standard previews render real screen states; they do not introduce a runtime gallery.

### Test boundaries and RED → GREEN slices

| Boundary | Driver | Observable assertions |
|---|---|---|
| `CharactersRepository` | Real remote implementation/client against MockWebServer, fixture JSON and controlled response failures | Request path/page, returned domain summaries/metadata, empty success, failure translation and cancellation |
| `HomeViewModel.process` + public state | Fake repository, controlled coroutine scheduling and Turbine | Loading/content/empty/error, idempotent initialization, guarded retry and cancellation |
| `HomeContent` | Whole-screen Compose state/callback tests with controlled Coil responses | Skeletons, real metadata, empty/error feedback, Retry callback, character ID and local image states |
| Production Home wiring | App startup plus one controlled route/wiring test where needed | ViewModel state is rendered and UI Retry reaches its state owner |

Tests assert public behavior rather than private DTO mappers, reducers or helper functions. Work in successive vertical slices: first successful request/display, then failure/recovery, then empty and independent portrait feedback. Observe each expected behavioral RED before implementing its GREEN; compilation/environment failures are not counted as TDD evidence. Do not write all tests before all production code.

Use JUnit, coroutines-test, Turbine and MockWebServer for JVM boundaries and AndroidX/Compose UI Test for screen behavior. Keep fixtures/loaders/fakes in test sources and prefer callbacks over mocking internal collaborators. Use a standard Compose test host, with no app-owned gallery activity. Screenshot tooling remains O03. Confirm the public test boundaries during proposal review before writing tests.

## Risks / Trade-offs

- The first-page increment is not complete catalogue browsing → preserve the later tickets and do not claim pagination, search/filter or detail navigation yet.
- New Hilt/compiler/network dependencies must match the pinned Kotlin/AGP toolchain → verify official releases/compatibility and dependency resolution before implementation; keep versions in the catalogue.
- Previous isolated UI tests encountered incompatible platform reflection on API 37 → select compatible AndroidX test dependencies, inspect resolved transitive versions and run retained Home screen tests on API 37; do not downgrade the app target or hide the failure by removing required screen tests.
- Static exports do not prove adaptive layout or runtime behavior → compare Home states on the emulator and check narrow/large-text behavior without claiming screenshot regression coverage.

## Review and validation

Prepared with Codex on 2026-10-01 after C03 acceptance, using the existing product/architecture/UI contracts and TDD/Compose state/testing guidance. [Official API documentation](https://rickandmortyapi.com/documentation) supports the summary fields and page metadata. [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations) support repository-backed ViewModels, UDF and lifecycle-aware collection. The documentation's sample values are not live-data fixtures to hardcode.

Implementation is complete, locally validated and accepted. The test boundaries above are approved. Actual evidence follows; the reviewer authorized C04 archival after the test-convention update on 2026-10-01. No commit or push is authorized for this change.

### Dependency verification

The version catalogue pins Hilt 2.60.1, KSP 2.3.10, AndroidX Hilt 1.4.0, Lifecycle 2.11.0, Coroutines/Serialization 1.11.0, immutable collections 0.5.0, Retrofit 3.0.0 and OkHttp/MockWebServer 5.3.0. Tests use JUnit 4.13.2, Turbine 1.2.1, AndroidX runner 1.7.0, test JUnit 1.3.0, Espresso infrastructure 3.7.0 and the BOM-aligned Compose UI Test v2 rule. Tests themselves use Compose APIs. The standard Compose test host is a debug dependency; there is no app-owned test Activity.

Compatibility was checked against [KSP setup](https://kotlinlang.org/docs/ksp-quickstart.html), [Hilt setup](https://dagger.dev/hilt/gradle-setup.html), [Dagger releases](https://github.com/google/dagger/releases), [AndroidX Hilt releases](https://developer.android.com/jetpack/androidx/releases/hilt), [Lifecycle releases](https://developer.android.com/jetpack/androidx/releases/lifecycle), [Retrofit 3.0.0](https://github.com/square/retrofit/releases/tag/3.0.0), [OkHttp releases](https://github.com/square/okhttp/releases) and [AndroidX Test releases](https://developer.android.com/jetpack/androidx/releases/test). Resolution, Hilt/KSP compilation, debug assembly and the first retained screen tests succeeded with compile 37.0 / target 37 / JDK 17. API 37 was read from the connected emulator, not inferred from the app target.

### Behavior-focused TDD evidence

All Gradle commands use the `gradle-run` managed wrapper; its diagnostic owner has read-only source access. Compilation/environment failures are not counted as behavioral RED. Each row describes an actual failed assertion/exception followed by the narrow implementation and a passing run.

| Slice / focused test | Observed RED | Verified GREEN |
|---|---|---|
| Repository `returnsCharacterSummariesAndPageMetadata` | Expected domain summaries; received InvalidResponse | Real Retrofit/DTO mapping returns summaries, total and next page; GET path/page verified |
| ViewModel `firstLoadPublishesCharacterContent` | Remained Loading | Load publishes immutable mapped Content |
| Home `contentDisplaysCharacterAndSelectsItsId` | Character text absent on API 37 | Lazy grid displays metadata and forwards ID |
| Repository `serverFailureReturnsServiceError` | 503 mapped to InvalidResponse | 503 maps to Service |
| Repository `connectionFailureReturnsNetworkError` | ConnectException escaped | I/O maps to Network |
| Repository `malformedResponseReturnsInvalidResponse` | JsonDecodingException escaped | Serialization failure maps to InvalidResponse |
| ViewModel `failedLoadPublishesError` | Failure left state Loading | Failure publishes Error |
| ViewModel `retryShowsLoadingThenReceivedContent` | Retry left state Error | Retry publishes Loading, then received Content |
| Home `errorDisplaysFeedbackAndRetryWithoutNavigation` | Error text absent | Resource-based feedback and Retry callback pass on API 37 |
| ViewModel `repeatedLoadAndObservationDoNotReload` | Two first-page requests | Repeated Load/new observation retains one request |

Repository regression run: nine tests passed, including actual pending HTTP cancellation, valid empty success, missing/malformed/absent response and unfiltered 404 remaining Service. These already-supported cases are regression evidence, not fabricated RED cycles.

Additional TDD slices: `loadingDisplaysSkeletonsWithoutCharactersOrRetry` failed on absent loading semantics, then passed with the accepted noninteractive skeleton grid; `validEmptyCataloguePublishesEmpty` failed with Content([]), then passed with Empty; `emptyCatalogueHasFeedbackWithoutRetryOrBack` failed on absent empty feedback, then passed on API 37.

The Home ViewModel suite passes seven tests covering received data, empty/error, synchronous retry progress, failed retry recovery, idempotent load/observation, repeated pending actions and owner cancellation. The seven-test Home screen suite passes on API 37 with controlled images: content/ID, skeletons, error/Retry, empty feedback, pending → success/failed/absent portraits preserving metadata and selection, narrow/2×-text reachable Retry, and the actual HomeRoute → ViewModel retry wiring. A Coil fixture constructor correction was a compilation fix, not behavioral RED. Distinct domain contracts/model types now have separate files; sealed Success/Failure stay with their result.

### Implementation ownership

- Domain: `CharactersRepository` is the suspending interface; `CharactersPageResult` owns Success/Failure; `CharacterRequestFailure` classifies failures; `CharacterSummary`, `CharacterStatus` and `CharacterPage` carry pure values in separate files.
- Data: internal `CharacterPageDto`, `PageInfoDto` and `CharacterDto` decode the summary payload. `CharactersApi` and its factory share the real serialization configuration between production and HTTP tests. `RemoteCharactersRepository` maps status/page metadata and translates specific serialization/I/O/HTTP failures while allowing cancellation to propagate. `CharactersBindings` binds the implementation; `CharactersNetworkModule` owns the singleton API client/base URL.
- Home: `HomeUiState`/`HomeAction` define renderable outcomes and commands. `HomeViewModel` initializes once, accepts Retry only from Error and owns requests in `viewModelScope`. `HomeRoute` performs initialization and lifecycle-aware collection. `HomeContent` renders the adaptive lazy grid or scrollable feedback; `HomeLayoutTokens` owns dimensions and thresholds; `HomeContentPreviews` supplies IDE-only state examples. Accepted card/portrait/skeleton components are reused.
- App: `RickAndMortyApplication` is the Hilt root and retains its shared Coil loader. `MainActivity` is a Hilt entry point and opens Home. Its character callback is connected to detail in C05; C04 verifies the supplied ID at Home rather than adding a fake destination.
- Tests: `RemoteCharactersRepositoryTest` verifies the real HTTP boundary; `HomeViewModelTest` verifies state/actions with `MainDispatcherRule` and a cancellable fake; `HomeContentTest` verifies the whole screen and one route/ViewModel wiring path. No test Activity, production fake or screenshot framework is added.
- Supporting files pin the verified dependencies/compiler plugins, provide English feedback strings and Material Symbols vectors/license, record status and introduce the concise root `AGENTS.md` rules.

### Final validation

Executed through the managed `gradle_run.py` wrapper: `./gradlew qualityCheck :app:assembleRelease :feature:home:connectedDebugAndroidTest`. Exit 0. The gate runs ktlint, Detekt, Android lint, JVM tests and debug assembly; release assembly also passes. XML reports confirm 16 JVM tests (9 repository + 7 ViewModel) and seven instrumented tests on the API 37 emulator, with no failures or skipped cases. Empty test tasks in other modules are not counted as coverage. Instrumentation remains a local command, outside the current CI gate. The Gradle workflow is finished and removed only wrapper-owned logs.

Verification questions and bounded answers:

| Managed-run question | Answer |
|---|---|
| “C04 repository regressions: do success, empty, HTTP/JSON failures and real HTTP cancellation preserve the contract?” | Yes: nine passing repository tests |
| “C04 Home state contract: do content, empty, error, retry, idempotency and cancellation pass?” | Yes: seven passing ViewModel tests |
| “C04 screen integration: do portrait states, reachable large-text Retry and HomeRoute wiring pass on API 37?” | Yes: seven passing Home tests |
| “C04 completion gate: do all JVM tests, formatting, static analysis, Android lint and debug/release builds pass?” | Yes: aggregate gate and release assembly pass |
| “C04 final visual correction: do quality, debug/release builds and all Home API 37 tests pass after explicit feedback title color?” | Yes: final aggregate gate and seven Home tests pass |

Production APK inspection on API 37 verified real API portraits/metadata, scrolling to the final first-page row, readable system-safe card metadata, genuine disconnected Error → network restored → Retry → Content, and one-column layout at 320dp width with 2× font size. The error-title contrast issue found during visual inspection was corrected with the theme's explicit `onBackground` role and visually rechecked. Original emulator network/font/size settings were restored. This is manual visual verification, not screenshot regression coverage or a performance benchmark.

OpenSpec strict validation and local-link/source-token checks pass. No commit or push was performed. Material Symbols assets come from the [Google icon catalogue](https://developers.google.com/fonts/docs/material_symbols).

### Current results rendering

Home renders its Loading, Content, Empty and Error branches directly. Skeleton pulse and Coil portrait crossfade remain local to their existing components. C11 evaluates additional UI animations after screen integration.

Revalidation through `gradle_run.py`:

| Managed-run question | Command and bounded answer |
|---|---|
| “¿Pasan los siete tests de Home en API 37 tras retirar el fundido?” | `./gradlew :feature:home:connectedDebugAndroidTest`: seven passing tests; no failures or skipped cases |
| “¿Pasa C04 el control de calidad y los builds debug/release sin el fundido?” | `./gradlew qualityCheck :app:assembleRelease`: quality gate and debug/release assembly pass |

The workflow is finished and removed only wrapper-owned logs. OpenSpec strict validation, 72 local documentation links and source/token checks pass. The current debug APK is installed on the API 37 emulator. No commit or push has been performed.

### Acceptance and test conventions

The reviewer accepted C04 and authorized its closure on 2026-10-01. Test targets use descriptive `lateinit var` names (`homeViewModel`, `charactersRepository`) separated from mocks/fakes by a blank line. JVM tests use backtick names with uppercase GIVEN/WHEN/THEN and spaces. Instrumented tests retain underscore names because [Kotlin documents spaced method names as supported only from Android API 30](https://kotlinlang.org/docs/coding-conventions.html#names-for-test-methods); the app retains minimum API 26. `AGENTS.md` preserves this convention, and ktlint/Detekt naming exceptions are limited to functions annotated `Test` or `Composable`.

The refactor changes test setup and naming, not production behavior or coverage boundaries. Managed `gradle_run.py` verification:

| Question | Command and bounded answer |
|---|---|
| “¿Pasan los 16 tests JVM y los 7 tests de Home en API 37 con GIVEN/WHEN/THEN y lateinit sut?” | Initial naming/setup pass: affected JVM tasks and Home instrumentation pass |
| “¿Pasan los 23 tests de C04 con nombres descriptivos y backticks en JVM?” | `./gradlew :data:characters:testDebugUnitTest :feature:home:testDebugUnitTest :feature:home:connectedDebugAndroidTest`: 9 repository, 7 ViewModel and 7 instrumented tests pass with the final convention |
| “¿Pasa qualityCheck con la convención definitiva de tests?” | `./gradlew qualityCheck`: pass |

The workflow is finished and removed only wrapper-owned logs. Human acceptance is complete; no commit or push is authorized or performed.
