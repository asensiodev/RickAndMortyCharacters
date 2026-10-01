# Implementation backlog

Status: product/design preparation complete; OpenSpec initialized. C01 is accepted and archived; C02 is accepted, validated locally and in CI, and archived. C03 is accepted and archived; C04 is accepted, locally validated and archived; C05 is accepted, locally validated and archived; C05A is accepted, locally validated and archived; later identifiers remain queued tickets. Work advances after acceptance and human review. No time estimates are assigned.

Move each ticket into its OpenSpec change when it is prepared, then replace its detailed entry here with a link. OpenSpec owns that change's tasks and evidence from then on. Retire this temporary file when the remaining queue has been migrated; do not maintain two copies. Product priorities live in [PRD](PRD.md), screen design in [UI/UX Definition](UI_UX.md), technical decisions in [ARCHITECTURE](ARCHITECTURE.md), and the shared process in [DEVELOPMENT](DEVELOPMENT.md).

Before implementation, expand only the next ticket into a change with observable scenarios, agreed public test interfaces and small tasks. A capability spec can evolve through several changes. Split a ticket if its diff contains independent decisions that cannot be reviewed comfortably together.

## Product and design — completed preparation

| Ticket | Reviewed result |
|---|---|
| P01 — Requirements and module contracts | Scope, priorities and the six-module graph selected in PRD/ARCHITECTURE; toolchain compatibility verified in C01 |
| P02 — Content screens | Selected Home/Detail references and API-backed fields reviewed in UI/UX Definition |
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

Depends on accepted C04 and C05A; next change to prepare for scope and test-boundary review. Add subsequent pages, remote completion and append retry while retaining loaded cards. Prevent duplicate concurrent loads. Verify multipage fixtures, failing/retried pages and one state owner. Include the floating counter with the real loaded-item count and API total for the current query; test growth across pages, retention on append failure and end-of-list behavior without counting placeholders. Distinguish a confirmed API list-end response from retryable append errors, retaining loaded cards in both cases. Review the Paging adapter and load-state contract without leaking Paging into domain.

### C07 — Search by name — Must

Depends on C06. Search the complete remote catalogue using the current name. Cover debounce, keyboard Search submission without duplicate requests, clear, in-place no matches, pagination/counter reset and late results from an obsolete query. Counts and cards must represent the same active query. Input focus/value must survive loading/empty/error rendering, and Retry must preserve the visible query and avoid duplicate in-flight operations. Use controlled coroutine time, repository fakes, HTTP parameter assertions and Compose interaction tests. Include the fixed Rick/Morty/Beth/Summer buttons only in no matches. Tapping one updates the input and submits immediately through the existing search path, dismissing the keyboard and resetting pagination/counter/scroll on Home. Verify the submitted name, no duplicate debounced request and the normal loading/result transitions. Search history, recommendation requests and local full-catalogue filtering are not part of this change.

### C08 — Status filter chips — Must

Depends on C07. Add single-choice All, Alive, Dead and Unknown chips, combining status with the name query. A status change resets pages, counter and scroll; clearing the name preserves status; All removes only status. Test selection semantics, query parameters and rejection of obsolete results. Cover filter-only/combined empty results on the same Home and reselection of the active chip without resetting content or scroll. Verify that suggested-name searches preserve the selected status and use the combined remote query. Do not mix results from different query identities.

### C09 — HTTP response caching — Must

Depends on C08, using the repository introduced in C04/C05. Configure a single bounded API cache in `data:characters`, separate from image caching. Recheck real endpoint headers; preserve server freshness/validation directives instead of hardcoding the observed 90-day lifetime.

Use MockWebServer and a temporary cache directory to verify: an identical fresh request produces no second HTTP request; a stale entry with an ETag sends a validator and reuses the body on 304; a changed response replaces the stored body; page/name/status/ID changes retain distinct responses; `no-store` is not retained; and an expired entry does not acquire custom stale-on-error behavior. Fully consume/close responses so tests exercise committed cache entries. Record these outcomes through the repository/HTTP boundary, not by testing OkHttp internals. No Room, cache-only mode or JSON-file repository is added.

### C10 — Minimal state restoration — Should

Depends on C09. Specify and verify recreation behavior for query, status and destination ID. Normal back retains scroll. After process death, request data through the repository and HTTP cache as usual; restore scroll when the needed items return or document an agreed reset. Distinguish activity recreation tests from an actual process-death check; do not persist a separate catalogue database.

### C11 — Visual and motion consistency

Depends on the implemented main flow. Reconcile cards, chips, detail, loading feedback, image fades and transitions with the approved design. Evaluate additional UI animations, including skeleton-to-content and navigation transitions, and add only motion that improves continuity or feedback without delaying data or disrupting interaction. Choose its scope and native Compose API after reviewing the integrated screens. Check system animation settings, narrow layouts, text size and image-heavy scrolling. Address observed issues and add regression tests where they protect behavior. Visual quality is implemented throughout earlier changes; this pass closes inconsistencies.

### C12 — Reproducible release candidate

Depends on all Must changes, including C05A Konsist checks, and selected optional work. Verify build instructions, automated checks, instrumented journey, release assembly and runtime behavior. Add actual screenshots and concise limitations to README; complete the AI/change record. Confirm the documentation matches the code and distinguish checks not executed from successful validation.

## Preferred enhancement

### S01 — Connectivity awareness — Should

Depends on C09 and is selected independently after the core request/error and cache flows exist. Add the native app-level monitor and snackbar described in ARCHITECTURE and UI/UX Definition. The visible outcome is one warning per disconnected period, including initial confirmed disconnection, with existing content and contextual Retry preserved. Recovery dismisses the warning without automatically reloading a screen.

Use a fake monitor to verify initial Unknown, disconnection/recovery, duplicate signals, navigation and foreground/background behaviour, including return with connectivity restored rather than replaying an old warning. Confirm snackbar placement leaves Retry reachable. Verify requests still work through their normal repository/cache path when the monitor reports unavailable. Review callback cleanup and manually check network loss/recovery on a device. Keep the agreed module graph and offline boundary. Record this Should separately from mandatory API error/retry behaviour and complete it before C12 if selected.

## Optional changes

**O01 — Shared character image transition:** prototype on the stable navigation flow; verify matching identity, back behavior and missing-image cases. Keep the basic transition if the shared version introduces fragile behavior.

**O02 — Catalogue accessibility demonstration:** verify TalkBack traversal of cards/search/chips/retry, meaningful announcements, large text and contrast. Record manual evidence and the exact screen scope.

**O03 — Additional visual verification:** use focused screenshot tests for card/skeleton geometry, portrait composition, theme and large text after identifying the visual behavior they protect. Compose interaction tests remain at real screen/flow boundaries; screenshots do not replace callbacks, retries or navigation assertions. Avoid assertions that merely restate implementation details. Mandatory architecture checks belong to C05A.

Each optional change is selected and reviewed independently before C12.
