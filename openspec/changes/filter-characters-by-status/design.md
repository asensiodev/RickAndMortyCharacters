## Context

C07 is accepted and archived. Home already separates raw input from the applied name and guards cards, errors and totals with a generation identity. C08 extends that existing query path; Paging remains the sole item/load/retry owner.

## Decisions

### Typed remote status

Extend the repository page contract with an optional existing domain `CharacterStatus`. Null means no status constraint (All), not the API's Unknown status. Data maps Alive/Dead/Unknown to `alive`/`dead`/`unknown` and omits null from Retrofit query parameters. Every page carries the same normalized name and status.

A recognized first-page no-match 404 becomes an empty page when either name or status is constrained. An entirely unfiltered first-page 404 remains a failure; malformed/unrecognized bodies remain errors. Append-end and cancellation semantics remain unchanged. Verify this distinction through HTTP fixtures.

### One combined query

Extend immutable Home state with the selected status. Reuse `CharacterStatusUi` for presentation selection, with null representing All, and map to the domain status at the repository boundary. Add an explicit status-selection action to the existing Home actions rather than another request controller or event bus.

A changed chip applies immediately using the latest visible name, cancels pending name debounce and starts one combined generation. Keep input text, selection, focus and keyboard as they are. Reselecting the active chip is a no-op: preserve pages/scroll/counter and allow an existing typing debounce to finish normally.

Keyboard Search and suggestions still submit immediately and dismiss the keyboard. Clear removes only name and retains status. All removes only status and preserves name. Deduplication uses the combined normalized name/status identity; a changed constraint resets page one, scroll and total together. Guard obsolete metadata/items/errors, including Alive → Dead → Alive races and old append completion.

### Persistent native chips

Place the horizontally scrollable single-choice row below search, outside the results branch. Initially All is selected. Use Material 3 chip components, readable selected/unselected colour roles and existing status text/dot styling. Selected semantics must expose the active choice; dot colour must not be the only status signal. Keep 48dp interaction targets, resource labels and named tokens. Do not add a title, toolbar or bottom navigation.

No matches applies to name-only, status-only and combined queries, retaining both controls and the four suggestions. Suggestions retain the selected status and do not guarantee a match. Empty unfiltered catalogue remains distinct. Retry repeats the failed operation with its combined query; append failure retains prior matching cards. The counter always belongs to the presented generation and stays hidden under the accepted loading/empty/error/keyboard rules.

## Observable test boundaries

- **Repository HTTP:** MockWebServer verifies each status value, All omission, combined name/status encoding on every page, contextual status-only/combined no matches, unfiltered and malformed errors, append end and cancellation.
- **ViewModel/Paging:** controlled time and fakes verify one immediate combined query when a chip changes during debounce, selected-chip no-op, clear/All/suggestion preservation, page/total reset, obsolete results and cached recollection.
- **Home screen:** API 37 Compose tests exercise the complete search/chips/results surface with controlled images. Verify selected semantics, persistent input/selection/focus, horizontal access at narrow widths/large text, status-only/combined no matches, contextual Retry and counter/scroll reset.
- **Production journey:** filtered later-page result → detail → Back retains query, selected chip, loaded pages and scroll without reopening the keyboard or reloading page one.

Use one observable RED → GREEN slice at a time, then add focused regressions. Existing C07 tests remain applicable and are extended rather than replaced. No isolated chip-gallery activity or screenshot tooling is introduced.

## Verification and review

Run affected repository/Home JVM checks first, then API 37 Home/journey suites. Finish with the documented quality gate, release assembly and a native name/status/clear/All/no-matches/Retry/Back check. Reuse unchanged matching evidence where appropriate. Record actual commands and results. Human acceptance precedes archival and C09 preparation.

## Planning evidence

Codex prepared C08 on 2026-10-01 from the accepted backlog, PRD/UI contract and existing Home state/actions and domain status source. Codebase Memory tools were unavailable; source inspection was used. Scope and test boundaries were approved before implementation.

