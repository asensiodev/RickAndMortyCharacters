**Status:** C05 accepted on 2026-10-01, locally validated and archived.

## Why

Home already loads real characters but selection has no destination. Add the second product screen and complete the first grid → detail → back flow while retaining Home's loaded content and scroll.

## What Changes

- Extend the shared character repository with a detail-by-ID operation and pure success/not-found/failure outcomes.
- Decode the character's identity, facts and episode-reference count through the existing Retrofit client; reuse the app-owned Coil loader.
- Implement Details state/actions, a Hilt ViewModel, an independently renderable screen and normal-size IDE previews.
- Add app-owned typed Navigation 3 destinations, entry-scoped ViewModels and retained Home UI state.
- Render the selected portrait/identity/facts design, loading/error/not-found states, guarded Retry and an always-available Back control.
- Add bounded portrait parallax with a static alternative and native destination transitions.
- Verify repository, ViewModel, screen and production navigation behavior through controlled fixtures and behavior-focused TDD.

C05 applies normal-back retention to today's first-page Home. Search/filter retention and keyboard behavior are extended and verified when C07/C08 introduce those controls. Pagination, HTTP caching, process-death restoration, shared-image transitions, full accessibility demonstration and screenshot tooling retain their own tickets.

## Capabilities

### New Capabilities

- `character-detail`: detail-by-ID data, immutable screen outcomes, portrait/facts rendering and recovery.
- `character-navigation`: app-owned Home/Detail destinations, entry lifetimes and return to the browsing context.

### Modified Capabilities

- `character-catalogue`: selection opens the selected character through app wiring while preserving independent portrait feedback.

## Impact

`:domain:characters`, `:data:characters`, `:feature:details`, `:feature:home`, `:app`, dependency catalogue and test configuration. Retain the six production modules, API 37 target, minimum API 26 and existing source-package conventions. Features remain independent; navigation belongs in app. No new runtime test/gallery destination or general navigation framework is introduced.
