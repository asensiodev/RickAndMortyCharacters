# Product Requirements Document (PRD)

**Rick And Morty Characters** lets users browse characters, narrow the catalogue by name and status, and inspect a selected character. The initial experience consists of a character grid and a detail screen, with an English interface. The MVP targets portrait orientation.

Status: planned. This document defines product scope, priorities and acceptance. Screen content and interaction design belong in [UI/UX Definition](UI_UX.md), technical decisions in [ARCHITECTURE](ARCHITECTURE.md), and capability-level scenarios and implementation tasks in OpenSpec changes.

## Priorities

MoSCoW distinguishes required capabilities, preferred improvements, optional enhancements and explicit boundaries for the current iteration.

| Priority | Capability | Observable outcome |
|---|---|---|
| **Must** | Paginated character grid | Browse progressively, with a floating loaded/total counter for the active result |
| **Must** | Character detail | Open the selected ID, inspect its information and return to the previous browsing context |
| **Must** | Search by name | The grid represents the current query; clearing removes the name constraint; no-match suggestions offer quick searches |
| **Must** | Status filter chips | All, Alive, Dead and Unknown; selection combines with the name query across every page |
| **Must** | Loading, empty, error and retry states | Initial and additional-page failures are distinct; failed pagination preserves existing content |
| **Must** | Image loading and caching | Correctly sized images, placeholders and fallback through a shared library-managed loader |
| **Must** | HTTP response caching | Reuse fresh eligible JSON responses and revalidate stale entries when validators are available |
| **Must** | Consistent UI and motion | Defined components, typography, spacing, loading feedback and navigation transitions |
| **Must** | Accessible component foundations | Meaningful semantics, adequate contrast, text-based status and touch targets of at least 48dp |
| **Must** | Verifiable quality | Critical behavior covered by tests, automated checks and reproducible development instructions |
| **Should** | Connectivity awareness | An app-level snackbar reports observed internet unavailability without removing content or blocking requests |
| **Should** | Light/dark themes | Consistent visual variants following system appearance |
| **Should** | State restoration | Restore minimal query, filter and destination state after recreation; refetch remote data as needed |
| **Could** | Shared character image transition | Animate the character image between its grid card and the detail screen |
| **Could** | Accessibility demonstration | A documented TalkBack and large-text journey on the catalogue screen |
| **Won't in this iteration** | Offline catalogue and accounts | No guaranteed offline journey, synchronized catalogue database or authentication |
| **Won't in this iteration** | Additional product flows | Onboarding, favourites and separate episode/location screens are outside the two-screen flow |

Basic accessibility is part of component design. The optional screen-level demonstration does not imply a complete accessibility audit. Spanish localization remains a possible later iteration; user-visible text belongs in string resources from the start.

## Functional contract

**Grid:** character image, name and status; species if supported by the selected layout. Start design exploration with two columns on phones and adapt to available width and text size. Use stable character identity, and distinguish the end of the catalogue from a failed request. Home starts with search below the system safe area, without an app title or top app bar. A read-only floating pill shows loaded characters out of the active query total from API pagination metadata; neither value is hardcoded or based on visible cards. It must not obstruct the final cards or pagination Retry. Its per-state visibility is defined in UI/UX Definition.

**Search and chips:** changing name or status starts a new paginated query and resets its loaded/total counter. Debounce text input and discard obsolete results. Selecting All removes only the status constraint; clearing the name preserves the selected status. Chip selection is single-choice. Filter the complete remote result set, rather than only the loaded page. No matches from search, status or both is an empty state inside Home: keep the input and chips visible and show the message in the results area. It adds no destination, Back action or Retry. In that no-match state, show fixed Rick, Morty, Beth and Summer search shortcuts. Selecting one replaces the name and submits immediately while preserving the selected status, using the normal query-reset behavior. These are search shortcuts, not guaranteed matches or API recommendations. Selecting the current status chip again preserves the current results and scroll.

**Detail:** image, name, status, species, gender, optional type, origin and location names, and episode count. Missing optional information must not break the layout. Navigate by ID. Counting existing episode references does not require fetching each episode.

**Navigation and restoration:** Home and Character detail are the only destinations; search/filter/loading/empty/error/retry changes happen in place. Only detail has an app Back action. Normal back navigation preserves query, filter and scroll while the browsing destination remains alive, without reopening the search keyboard. After process death, only the agreed minimal state is restored; catalogue data may need another request. The precise scroll-restoration behavior belongs in its change.

**Errors:** initial home/detail request failures show a persistent contextual message and Retry. Retry repeats the failed operation for the current query or character ID. A failed image does not disable a character card; a failed additional page retains loaded cards with an inline retry. Empty results and a confirmed missing character have their own in-place states. Keep the relevant controls available during failures; show retry progress in the affected area and prevent duplicate submissions. A confirmed out-of-range append response ends pagination without removing loaded cards.

**Connectivity (Should):** notify once per observed disconnected period while the app is visible, including an initial confirmed disconnected state. The dismissible snackbar does not replace screen errors. On recovery, dismiss an outstanding warning; keep recovery through explicit Retry rather than automatic reload. Connectivity is advisory: do not gate requests or cache access, classify every API failure as offline, or introduce a guaranteed offline journey.

## Two distinct caches

Both image caching and HTTP response caching are Must capabilities. Coil handles image caching; OkHttp handles cacheable API responses. Different pages, search terms, statuses and detail IDs must retain distinct request identities. Cache storage is bounded and disposable, and HTTP freshness directives and validators govern reuse.

A fresh cached response can be served without a network request, including incidentally when connectivity is unavailable. This does not guarantee an offline catalogue: uncached or expired data can still require the network. Do not force stale responses after failures or add a separate database. Verify a fresh cache hit, stale revalidation, request separation and `no-store` behavior with deterministic HTTP fixtures.

The REST API provides character details, pagination and combined name/status queries. Representative 404 responses were observed for an empty first-page query, a missing detail ID and an out-of-range page; [ARCHITECTURE](ARCHITECTURE.md#api-outcomes-and-visible-states) defines their distinct outcomes. Verify that mapping with deterministic fixtures during implementation. [API documentation](https://rickandmortyapi.com/documentation).

## Completion

The initial product is complete when all Must capabilities work together, required checks pass and documentation reflects the implemented behavior. Should and Could items retain their own acceptance criteria; optional enhancements do not replace an incomplete core flow.
