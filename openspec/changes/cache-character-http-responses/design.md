## Context and readiness

The draft was prepared while C08 completed verification. C08 now records human acceptance and local API 37 validation. On 2026-10-01 the human authorized C09 implementation after reviewing its purpose and approach, approving the prepared scope and repository/HTTP test boundaries. C08 archival remains a separate step.

The current `CharactersNetworkModule` provides a singleton bare OkHttp client and passes it to `createCharactersApi`. `RemoteCharactersRepository` already maps catalogue and detail responses to pure domain outcomes. Extend that composition rather than introducing another repository or changing Home/Paging ownership.

## Decisions

### Native cache at the existing data boundary

Use OkHttp's disk Cache with a 10 MiB limit under application `cacheDir/character_http`, separate from Coil's `character_images`. Inject application context at data's Hilt composition boundary. Keep directory and size constants with their owner and retain singleton client/cache lifetime. Do not open simultaneous independent caches against the same directory.

Share the smallest internal client-construction function needed by production and integration tests, accepting a disk directory so JVM tests need no Android context. Avoid a configurable network framework or public cache API. Use existing dependencies and catalogue pins.

### Header-driven reuse

Delegate storage, response age, freshness, validation and Vary handling to OkHttp. Do not rewrite Cache-Control, add a 90-day constant, force cache-only access, or implement custom stale fallback. On a miss or expired entry the existing HTTP/repository result path applies. There is no new cache status in UI state and no connectivity gating.

The earlier endpoint observation is historical: 2026-09-30 probes reported ETag and `public, max-age=7776000, immutable`. Recheck GET headers for a combined filtered page and a detail ID during implementation and record date, URL, status, Cache-Control, ETag, Date, Age and Vary where present. A probe cannot establish 304 behavior; controlled fixtures verify that contract.

## Observable test boundaries

- **Repository/HTTP integration:** real `RemoteCharactersRepository` and Retrofit with the production client factory, MockWebServer and isolated temporary disk directories. Assert mapped page/detail values plus request count, URL and validators. Exercise both endpoints for fresh reuse; use a representative endpoint for each generic policy and independent catalogue-constraint/detail-ID cases for isolation.
- **Persistence:** consume an eligible detail response, close the cache/client resources, recreate them at the same directory and verify fresh reuse without a second server request. Keep cache ownership sequential.
- **Deterministic policy fixtures:** use Date/Age/Cache-Control/ETag fixtures for fresh and expired entries; renewed freshness on 304/changed 200 verifies subsequent reuse. Use bounded server-request observation to prove the absence of traffic; do not hang awaiting a nonexistent second request. Fully consume successful bodies, close error bodies and clean up resources in test teardown.
- **Failure regression:** force transport failure with a stale entry requiring validation; assert the existing Network outcome. A service-error fixture verifies Service rather than an expired-body fallback. Retain existing repository cancellation and contextual 404 tests.
- **Native composition:** API 37 launch, combined-filter pagination, detail and Back confirm production Hilt wiring. Controlled production-Activity journeys verify normal error/Retry behavior. Existing fake-backed screen journeys verify UI regressions; they do not prove real HTTP cache reuse. No new screenshot or cache-debug screen is needed.

Follow the repository's uppercase GIVEN / WHEN / THEN naming, field layout and behavior-focused RED → GREEN slices. Start with fresh reuse failing because the second request reaches the server. A configuration/compilation failure is not behavioral RED. Once the native cache is installed, add focused regressions without manufacturing further failing phases.

## Verification and acceptance

Run affected data JVM tests first, then `qualityCheck` and `:app:assembleRelease` through the repository's managed Gradle workflow. Run affected API 37 journeys and native composition smoke checks. Record exact commands, outcomes, live header observations and limitations here during implementation. No Gradle check, live API probe or manual device check has been executed for this draft.

Human acceptance precedes archival. Commit and push require their separate explicit authorizations.

## Planning evidence

Codex prepared this draft on 2026-10-01 from BACKLOG C09, PRD, ARCHITECTURE and DEVELOPMENT, preserving the repository's English artifact format. Codebase Memory Tier 2 identified the current data network composition and repository, with the API factory's inbound production caller. Coverage metadata generation `2026-10-01T18:52:00Z` matched consulted paths with no recorded gaps; this is a best-effort signal, not proof of complete indexing. Exact graph snippets supplied the material source evidence. Reconcile with the final accepted C08 diff before coding.

