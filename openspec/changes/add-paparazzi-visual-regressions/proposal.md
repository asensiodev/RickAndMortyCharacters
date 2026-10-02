**Status:** O03 implementation authorized and implemented on 2026-10-02, without commit or push. Paparazzi 2.0.0-alpha05.1 renders nine Home/Detail baselines with the approved JDK 21 build runtime, existing AGP and unchanged Java/Kotlin targets 17. The design records the failed earlier alpha05/JDK 17 smoke, alpha tooling decision and actual checks. Baseline human acceptance, remote CI and archival remain pending.

## Why

Recent visual changes include Detail's larger square portrait and episode cards. Interaction tests prove actions and state, but do not detect unintended changes to their geometry, wrapping or visual hierarchy. Add a small set of reproducible screenshot regressions before C12 final delivery.

## What Changes

- Introduce Paparazzi in the existing Home and Detail test boundaries, with versions pinned in the catalogue after compatibility verification.
- Protect a bounded set of real components and screen compositions using deterministic data, images, theme, viewport and animation state.
- Separate recording reviewed golden images from verifying them; prove that an intentional visual change produces a useful diff.
- Add verification to the existing quality/CI path without automatically rewriting baselines, and document record/verify/report commands.

No production redesign, banner, enlarged-text/accessibility matrix, new destination, preview enumeration, runtime gallery or screenshot framework abstraction is included. O02 retains accessibility review. Native scroll/parallax/navigation and device instrumentation remain separate checks. C11 integrated review and the connected Pixel instrumentation crash are not declared resolved by this draft.

## Capabilities

### New Capabilities

- `screenshot-regressions`: deterministic, reviewed visual baselines and repeatable verification at existing rendering boundaries.

### Modified Capabilities

None; screenshots protect the accepted rendering contract without adding product behavior.

## Impact

Home and Detail test sources/build configuration, version catalogue, quality/CI configuration and development documentation. Use the existing design system and shared image-loader contract with controlled test images. No new production module or dependency is required. Paparazzi/renderer compatibility with the current toolchain must be demonstrated before accepting baselines.
