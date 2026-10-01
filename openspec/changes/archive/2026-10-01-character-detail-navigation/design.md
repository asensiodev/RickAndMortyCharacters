## Context and scope

C04 is accepted and archived. Home exposes an ID callback, a repository-backed ViewModel and a lazily rendered first page. The Details module is still a scaffold, and MainActivity leaves selection unconnected. This change completes one user flow across those existing modules.

Read [architecture](../../../../docs/ARCHITECTURE.md), [UI/UX](../../../../docs/UI_UX.md#detail-hierarchy-and-components) and the selected [content](../../../../docs/design/stitch/details/content/screen.png), [loading](../../../../docs/design/stitch/details/loading/screen.png) and [error](../../../../docs/design/stitch/details/error/screen.png) references. Preserve the rendered palette and remove the unsupported Share glyph from the loading reference. Back is the only screen-level detail action.

## Detail contracts and data ownership

Add `CharactersRepository.getDetails(characterId)` and separate pure Kotlin detail/result files in the existing domain `model` and `repository` packages. Reuse `CharacterStatus` and `CharacterRequestFailure`. A detail value contains ID, name, status, species, gender, nullable/optional type, origin name, last-known-location name, episode-reference count and nullable portrait URL. Return explicit Success, NotFound or Failure outcomes. Navigation and transport types stay outside domain.

Data adds an internal detail DTO and a `character/{id}` operation on the existing API client. Map the returned episode array length without fetching its URLs. Preserve supplied unknown values; normalize their visible label to “Unknown”. An empty type omits its row. Missing/failed images remain portrait outcomes, not a character-data error. Keep the summary page operation and its first-page 404 behavior unchanged.

For detail, map a 404 with the recognized `{"error":"Character not found"}` response to NotFound. An unrecognized/malformed HTTP error stays a service failure; successful malformed/absent required data stays InvalidResponse. Network failures stay typed failures, and cancellation propagates. Close error bodies after reading. Use the existing singleton API client, without new caches or another HTTP stack.

The [official character schema](https://rickandmortyapi.com/documentation/#character-schema) owns the fields. Sample counts and portraits are fixtures/design references, never constants in production behavior.

## Details state, routing and packages

Use Details Loading, Content, Error and NotFound states, explicit Load/Retry actions, read-only StateFlow and requests owned by `viewModelScope`. The route supplies the destination ID; initialize idempotently with `LaunchedEffect(viewModel, characterId)`. One NavEntry owns one character identity. Retry retains that identity, exposes Loading immediately and ignores repeated pending actions. Clearing the entry cancels its pending request; no navigation event queue is needed.

The public `DetailsRoute` exposes ID, image loader, Back callback and modifier. Its internal wiring obtains the Hilt ViewModel, collects with `collectAsStateWithLifecycle()` and forwards immutable state/callbacks to `DetailsContent`. Keep ViewModel types internal, as in Home. `hiltViewModel()` must execute inside the destination's provided owner, whether called in the route body or as an internal default parameter.

Follow the existing feature organization: `composables` owns rendering and its tokens; `model` owns UI values/state/actions. Keep normal-size state previews and their private helpers with the rendering composables, following AGENTS.md. Route and ViewModel remain at the feature root. Tests mirror their subjects. Do not add empty layer folders, forwarding use cases or an extra module.

## UI and motion

Render the bounded approximately square portrait, identity block and grouped read-only facts from the selected design. Identity shows name, status and species once. Facts show Type when nonempty, Gender, Origin, Last known location and “Episode appearances” with one count. An ID eyebrow may use the actual ID. Do not show episode titles, unsupported lore, Share/bookmark controls or fact navigation arrows.

Back remains fixed, labelled and reachable in Loading, Error, NotFound and Content. Retry appears only for a recoverable data failure; confirmed NotFound shows “Character not found” and Back. Error/NotFound feedback stays on this detail destination. Skeletons contain no invented facts; once data exists, facts appear even if the portrait is pending or unavailable. Strings, sizes and spacing use existing resources/tokens or focused new owners. Fact values wrap and the content scrolls within safe insets.

Keep parallax subtle and clipped to the portrait, with Back unaffected. Read frame-rate scroll state in the draw/layer phase. Verify a static alternative when system motion is disabled; scroll-linked translation is not automatically disabled merely because it uses Compose. Use the pinned framework's supported motion signal and keep any required observer local and lifecycle-owned. Do not add a global motion service. Start with native NavDisplay destination transitions; additional UI animation evaluation remains C11, and a shared portrait transition remains O01.

Only Home retains the extra narrow/large-text preview. Details has ordinary-size Loading, Content, Error and NotFound previews with controlled images. Basic semantics, text status, contrast, wrapping and touch targets still apply; O02 remains the optional full accessibility demonstration.

## App navigation and retained state

App owns two typed keys: Home and Detail(characterId). Select and pin compatible Navigation 3 runtime/UI plus Lifecycle entry integration against the existing Kotlin/Compose/AGP toolchain before implementation. Use `NavDisplay`, a saveable back stack and the saveable-state/ViewModel-store entry decorators in the documented order. Do not serialize character objects, repositories or ViewModels as route arguments.

Selection appends Detail for the supplied ID. Guard rapid repeated selection from the outgoing Home entry so one selection interaction does not stack duplicate details. Screen Back and system Back return through the same app-owned pop behavior; never remove the root Home entry through a detail callback. A missing/error detail never adds an extra route.

Keep Home's NavEntry and ViewModel while detail is on top; retain its saveable lazy-grid position. Returning does not issue another first-page request solely because Home recomposes. Popping Details clears its entry-owned ViewModel; reopening loads the newly selected ID through its own request owner. Features only receive callbacks/IDs and never depend on each other.

The saveable-key plumbing is part of using Navigation 3 correctly. Search/status/keyboard retention is tested in C07/C08 when implemented, rather than simulated now.

References checked during preparation: [Navigation 3 basics](https://developer.android.com/guide/navigation/navigation-3/basics), [entry saveable state and ViewModel ownership](https://developer.android.com/guide/navigation/navigation-3/save-state), [Hilt integration](https://developer.android.com/training/dependency-injection/hilt-jetpack#navigation). No dependency version or runtime behavior is claimed verified until implementation checks run.

## Test boundaries and successive TDD slices

| Boundary | Driver | Observable behavior |
|---|---|---|
| Repository `getDetails` | Real Retrofit/repository against MockWebServer and detail/error JSON fixtures | Selected request ID, mapped facts/count, optional type/portrait, recognized missing ID, other failures and real pending-request cancellation |
| Details ViewModel actions/state | Fake repository, coroutines-test and Turbine | Loading → outcomes, one initial request, same-ID Retry, duplicate-pending guard and owner cancellation |
| `DetailsContent` and route | Whole-screen Compose tests with controlled Coil results and callbacks | Correct facts/labels/count, Back in every state, local portrait outcomes, contextual Retry and route/ViewModel wiring |
| Production app navigation | MainActivity/App navigation with test-only Hilt repository replacement and deterministic data | Selected ID, normal/system Back, Home content/scroll retained without reload, rapid-selection guard and detail cancellation/lifetime |

Use the existing JUnit/coroutines-test/Turbine/MockWebServer and compatible AndroidX Compose test infrastructure. Add Hilt test dependencies/runner only for the app journey. Test binding replacements and fixtures stay in test sources; expose only a DI binding module if test replacement needs that legitimate data boundary. Use the real production Activity, not an app-owned demo/test destination. Give journey fixtures absent/local portraits so they perform no uncontrolled image requests; the Details screen suite independently exercises controlled pending/success/failure portraits.

Preserve all existing Home tests and adapt repository fakes to the extended interface. Name tests GIVEN/WHEN/THEN; JVM names use backticks/spaces, instrumented names remain API-26-compatible. Use descriptive `lateinit var` subjects separated from fakes. Do not add tests for private mappers/components, screenshot tooling, or constants that mirror implementation.

Work vertically: first successful detail request/state/screen; then real navigation/return; then failure and Retry; then NotFound and optional/image values. Observe expected behavioral RED before GREEN at each approved seam. A failed compiler or environment is not behavioral RED.

## Validation and AI record

Scope and the four test boundaries were approved before implementation on 2026-10-01. Codex assisted the successive TDD slices, Kotlin/Compose implementation and source/visual review. The six-module graph, target API 37 and minimum API 26 remain unchanged. Human completion review accepted C05 on 2026-10-01; no commit or push was made.

### Material implementation decisions

- Pin stable Navigation 3 1.2.0 and the existing Lifecycle 2.11.0 integration; reuse Hilt 2.60.1 for app instrumentation. Compatibility is verified by builds and the production journey, rather than release notes alone.
- Reuse Retrofit/OkHttp/serialization for detail, with recognized-body 404 mapping and private validation/mapping functions. Keep cancellation exceptional and all user-visible request outcomes typed.
- Keep one identity per Details ViewModel entry. No SavedStateHandle route decoding, event queue, forwarding use case or shared feature dependency is needed.
- Use the framework `MotionDurationScale` from the Compose effect context. The pinned Compose UI 1.12.1 source exposes its system-backed, snapshot-observable scale, so a local `snapshotFlow` supplies the static alternative without a duplicate observer. Scroll state is read in `graphicsLayer` and translation is bounded inside the clipped portrait.
- Share only the actual reusable status colour palette in design system. Retain feature-owned geometry tokens and private previews with their rendering owners.
- Use `@TestInstallIn` to replace the repository binding only in app test sources. The public binding module is the replacement boundary; DTOs, client, implementation and ViewModels stay internal. The custom Hilt runner also lives only in `androidTest`; journeys launch production `MainActivity`.
- Selected Material Symbols are converted to Android vectors with the source viewBox origin applied, and kept under the existing license. Semantics tests verify behavior; native visual review verifies that the glyphs actually render.

### Changed source responsibilities

| Source owner | Implementation and purpose |
|---|---|
| `CharacterDetails`, `CharacterDetailsResult`, `CharactersRepository` | Separate pure detail value/outcome files and the ID-based repository operation |
| `CharacterDetailsDto`, `CharacterLocationDto`, `CharacterErrorDto`, `CharactersApi` | Internal REST detail/error decoding, shared serializer and `character/{id}` endpoint |
| `RemoteCharactersRepository` | Reuse the client; map identity/facts/count, validate returned ID, recognize missing characters and preserve cancellation |
| `CharactersBindings` | Expose the binding module for test replacement while keeping its implementation parameter internal |
| `CharacterDetailsUiModel`, `DetailsUiState`, `DetailsAction` | Immutable render values and explicit Load/Retry actions; sealed variants remain with their owners |
| `DetailsViewModel`, `DetailsRoute` | Entry-owned request/recovery lifecycle, lifecycle-aware state collection and callback wiring |
| `DetailsContent` and its private identity/status helpers | Screen branches, fixed labelled Back, portrait/identity/facts composition and ordinary-size state previews |
| `DetailsFacts`, `DetailsFeedback`, `DetailsSkeleton`, `DetailsPortrait`, `DetailsTokens` | Read-only wrapping rows/count, contextual recovery, data skeletons, independent image feedback and bounded/static motion using named tokens |
| `AppDestination`, `AppNavigation`, `MainActivity` | Typed app-owned Home/Detail entries, guarded selection/pop, entry decorators and the real entry point |
| `StatusColors`, `CharacterCardTokens` | One shared Alive/Dead palette, retaining existing Home appearance |
| `RemoteCharacterDetailsTest` and JSON fixture | Eight HTTP-boundary tests; selected path/facts/count, optional/unknown values, recognized/other HTTP errors, invalid data, network failure and pending-call cancellation |
| `DetailsViewModelTest`, Details `MainDispatcherRule` and fake | Seven state/action tests with controlled scheduling: load, idempotence, Retry, pending guards, missing character and owner cancellation |
| `DetailsContentTest` | Seven whole-screen tests for content/facts/Back, recovery, unavailable/loading states and controlled image/optional-value outcomes |
| `NavigationJourneyTest`, `JourneyCharactersModule`, `JourneyCharactersRepository`, `HiltTestRunner` | Six real-Activity journeys with test-only DI; selected ID, retained scroll/data, both Back paths, independent owners, duplicate selection, pending cancellation and recovery |
| Existing Home test fakes | Implement the extended repository contract; preserve their original catalogue behavior assertions |

Supporting files: the catalogue and app/Details Gradle files configure Navigation 3, serialization, Hilt and the existing test stack; Details string/vector resources supply labels and selected glyphs; README and product technical documents reflect implemented status. No screenshot framework, runtime gallery or additional production module was added.

### Observed TDD slices

All Gradle commands ran through `gradle_run.py`, workflow `af81f5c9ca3832db34380731e265fd10`. The following RED phases were actual behavior failures after test compilation, not compiler/environment failures.

| Slice | RED observation | GREEN observation |
|---|---|---|
| HTTP success | Run 0001: expected Toxic Rick success, received the not-yet-implemented failure outcome | 0002: detail identity/facts/count/path plus existing catalogue tests pass |
| ViewModel content | 0003: Load never produced Content | 0004: selected-ID Loading → Content passes; app/dependency assembly passes |
| Details content | 0005: real character text/Back contract absent | 0006: whole-screen Content/facts/Back passes on API 37 |
| Production navigation | 0007: selection did not display `ENTITY PROFILE #15` | 0008: selected ID → detail → Back preserves the scrolled Home without reload |
| Recognized missing ID | 0009: recognized detail 404 returned Service | 0010: recognized NotFound passes while the page 404 contract stays unchanged |
| Retry | 0011: failed detail stayed Error after Retry | 0012: same-ID Loading/Content, duplicate guards and cancellation pass, with Home/data regressions |
| Error screen | 0013: contextual error and Retry missing | 0014: Error/Retry/Back callbacks pass |
| Missing screen | 0015: unavailable-character message missing | 0016: NotFound/Back without Retry passes |
| Loading screen | 0017: loading-progress semantics missing | 0018: skeleton/Back plus seven Details tests and six app journeys pass |

Additional HTTP/image/state cases verify the existing implementations at those seams; no retrospective RED is claimed. After GREEN, source review extracted private response/mapping functions, and run 0020 passed ktlint/Detekt plus affected JVM tests.

### Completion checks

- Run 0021: `./gradlew qualityCheck :app:assembleRelease :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest` passes. Counts: **31 JVM tests** (17 repository, 7 Home ViewModel, 7 Details ViewModel) and **20 instrumented tests** (7 Home, 7 Details, 6 app journeys), all without failures/skips. Empty test tasks are not counted.
- After native asset review, run 0022 passes debug assembly and the 13 Details/app instrumented tests at a reduced emulator height. Run 0023 passes `qualityCheck :app:assembleRelease` with the final resources. Instrumentation is still local, not added to CI or the quality task in this change.
- Native API 37 review opens Rick Sanchez from the real catalogue and checks the live [character response](https://rickandmortyapi.com/api/character/1): ID/identity, omitted empty Type, origin/location and 51 episode references. The portrait, fixed Back and all final facts render with the selected glyphs. Error, NotFound and pending-image recovery are verified with deterministic screen/journey fixtures, not a claimed live server outage.
- At the same scrolled position, the portrait bounds remain `[204,183][876,855]`; disabling motion changes the image layer's top from 171 to 156 while Back remains `[79,216][132,269]`. This confirms that scroll translation is removed and the fixed control is unaffected. The final episode row stays reachable. The original animator setting and 1080×2424 display are restored, and native system Back returns to Home.
- Strict OpenSpec validation, local documentation links and `git diff --check` are checked after updating this record. The wrapper is finished after validation, removing only its own temporary logs.

There is no measured performance claim or screenshot regression baseline. Android Studio preview rendering has not been manually exercised. Search/filter/pagination retain their later increments. Human acceptance completed on 2026-10-01. All C05 tasks are checked; the change is archived and its accepted requirements are merged into the capability specifications.
