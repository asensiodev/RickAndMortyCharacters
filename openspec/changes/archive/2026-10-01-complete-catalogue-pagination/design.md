## Context and scope

C04 provides `CharactersRepository.getPage(page)`, `CharacterPage(characters, totalCount, nextPage)` and first-page Home states. C05 retains Home's ViewModel and grid position on normal return from Detail. C05A protects implementation visibility and explicitly typed read-only ViewModel state.

C06 completes unfiltered remote pagination and its feedback. Product/search/filter contracts remain in PRD/UI_UX; this change owns the pagination implementation and tests. Search, status selection, HTTP caching and recreation behavior retain their later increments.

## Ownership and flow

```text
HomeRoute → cached Home paging flow → Pager / Home PagingSource
                                      → CharactersRepository.getPage(page)
                                      → existing Retrofit data implementation
HomeContent ← presented items, load states and same-generation total
```

Use Paging 3 rather than maintaining a second request queue and accumulated list. Pin the compatible stable Paging runtime, Compose and test artifacts during setup, verifying them against the existing toolchain. No Paging dependency enters domain or data. The feature-owned adapter is internal and calls the injected repository directly.

The ViewModel owns a single Pager flow, maps summaries to card models and uses `cachedIn(viewModelScope)`. Recomposition and repeated UI collection do not create new Pagers or restart page 1. HomeRoute collects with the standard Paging Compose integration; it does not start a competing catalogue collector. Existing initial-load/retry actions are adapted or removed where Paging already owns that operation. Use explicit action/callback boundaries for Retry and selection; keep navigation in app.

Paging is the only owner of the loaded items and request/load states. The existing read-only `state` can retain immutable metadata, including a nullable total; it must not copy Paging items or independently track refresh/append jobs. The screen derives Loading/Content/Empty/Error from presented items and load states. A rendering projection is allowed; a competing loader is not. Keep the existing Konsist rule meaningful with explicitly typed outward properties.

Start at page 1. Configure page size and initial load size as 20, matching the page-number API, with placeholders disabled and a small named prefetch distance. Each adapter load requests one API page and follows `nextPage`, rather than treating `loadSize` as an API offset or inventing an additional request. Pagination is forward-only. Refresh-key fallback starts from page 1; there is no user refresh operation in C06. Retain loaded pages for this bounded catalogue, without eviction that makes the loaded counter decrease. Do not claim memory/performance improvements without measurements.

## End and failure mapping

Extend `CharactersPageResult` with a separate confirmed-end outcome rather than fabricating an empty page with a zero total. Data may produce that outcome only for page > 1 with HTTP 404 and the recognized API error body `There is nothing here`. Close consumed error bodies. An unrecognized/malformed 404, first-page 404, 5xx, invalid successful body or I/O failure remains the existing typed failure. Cancellation propagates.

The PagingSource translates a successful page using its next-page key, confirmed end into an empty terminal append without changing the total, and typed failure into a Paging load error with no transport object leakage. A successful page with `nextPage == null` ends normally; no speculative extra HTTP request is made. Keep the total from the first successful response of this generation; do not overwrite it with a fabricated append-end value. The API does not guarantee a transactional snapshot during browsing; C06 adds no reconciliation service or fabricated counts.

## Home states and interaction

- Initial loading/error/empty retain the accepted C04 skeletons, in-place feedback and contextual Retry.
- Append loading keeps every existing card and adds one full-width grid footer indicator.
- Append failure keeps cards and displays `Couldn't load more characters` plus footer Retry. Retry repeats the failed page; it does not reload page 1 or navigate.
- Once Retry is pending, replace/disable its action so repeated taps cannot queue duplicate requests. Verify request counts through the repository boundary, including repeated scroll hints.
- End removes progress/Retry; cards and the counter remain. Selecting any loaded card still opens its ID, including while append is pending or failed.
- Returning from Detail preserves loaded data and grid position. A pending load is cancelled when its actual ViewModel owner clears; merely navigating to Detail does not destroy Home.

Use existing tokens, card metadata surfaces, shared image loader, adaptive columns and colocated previews. Add only product footer/counter composables and English string resources. Footer items span all columns and do not count as characters. Stable character IDs and content types remain in use.

## Counter

Read loaded count from real presented items, not visible cards, `info.count`, placeholders or a copied list. Read total from API metadata for the same generation. Use `Loaded {loadedCount} of {totalCount} characters`; test illustrative fixture values rather than hardcoded production totals.

