## ADDED Requirements

### Requirement: Bounded visual regression coverage

The project SHALL use Paparazzi to protect the nine Home/Detail visual contracts listed in the approved design matrix at existing rendering boundaries. Tests SHALL render real production composables with fixed data, controlled images, the accepted dark theme, an explicit ordinary phone profile and default font scale. This increment SHALL NOT introduce runtime demo screens or enlarged-text/light-theme matrices.

#### Scenario: Capture the accepted Detail composition
- **WHEN** the Detail content and episode fixtures render
- **THEN** screenshots protect the square portrait, Back/identity/fact hierarchy and episode-card or section-feedback geometry without fetching live data

#### Scenario: Capture the accepted Home composition
- **WHEN** the card, skeleton, no-match and append-error fixtures render
- **THEN** screenshots protect the accepted wrapping, portrait/fallback bounds, persistent controls and feedback/counter composition

### Requirement: Repeatable real rendering

Screenshot tests SHALL use explicit renderer/API, viewport, density, locale, fonts and controlled animation frames. They SHALL avoid live network requests, arbitrary sleeps and screenshot-specific production branches. Paparazzi compatibility with the current production toolchain SHALL be demonstrated and pinned before baselines are accepted.

#### Scenario: Verify unchanged fixtures repeatedly
- **WHEN** accepted baselines are verified repeatedly under the documented environment
- **THEN** unchanged fixtures pass without unexplained image drift

### Requirement: Reviewed golden images and useful failures

Recording golden images SHALL be an explicit operation separate from verification. Every accepted golden SHALL be reviewed against the intended UI. Verification SHALL fail for a missing or changed expected snapshot and provide a useful report/diff; it SHALL NOT silently record or accept replacements.

#### Scenario: Detect and restore an intentional visual change
- **WHEN** a temporary visual token/layout change is verified against an accepted golden
- **THEN** verification fails with a meaningful diff, and restoring the source passes without rerecording that golden

### Requirement: Quality integration and honest limits

The existing quality and CI paths SHALL invoke screenshot verification rather than recording. Documentation SHALL state actual commands, profile/version assumptions, review policy, results and remaining limitations. Screenshot verification SHALL supplement existing interaction tests and SHALL NOT be presented as proof of native parallax, navigation, accessibility, performance or device crash recovery.

#### Scenario: Complete the visual testing increment
- **WHEN** screenshot checks, the documented quality gate and release assembly finish
- **THEN** executed outcomes and any unexecuted or failed CI/device checks are recorded, with human acceptance preceding archival and C12
