**Status:** C06 scope and test boundaries accepted on 2026-10-01; implemented, locally validated and accepted on 2026-10-01; archival authorized before C07.

## Why

Home currently loads only the first remote page. Users need to browse the remaining characters, recover from an additional-page failure without losing content, and see the real loaded/total count.

## What Changes

- Adapt the pure repository page contract to Paging 3 inside Home; retain one ViewModel-owned paginated generation across normal Detail → Back navigation.
- Load subsequent pages as the user approaches the loaded end. Retain existing cards during append progress and failure; provide contextual footer Retry.
- Stop when API metadata has no next page or data recognizes the documented additional-page end response. Other failures remain retryable.
- Add the selected floating loaded/total pill with real values, system-safe placement and enough scroll clearance for the last cards and Retry.
- Migrate existing first-page tests to the Paging boundary while preserving their accepted loading, empty, error, cancellation and selection behavior.

Search and chips remain C07/C08. HTTP caching remains C09. This increment adds no inactive search controls, refresh gesture, database, navigation destination or runtime gallery.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `character-catalogue`: paginated ownership, contextual end mapping, append states/retry and loaded/total feedback.

## Impact

Home ViewModel/route/rendering and tests; a small Home-owned PagingSource; the domain page-result contract and data mapping/tests for confirmed append completion; version catalogue and Home Paging dependencies. Existing navigation and detail remain consumers of the same independent feature boundary. No new production module or CI workflow is required. A JVM-only `:core:testing` support module shares coroutine test setup between Home and Details through test dependencies. The existing quality gate continues to apply.
