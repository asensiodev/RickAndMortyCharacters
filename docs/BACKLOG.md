# Implementation backlog

Status: product/design preparation complete; OpenSpec initialized. C01 is accepted and archived; C02 is accepted, validated locally and in CI, and archived. C03 is accepted and archived; C04 is accepted, locally validated and archived; C05 is accepted, locally validated and archived; C05A is accepted, locally validated and archived; C06 is accepted, locally validated and archived; C07 is accepted, locally validated and archived; later identifiers remain queued tickets. Work advances after acceptance and human review. No time estimates are assigned.

Move each ticket into its OpenSpec change when it is prepared, then replace its detailed entry here with a link. OpenSpec owns that change's tasks and evidence from then on. Retire this temporary file when the remaining queue has been migrated; do not maintain two copies. Product priorities live in [PRD](PRD.md), screen design in [UI/UX Definition](UI_UX.md), technical decisions in [ARCHITECTURE](ARCHITECTURE.md), and the shared process in [DEVELOPMENT](DEVELOPMENT.md).

Before implementation, expand only the next ticket into a change with observable scenarios, agreed public test interfaces and small tasks. A capability spec can evolve through several changes. Split a ticket if its diff contains independent decisions that cannot be reviewed comfortably together.

## Product and design — completed preparation

| Ticket | Reviewed result |
|---|---|
| P01 — Requirements and module contracts | Scope, priorities and the six-production-module graph selected in PRD/ARCHITECTURE; toolchain compatibility verified in C01 |
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

Depends on C09 and is selected independently after the core request/error and cache flows exist. Add the native app-level monitor and snackbar described in ARCHITECTURE and UI/UX Definition. The visible outcome is one warning per disconnected period, including initial confirmed disconnection, with existing content and contextual Retry preserved. Recovery dismisses the warning without automatically reloading a screen.

Use a fake monitor to verify initial Unknown, disconnection/recovery, duplicate signals, navigation and foreground/background behaviour, including return with connectivity restored rather than replaying an old warning. Confirm snackbar placement leaves Retry reachable. Verify requests still work through their normal repository/cache path when the monitor reports unavailable. Review callback cleanup and manually check network loss/recovery on a device. Keep the agreed module graph and offline boundary. Record this Should separately from mandatory API error/retry behaviour and complete it before C12 if selected.

## Optional changes

**O01 — Shared character image transition:** prototype on the stable navigation flow; verify matching identity, back behavior and missing-image cases. Keep the basic transition if the shared version introduces fragile behavior.

**O02 — Catalogue accessibility demonstration:** verify TalkBack traversal of cards/search/chips/retry, meaningful announcements, large text and contrast. Own the deferred Home/Detail enlarged-text layout and preview review from C11, including narrow and wide viewports without arbitrary font-scale breakpoints. Record manual evidence and the exact screen scope.

**O03 — Additional visual verification:** use focused screenshot tests for card/skeleton geometry, portrait composition, theme and large text after identifying the visual behavior they protect. Compose interaction tests remain at real screen/flow boundaries; screenshots do not replace callbacks, retries or navigation assertions. Avoid assertions that merely restate implementation details. Mandatory architecture checks belong to C05A.

Each optional change is selected and reviewed independently before C12.

## Independently selected visual adjustment

**Portal gun launcher and native splash:** implemented, locally validated on API 37 and accepted by the user on 2026-10-01; not yet archived. [Change and evidence](../openspec/changes/add-portal-gun-branding/proposal.md). This adjustment does not advance the C08/C09 queue.

**Search keyboard dismissal on result drag:** independently approved by the user on 2026-10-01; implemented, locally validated and accepted for publication; archival pending. [Change and evidence](../openspec/changes/hide-search-keyboard-on-scroll/proposal.md). This refinement preserves the active name/status and does not advance the main queue.
