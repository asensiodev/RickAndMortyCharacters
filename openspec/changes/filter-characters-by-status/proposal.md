**Status:** C08 scope approved and implemented; locally validated on API 37; accepted for publication; archival pending.

## Why

Name search is implemented. Users also need to select a character status, alone or combined with the name, across the complete remote catalogue.

## What Changes

- Add single-choice All, Alive, Dead and Unknown chips below the persistent search field, using the selected native design.
- Apply name/status together on every page; changing status starts a new generation with page, scroll and counter reset.
- Preserve status when clearing the name or choosing a suggested name. All removes only the status constraint.
- Keep the selected controls across loading, empty, error and Detail → Back; reject obsolete combined-query results.

HTTP caching remains C09. No new module, dependency, destination or local catalogue filtering is required.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `character-catalogue`: remote status constraints, combined query identity and persistent single-choice controls.

## Impact

Pure character repository and HTTP page contracts, contextual no-match mapping, Home actions/state/Paging adapter, chip rendering/strings and HTTP/ViewModel/screen/journey tests. Details remains unchanged; the existing quality gate applies.
