## ADDED Requirements

### Requirement: Selectable character metadata

The Home-owned character card SHALL show the supplied full name in semantics, readable name, species and text-based Alive/Dead/Unknown status on a solid surface. The entire card SHALL select the supplied character ID through a callback, regardless of portrait state. Caller placement SHALL be accepted through a root modifier. Portrait bounds SHALL remain square. At ordinary text sizes names SHALL allow two lines; larger text SHALL expand rather than lose the full visible name.

#### Scenario: Character selected
- **WHEN** a user selects a card with known metadata and a loaded portrait
- **THEN** the metadata remains readable and the callback receives that character ID

#### Scenario: Unknown status and long name
- **WHEN** a card contains a long name and Unknown status, including at large text size
- **THEN** the full name remains in semantics, status is stated in text and the layout remains usable

### Requirement: Independent portrait state

The portrait SHALL load through the caller's shared Coil loader using its layout constraints and crop scale. Pending images SHALL have contained loading feedback; failed or absent images SHALL have a neutral fallback. Metadata and selection SHALL NOT depend on image success. A loaded portrait SHALL replace its loading feedback without changing image bounds.

#### Scenario: Portrait pending then ready
- **WHEN** a controlled portrait request is pending and later succeeds
- **THEN** loading is limited to the portrait, text and selection remain available, and the loading feedback disappears after success

#### Scenario: Failed or absent portrait
- **WHEN** the portrait request fails or its URL is absent
- **THEN** the image area shows a neutral fallback and metadata and selection remain available

### Requirement: Noninteractive data skeleton

The skeleton SHALL preserve the card's image and metadata structure without invented character data or selection. Its loading semantics SHALL describe loading without announcing decorative placeholder shapes. Loading motion SHALL have a static equivalent and end when the component leaves composition.

#### Scenario: Character data is pending
- **WHEN** a loading skeleton is rendered before metadata exists
- **THEN** it exposes loading feedback with no click action or artificial character name

### Requirement: Shared image and visual foundations

The app SHALL own one lazily shared Coil ImageLoader with bounded memory and disposable disk caches. Features SHALL accept a loader rather than create one per card. The design system SHALL own generic theme/tokens/placeholders and SHALL NOT depend on character models.

#### Scenario: Shared visual review
- **WHEN** Android Studio previews render cards and skeletons
- **THEN** they use the shared theme with deterministic images and static loading shapes
