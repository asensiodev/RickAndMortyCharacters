# character-catalogue Specification

## Purpose
TBD - created by archiving change first-remote-catalogue. Update Purpose after archive.
## Requirements
### Requirement: Pure character-page contract

The character repository SHALL expose a suspending page request and return pure Kotlin character summaries, total-count/next-page metadata or a typed request failure. DTOs, Retrofit/OkHttp responses, Android types and Paging types SHALL remain outside that public contract. Character summaries SHALL carry ID, name, species, status and portrait reference. Data SHALL translate documented Alive, Dead and unknown statuses into the domain representation.

#### Scenario: A remote page is received
- **WHEN** the repository receives a valid response containing character summaries and pagination metadata
- **THEN** callers receive the corresponding domain values, including the API total and next-page availability, without transport objects

#### Scenario: Extra API fields are present
- **WHEN** a valid character response also contains fields not needed by this increment
- **THEN** the requested summary is decoded without requiring detail fields or fetching related resources

### Requirement: Contextual repository failures and cancellation

The repository SHALL translate transport failures, HTTP failures and invalid response bodies into typed request failures. An unrecognized 404 SHALL NOT become an empty success. A valid successful response with an empty results array SHALL produce an empty page. Cancelling a request SHALL propagate cancellation rather than produce a normal error result. C04 requests the unfiltered first page; filtered no-match and append-end mappings belong to their corresponding changes.

#### Scenario: Transport or server failure
- **WHEN** a first-page request encounters an I/O failure or unsuccessful HTTP response
- **THEN** the repository returns a request failure and no fabricated characters

#### Scenario: Invalid response
- **WHEN** a successful HTTP response cannot be decoded as the required catalogue response
- **THEN** the repository returns an invalid-response failure rather than an empty catalogue

#### Scenario: Unexpected first-page 404
- **WHEN** an unfiltered first-page request receives a 404 without an applicable verified empty-result contract
- **THEN** it remains a request failure

#### Scenario: Request cancellation
- **WHEN** the caller cancels a pending repository request
- **THEN** cancellation propagates and is not converted into a normal repository failure

### Requirement: One Home request and state owner

The Home ViewModel SHALL receive the repository interface directly, expose read-only observable state and process explicit load/retry actions. Initial loading SHALL happen once per ViewModel instance rather than on every recomposition or new state subscriber. Home SHALL render immutable Loading, Content, Empty or Error outcomes. Only one first-page operation SHALL be pending at a time; repeated actions while it is pending SHALL NOT launch duplicate requests.

#### Scenario: First load succeeds
- **WHEN** Home starts and the pending first-page request succeeds with characters
- **THEN** the observable state moves from Loading to Content containing those characters

#### Scenario: Home is observed again
- **WHEN** the same ViewModel receives another initial-load action or another state subscriber after its first request
- **THEN** it does not issue another initial request

#### Scenario: Empty catalogue
- **WHEN** the first-page request succeeds with no characters
- **THEN** Home becomes Empty rather than Error or indefinite Loading

#### Scenario: Request fails
- **WHEN** the pending request returns a failure
- **THEN** Home becomes Error with contextual recovery available

#### Scenario: Duplicate pending actions
- **WHEN** load/retry is requested repeatedly while the first-page operation is pending
- **THEN** only that operation is active and its eventual result controls the state

### Requirement: Native Home results states

Home SHALL use the accepted dark theme and token-based layout without a title, top app bar or bottom navigation. Pending character data SHALL render noninteractive card skeletons. Content SHALL render the received characters in a lazily composed grid with stable character identity. Ordinary phone widths SHALL show two readable columns; narrow layouts or large text SHALL adapt rather than clip essential content. Empty and error feedback SHALL occupy the results area of this same screen and remain reachable within system safe areas. User-visible strings SHALL be resources.

#### Scenario: Data is pending
- **WHEN** Home is Loading
- **THEN** it shows card skeletons without invented metadata or character-selection actions

#### Scenario: Content is available
- **WHEN** Home has a successful first page
- **THEN** it shows the supplied real character cards using the accepted portrait/metadata structure

#### Scenario: No characters are available
- **WHEN** Home is Empty for the unfiltered catalogue
- **THEN** it displays “No characters available.” without Back, Retry or a new destination

#### Scenario: Initial request fails
- **WHEN** Home is Error
- **THEN** it displays “Couldn't load characters” and a reachable Retry button without navigating elsewhere

### Requirement: Contextual guarded Retry

Retry SHALL repeat the failed first-page operation on Home without navigating or fabricating successful content. Progress SHALL appear in the results area while the retry is pending, and the previous Retry action SHALL no longer submit additional work. A failed retry SHALL restore the error and Retry; a successful retry SHALL show its actual content or empty result.

#### Scenario: Retry succeeds
- **WHEN** the user selects Retry after an initial failure and the next request succeeds
- **THEN** Home shows loading followed by the returned content on the same screen

#### Scenario: Retry fails again
- **WHEN** a retry returns another failure
- **THEN** Home shows the contextual error and allows another attempt after completion

### Requirement: Screen selection and independent portrait feedback

Home SHALL expose character selection through an ID callback connected by app navigation to the selected Detail destination. Image requests SHALL use the app-owned shared loader; pending, failed or absent portraits SHALL NOT suppress real metadata or prevent selection. Home SHALL NOT own the detail implementation or its navigation types.

#### Scenario: Select a character with a failed portrait
- **WHEN** a real Home card has a failed or absent portrait and the user selects it
- **THEN** its metadata remains available and app navigation receives its ID to open Detail

#### Scenario: Portrait loading completes
- **WHEN** a controlled image request moves from pending to success within Home
- **THEN** its local loading feedback disappears without replacing character data or reloading the catalogue

### Requirement: Production wiring and verification boundaries

The production application SHALL display Home and obtain its repository-backed ViewModel through Hilt. Network/DTO ownership SHALL remain in data and UI rendering SHALL remain in Home. Verification SHALL cover the repository HTTP boundary, ViewModel actions/state and the real Home composable. Instrumented screen tests SHALL run on API 37 using controlled data and images. Test-only activities, fake repositories and image fixtures SHALL NOT be added to production sources.

#### Scenario: Application starts
- **WHEN** the application launches with the configured production dependencies
- **THEN** it opens Home and attempts the first remote catalogue page without a custom review/debug destination

#### Scenario: Deterministic screen verification
- **WHEN** instrumented tests exercise Home loading, content, empty, error, Retry and selection on API 37
- **THEN** those outcomes are observed through screen semantics/callbacks without a live API dependency or component-gallery activity
