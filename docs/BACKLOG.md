# Implementation index

Updated 2026-10-02. The catalogue, search, filters, Detail, episodes, caching and recovery are implemented. The user validated the browsing flow and bounded Home/Detail accessibility on a physical Pixel 9a in debug. Quality CI passes; instrumented tests run locally, not in CI.

[README](../README.md) owns delivery scope, architecture and setup. [Development process](DEVELOPMENT_PROCESS.md) explains the workflow. OpenSpec owns each change's requirements and dated evidence; archival is a separate administrative step, not a missing app feature.

## Implemented changes

| Change | State and evidence |
|---|---|
| C01 — Android foundation | Accepted and archived: [change](../openspec/changes/archive/2026-09-30-android-foundation/) |
| C02 — Quality checks | Accepted and archived: [change](../openspec/changes/archive/2026-09-30-shared-quality-checks/) |
| C03 — Cards and images | Accepted and archived: [change](../openspec/changes/archive/2026-10-01-character-card-images/) |
| C04 — Remote catalogue | Accepted and archived: [change](../openspec/changes/archive/2026-10-01-first-remote-catalogue/) |
| C05 — Detail and navigation | Accepted and archived: [change](../openspec/changes/archive/2026-10-01-character-detail-navigation/) |
| C05A — Konsist checks | Accepted and archived: [change](../openspec/changes/archive/2026-10-01-konsist-architecture-checks/) |
| C06 — Pagination | Accepted and archived: [change](../openspec/changes/archive/2026-10-01-complete-catalogue-pagination/) |
| C07 — Name search | Accepted and archived: [change](../openspec/changes/archive/2026-10-01-search-characters-by-name/) |
| C08 — Status filters | Implemented, locally validated and published: [change](../openspec/changes/filter-characters-by-status/) |
| C09 — HTTP cache | Implemented, locally validated and published: [change](../openspec/changes/cache-character-http-responses/) |
| C11 — Visual and motion consistency | Implemented and published; normal-flow manual review recorded in C12: [change](../openspec/changes/polish-visual-motion-consistency/) |
| O02 — Accessibility | Home/Detail support implemented and manually validated: [manual results](MANUAL_QA.md#home-accessibility-demonstration) and [C12 evidence](../openspec/changes/validate-showcase-delivery/design.md) |
| O03 — Visual regression tests | Nine Paparazzi baselines implemented and verified locally/in CI; formal baseline acceptance and archival remain tracked in [tasks](../openspec/changes/add-paparazzi-visual-regressions/tasks.md) |
| O04 — Episode appearances | Implemented and published; original test evidence retained in [C12](../openspec/changes/validate-showcase-delivery/design.md#historical-episode-implementation-evidence) |
| Branding | Portal gun icon and native splash implemented and published: [change](../openspec/changes/add-portal-gun-branding/) |
| Search focus | Keyboard/focus correction implemented, tested and manually validated: [change](../openspec/changes/hide-search-keyboard-on-scroll/) |
| Portrait recovery | Bounded rate-limit recovery implemented and tested: [change](../openspec/changes/recover-rate-limited-portraits/) |

## Delivery review

C12 records the [manual results](MANUAL_QA.md), [automated evidence](../openspec/changes/validate-showcase-delivery/design.md) and remaining [formal closure tasks](../openspec/changes/validate-showcase-delivery/tasks.md). Manual validation is complete. The remote emulator trials failed and their CI job was removed; no successful final full device suite or final release-runtime acceptance is inferred from the debug review.

Latest reviewed Quality CI: [37039977135](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/37039977135), commit `9fdb7fb`, PASS. Later runs belong to their own source revisions.

## Outside this delivery

Connectivity snackbar (S01), shared-image navigation (O01), light/system appearance, process-death session restoration and a complete accessibility audit remain outside scope. See [Delivery scope](../README.md#delivery-scope).

The [named Kotlin arguments pass](../openspec/changes/name-kotlin-call-arguments/) was paused by the user and remains unimplemented; it is not a delivery requirement.
