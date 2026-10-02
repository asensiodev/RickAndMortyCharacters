**Status:** Normal-flow and bounded Home/Detail accessibility review completed by the user in debug on 2026-10-02. Quality CI passes and publication is authorized. Complete final device instrumentation, release-runtime acceptance and formal closure/archival remain separate; see current tasks and dated evidence.

## Why

The user wants to freeze the implemented showcase and complete delivery verification. Local quality and nine Paparazzi contracts pass; current Quality CI and human debug review are recorded in the evidence. Earlier release checks retain their tested revision. An earlier physical Pixel navigation instrumentation run crashed without an identified assertion; this is not a confirmed normal-use application crash.

## What Changes

- Define a bounded delivery-verification checklist for the current two-screen product, without adding features or redesigning the UI.
- Give the user a separate editable [manual QA checklist](../../../docs/MANUAL_QA.md); spec tasks link to its results instead of duplicating individual test cases.
- Record local automation, instrumented journeys, the user-selected debug runtime review and Quality CI for identified revisions; retain earlier release evidence separately.
- Record results, unresolved limitations and human acceptance, including remaining C11/O03 review.

The user owns manual physical-device testing and is editing documentation in parallel. C12 checks documentation consistency and links to their final material; it does not repeat that rewrite. On 2026-10-02 the user selected bounded Home/Detail accessibility and validated it manually. The remote API 37 emulator job was trialled and removed after failures; CI now runs qualityCheck only. Process-death restoration, a full accessibility audit, O01, connectivity monitoring, light theme, extra destinations, new screenshot matrices and performance claims remain outside this increment.

## Capabilities

### New Capabilities

- `showcase-delivery`: reproducible verification, physical-device evidence and explicit acceptance for the frozen showcase.

### Modified Capabilities

None. Any discovered behavior fix must remain bounded to the accepted product contract and receive focused regression verification.

## Impact

Delivery evidence, the manual QA document, existing build/test/CI tasks and final documentation consistency. User-selected accessibility refinements cover Home and Detail semantics, without new dependencies. Remote publication, commits, pushes and archival remain separately authorized actions.
