## 1. Scope and test boundaries

- [x] 1.1 Obtain human review of C07 scenarios, query semantics and observable test boundaries before implementation.

## 2. Remote name contract

- [x] 2.1 Observe repository HTTP tests fail for name parameters and contextual filtered emptiness; implement the optional name contract and mapping without transport leakage.
- [x] 2.2 Verify blank omission, encoded names, append identity, malformed/unfiltered errors and cancellation; preserve accepted detail and append-end behavior.

## 3. Query coordination

- [x] 3.1 Observe a failing controlled-time debounce test; implement draft/applied coordination and verify immediate submission/deduplication and clear.
- [x] 3.2 Observe a failing Paging test for query replacement; implement generation/page/total reset and verify obsolete-result rejection with latest-wins switching and generation-safe metadata, without a second item owner.
- [x] 3.3 Verify active-query append Retry, A → B → A races and cached recollection.

## 4. Native search surface

- [x] 4.1 Observe screen failures for persistent search and result transitions; implement resource/token-based input, clear and IME submission with proper focus/keyboard ownership.
- [x] 4.2 Observe no-match shortcut failures; implement the four fixed buttons using the shared immediate-submit path, without delayed duplicate requests.
- [x] 4.3 Verify reachable query error/Retry, input selection/focus, query counter/scroll reset and keyboard visibility on API 37.
- [x] 4.4 Verify the production search → later page → detail → Back journey preserves browsing context and keeps the keyboard closed.

## 5. Completion

- [x] 5.1 Run affected checks, the documented quality gate and release assembly; record actual RED/GREEN commands and results.
- [x] 5.2 Review native design, search/clear/no-matches/retry/Back behavior and update architecture/setup statements only where implementation changes them.
- [x] 5.3 Obtain human acceptance before archival and C08 preparation. Commit and push only on explicit authorization.
