## 1. Review and setup

- [x] 1.1 Obtain human review of C06 scope, Paging ownership and public test boundaries before implementation.
- [x] 1.2 Pin compatible stable Paging runtime/Compose/testing artifacts in the existing catalogue and Home module; preserve pure domain/data and the production module graph.

## 2. Data and paging slices

- [x] 2.1 Observe repository tests fail for recognized append completion and unexpected append errors; add the pure end outcome/data mapping and verify GREEN plus first-page/detail/cancellation regressions.
- [x] 2.2 Observe PagingSource behavior tests fail for next-page/end/error mapping; implement the internal Home adapter and verify GREEN without leaking transport/Paging into domain.
- [x] 2.3 Observe Pager/ViewModel tests fail for multipage loading, guarded retry, cached recollection and owner cancellation; migrate the Home owner to one cached generation and metadata state, then verify GREEN.

## 3. Home feedback slices

- [x] 3.1 Observe Home screen tests fail for append progress/error/Retry; implement full-width footer feedback using existing tokens/resources, retaining cards and selection.
- [x] 3.2 Observe real loaded/total and visibility/clearance checks fail; add the floating counter derived from presented items and same-generation API total, without a copied list.
- [x] 3.3 Verify one real Pager/fake-repository scroll/retry screen flow and the production navigation journey for a later-page card, retained data/count and Back position.

## 4. Validation and acceptance

- [x] 4.1 Run affected JVM tests, Konsist and API 37 Home/navigation instrumentation through the managed workflow; complete qualityCheck and release assembly, recording actual results.
- [x] 4.2 Review native live scrolling and controlled append failure/Retry, final-card/footer clearance and large-text counter; document any remaining manual limitations.
- [x] 4.3 Update implementation/AI evidence and applicable setup/status documentation; validate OpenSpec, local links and diff cleanliness.
- [x] 4.4 Obtain human acceptance before archival and C07. Do not commit or push without explicit authorization.
