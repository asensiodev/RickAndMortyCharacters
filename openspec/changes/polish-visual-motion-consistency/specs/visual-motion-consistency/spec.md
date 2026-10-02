## ADDED Requirements

### Requirement: Consistent integrated visual language

Home and Detail SHALL preserve the selected dark visual direction and use consistent typography, spacing, shapes, surface roles, outlined icons and text-based status treatment. Visual corrections SHALL use named tokens with their existing owner. Controls and feedback SHALL retain their accepted meaning across loading, content, empty, missing-character and error states. Generated references SHALL NOT introduce unsupported actions or replace the documented native handoff corrections.

#### Scenario: Review the complete browsing journey
- **WHEN** Home content, a selected character's Detail and the returned Home are reviewed together
- **THEN** their shared visual roles are consistent, the selected identity remains clear and the app preserves its two-screen design

#### Scenario: Review feedback and status variants
- **WHEN** Home loading, both empty cases, initial error, append loading/error and Detail loading/error/missing/content are rendered with controlled data including Alive, Dead and Unknown
- **THEN** selected chips remain readable and unambiguous, Retry has consistent contextual treatment, and unsupported Share or filter actions are absent

### Requirement: Immediate data and local image feedback

Available metadata SHALL render without waiting for an animation or portrait. Portrait loading/failure SHALL remain inside stable image bounds and SHALL NOT disable selection or hide facts. Successful portrait appearance SHALL use a brief local fade managed by the shared image loader without replay solely on recomposition. Data skeletons SHALL remain noninteractive; append loading SHALL preserve real cards. Skeleton-to-content motion MAY be added only if it preserves immediate interaction and the current result identity.

#### Scenario: Slow or failed portrait
- **WHEN** character data is available while a controlled portrait request remains pending or fails
- **THEN** metadata and permitted actions are available, the portrait alone shows loading/fallback, and its bounds remain stable

#### Scenario: Fast data replaces skeletons
- **WHEN** a current-query response arrives while skeletons are displayed
- **THEN** real current-query content becomes usable without a minimum loading duration, stale outgoing cards or decorative placeholder semantics

#### Scenario: More characters are loading
- **WHEN** a loaded catalogue requests another page or retries a failed append
- **THEN** loaded cards remain mounted and usable, with progress replacing footer Retry in the same reserved area

### Requirement: Motion preserves browsing continuity

Motion SHALL support feedback or continuity without driving requests, remounting persistent search/chip controls or changing navigation identity. Query changes SHALL NOT leave old matches selectable as current results. Forward and Back transitions SHALL remain coherent using the existing navigation owner. Recomposition, append and normal Back SHALL NOT restart entrance effects across all loaded cards. Detail parallax SHALL remain clipped and bounded with a steady Back control.

#### Scenario: Edit and filter during result replacement
- **WHEN** the user edits a name or selects a different status while results are changing
- **THEN** the input and chips remain available with their accepted focus/selection behavior, and results/actions belong to the active query without an animation-induced duplicate request

#### Scenario: Return from scrolled Detail
- **WHEN** the user opens Detail from a later Home page, scrolls Detail and invokes screen Back or system Back
- **THEN** transitions preserve the selected identity and accepted Home query/filter/viewport without another navigation-induced reload or automatically reopened keyboard

### Requirement: Native motion policy

Compose animations SHALL use native duration handling without a custom system-animation adapter. Portrait fades SHALL use the shared image loader configuration and detail parallax SHALL remain bounded. Loading and selected-state meaning SHALL remain visible. Explicit disabled-motion adaptation for Coil fades and scroll-linked parallax is deferred to O02 by the user's scope revision on 2026-10-02. Repeating loading effects SHALL stop when their component leaves composition.

#### Scenario: Render local motion
- **WHEN** Home or Detail renders loading feedback or portraits
- **THEN** native animation and image-loader APIs own motion without a custom system-scale check or motion-dependent image request

#### Scenario: Leave an animated loading region
- **WHEN** data replaces skeletons or navigation removes the loading region
- **THEN** its repeating animation no longer runs and no delayed effect changes the new screen

### Requirement: Accepted layout continuity

The integrated flow SHALL preserve the accepted phone layout at default font size. System/keyboard insets and floating-counter clearance SHALL keep final cards, feedback, Retry and Back reachable. C11 SHALL NOT introduce font-scale layout policies or accessibility-specific previews. Enlarged-text layout review, contrast/target audits and TalkBack verification are deferred to O02; existing accessibility basics remain applicable.

#### Scenario: Reach the final cards and pagination Retry
- **WHEN** the user reaches the end of loaded results with the counter visible, or opens the keyboard during feedback
- **THEN** final cards and footer actions can clear the overlay, the counter follows its accepted keyboard visibility policy and recovery remains reachable above system insets

### Requirement: Bounded review and regression evidence

C11 SHALL record the reviewed state/layout/motion matrix, actual captures or observations, observed issues, corrections and remaining limitations. Behavioral corrections SHALL have focused regression evidence at existing rendering or production-journey boundaries. Visual-only changes SHALL have before/after review evidence without fabricated behavioral RED. Image-heavy scrolling observations SHALL identify device and conditions; they SHALL NOT be reported as measured frame-rate guarantees without measurements. Human acceptance SHALL precede C12.

#### Scenario: Complete the consistency pass
- **WHEN** affected checks, the documented quality gate, release assembly and API 37 visual/motion journeys have completed
- **THEN** the change records executed commands/results, visual/motion evidence and any remaining manual limitations, with implementation tasks checked only after verification
