## ADDED Requirements

### Requirement: Pure detail-by-ID repository contract

The character repository SHALL provide a suspending detail request for the supplied character ID and return a pure detail value or explicit NotFound/Failure outcome. Details SHALL contain identity, gender, optional type, origin/location names, portrait reference and episode-reference count. Data SHALL use the existing API client; transport and navigation objects SHALL NOT cross the domain contract.

#### Scenario: A character detail is received
- **WHEN** the repository receives a valid detail response for the selected ID
- **THEN** it returns the corresponding character identity and facts
- **AND** episode appearances equal the returned episode-reference array length without fetching those references

#### Scenario: Optional type or portrait is absent
- **WHEN** the response has empty type or no usable portrait reference
- **THEN** the character still loads with its remaining supplied facts

### Requirement: Contextual missing character and request failures

A recognized missing-character response SHALL produce NotFound. Network, service and successful-response decoding failures SHALL remain typed failures. Unrecognized 404 responses SHALL NOT be guessed to mean NotFound. Cancelling the request SHALL propagate cancellation rather than a user error.

#### Scenario: The character is confirmed missing
- **WHEN** the detail endpoint returns 404 with the recognized “Character not found” error
- **THEN** the repository returns NotFound

#### Scenario: The response fails for another reason
- **WHEN** the request encounters network/service failure, an unrecognized HTTP error or invalid successful response
- **THEN** it returns the corresponding failure without fabricated character facts

#### Scenario: A pending request is cancelled
- **WHEN** its owner cancels the detail request
- **THEN** cancellation propagates and no ordinary error result is returned

### Requirement: One request owner per detail identity

Details SHALL expose immutable Loading, Content, Error or NotFound state from a Hilt ViewModel with read-only observation and explicit actions. Its destination ID SHALL identify initial load and Retry. Repeated initialization SHALL NOT reload the same entry, and pending actions SHALL NOT issue duplicate requests. Removing the destination owner SHALL cancel pending work.

#### Scenario: Initial detail load succeeds
- **WHEN** a detail entry starts for a character ID and the request succeeds
- **THEN** its state moves from Loading to that character's Content

#### Scenario: Initialization or pending actions repeat
- **WHEN** the same entry is initialized again or Retry is repeated while loading
- **THEN** only the intended single request owns the outcome

#### Scenario: The entry owner is cleared
- **WHEN** its ViewModel owner is cleared during a pending request
- **THEN** the request is cancelled without publishing Error for cancellation

### Requirement: Native identity and read-only facts

Details SHALL use the selected dark theme, bounded portrait and grouped fact rows. Identity SHALL show name/status/species. Facts SHALL show optional Type, Gender, Origin, Last known location and exactly one “Episode appearances” count. Empty Type SHALL omit its row; supplied unknown values SHALL display “Unknown”. Facts SHALL wrap and remain reachable by scrolling within system safe areas. Back SHALL be the sole screen-level action, with a labelled touch target of at least 48dp.

#### Scenario: Character facts are rendered
- **WHEN** Details has Content
- **THEN** it displays the supplied identity, locations and one episode count with visible labels
- **AND** it adds no episode titles, unsupported lore, duplicate identity facts or extra actions

#### Scenario: Optional and long values are rendered
- **WHEN** Type is empty or other supplied values are unknown/long
- **THEN** Type is omitted, unknown values remain explicit and long facts wrap without becoming hidden

### Requirement: In-place loading and recovery with persistent Back

Details SHALL retain Back across Loading, Content, Error and NotFound. Loading SHALL show noninteractive skeletons. Error SHALL show “Couldn't load character” and guarded Retry for the same ID. Retry SHALL expose progress, then the actual outcome. NotFound SHALL show “Character not found” and Back without Retry or another destination.

#### Scenario: The user leaves a loading or failed detail
- **WHEN** the user selects Back while detail is loading or failed
- **THEN** the Back callback remains usable

#### Scenario: A failed detail is retried
- **WHEN** the user selects Retry after a recoverable failure
- **THEN** the same ID loads again with progress and no repeated pending submissions
- **AND** failure restores Error/Retry while success shows its returned outcome

#### Scenario: A missing character is rendered
- **WHEN** detail is NotFound
- **THEN** it shows its unavailable message and Back with no Retry

### Requirement: Local portrait feedback and bounded motion

The portrait SHALL use the shared Coil loader and bounds-sized requests. Its loading/failure SHALL NOT hide loaded facts or disable Back. Scroll-linked parallax SHALL remain clipped to the portrait with Back steady and a static alternative when motion is disabled.

#### Scenario: Facts exist while the portrait is pending or failed
- **WHEN** loaded detail has a pending, failed or absent portrait
- **THEN** its real facts and Back remain available while feedback stays in the portrait region

#### Scenario: Detail scroll or disabled motion
- **WHEN** the user scrolls detail with motion enabled
- **THEN** portrait movement remains bounded and Back stays fixed
- **AND** disabling motion retains a usable static portrait and reachable facts

### Requirement: Observable detail verification boundaries

Implementation SHALL verify repository behavior, ViewModel actions/state and the whole Details screen using deterministic fixtures. Compose tests SHALL run on API 37 with controlled image outcomes. Test fixtures SHALL stay outside production; no runtime demo Activity SHALL be introduced.

#### Scenario: Details screen verification runs
- **WHEN** instrumented tests exercise detail outcomes and callbacks
- **THEN** they observe actual screen content, Back and Retry without live API/image dependency