The proposed native storage/validation mechanism follows the [OkHttp Cache contract](https://github.com/lysine-dev/okhttp/blob/main/okhttp/src/commonJvmAndroid/kotlin/okhttp3/Cache.kt). Freshness, validation, Vary and storage policy follow [RFC 9111](https://httpwg.org/specs/rfc9111.html). These primary references were consulted during drafting; current provider headers remain an implementation check.

## Draft validation

On 2026-10-01, `openspec validate cache-character-http-responses --strict --no-interactive` passed. `openspec status --change cache-character-http-responses` reported all four planning artifacts complete. `git diff --check` passed. These checks validate planning structure and whitespace, not Android behavior or scope approval.

## Implementation and validation record

Codex implemented C09 on 2026-10-01 after the human authorized implementation and C08 acceptance was confirmed in its proposal/backlog. The production change adds an internal OkHttp factory with a 10 MiB native Cache at `cacheDir/character_http`; the existing singleton Hilt provider supplies application context and uses that factory. There are no new dependencies, modules, repository contracts or feature changes.

Codebase Memory Tier 2 generation `2026-10-01T19:52:53Z` supplied the current API factory and its production/test callers, with matching coverage metadata and no recorded gaps on the consulted source/test paths. The existing repository and HTTP fixtures were inspected directly. No complete-index claim is made.

### Live header observations

GET probes executed through curl on 2026-10-01 returned 200 for both endpoints:

| URL | Cache-Control | ETag | Date | Age | Vary |
|---|---|---|---|---|---|
| `https://rickandmortyapi.com/api/character?page=1&name=rick&status=alive` | `public, max-age=7776000, immutable` | `W/"2d39-/yT6JQRJMhyFXpy2sWNwfHOugNU"` | `Thu, 01 Oct 2026 19:54:40 GMT` | absent | absent |
| `https://rickandmortyapi.com/api/character/1` | `public, max-age=7776000, immutable` | `W/"a9f-I68Cx5oSPFclkl+Wy2Fr9jNsFS8"` | `Thu, 01 Oct 2026 19:54:43 GMT` | `5523673` | absent |

The client uses the complete server freshness/age policy; it does not treat max-age as a new lifetime starting at each app read. Probes establish these responses only, not conditional validation or universal provider policy.

### Managed RED, GREEN and completion checks

All Gradle commands ran through `gradle_run.py`, workflow `fe43f516ba46a0546bba32e8649912e3`. The first sandbox attempt failed on the Gradle wrapper lock and initial sandbox curl/adb calls lacked network/socket access; these were environment failures. Authorized escalated execution supplied the actual checks.

| Question / nested command | Observed result |
|---|---|
| Does a repeated fresh page reuse its HTTP response before cache configuration? `:data:characters:testDebugUnitTest --tests '*CharacterHttpCacheTest*'` | Behavioral RED: repeated result had total 99 instead of the stored total 20; the uncached request reached the server again |
| Does a repeated fresh page reuse its HTTP response after cache configuration? Same targeted command | GREEN; fresh result reused without another request |
| Do repository cache policies and existing data regressions pass? `:data:characters:testDebugUnitTest` | PASS: 14 cache tests plus 19 catalogue and 8 detail tests, zero failures/skips |
| Do the completion quality gate and release assembly pass for C09? `qualityCheck :app:assembleRelease` | First run failed internally at `:app:lintAnalyzeDebugAndroidTest`; no cache behavior failure was reported |
| What is the root cause of the unexpected lint analysis failure in :app:lintAnalyzeDebugAndroidTest? Focused `:app:lintAnalyzeDebugAndroidTest --stacktrace` | Focused task passed; original failure was not reproduced and its root cause is unproven. No source workaround was added |
| Do the documented quality gate and release assembly pass after the focused lint task recovered? `qualityCheck :app:assembleRelease` | PASS: gate and release assembly complete; no warnings/failures in the bounded wrapper summary |

The 14 cache regressions exercise repository outcomes and recorded server traffic using the production client factory. Age/header fixtures establish expiry without real-time waiting. Disk reopening closes the first cache before opening another instance. Vary tests use derived clients sharing the existing cache. Existing data cancellation/contextual 404 tests remain green. The Gradle diagnostic owner was a read-only subagent required by the gradle-run skill after the lint failure; repository edits remained with the parent.

### API 37 and production smoke validation

The managed question “Do all 10 app navigation journey tests pass on API37 with C09 production wiring, and does the debug app install for native smoke validation?” ran `:app:connectedDebugAndroidTest :app:installDebug` and passed all 10 navigation tests with zero failures/skips. These fake-repository journeys protect navigation, request ownership, contextual Retry and filtered Back behavior; they do not measure real HTTP reuse.

Instrumentation cleanup left the production package absent despite the combined task graph succeeding. A separate post-instrumentation `:app:installDebug` answered “Does installing the debug app after instrumentation cleanup make it available for native smoke validation?” with PASS and “Installed on 1 device.” The production Activity then launched successfully through adb.

Native validation on `emulator-5554`, API 37, used the real Hilt/repository/API composition. Home loaded 20 of 826 characters. `cache/character_http` contained its journal and response metadata/body files, separate from images. Searching Rick and selecting Alive returned 20 of 29; scrolling loaded 29 of 29. Selecting Hairdresser Rick opened entity #477 with real metadata. System Back retained Rick, selected Alive, 29 of 29 and the same scrolled cards with the keyboard closed. Additional page and detail cache files were present. The native journey checks composition and UI continuity; the controlled HTTP tests establish cache reuse and validation.

No offline guarantee, measured performance claim, cache-debug UI or automatic stale fallback was introduced. Live provider headers remain outside application control. Human acceptance, archival, commit and push remain pending.

After validation, `gradle_run.py finish --workflow fe43f516ba46a0546bba32e8649912e3` completed successfully and removed only wrapper-owned logs. Final `openspec validate cache-character-http-responses --strict --no-interactive` and `git diff --check` passed. The change remains active for human acceptance; no commit or push was performed.

## Publication acceptance

On 2026-10-01, the human requested commit and push of C09 and the keyboard fix after their verification reports. This records acceptance for publication and explicit commit/push authorization. Archival remains a separate workflow step.
