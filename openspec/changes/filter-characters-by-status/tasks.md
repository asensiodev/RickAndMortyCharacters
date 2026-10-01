## 1. Scope and test boundaries

- [x] 1.1 Obtain human review of combined-query semantics, chip interactions and test boundaries before implementation.

## 2. Remote combined-query contract

- [x] 2.1 Observe a failing HTTP test for status filtering; implement the typed optional status and wire mapping.
- [x] 2.2 Verify combined parameters across pages, All omission and status-only/combined no matches; preserve unfiltered/malformed failures and cancellation.

## 3. Combined query coordination

- [x] 3.1 Observe a failing ViewModel test for a changed status; extend query identity and generation reset without another item/load owner.
- [x] 3.2 Verify pending-debounce chip selection makes one query, active-chip reselection does nothing, and Clear/All/suggestions preserve the other constraint.
- [x] 3.3 Verify named/status append and Retry, obsolete query/append rejection and cached recollection.

## 4. Native controls and journey

- [x] 4.1 Observe a failing Home screen test for status selection; implement the persistent token/resource-based chip row and selected semantics.
- [x] 4.2 Verify input text/selection/focus across result states, narrow/large-text access, status-only/combined emptiness, suggestions and guarded Retry.
- [x] 4.3 Verify counter/page/scroll reset and the filtered later-page detail/back production journey on API 37.

## 5. Completion

- [x] 5.1 Run affected checks, the completion gate and release assembly; record actual RED/GREEN and regression results.
- [x] 5.2 Review the native combined-filter journey and update documentation to match verified behavior.
- [x] 5.3 Obtain human acceptance before archival and C09 preparation. Commit and push only on explicit authorization.
