**Status:** C09 scope and test boundaries approved by the human implementation request on 2026-10-01. C08 is accepted and locally validated; C09 is implemented and locally validated, including API 37 navigation and native production composition. Human acceptance and commit/push authorization recorded on 2026-10-01; archival pending.

## Why

Catalogue and detail requests currently use an API client without a disk cache. Repeated eligible requests should reuse fresh HTTP responses and validate expired responses through the existing repository, following the provider's cache policy.

## What Changes

- Configure one data-owned, bounded 10 MiB OkHttp disk cache shared by catalogue and detail, separate from the image cache.
- Reuse fresh eligible responses and revalidate stale entries with available validators; retain server freshness and storage directives.
- Keep page, name, status and detail-ID responses distinct, with ordinary repository failures on network-dependent requests that fail.
- Verify the contract through repository calls, MockWebServer and temporary disk storage; recheck real endpoint headers during implementation.

No offline catalogue, Room database, custom JSON store, forced cache-only requests, custom stale-on-error policy, connectivity monitor or UI controls are added.

## Capabilities

### New Capabilities

- `character-http-cache`: shared bounded HTTP response storage, freshness, validation and request isolation for character pages and detail.

### Modified Capabilities

None. Catalogue/detail result contracts and UI behavior remain applicable without adding transport/cache metadata to domain.

## Impact

Primarily `:data:characters` network composition and repository integration tests, plus documentation. Reuse the pinned OkHttp, Retrofit and MockWebServer dependencies. Keep the module graph, repository interfaces and feature request ownership. Final scope must be reconciled with accepted C08 before implementation.
