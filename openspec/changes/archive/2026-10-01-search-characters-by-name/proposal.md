**Status:** C07 scope and test boundaries approved on 2026-10-01; implemented, locally validated and accepted on 2026-10-01; archival, commit and push authorized.

## Why

Home can browse the complete catalogue but cannot narrow it by name. Search must operate on the remote catalogue, preserve the input across result states and prevent obsolete requests from replacing the active result.

## What Changes

- Add the persistent search field, clear action and immediate keyboard Search submission, with a 300 ms typing debounce.
- Start a new paginated generation for each changed applied name, resetting its cards, total and scroll together. Ignore obsolete items and metadata.
- Show contextual no matches with fixed Rick, Morty, Beth and Summer search shortcuts on the same Home destination.
- Preserve query, loaded pages and scroll on normal Detail → Back without reopening the keyboard.

Status chips remain C08; response caching remains C09. No new destination, module or external dependency is required.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `character-catalogue`: remote name requests, persistent search interactions, generation isolation and contextual no matches.

## Impact

Character repository/page HTTP contract and fixtures; Home ViewModel, PagingSource, route, rendering, strings and tests; production navigation journey fixtures. Existing detail behavior and module boundaries remain unchanged. The existing quality gate applies.
