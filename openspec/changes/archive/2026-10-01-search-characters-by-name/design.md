## Context

C06 is accepted and archived. Home currently owns one cached unfiltered Pager; the repository accepts only a page number. HomeRoute prioritizes existing items over refresh state, so adding a new query must explicitly prevent old cards from appearing as new matches. UI/UX Definition owns the visual contract.

## Decisions

### Remote request boundary

Extend `CharactersRepository.getPage` with an optional name, keeping the page result pure Kotlin. Data sends it through Retrofit `@Query("name")` on every page. Trim request names and omit blank names; preserve the raw visible input and do not introduce undocumented case conversion. No forwarding use case or speculative status query model is needed.

A recognized HTTP 404 `There is nothing here` for a nonblank first-page name maps to an empty page with total zero and no next page. An unfiltered first-page 404 remains a failure. The existing recognized append-end outcome remains terminal; unknown/malformed 404 and server/network failures remain errors. Cancellation propagates. The contextual 404 mapping requires deterministic repository fixtures; it is not inferred from every 404.

### Draft and applied query

Keep visible input separate from the normalized applied name. Typing waits 300 ms after the latest edit. Keyboard Search and suggestion actions cancel the pending debounce and submit immediately through the same deduplicated path. Applying an equivalent normalized name does not reload or reset scroll, including while a request is pending; explicit Retry remains responsible for a failed operation.

Clear empties the input and applies the unfiltered query immediately, retaining focus for further typing. During debounce the previous applied result may remain visible. Once a different name is applied, hide previous cards and total, show query loading and reset scroll once. Empty results must only follow a completed empty request, not Paging's transitional initial state.

### One paginated owner

Use an applied-query flow to switch Pager generations with latest-wins cancellation, then map card models and cache the resulting stream in `viewModelScope`. Each PagingSource captures its name for every page. Paging remains the item/load/retry owner; do not introduce a copied accumulated list or a parallel network job for search.

Associate presentation metadata with a generation identity so late totals and items cannot overwrite the active generation, including A → B → A. Pair the presented generation and total before exposing the counter. Discard old-generation callbacks. Retain append progress/error behavior within the active query and keep Retry scoped to that generation.

### Persistent native controls

Place search outside the results-state branch, first below system safe insets. Add no title, top app bar or inactive status chips. C08 will place chips underneath it. Compose owns input selection/focus, keyboard and grid state; ViewModel owns draft/applied query coordination. Loading, empty and error replace only results. Keep feedback and Retry reachable with the keyboard open; the counter stays hidden while the IME is visible.

Keyboard Search and suggestions dismiss the keyboard and clear focus. Clear does not force dismissal. Opening detail dismisses the keyboard; normal Back retains query, pages and scroll without restoring input focus. Keep strings in resources, named tokens, accessible search/clear descriptions and 48dp interactive targets.

Filtered empty results show “No characters found” and the existing UI/UX recovery message, plus Rick/Morty/Beth/Summer text buttons. A shortcut updates input and submits immediately using the normal path; no recommendation endpoint, guaranteed result or new navigation is involved. Unfiltered emptiness keeps “No characters available.” Errors offer contextual Retry rather than suggestions.

## Observable test boundaries

- **Repository HTTP:** MockWebServer verifies encoded name/page parameters, blank omission, name retained on append, recognized filtered-first-page emptiness, unfiltered and malformed 404 failures, append end and cancellation.
- **ViewModel/Paging:** controlled coroutine time and repository fakes verify debounce, immediate submission without a delayed duplicate, normalized deduplication, clear, page/total reset, cancelled/late results and A → B → A isolation. Reuse Paging snapshot tests for active-query append/retry and recollection.
- **Home screen:** API 37 Compose tests exercise the implemented search/results surface with controlled images. Verify input text/selection/focus survives result transitions, IME Search, clear, no-match shortcuts, query error/Retry and keyboard counter visibility. Test screen behavior rather than isolated input components.
- **Production journey:** query → later page → detail → Back preserves the applied query, loaded result and browsing position without keyboard reopening or a first-page reload.

Implement one observable RED → GREEN slice at a time. Compilation/environment failures are not behavioral RED. At proposal preparation, no search checks had run. Executed implementation evidence follows below.

## Verification and review

Run affected repository and Home JVM tests first, then API 37 screen/journey tests. Finish with the documented quality gate, release assembly and a native search/clear/no-matches/retry/Back check. Review visual spacing against the selected design; no additional Stitch export is needed. Human acceptance precedes archival.

## Planning evidence

Codex inspected the current repository, HTTP API, HomeViewModel and HomeRoute source on 2026-10-01 to prepare this increment. Codebase Memory tools were unavailable; source inspection was used. This records a proposed design, not implementation evidence.

