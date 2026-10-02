# Implementation backlog

Status: product/design preparation complete; OpenSpec initialized. C01 is accepted and archived; C02 is accepted, validated locally and in CI, and archived. C03 is accepted and archived; C04 is accepted, locally validated and archived; C05 is accepted, locally validated and archived; C05A is accepted, locally validated and archived; C06 is accepted, locally validated and archived; C07 is accepted, locally validated and archived; later identifiers remain queued tickets. Work advances after acceptance and human review. No time estimates are assigned.

Move each ticket into its OpenSpec change when it is prepared, then replace its detailed entry here with a link. OpenSpec owns that change's tasks and evidence from then on. Retire this temporary file when the remaining queue has been migrated; do not maintain two copies. Product priorities live in [Delivery scope](../README.md#delivery-scope), screen design in [Design handoff](DEVELOPMENT_PROCESS.md#ui-design), technical decisions in [ARCHITECTURE](../README.md#architecture), and the shared process in [Development process](DEVELOPMENT_PROCESS.md).

Before implementation, expand only the next ticket into a change with observable scenarios, agreed public test interfaces and small tasks. A capability spec can evolve through several changes. Split a ticket if its diff contains independent decisions that cannot be reviewed comfortably together.

## Product and design — completed preparation

| Ticket | Reviewed result |
|---|---|
| P01 — Requirements and module contracts | Scope, priorities and the six-production-module graph selected in PRD/ARCHITECTURE; toolchain compatibility verified in C01 |
| P02 — Content screens | Selected Home/Detail references and API-backed fields reviewed in the Stitch design handoff |
| P03 — Home state variants | Visual references reviewed; loading/contrast adjustments recorded for Compose implementation |
| P04 — Detail states and handoff | Content/loading/error references reviewed; Share-glyph omission recorded for implementation |

These statuses describe completed planning, not a working Android application. Native layout, navigation, motion, keyboard, contrast and touch-target checks belong to the implementation changes.

## Implementation changes

### C01 — Android foundation

**Accepted and archived.** See the [OpenSpec proposal](../openspec/changes/archive/2026-09-30-android-foundation/proposal.md), [acceptance scenarios](../openspec/changes/archive/2026-09-30-android-foundation/specs/android-foundation/spec.md), [design](../openspec/changes/archive/2026-09-30-android-foundation/design.md) and [tasks](../openspec/changes/archive/2026-09-30-android-foundation/tasks.md). OpenSpec owns C01's detailed requirements, tasks and evidence.

### C02 — Shared quality checks

**Accepted, validated locally and in CI, and archived.** See the [proposal](../openspec/changes/archive/2026-09-30-shared-quality-checks/proposal.md), [acceptance scenarios](../openspec/changes/archive/2026-09-30-shared-quality-checks/specs/shared-quality-checks/spec.md), [design](../openspec/changes/archive/2026-09-30-shared-quality-checks/design.md) and [tasks](../openspec/changes/archive/2026-09-30-shared-quality-checks/tasks.md). OpenSpec owns the detailed scope and evidence.

### C03 — Character card and image loading

**Accepted, locally validated and archived.** See the [proposal](../openspec/changes/archive/2026-10-01-character-card-images/proposal.md), [scenarios](../openspec/changes/archive/2026-10-01-character-card-images/specs/character-card-images/spec.md), [design/evidence](../openspec/changes/archive/2026-10-01-character-card-images/design.md) and [tasks](../openspec/changes/archive/2026-10-01-character-card-images/tasks.md).

### C04 — First remote catalogue page

**Accepted, locally validated and archived.** See the [proposal](../openspec/changes/archive/2026-10-01-first-remote-catalogue/proposal.md), [scenarios](../openspec/changes/archive/2026-10-01-first-remote-catalogue/specs/character-catalogue/spec.md), [design/test boundaries](../openspec/changes/archive/2026-10-01-first-remote-catalogue/design.md) and [tasks](../openspec/changes/archive/2026-10-01-first-remote-catalogue/tasks.md). OpenSpec owns the detailed C04 work.

### C05 — Character detail and back navigation

