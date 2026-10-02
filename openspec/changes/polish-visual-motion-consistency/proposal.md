**Status:** Implemented motion simplification and feedback corrections accepted for publication by the user on 2026-10-02. Remaining integrated-review tasks stay open; archival remains separate.

## Why

The main Home → Detail → Back flow has grown through separate increments. Before final delivery, review the integrated screens against the selected design and close observed inconsistencies in visual hierarchy, loading, motion and usable layout.

## What Changes

- Review Home and Detail together against the selected Stitch references and the corrections in `docs/UI_UX.md`; record actual findings before changing components.
- Correct observed differences in typography, spacing, shapes, surfaces, status treatment, icons and contextual loading/error feedback through existing tokens and component owners.
- Verify local image fades, skeleton feedback, chip feedback, bounded detail parallax and forward/back transitions using native Compose duration handling and shared image-loader defaults.
- Evaluate skeleton-to-content and result-replacement motion; retain immediate replacement unless a small native effect improves continuity without stale content or disrupted input.
- Verify the accepted phone layout, system/keyboard insets, footer clearance and image-heavy scrolling with focused regression evidence.
- Defer enlarged-text layout changes, accessibility-specific previews, contrast/target audits and TalkBack review to O02, as requested by the user on 2026-10-02. Preserve existing accessibility basics.

Preserve the accepted dark design, two destinations, search/filter/pagination contracts. Shared image transitions, a light theme, connectivity monitoring, new product flows, a runtime gallery and a screenshot-testing framework are outside this increment.

## Capabilities

### New Capabilities

- `visual-motion-consistency`: integrated visual review, motion continuity/native duration handling, accepted-layout usability and bounded acceptance evidence for the showcase.

### Modified Capabilities

None. Existing card, catalogue, detail and navigation contracts remain applicable; this capability defines their integrated visual acceptance without replacing them.

## Impact

Primarily `:core:designsystem`, Home/Detail rendering and tokens, plus app navigation or shared image-loader configuration only if an observed motion issue requires a correction there. Reuse current Compose, Navigation 3 and Coil dependencies. Domain/data contracts, request ownership and the module graph remain intact. C12 owns final README captures, delivery instructions and release-candidate validation.
