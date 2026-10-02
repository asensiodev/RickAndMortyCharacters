**Status:** C12 execution authorized by the user on 2026-10-02. Automated verification is in progress; physical-device review and final human acceptance remain pending. Commit, push and archival are not authorized by this execution request.

## Why

The user wants to freeze the implemented showcase and complete delivery verification. Local quality/release and nine Paparazzi contracts pass, but final physical-device review, remote CI against the final revision and human acceptance remain outstanding. An earlier physical Pixel navigation instrumentation run crashed without an identified assertion; this is not a confirmed normal-use application crash.

## What Changes

- Define a bounded release-readiness checklist for the current two-screen product, without adding features or redesigning the UI.
- Give the user a separate editable [manual QA checklist](../../../docs/MANUAL_QA.md); spec tasks link to its results instead of duplicating individual test cases.
- Verify local automation, instrumented journeys, release assembly/runtime and the existing Quality CI job for an identified source revision.
- Record results, unresolved limitations and human acceptance, including remaining C11/O03 review.

The user owns manual physical-device testing and is editing documentation in parallel. C12 checks documentation consistency and links to their final material; it does not repeat that rewrite. The user subsequently selected an API 37 instrumented CI job, bounded Home accessibility improvements and their manual demonstration on 2026-10-02. Process-death restoration, a full accessibility audit, O01, connectivity monitoring, light theme, extra destinations, new screenshot matrices and performance claims remain outside this increment.

## Capabilities

### New Capabilities

- `showcase-delivery`: reproducible verification, physical-device evidence and explicit acceptance for the frozen showcase.

### Modified Capabilities

None. Any discovered behavior fix must remain bounded to the accepted product contract and receive focused regression verification.

## Impact

Delivery evidence, the manual QA document, existing build/test/CI tasks and final documentation consistency. User-selected accessibility refinements cover Home and Detail semantics, without new dependencies. Remote publication, commits, pushes and archival remain separately authorized actions.
