**Status:** Implemented, validated and accepted by the user on 2026-10-02. Commit and push explicitly authorized; archival remains separate.

## Why

Rapid scrolling produces portrait HTTP 429 responses. The API supplies Retry-After, but the shared image pipeline currently returns a terminal error without retrying; visible portraits only recover when their card is recreated.

## What Changes

- Retain independently available metadata and card selection while a portrait loads.
- Retry an image once after HTTP 429, respecting Retry-After and cancelling with its existing Coil request.
- Keep retry waits bounded; permanent errors and exhausted retries use the existing portrait fallback.
- Preserve existing image caches, sizing, motion changes, scroll and page Retry ownership.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `character-card-images`: bounded rate-limit recovery through the shared loader.

## Impact

App-owned shared Coil configuration and a focused interceptor; tests at the existing controlled-image Home screen and ImageLoader request boundaries. No new module, runtime dependency, scroll controller or ViewModel state is introduced.