Keep the subdued rounded overlay and blue dot above system navigation insets. Show it with nonempty content and a known total, including append progress/error and end. Hide it in initial loading/error/empty and when the IME is visible. Connectivity snackbar integration remains its own change; do not introduce an unused monitor or snackbar host here. A future query generation must own/reset its metadata alongside its Pager; no speculative query model is added in C06.

Provide sufficient bottom content padding for the final row and footer to scroll completely above the counter, including large text. The pill is read-only, has no click action and wraps or sizes naturally. It does not replace pagination feedback or obscure Retry.

## Test boundaries and TDD slices

1. **Data HTTP contract:** extend MockWebServer repository tests for requested page parameters, terminal `next == null`, recognized append 404, unexpected/malformed append 404, 5xx and cancellation. Preserve first-page/detail regressions. Request counts and returned domain results are the observations.
2. **PagingSource contract:** controlled repository pages verify next-key traversal, terminal mapping, typed error and propagated cancellation. Test observable LoadResult values; do not assert library internals.
3. **ViewModel/Pager integration:** use coroutine test scheduling and Paging's `paging-testing` snapshot helpers to traverse several pages, retry a failed append and observe exact page requests. Confirm retained content, no overlapping duplicate page work, cached recollection and cancellation on owner clear. Keep descriptive `homeViewModel` setup and GIVEN/WHEN/THEN names.
4. **Real Home screen:** deterministic Paging data/load states and a controlled image loader verify initial states, full-width append feedback, Retry progress, real counter, image-independent selection and adaptive layout. At least one test uses a real Pager plus fake repository to exercise scroll-triggered loading and footer Retry, rather than only manufactured snapshots.
5. **Production navigation journey:** extend the existing Hilt test repository with multiple pages; load later cards, open Detail and return with the same loaded count and grid position. No production fake/activity is introduced.

Take one behavior slice through observed RED → GREEN → refactor before the next. A missing dependency/compiler failure is not behavioral RED. Do not delete existing behavior coverage just because the state API changes; port it to the new contract. Screenshot verification is not selected here.

## Validation and acceptance

Run affected repository, Home/Paging and architecture JVM tests first through the managed Gradle wrapper. Run affected Home and navigation instrumentation on API 37 with deterministic fixtures. Complete `qualityCheck` and `:app:assembleRelease`; record actual commands, test results and manual checks. Manually inspect live scrolling, append feedback/Retry with controlled failure, last-card/footer clearance, large-text counter and Detail → Back. A live API probe alone cannot prove error behavior.

Validate this OpenSpec change and documentation links. Do not mark implementation tasks complete before their checks. Human review precedes archival and C07. Commit and push require fresh explicit authorization.

## Planning and AI record

Codex inspected the accepted documents and implemented repository/Home/navigation contracts to prepare this proposal. Paging ownership and test seams follow the official guides below. At proposal preparation, no C06 implementation or runtime validation had occurred. Execution evidence follows below.