**Accepted, locally validated and archived.** See the [proposal](../openspec/changes/archive/2026-10-01-character-detail-navigation/proposal.md), [scenarios](../openspec/changes/archive/2026-10-01-character-detail-navigation/specs/character-detail/spec.md), [navigation contract](../openspec/changes/archive/2026-10-01-character-detail-navigation/specs/character-navigation/spec.md), [design/test boundaries](../openspec/changes/archive/2026-10-01-character-detail-navigation/design.md) and [tasks](../openspec/changes/archive/2026-10-01-character-detail-navigation/tasks.md). OpenSpec owns the detailed C05 work.

### C05A — Architecture checks with Konsist — Must

**Accepted, locally validated and archived.** See the [proposal](../openspec/changes/archive/2026-10-01-konsist-architecture-checks/proposal.md), [scenarios](../openspec/changes/archive/2026-10-01-konsist-architecture-checks/specs/architecture-checks/spec.md), [design/test boundaries](../openspec/changes/archive/2026-10-01-konsist-architecture-checks/design.md) and [tasks](../openspec/changes/archive/2026-10-01-konsist-architecture-checks/tasks.md). OpenSpec owns the detailed C05A work. It follows accepted C05 and precedes C06.

### C06 — Complete pagination

**Accepted, locally validated and archived.** See the [proposal](../openspec/changes/archive/2026-10-01-complete-catalogue-pagination/proposal.md), [scenarios](../openspec/changes/archive/2026-10-01-complete-catalogue-pagination/specs/character-catalogue/spec.md), [design/test boundaries](../openspec/changes/archive/2026-10-01-complete-catalogue-pagination/design.md) and [tasks](../openspec/changes/archive/2026-10-01-complete-catalogue-pagination/tasks.md). OpenSpec owns the detailed C06 work. It follows accepted C04/C05/C05A; C07 is accepted and archived.

### C07 — Search by name — Must

**Accepted, locally validated and archived.** See the [proposal](../openspec/changes/archive/2026-10-01-search-characters-by-name/proposal.md), [scenarios](../openspec/changes/archive/2026-10-01-search-characters-by-name/specs/character-catalogue/spec.md), [design/test boundaries](../openspec/changes/archive/2026-10-01-search-characters-by-name/design.md) and [tasks](../openspec/changes/archive/2026-10-01-search-characters-by-name/tasks.md). OpenSpec owns the detailed C07 work. It follows accepted C06; C08 is accepted and locally validated.

### C08 — Status filter chips — Must

**Accepted and locally validated on API 37.** See the [proposal](../openspec/changes/filter-characters-by-status/proposal.md), [scenarios](../openspec/changes/filter-characters-by-status/specs/character-catalogue/spec.md), [design/test boundaries](../openspec/changes/filter-characters-by-status/design.md) and [tasks](../openspec/changes/filter-characters-by-status/tasks.md). OpenSpec owns C08's detailed work. It follows accepted C07; publication is authorized; archival remains a separate workflow step.

### C09 — HTTP response caching — Must

**Accepted for publication and locally validated; archival pending.** See the [proposal](../openspec/changes/cache-character-http-responses/proposal.md), [scenarios](../openspec/changes/cache-character-http-responses/specs/character-http-cache/spec.md), [design/test boundaries](../openspec/changes/cache-character-http-responses/design.md) and [tasks](../openspec/changes/cache-character-http-responses/tasks.md). OpenSpec owns C09's detailed work. C08 and C09 acceptance are recorded; archival remains pending.

### C11 — Visual and motion consistency

**Scope approved; implementation and validation in progress.** See the [proposal](../openspec/changes/polish-visual-motion-consistency/proposal.md), [scenarios](../openspec/changes/polish-visual-motion-consistency/specs/visual-motion-consistency/spec.md), [design/test boundaries](../openspec/changes/polish-visual-motion-consistency/design.md) and [tasks](../openspec/changes/polish-visual-motion-consistency/tasks.md). OpenSpec owns C11's detailed scope and evidence. Accessibility-specific layout changes, previews and audits are deferred to O02 at the user's request. Prepared at the user's request; implementation follows review of its scope. C12 remains the final reproducible delivery increment.

### C12 — Reproducible release candidate

Depends on all Must changes, including C05A Konsist checks, and selected optional work. Verify build instructions, automated checks, instrumented journey, release assembly and runtime behavior. Add actual screenshots and concise limitations to README; complete the AI/change record. Confirm the documentation matches the code and distinguish checks not executed from successful validation.

