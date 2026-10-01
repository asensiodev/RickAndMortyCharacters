**Status:** scope and Home screen test boundary approved by the human request on 2026-10-01; implemented and locally validated; accepted for publication on 2026-10-01; archival pending.

## Why

After typing a name, users can start browsing results while the keyboard still covers part of the grid. A deliberate drag should dismiss it without changing the active query.

## What Changes

- Hide the keyboard when the user begins dragging Home's result grid.
- Preserve search text, selection, focus, selected status and query ownership.
- Keep programmatic scrolling and result-generation resets from dismissing the keyboard.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `character-catalogue`: dismiss the search keyboard on a user grid drag.

## Impact

Home rendering and its screen tests, plus the UI behavior document. This independently authorized refinement does not accept/archive C09 or advance state-restoration scope.