- [Load and display paged data](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data)
- [Paging load states](https://developer.android.com/topic/libraries/architecture/paging/load-state)
- [Test Paging implementations](https://developer.android.com/topic/libraries/architecture/paging/test)

## Implementation record

Scope and test boundaries were accepted on 2026-10-01. Codex assisted implementation through the agreed repository, Paging, screen and navigation seams. Production source remains free of runtime fixture/demo activities; no commits or pushes are authorized for this increment.

Paging 3.5.1 was selected from the [official stable release notes](https://developer.android.com/jetpack/androidx/releases/paging) and resolved with the existing toolchain. Home owns runtime/Compose dependencies and test-only Paging helpers; domain and data remain Paging-free.

### Observed TDD evidence

| Slice | Behavioral RED | Subsequent GREEN |
|---|---|---|
| Repository append completion | Recognized page-2 list-end returned Service rather than EndOfCatalogue | Recognized end passes; first-page errors and detail regressions remain covered |
| PagingSource page adaptation | Compilable adapter returned Error rather than the requested real page/next key/total | Page mapping passes; terminal/error/cancellation regressions also pass |
| ViewModel paginated stream | Compilable empty paging flow returned no Rick card from the repository | Pager/cachedIn integration passes through real snapshot collection |
| Home append failure | API 37 screen could not find the append-error message or footer recovery | Retained card selection and footer Retry pass |
| Loaded counter | API 37 screen had no `Loaded 20 of 40 characters` indicator | Real loaded count and noninteractive semantics pass |

Compiler/setup failures are not recorded as behavioral RED. All Gradle runs use the managed wrapper; completed suite/gate counts and manual evidence follow.

### Ownership and changed files

- `CharactersPageResult` adds a confirmed-end variant; `RemoteCharactersRepository` recognizes the contextual append body while preserving first-page/detail failure rules and cancellation.
- `CharactersPagingSource` adapts the pure page results and publishes the first successful page total. `HomeViewModel` replaces manual request/list ownership with one cached Pager and read-only `HomePagingState` metadata.
- `HomeRoute` collects Paging items and load states, deriving the render-only `HomeUiState`. Its Content list references Paging's item snapshot directly, without a copied accumulation. The card slot performs indexed Paging access during lazy rendering so scrolling supplies normal prefetch hints; keys come from the snapshot without triggering loads for every key.
- `HomeUiState` retains loading/content/empty/error rendering and adds total/`HomeAppendState` feedback. The obsolete manual Load/Retry ViewModel actions are removed; Retry now belongs to the Paging receiver.
- `HomeContent` retains the real grid and first-page feedback, adding full-width append feedback and measured overlay clearance. Its small card-content slot preserves plain state-driven tests/previews while production wiring supplies Paging access. `HomeLoadedCounter` renders the read-only pill; `HomeLayoutTokens` supplies its dot and footer progress dimensions.
- Repository, PagingSource and ViewModel tests exercise HTTP/stream contracts. Home screen tests cover real rendering plus one Pager/fake-repository flow. The test-only journey repository supports several pages and the production-Activity journey verifies a final-page character and Back.
- String resources and the version catalogue/Home dependencies support the feature. Preview fixtures remain colocated with their rendering composables.


### Completed validation — 2026-10-01

Every Gradle invocation used `gradle_run.py` under one persistent diagnostic owner. The final aggregate `./gradlew qualityCheck :app:assembleRelease :app:installDebug` passes, covering ktlint, Detekt, Android Lint, debug/release assembly and 43 passing JVM tests: repository 12, details repository 8, PagingSource 5, Home ViewModel 8, Details ViewModel 7 and Konsist 3. No test failures, errors or skips were reported.

After the final production rendering refactor, `:feature:home:connectedDebugAndroidTest :app:connectedDebugAndroidTest` passes on API 37 with 12 Home and seven navigation tests. One subsequent test-only addition passes via `ktlintCheck detekt :feature:home:connectedDebugAndroidTest` filtered to `HomeContentTest#GIVEN_keyboardVisible_WHEN_counterRenders_THEN_hiddenUntilDismissal`. Thus 20 affected instrumented tests are verified across those executions, not claimed as one final XML suite. The seven unchanged Details instrumented tests were last executed in C05. `:app:installDebug` then restores the real production debug APK for native review.

Native API 37 review confirms initial `Loaded 20 of 826 characters` with real portraits. Controlled connectivity loss retains 240 loaded cards, displays append error and a reachable footer Retry above the counter. After connectivity restoration and Retry, the error clears and loading reaches 280/826. Font scale 1.5 changes the grid to one column, with readable card metadata and counter; the baseline 1.0 setting and prior connectivity settings were restored. Final-page completion, last-card clearance, duplicate pending Retry and Detail → Back position are verified with deterministic API 37 fixtures rather than claiming a manual traversal of all 826 characters. The real keyboard test opens an IME through a test-only input host and verifies counter hiding/restoration; no search field is added to production before C07.

OpenSpec strict validation, local Markdown links and `git diff --check` pass. C06 remains active pending human acceptance; no commit or push is made for this increment.


### Test-readability review

Human review requested a consistent field layout and sentence-style test names. All 70 names now express GIVEN context, WHEN action and THEN observable outcome with verbs. JVM names use backticks and instrumented names use word-separated underscores. Dependencies are grouped as initialized `val` fields, with framework-dependent image loaders and the Konsist scope initialized lazily. Only the named subject uses `lateinit`, after a blank separator. Repository/server and ViewModel/fake setup retain per-test isolation. Navigation uses the test's `@BindValue` repository instance through the replacement module, preserving shared observations across real screen owners.

The three Konsist rules retain type-based visibility/read-only checks and explicit nonempty source/declaration assertions. Their setup no longer needs a mutable scope or Before method. MainDispatcherRule changes are formatting only. AGENTS.md records these conventions. Test sources use ktlint's 140-character line limit; Detekt's overlapping line-length rule remains applied to production, with other Detekt rules still applied to tests.

Managed validation runs `:data:characters:testDebugUnitTest :feature:home:testDebugUnitTest :feature:details:testDebugUnitTest konsistCheck ktlintCheck detekt`, with the final Detekt policy checked through a focused `detekt` execution. The 43 JVM/architecture tests pass, ktlint passes and Detekt reports no violations. A subsequent combined `:feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest` run verifies all 27 instrumented tests on API 37: Home 13, Details 7 and navigation 7, without failures/errors/skips. No behavior assertions or production code are removed by this readability refactor. The managed workflow is finished and only its owned logs are removed. No commit, push or archival occurs.


### Card subtitle and source formatting review

The reported long-species layout was reproduced through Home's approved screen seam: the new regression failed because the status badge moved below the species. CharacterCard now uses a fixed Row; the species takes remaining width with one-line ellipsis, while the badge reserves its own width and stays on that row. Full species text remains in semantics and detail. CharacterCardTokens names the one-line metadata limit. Names retain the reviewed two-line ordinary-size policy and expanded large-text behavior. The same test then passes, checking vertical alignment, one line and actual ellipsis through the rendered text layout.

The agreed formatter limit is explicit: production Kotlin/Kotlin DSL 120 characters, test sources 140. Sources were formatted consistently; the DetailsViewModel supertype now fits on the declaration line. Those supporting source edits change formatting, not behavior.

Managed validation passes `qualityCheck :app:assembleRelease :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest`: 43 JVM/architecture tests and 28 instrumented tests on API 37 (Home 14, Details 7, navigation 7), with zero failures/errors/skips. The quality gate includes ktlint, Detekt, Android Lint and debug assembly; release assembly also passes. The workflow is finished, removing only wrapper-owned logs. Human acceptance is still pending; no commit, push or archival occurs.


### Shared coroutine test support

Human review identified identical MainDispatcherRule copies in Home and Details. The single unchanged JUnit 4/StandardTestDispatcher implementation now lives in Kotlin/JVM `:core:testing`; both feature test suites import it through `testImplementation`. The two local copies are removed. The support module exports the JUnit/coroutines-test types present in its public API and introduces no runtime feature dependency, Android manifest or product-domain dependency. The production dependency graph remains the six-module graph; the seventh module is test support. Its `check` participates in `qualityCheck`.

Managed targeted verification passes `:core:testing:build :feature:home:testDebugUnitTest :feature:details:testDebugUnitTest konsistCheck ktlintCheck detekt`, including 20 Home/Details JVM tests and three Konsist checks. `qualityCheck` then passes with the support module integrated. The module's empty own test task is not claimed as behavioral coverage: existing consuming suites validate the rule's real lifecycle use. The workflow closes and removes only its owned logs. No commit, push or archival occurs.


### Module documentation and typed project accessors

The architecture table and graph now include the test-support module, showing six production modules and dotted test-only dependencies to `:core:testing`. README and Development describe the same distinction. The accepted foundation archive remains a historical record of the original six production modules.

`settings.gradle.kts` enables `TYPESAFE_PROJECT_ACCESSORS`; all 12 local module dependency declarations now use generated `projects.*` accessors, including `testImplementation(projects.core.testing)` and `implementation(projects.domain.characters)`. External libraries/plugins retain `libs.*` catalogue access. This is a Gradle capability independent of convention plugins; the dependency scopes and runtime graph are preserved. See [Gradle project accessors](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html#sec:type-safe-project-accessors).

Managed `qualityCheck :app:assembleRelease` passes after regenerating accessors and compiling the build scripts. OpenSpec strict validation, 90 local documentation links and diff cleanliness pass. Workflow finish removes only wrapper-owned logs. No commit, push or archival occurs.


### Acceptance

Human review accepted C06 on 2026-10-01 and explicitly authorized closure, commit and push, followed by preparation of C07. All implementation tasks are complete. The final source/build state passes qualityCheck and release assembly; 43 JVM/architecture and 28 API 37 instrumented tests are verified as recorded above. Archive the accepted requirements before preparing the next change.