The API supports remote name filtering and paginated metadata: [Rick and Morty API](https://rickandmortyapi.com/documentation/#filter-characters). Generation switching and ViewModel-scoped caching follow the Paging flow model: [Android Paging guidance](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data). Native behavior and race protection must still be verified through the boundaries above.

## Implementation and validation record

Scope and the repository, ViewModel/Paging, Home-screen and production-journey test boundaries were approved on 2026-10-01. Codex implemented C07 using the existing dependencies and modules, with focused TDD/Kotlin/Compose guidance. No application dependency on AI tooling was introduced.

### Observed behavioral RED → GREEN

| Boundary | Observed failing behavior | Passing implementation |
|---|---|---|
| Repository HTTP | A recognized first-page name no-match response returned a failure | Optional encoded name requests and contextual empty-page mapping |
| Home ViewModel | Successive edits did not apply the final name after debounce | Owned 300 ms debounce and normalized applied-query state |
| ViewModel/Paging | Submitting another name reused the old cached catalogue | Query-scoped Pager switching, generation-safe totals and card/error identity |
| Home screen | Search was absent across result-state changes | Persistent native input above the results, preserving focus/selection |
| Home no matches | The fixed name shortcut could not be selected | Four resource-backed suggestion buttons through immediate submission |

Additional regression tests cover encoded/blank/page names, malformed filtered errors, equivalent query deduplication, clear, named append/retry, cooperative cancellation and late A → B → A results. Screen tests cover real IME submission, query loading without old cards/counters, focused selection across loading/empty/error, double Retry, suggestions and clear. Production journeys cover searched later-page Detail → Back and scroll/page reset when another name is applied. These regression tests are not recorded as separate observed RED cycles.

### Commands and results

All Gradle invocations used the managed `gradle_run.py` wrapper. Workflows `4f2c8526dce31ec548066c5e724a6a28` and `3f8827c75c8932c53bdaebcf6ac1ef60` are recorded with their bounded results; full logs are temporary wrapper-owned artifacts.

- `./gradlew :data:characters:testDebugUnitTest --tests '*RemoteCharactersRepositoryTest*'`: observed behavioral failure, then passed with contextual no-match/name fixtures.
- `./gradlew :feature:home:testDebugUnitTest --tests '*HomeViewModelTest*'`: observed debounce and query-replacement failures, then passed. The cached-generation snapshot test explicitly advances the controlled scheduler before observing the replacement; an intermediate helper index exception was not treated as a behavioral RED.
- `./gradlew :feature:home:testDebugUnitTest`: passed with the complete paging/query suite.
- Targeted `:feature:home:connectedDebugAndroidTest` runner filters for persistent input and fixed suggestion selection: each observed the missing behavior, then passed on API 37.
- `./gradlew qualityCheck :app:assembleRelease`: passed with 54 JVM tests: 24 repository, 22 ViewModel, five PagingSource and three Konsist checks, zero failures/errors/skips. Formatting, Detekt, Android lint and debug/release assembly passed.
- `./gradlew :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest`: 19 Home + seven Details + eight production journeys passed together on API 37, zero failures/skips. A subsequent additional production journey checks scroll reset on a changed name. `./gradlew ktlintCheck detekt :app:connectedDebugAndroidTest` passed all nine journeys with zero failures/errors/skips. Final coverage is 35 instrumented tests: 19 Home, seven Details and nine production journeys; no production source changed after the complete gate. Both managed workflows were finished, removing only wrapper-owned logs.

Formatting used the pinned ktlint 1.8.0 CLI on JDK 17. Build/configuration corrections were not counted as behavioral RED. Instrumented tests still run locally, outside the current CI quality gate.

### Native production check

The debug APK was installed and launched on the API 37 emulator with the real API. The unfiltered first page reported 20 of 826; searching Rick reported 20 of 107. The software-keyboard Search action dismissed the IME and restored the floating counter. Opening character ID 1 and returning retained Rick and the same count without reopening the keyboard.

Temporary airplane mode caused a new name request to show the contextual error with its input preserved. Connectivity was restored; explicit Retry completed as no matches with the four suggestion buttons and no counter. Clear was also checked against the live API: it removed Rick and restored the unfiltered 20-of-826 result with the keyboard dismissed. The emulator's airplane-mode setting was restored to its original disabled value. Captures and hierarchy evidence are temporary files under `/private/tmp/c07-*`, not production assets or committed screenshots.

### Acceptance

Human acceptance was received on 2026-10-01, with explicit authorization to archive, commit and push C07, then prepare C08. Status chips are the next increment; caching retains its separate change.
