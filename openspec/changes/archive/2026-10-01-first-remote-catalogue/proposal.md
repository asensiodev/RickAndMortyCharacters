**Status:** accepted, locally validated and archived on 2026-10-01. Scope and test boundaries approved on 2026-10-01.

## Why

The accepted cards and image states need a real catalogue. Connect the first remote page to Home through the agreed domain/data boundary and make loading, empty data and recovery observable before adding further browsing capabilities.

## What Changes

- Add the pure character summary/page models and repository contract consumed by Home.
- Implement the first-page REST request, internal DTO mapping and contextual error translation in data.
- Configure Hilt and the Retrofit/OkHttp/kotlinx.serialization client in the existing modules.
- Add Home state/actions, its ViewModel and a native grid using the accepted cards and skeletons.
- Replace the app shell with Home, including initial loading, content, empty catalogue, error and guarded Retry.
- Add repository and ViewModel TDD slices plus screen-level Compose tests on API 37 with controlled data/images.

This change delivers the first unfiltered page. Detail/navigation, subsequent pages/counter, search/chips and HTTP response caching retain their existing tickets. Do not render inactive controls for those future capabilities.

## Capabilities

### New Capabilities

- `character-catalogue`: remote character-page access and the Home first-page loading/content/empty/error/retry flow. Later browsing changes extend this capability.

### Modified Capabilities

None. The accepted card/image requirements are exercised through Home without changing their contract.

## Impact

`:domain:characters`, `:data:characters`, `:feature:home`, `:app`, the version catalogue and test configuration. Reuse `:core:designsystem`; retain the six-module graph and API 37 target. Library compatibility is verified and versions are pinned during implementation. No extra production activity or module is introduced.
