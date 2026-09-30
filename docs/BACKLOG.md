# Implementation backlog

Status: temporary planning queue until OpenSpec is initialized. Each ticket has one reviewable result; work advances after its acceptance criteria and human review are complete. These identifiers are planning references, not existing OpenSpec changes. No time estimates are assigned.

Move each ticket into its OpenSpec change when it is prepared, then replace its detailed entry here with a link. OpenSpec owns that change's tasks and evidence from then on. Retire this temporary file when the remaining queue has been migrated; do not maintain two copies. Product priorities live in [PRD](PRD.md), screen design in [UI/UX Definition](UI_UX.md), technical decisions in [ARCHITECTURE](ARCHITECTURE.md), and the shared process in [DEVELOPMENT](DEVELOPMENT.md).

Before implementation, expand only the next ticket into a change with observable scenarios, agreed public test interfaces and small tasks. A capability spec can evolve through several changes. Split a ticket if its diff contains independent decisions that cannot be reviewed comfortably together.

## Product and design

### P01 — Confirm requirements and module contracts

Review Must/Should/Could priorities, the six-module dependency graph and the proposed stack. Confirm independent home/details presentation, shared pure domain contracts and data implementation isolation. Complete when dependencies and responsibilities are clear enough to guide the scaffold. Documentation review only.

### P02 — Review and correct the two content screens

Depends on P01. Selected exports and handoff decisions are linked in UI/UX Definition. The content composition and API-backed detail fields provide the implementation reference, including “Episode appearances”. Preserve the exports. Native behavior, contrast and layout checks remain pending and will be performed as the corresponding components are implemented.

### P03 — Home state variants

Depends on P02. Selected Home states provide sufficient visual references. Follow UI/UX Definition's handoff: the loading PNG is stale but its HTML removes counter/tune; unify selected-chip styling and retain name shortcuts. Resolve pagination and local image states directly in Compose. Verify their documented visibility, search/filter behavior, retries, keyboard/large-text layout and end-of-scroll clearance in the implementation. No additional Home generation is queued.

### P04 — Detail state variants, navigation and motion

Depends on P03. The selected export includes the requested Detail loading and API-error references. Omit the generated Share glyph from loading and preserve the content screen's Back/portrait geometry. Use the selected feedback style for not-found and a neutral portrait fallback for image failure; no separate mockups are needed. The visual handoff is ready for C01. Validate optional/unknown fields, long text, the grid → detail → back journey, retries and motion during the relevant implementation changes; static review does not establish working navigation or animation.

## Implementation changes

### C01 — Android foundation

Depends on P04. Create the agreed module graph, compatible toolchain/version catalogue, minimal runnable app and OpenSpec configuration. Document actual build commands. Verify assembly and startup; domain must not acquire Android/Compose/HTTP-library dependencies. This is configuration validation, not a fabricated TDD cycle.

### C02 — Shared quality checks

Depends on C01. Configure formatting, Detekt, Android Lint, JVM test/build tasks, a GitHub Actions workflow and explicit hook installation. Verify a controlled check failure and recovery, without automatic staging. Record local results; a written workflow is not evidence of a successful remote run.

### C03 — Character card and image loading

Depends on C01 and uses C02 checks. Implement generic visual foundations in the design system and a character card in home with a noninteractive skeleton variant, independent image/content/error states, status text and selection callback. Configure one shared image loader and correctly sized requests. Test observable semantics/interaction with controlled images and inspect previews. Do not duplicate the image library's internal cache tests or introduce catalogue networking yet.

### C04 — First remote catalogue page

Depends on C03. Deliver API → shared repository contract → home ViewModel → grid, with card-skeleton loading/content/error/retry. Test repository HTTP mapping and errors with MockWebServer and state behavior through a fake repository. Preserve cancellation and keep DTOs inside data. This increment does not yet complete catalogue browsing.

### C05 — Character detail and back navigation

Depends on C04. Implement the independent details module; the app connects home and details using an ID and callbacks. Both features consume the shared character-domain contract and neither imports the other feature. Cover loading/error/retry/not-found, optional fields and episode count without extra episode requests. Use repository/state tests and a deterministic instrumented grid → detail → back journey. Verify normal return keeps query, filter and scroll without reopening the keyboard; missing detail is a state of that destination, never a route used for empty search. Include the approved portrait/detail structure, subtle parallax with a static alternative, an always-available Back control, the basic navigation transition and preserved browsing context.

### C06 — Complete pagination

Depends on C04; review after C05. Add subsequent pages, remote completion and append retry while retaining loaded cards. Prevent duplicate concurrent loads. Verify multipage fixtures, failing/retried pages and one state owner. Include the floating counter with the real loaded-item count and API total for the current query; test growth across pages, retention on append failure and end-of-list behavior without counting placeholders. Distinguish a confirmed API list-end response from retryable append errors, retaining loaded cards in both cases. Review the Paging adapter and load-state contract without leaking Paging into domain.

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

Depends on the implemented main flow. Reconcile cards, chips, detail, loading feedback, image fades and transitions with the approved design. Check system animation settings, narrow layouts, text size and image-heavy scrolling. Address observed issues and add regression tests where they protect behavior. Visual quality is implemented throughout earlier changes; this pass closes inconsistencies.

### C12 — Reproducible release candidate

Depends on all Must changes and selected optional work. Verify build instructions, automated checks, instrumented journey, release assembly and runtime behavior. Add actual screenshots and concise limitations to README; complete the AI/change record. Confirm the documentation matches the code and distinguish checks not executed from successful validation.

## Preferred enhancement

### S01 — Connectivity awareness — Should

Depends on C09 and is selected independently after the core request/error and cache flows exist. Add the native app-level monitor and snackbar described in ARCHITECTURE and UI/UX Definition. The visible outcome is one warning per disconnected period, including initial confirmed disconnection, with existing content and contextual Retry preserved. Recovery dismisses the warning without automatically reloading a screen.

Use a fake monitor to verify initial Unknown, disconnection/recovery, duplicate signals, navigation and foreground/background behaviour, including return with connectivity restored rather than replaying an old warning. Confirm snackbar placement leaves Retry reachable. Verify requests still work through their normal repository/cache path when the monitor reports unavailable. Review callback cleanup and manually check network loss/recovery on a device. Keep the agreed module graph and offline boundary. Record this Should separately from mandatory API error/retry behaviour and complete it before C12 if selected.

## Optional changes

**O01 — Shared character image transition:** prototype on the stable navigation flow; verify matching identity, back behavior and missing-image cases. Keep the basic transition if the shared version introduces fragile behavior.

**O02 — Catalogue accessibility demonstration:** verify TalkBack traversal of cards/search/chips/retry, meaningful announcements, large text and contrast. Record manual evidence and the exact screen scope.

**O03 — Additional verification:** add a focused architectural rule or screenshot scenario only after identifying the behavior or dependency rule it protects. Avoid assertions that merely restate implementation details.

Each optional change is selected and reviewed independently before C12.