The provider supports combined name/status parameters: [Rick and Morty API filtering](https://rickandmortyapi.com/documentation/#filter-characters). Existing selected Stitch references remain sufficient; no further design export is needed.

## Counter colour preview — 2026-10-01

Human review authorized trying a darker blue-grey counter surface to distinguish it from card metadata and the screen background. Codex changed only HomeLoadedCounter to use the existing secondaryContainer/onSecondaryContainer pair (#39485A/#D4E4FA), retaining the blue dot and existing layout and behavior.

Managed Gradle validation passed `:feature:home:compileDebugKotlin :app:installDebug` on the API 37 emulator. The first sandboxed attempt could not access the Gradle wrapper lock; this was an environment failure, not behavioral RED. After authorized escalation, compilation and installation succeeded. A native screenshot confirms the new counter surface with live catalogue content. Workflow finish removed only wrapper-owned logs; `git diff --check` passed. This visual trial adds no behavioral tests and does not establish C08 completion or acceptance; the complete gate and human visual acceptance remain pending.

## Implementation and validation record

Codex implemented C08 on 2026-10-01 using the existing repository, Paging generation and native Material 3 components. No dependency, module or route was added. Codebase Memory tools were unavailable; exact source inspection supplied implementation evidence.

Three initial behavior-focused TDD slices were observed through `gradle_run.py`, workflow `fb771935aed705debb03c55b86b0cf17`:

| Boundary | Observed RED | GREEN command |
|---|---|---|
| HTTP repository | The status-filter test received no `status` query parameter | `:data:characters:testDebugUnitTest --tests '*RemoteCharactersRepositoryTest*'` |
| ViewModel/Paging | Selecting Dead during pending name input left the applied name null | `:feature:home:testDebugUnitTest --tests '*HomeViewModelTest*'` |
| Home screen | The All selected chip did not exist | `:feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.rickandmortycharacters.feature.home.composables.HomeContentTest#GIVEN_visible_status_filters_WHEN_Dead_is_selected_THEN_the_screen_exposes_the_selected_constraint` |

Follow-up regression tests cover wire values and omission on both pages; filtered first-page emptiness; active-chip reselection during debounce; Clear/All/suggestion preservation; combined append Retry and cached recollection; late Alive → Dead → Alive totals and obsolete append items; selected semantics, input selection/focus, narrow large-text access and status-only no matches; combined-query Retry and filtered later-page Back retention.

Visual refinement after review uses content-sized chips with single-line labels, a 40dp minimum visible height and the shared medium shape (12dp corners, refined after the screenshot review). The human screenshot reference selects solid unoutlined surfaces, a blue-grey selected surface and a decorative checkmark for the selected option, with status dots on the other options. The row scrolls only when the natural content widths exceed the available space. Native Material 3 retains minimum interactive targets independently of the visible chip height. Status dots reuse character-status tokens; labels stay in resources. The floating counter and result-state contracts remain unchanged.

The screen regression review identified ambiguous text matches after adding status labels and no-match suggestions, and footer tests that tapped Retry before scrolling past the counter overlay. Tests now select the actual input/selected chip semantics and scroll the footer into the unobstructed end position before physical taps. Footer loading/error height changes are exercised without changing production behavior.

A production-journey regression exposed a fourth behavioral RED: changing status after scrolling to page two produced requests `[1, 1, 2, 1, 2]`, including an unintended second page for the new status before the scroll reset. Home now keeps the results in Loading until that generation's grid reset completes, preventing old viewport prefetch hints from reaching the new Paging data. The input and chips stay mounted. The regression asserts the new first-page position, count and request constraints.

Final verification through `gradle_run.py` passed:

- `qualityCheck :app:assembleRelease`: formatting, Detekt, Android Lint, debug/release assembly, 63 JVM tests (27 repository, 28 ViewModel, five PagingSource and three Konsist), zero failures/errors/skips.
- `:feature:home:connectedDebugAndroidTest :app:connectedDebugAndroidTest`: 22 Home screen tests and ten production navigation journeys passed on API 37 after the visual refinement. The seven unchanged Details screen tests passed in the same workflow and are reused; 39 instrumented tests in total, zero failures/errors/skips. Only connected API 37 XML reports are counted; older managed-device output is excluded.
- `openspec validate --all --strict --no-interactive`: nine items passed. Local documentation links and `git diff --check` passed.

The ordinary-width chip test checks the actual final glyph bounds, complete line end and absence of ellipsis. A retained wider paragraph layout can report generic width overflow despite all glyphs fitting; the assertion verifies visible text rather than that flag. At 400dp the full Unknown label remains on one line. The native review confirms the compact pills, centred labels and distributed width.

Native production APK review on API 37 used the real API: Dead loaded 20 of 287; Rick + Dead loaded 20 of 54; opening Adjudicator Rick (ID 8) and returning retained the query and selected chip without the keyboard. Clear restored the status-only total. Unknown plus an unmatched name showed the existing no-match message and shortcuts. A controlled connection interruption produced contextual error/Retry while retaining the combined query; restoring the connection and Retry returned no matches. Airplane mode was restored to its previous disabled state. Clear and All returned the catalogue to 20 of 826, with the keyboard closed. These totals are observations, not hardcoded expectations.

The workflow was finished; cleanup removed only wrapper-owned logs. Human acceptance remains pending; this change is not archived. No commit or push was performed.


## Equal-width filter refinement — 2026-10-01

Human review requested equally wide filters, an intermediate height and squarer corners. HomeStatusFilters now measures all resource labels using the active typography and density, reserves dot/gap and Material chip padding, and applies one common width. When that width cannot fit four chips, the row scrolls instead of shrinking or truncating the text. The minimum visible height is 40dp and the shared small shape supplies 8dp corners. Native Material touch expansion remains enabled.

Managed Gradle workflow `1d53cb41cb4a3cc493b00d458bb01d19` observed behavioral RED in the ordinary-width Home screen test when the original weighted widths differed. The first equal-width test attempt incorrectly compared viewport-clipped bounds; it was corrected to scroll each chip fully into view before comparing. A second assertion incorrectly measured the visible 40dp height instead of touch bounds; `assertTouchHeightIsEqualTo(48.dp)` verifies the actual native target. The final assertions also verify complete single-line glyphs at font scale 2, equal widths, reachability and selected semantics. All eight HomeSearchTest instrumented tests passed on API 37 before the final source simplification.

The completion question “Do the completion gate and release build pass after the filter refinement?” passed through managed `qualityCheck :app:assembleRelease` after resolving Detekt length/line limits and a trailing blank-line formatting violation. One combined installation/instrumentation attempt lost its Compose activity in an unrelated search-generation test (`No compose hierarchies found`); subsequent installation and instrumentation are run sequentially. This is recorded as a harness/environment failure, not behavioral RED.

Final source verification passed managed `:feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.rickandmortycharacters.feature.home.composables.HomeSearchTest`: eight tests, zero failures/skips. The question “Do equal-width filters keep complete labels, 48dp touch targets, large-text access and selection?” is answered yes. Managed `:app:installDebug` also passed, answering “Can the updated app be installed for native visual review?” yes. Native API 37 screenshots confirm the 40dp squarer equal-width chips and horizontal access to the complete Unknown label on the production catalogue; the rightmost chip is partially outside the viewport initially and fully visible after swiping. `openspec validate filter-characters-by-status --strict --no-interactive` and `git diff --check` passed. Human visual acceptance and a TalkBack journey remain manual; no archival, commit or push occurred. Workflow finish removes only wrapper-owned logs.


## Content-width screenshot refinement — 2026-10-01

Human review rejected hiding the fourth filter solely for equal widths, then supplied a screenshot of solid rounded chips with a selected checkmark. This supersedes the equal-width visual trial above. Codex removed the text measurer, explicit widths and constraint wrapper, allowing Material chips to size to their labels/indicators with consistent padding. It retained 40dp minimum visible height and native touch targets, and used existing large shape, labelLarge typography, surfaceContainer and secondaryContainer/onSecondaryContainer roles. The selection check is decorative; selected semantics and visible status names remain authoritative.

Managed workflow `d89342c40441e0edff90cd394f5d7edf` observed RED in the revised 400dp screen test: expected zero horizontal overflow, received 85px. Natural-width chips then passed all eight HomeSearchTest instrumented tests on API 37. The revised test verifies all four fit without scrolling at ordinary width, complete Unknown glyphs, and 48dp touch height; the large-text test still checks complete labels, horizontal reachability and selection at font scale 2. The supplied screenshot styling was applied after that initial GREEN and receives its own final verification below.

The screenshot-styled variant passed the eight API 37 HomeSearchTest cases. Managed `:app:installDebug` passed; native review of the installed production app confirms all four chips are fully visible together, complete Unknown text, solid surfaces and the selected All checkmark. The first final gate detected ktlint formatting errors in concurrently modified HomeContent/HomeContentTest files outside this chip edit; those files were observed corrected before retrying the aggregate. Codex did not edit those concurrent changes. Native screenshot: `/private/tmp/rickmorty-reference-chips.png`.

The final aggregate question “Do the screenshot-styled chips pass the completion gate, release build and screen tests?” is only partially verified: the eight chip/search screen tests pass with zero failures/errors, but `qualityCheck` remains blocked by Detekt LongMethod in concurrently edited `HomeContentTest.kt:275` (64 lines, limit 60), outside this edit. No changes were made to that test. The latest aggregate exit is not claimed as a completed release/gate pass. OpenSpec validation and diff whitespace checks passed. Human acceptance/TalkBack review remain manual. No archive, commit or push occurred. Workflow finish removes only wrapper-owned logs.


## Card metadata typography refinement — 2026-10-01

Human review requested slightly larger species and Alive/Dead/Unknown labels inside character cards. Codex changed only CharacterCard species from bodySmall (12sp) to the existing bodyMedium role (14sp), and its private status badge from labelSmall (11sp) to labelMedium (12sp). The shared typography definitions, card title and filter chips are unchanged. This is a visual refinement; no behavioral RED or new implementation-mirroring test is fabricated. Existing same-row long-species and enlarged-text final-card/Retry clearance regressions validate the affected layout.


## Stable pagination Retry — 2026-10-01

Human review reported that tapping append Retry moved feedback behind the floating counter. A complete Home screen regression observed behavioral RED: switching Error to Loading moved the feedback centre from 2088.0 to 2119.5px. Loading had a shorter footer than Error, so the lazy grid readjusted its scroll position.

The footer now retains its measured error-text and button slots while loading, hides their visuals and semantics, disables Retry and places progress at the button centre. Another failure restores the button in place; success appends items and removes the footer. Paging remains the sole request/retry owner. No animations or extra request controller were added.

The original screen regression passed after the change. A controlled HomeRoute/Paging regression verifies two failed attempts followed by success, one pending request despite double taps, visible progress, retained cards/count, and repeated Retry without another scroll. It passed individually on API 37. A matching large-text screen regression covers font scale 2. Final aggregate results follow below.

Managed workflow `c5f73cb02c450aef30957466566ce1d3` initially passed the same-row long-species check but failed the enlarged-text footer check because it asserted Retry visibility before performing its existing explicit scroll to footer index 20. The test now performs that scroll before the same visibility and counter-clearance assertions; production footer behavior is unchanged. A diagnostic run with previous card text sizes crashed the instrumentation process, so it does not establish a baseline behavior result. The requested typography was restored before final verification.

Final managed targeted verification passed both selected API 37 tests, confirmed from connected XML: two tests, zero failures/errors. This answers “Do card metadata layout and large-text clearance still pass with larger typography?” yes. Managed `qualityCheck :app:assembleRelease :app:installDebug` passed, answering the completion-gate/release question yes; the matching prior JVM results were reused where Gradle considered them up to date. Native review confirms larger Human/Alive labels fitting together on production cards; the badge change applies equally to Dead/Unknown. Screenshot: `/private/tmp/rickmorty-card-text.png`. OpenSpec strict validation and `git diff --check` passed. No archival, commit or push occurred; human visual acceptance remains pending. Workflow finish removes only wrapper-owned logs.


## Persistent filter/grid spacing refinement — 2026-10-01

Human review requested the same visible gap between input/filters and filters/cards, including while scrolling. Codex removed only HomeGrid's additional 16dp top content padding. HomeStatusFilters retains its symmetric 8dp outer vertical padding and native interactive layout spacing, outside the scrolling results panel. The grid now begins at its viewport edge in both loaded and skeleton states, so the same fixed separation remains when cards scroll beneath that edge. Horizontal grid gaps, inter-card vertical spacing and bottom counter/Retry clearance are unchanged. This low-impact spacing refinement uses compilation/gate and native before/after-scroll review rather than a fabricated behavioral RED or an implementation-mirroring test.

Final managed verification passed `qualityCheck :app:assembleRelease` and the sequential Home, Details and app connected suites: 63 JVM tests and 41 instrumented tests (24 Home, seven Details, ten navigation), with zero failures/errors/skips on API 37. Both normal and large-text stable-footer regressions passed, together with the repeated-failure/success Paging journey. OpenSpec strict validation passed all nine items and diff whitespace checks passed. The native real-network append journey remains unconfirmed because concurrent UI work restarted/reinstalled the shared app during that check; controlled screen/Paging recovery is verified. The connection was restored to its original enabled state. Subsequent spacing refinements are validated by their own workflow. No archival, commit or push was performed.

Managed workflow `b645f3169e242cb8866bcbf41567cba6` passed `qualityCheck :app:assembleRelease :app:installDebug`, answering the gate/release question yes. Native review encountered an offline emulator, so the spacing contract was verified through a controlled Home screen geometry regression instead: equal visible input/filter and filter/card distances, retained filter position and equal gap after scrolling to a later card. That single API 37 test passed. Initial test compilation used the previous CharacterStatusUi type while another agent was migrating to domain CharacterStatus; the fixture was updated to the current model without reverting that migration. Compilation failures are not behavioral RED. The final question “Are input/filter and filter/card gaps equal and unchanged after scrolling?” is answered yes. Human visual acceptance remains pending; no connectivity settings were changed and no archive/commit/push occurred.

Final `ktlintCheck detekt` passed with the new geometry test. Workflow finish removes only wrapper-owned logs.


## Chip corner refinement — 2026-10-01

Human review requested slightly less curved chip corners. Codex changed only HomeStatusFilters from the shared large shape (16dp) to medium (12dp), retaining its size, content widths, colors, spacing and native accessibility. This visual token change introduces no behavior or new test.

Managed workflow `04673c2109c61ec74f87a07150e963a8` passed `:feature:home:compileDebugKotlin :app:installDebug`, answering “Does the 12dp chip radius compile and install successfully?” yes. `git diff --check` passed. Full unchanged behavior checks were not repeated for this visual-only token edit. Workflow finish removes only wrapper-owned logs; human visual acceptance remains pending.


## Rapid-scroll network diagnosis — 2026-10-01

The production API rapid-scroll reproduction reached 340 loaded characters and retained four visible portrait errors after ten seconds. Temporary request listeners captured 138 image failures, all `coil3.network.HttpException: HTTP 429`; page requests in that run returned 200. A further scroll reached 520 loaded characters, with page requests 18–26 returning 200 and 148 additional image 429 responses. These observations confirm server rate limiting for portraits, not a page Retry requesting earlier portraits. The intermittent page failure was not reproduced with the diagnostic build and remains unclassified.

Coil leaves a failed request in Error; waiting does not initiate another request. Disposing and recreating a card initiates another load, explaining recovery after scrolling away and back when the service accepts requests again. Image caching is already configured separately from the planned HTTP JSON cache. No retry policy or server-limit assumptions were introduced. Temporary diagnostic listeners/interceptors were removed after capture.


## Acceptance and publication — 2026-10-01

Human review accepted C08 for commit and push, explicitly excluding C09 and the separate icon/splash work. Final API 37 connected reports contain 25 Home tests, seven Details tests and ten production navigation journeys, with zero failures/errors/skips. The aggregate navigation attempt was interrupted by an instrumentation-process crash during concurrent emulator installation; repeating its ten tests after the device was free passed. The 63 JVM tests, OpenSpec strict validation, staged local documentation links and diff whitespace checks passed. The known portrait HTTP 429 behavior remains deferred without introducing a retry policy. C08 remains in the active change directory until a separate archival step.