## Preferred enhancement

### S01 — Connectivity awareness — Should

**Outside the current delivery.** Retained as a future enhancement; contextual errors and Retry are part of the implemented core. Light appearance and system-theme switching are also outside this delivery; see [Delivery scope decisions](../README.md#delivery-scope).

Depends on C09 and is selected independently after the core request/error and cache flows exist. Add the native app-level monitor and snackbar described in this ticket. The visible outcome is one warning per disconnected period, including initial confirmed disconnection, with existing content and contextual Retry preserved. Recovery dismisses the warning without automatically reloading a screen.

Use a fake monitor to verify initial Unknown, disconnection/recovery, duplicate signals, navigation and foreground/background behaviour, including return with connectivity restored rather than replaying an old warning. Confirm snackbar placement leaves Retry reachable. Verify requests still work through their normal repository/cache path when the monitor reports unavailable. Review callback cleanup and manually check network loss/recovery on a device. Keep the agreed module graph and offline boundary. Record this Should separately from mandatory API error/retry behaviour and complete it before C12 if selected.

## Optional changes

**O01 — Shared character image transition:** prototype on the stable navigation flow; verify matching identity, back behavior and missing-image cases. Keep the basic transition if the shared version introduces fragile behavior.

**O02 — Catalogue accessibility demonstration:** verify TalkBack traversal of cards/search/chips/retry, meaningful announcements, large text and contrast. Own the deferred Home/Detail enlarged-text layout and preview review from C11, including narrow and wide viewports without arbitrary font-scale breakpoints. Record manual evidence and the exact screen scope.

**O03 — Additional visual verification:** Paparazzi 2.0.0-alpha05.1 is implemented with a Java 21 build runtime, existing AGP 9.2.1 and unchanged JVM targets 17. Nine bounded Home/Detail baselines supplement interaction tests. [Design and evidence](../openspec/changes/add-paparazzi-visual-regressions/design.md) and [tasks](../openspec/changes/add-paparazzi-visual-regressions/tasks.md) own verification and pending human acceptance; remote CI awaits its first run. O02 retains enlarged-text/accessibility review. Commit and push authorized by the user on 2026-10-02; human acceptance and archival remain pending.

**O04 — Character episode appearances:** selected by the user on 2026-10-02 before screenshot testing; implemented and locally verified; accepted for commit/push by the user on 2026-10-02. Enrich Detail with a horizontal list of non-navigating episode cards below the character facts, showing episode code, title and air date. The API supplies episode references on characters and supports fetching multiple episodes in one request, but does not provide episode images or descriptions; use text cards without another content source. Preserve the existing appearance count and keep character information available while episodes load or fail, with section-local loading, empty and error/Retry states. Verify character identity, episode ordering, horizontal scrolling and recovery without adding destinations. [API documentation](https://rickandmortyapi.com/documentation/#episode-schema).

### O04 implementation and verification — 2026-10-02

Implemented at the user's request without creating or modifying an OpenSpec change. Observable boundaries reuse the existing repository, Details ViewModel, state-driven screen and production-navigation tests. Data resolves episode references through the existing HTTP client/cache, normalizes single-object/batch-array responses and validates identity/facts before restoring requested order. The ViewModel keeps character content and count available, guards pending section retries and owns cancellation. Text cards use existing theme/spacing and focused size tokens; no new dependency, artwork, destination or parallax tuning.

Managed Gradle workflow `428af206b3d3d5cf12733b5475e67448` ran the following checks. Behavioral RED was observed before repository, state and rendering implementations. Compilation/static-analysis failures are not RED.

| Run and question | Nested Gradle tasks | Observed result |
|---|---|---|
| 0001: Does the repository return episode facts in character order before implementation? | `:data:characters:testDebugUnitTest --tests '*RemoteEpisodesRepositoryTest'` | RED: expected episode facts, received empty list |
| 0002: Does the implemented repository load ordered episode facts with one batch request? | Same focused repository test | GREEN |
| 0003: Does Detail keep character facts visible while episode requests are pending before the state integration? | `:feature:details:testDebugUnitTest --tests '*DetailsViewModelTest'` | RED: character content had Empty rather than Loading episodes |
| 0004: Do Detail facts remain visible during episode loading and do existing detail contracts still pass? | `:feature:details:testDebugUnitTest :data:characters:testDebugUnitTest` | GREEN |
| 0005: Do episode scrolling and section-only Retry exist before rendering the section? | `:feature:details:connectedDebugAndroidTest`, filtered to DetailsContentTest | RED: episode list and section Retry were absent |
| 0006: Do the episode data, isolated retry and cancellation tests pass, and does Detail support vertical and horizontal scrolling with Back? | Both affected JVM suites and Detail instrumentation | Compilation failed: placeholder API has no shape parameter; corrected to the existing clip modifier |
| 0007: Do episode contracts and Detail scrolling pass after using the existing placeholder modifier API? | Both affected JVM suites and Detail instrumentation | GREEN: 47 data JVM, 11 Details ViewModel tests; nine screen tests on each API 37 Pixel/emulator |
| 0008: Do the completion quality gate, release assembly and existing navigation journeys pass with the episode section? | `qualityCheck :app:assembleRelease :app:connectedDebugAndroidTest` | Detekt flagged return count/complex condition; simplified the identified branches |
| 0009: Do quality, release assembly and navigation pass after simplifying episode validation to the project lint limits? | Same completion tasks | Navigation fixture compilation lacked the new episode binding; added a controlled dependency and episode data |
| 0010: Do quality, release assembly and production navigation regressions pass with controlled episode dependencies? | Same completion tasks | Emulator: 18 navigation tests passed. Connected Pixel: instrumentation crashed during a test, without an assertion message; device navigation acceptance remains incomplete |
| 0011: Do the standalone quality gate and release assembly pass independently of device instrumentation? | `qualityCheck :app:assembleRelease` | GREEN; aggregate JVM reports contain 87 tests including three architecture checks, zero failures/errors/skips |

The wrapper rejected a `--scope completion` invocation and an attempt to select the emulator via an `env` launcher; neither executed Gradle or counts as a verification run. All successful invocations above used the managed launcher. Wrapper-owned logs were removed on finish; source and test reports remain.

Native production smoke used emulator-5554 (Pixel 9a, API 37, 1080×2424, density 420, default font size). Rick #1 loaded 51 appearances with actual episode code/title/air date. Vertical scroll exposed the complete cards; horizontal swipe reached S01E03 “Anatomy Park” without moving the fixed Back control. Vertical movement was approximately 201 pixels, enough to inspect the existing subtle parallax but not a long page or measured performance result. Debug APK is installed on that emulator. Temporary visual-review pixels were deleted; no screenshot files are retained. Loading/error/Retry/cancellation and single-episode REST behavior are covered by controlled tests, rather than claimed as a full native failure matrix. Human visual acceptance and the connected Pixel navigation crash remain manual limitations.

### Detail portrait size adjustment — 2026-10-02

At the user's request, increased the shared portrait/skeleton width cap from 256dp to 288dp (12.5%), retaining the square aspect ratio, existing clipping and bounded parallax. No new behavior test or fabricated RED was added for this visual token adjustment. Managed workflow `bbae0baf4bb201c11e9b449c9b34a3f6` asked “Do quality and release assembly pass with the slightly larger square Detail portrait?” and ran `./gradlew qualityCheck :app:assembleRelease`: BUILD SUCCESSFUL in 20s. Native Rick #1 review on emulator-5554 confirmed the larger square image, available Back and episode content below; temporary review pixels were deleted. The debug APK is installed on that emulator. This verification includes the current working tree, including an independently edited Details ViewModel; this adjustment changes only the portrait token and associated documentation. Wrapper finish removed only its managed logs. The user accepted the square presentation and authorized committing this adjustment; no push is authorized in this request.

Each optional change is selected and reviewed independently before C12.

## Independently selected visual adjustment

**Portal gun launcher and native splash:** implemented, locally validated on API 37 and accepted by the user on 2026-10-01; not yet archived. [Change and evidence](../openspec/changes/add-portal-gun-branding/proposal.md). This adjustment does not advance the C08/C09 queue.

**Search keyboard dismissal on result drag:** independently approved by the user on 2026-10-01; implemented, locally validated and accepted for publication; archival pending. [Change and evidence](../openspec/changes/hide-search-keyboard-on-scroll/proposal.md). This refinement preserves the active name/status and does not advance the main queue.
