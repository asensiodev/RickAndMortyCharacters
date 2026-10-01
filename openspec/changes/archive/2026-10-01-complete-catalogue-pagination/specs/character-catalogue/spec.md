## MODIFIED Requirements

### Requirement: Pure character-page contract

The character repository SHALL expose a suspending page request and return pure Kotlin character summaries, total-count/next-page metadata, a confirmed catalogue-end outcome or a typed request failure. DTOs, Retrofit/OkHttp responses, Android types and Paging types SHALL remain outside that public contract. Character summaries SHALL carry ID, name, species, status and portrait reference. Data SHALL translate documented Alive, Dead and unknown statuses into the domain representation.

#### Scenario: A remote page is received
- **WHEN** the repository receives a valid response containing character summaries and pagination metadata
- **THEN** callers receive the corresponding domain values, including the API total and next-page availability, without transport objects

#### Scenario: Extra API fields are present
- **WHEN** a valid character response also contains fields not needed by the catalogue
- **THEN** the requested summary is decoded without requiring detail fields or fetching related resources

#### Scenario: A confirmed additional-page end is received
- **WHEN** an additional-page request receives the recognized API list-end response
- **THEN** callers receive a distinct terminal outcome without fabricated characters or a fabricated zero total

### Requirement: Contextual repository failures and cancellation

The repository SHALL translate transport failures, HTTP failures and invalid response bodies into typed request failures. Only HTTP 404 for page greater than 1 with the recognized API body `There is nothing here` SHALL produce the confirmed catalogue-end outcome. An unrecognized or malformed 404 SHALL NOT become an empty success or catalogue end. A valid successful response with an empty results array SHALL produce an empty page. Cancelling a request SHALL propagate cancellation rather than produce a normal error result. Filtered first-page no-match mapping belongs to its later change.

#### Scenario: Transport or server failure
- **WHEN** a page request encounters an I/O failure or unsuccessful HTTP response without the recognized append-end contract
- **THEN** the repository returns a request failure and no fabricated characters

#### Scenario: Invalid response
- **WHEN** a successful HTTP response cannot be decoded as the required catalogue response
- **THEN** the repository returns an invalid-response failure rather than an empty catalogue

#### Scenario: Unexpected first-page 404
- **WHEN** an unfiltered first-page request receives HTTP 404
- **THEN** it remains a request failure rather than append completion

#### Scenario: Recognized append end
- **WHEN** page 2 or later receives HTTP 404 with the recognized list-end error body
- **THEN** the repository reports confirmed end

#### Scenario: Unrecognized append 404
- **WHEN** page 2 or later receives HTTP 404 with an unknown, absent or malformed error body
- **THEN** the repository reports a retryable request failure rather than confirmed end

#### Scenario: Request cancellation
- **WHEN** the caller cancels a pending repository request
- **THEN** cancellation propagates and is not converted into a normal repository failure

### Requirement: One Home request and state owner

The Home ViewModel SHALL receive the repository interface directly and own one cached Paging generation for the current catalogue. It SHALL expose explicitly typed read-only metadata state and a paginated flow without a second copied character list or competing request/load-state owner. Home SHALL render Loading, Content, Empty or Error from presented items and Paging load states, accepting explicit interaction callbacks/actions. Recomposition, repeated observation and normal return from Detail SHALL NOT restart the first page. Repeated pending load/retry hints SHALL NOT create duplicate overlapping requests for the same page.

#### Scenario: First load succeeds
- **WHEN** Home starts and the pending first-page request succeeds with characters
- **THEN** Home moves from Loading to Content containing those characters

#### Scenario: Home is observed again
- **WHEN** the same ViewModel is recollected or rendered after its first request
- **THEN** its cached generation is reused without a new first-page request

#### Scenario: Empty catalogue
- **WHEN** the first-page request succeeds with no characters
- **THEN** Home becomes Empty rather than Error or indefinite Loading

#### Scenario: Request fails
- **WHEN** the pending first-page request returns a failure
- **THEN** Home becomes Error with contextual recovery available

#### Scenario: Duplicate pending actions
- **WHEN** load/retry hints repeat while a page operation is pending
- **THEN** only that page operation is active and its eventual result controls the presented outcome

#### Scenario: Home owner is cleared
- **WHEN** the ViewModel owner is cleared during a pending request
- **THEN** the request is cancelled without publishing cancellation as a user error

## ADDED Requirements

### Requirement: Progressive catalogue traversal

Home SHALL request subsequent pages as the user approaches the loaded end, using the API next-page metadata. It SHALL retain existing cards during append work. A null next page or confirmed append-end outcome SHALL stop additional requests while retaining content. Normal Detail → Back SHALL preserve loaded pages and browsing position within the surviving Home owner.

#### Scenario: Another page is available
- **WHEN** the user approaches the end of loaded cards and the API supplied a next page
- **THEN** that page is requested and its returned cards extend the same catalogue in order

#### Scenario: Metadata indicates the final page
- **WHEN** a successful page has no next page
- **THEN** further scrolling issues no additional-page request and the loaded cards remain

#### Scenario: The requested next page no longer exists
- **WHEN** an append receives the confirmed repository end outcome
- **THEN** pagination ends without replacing loaded cards or showing Retry

#### Scenario: Return from a later card
- **WHEN** the user opens a character loaded after the first page and returns from Detail
- **THEN** Home retains the loaded pages, count and browsing position without reloading page 1

### Requirement: Local append progress and recovery

Append loading SHALL show a full-width footer indicator beneath retained cards. Append failure SHALL show `Couldn't load more characters` and reachable footer Retry without replacing the grid. Retry SHALL repeat the failed append operation and preserve earlier cards. Pending Retry SHALL NOT accept duplicate submissions. End-of-catalogue SHALL remove append progress and Retry.

#### Scenario: Append is pending
- **WHEN** the next-page request is pending
- **THEN** loaded cards remain selectable and a footer loading indicator is visible

#### Scenario: Append fails
- **WHEN** the next-page request fails
- **THEN** loaded cards remain and the footer offers contextual Retry

#### Scenario: Append Retry succeeds
- **WHEN** the user retries a failed append and the request succeeds
- **THEN** the same failed page is requested, its cards extend the catalogue and page 1 is not reloaded

#### Scenario: Repeated pending Retry
- **WHEN** the user repeats Retry while the append retry is pending
- **THEN** only one retry request runs and progress replaces or disables the action

### Requirement: Floating loaded and total feedback

Home SHALL display a read-only floating pill with `Loaded {loadedCount} of {totalCount} characters` when real items and the API total for the same generation are available. Loaded count SHALL reflect real loaded items, excluding visible-card counts, placeholders and footers. The pill SHALL remain during append progress/error and at catalogue end; it SHALL be hidden during initial loading/error/empty or while the keyboard is visible. System insets and bottom scroll padding SHALL keep the final cards and footer Retry reachable above the overlay. User-visible text and dimensions SHALL use resources and named tokens.

#### Scenario: First page is loaded
- **WHEN** the API supplies twenty characters and a total while only four cards fit on screen
- **THEN** the pill reports twenty loaded and the supplied API total rather than four visible cards

#### Scenario: Additional page completes
- **WHEN** more real characters join the active result
- **THEN** the loaded count grows with those items without counting the footer

#### Scenario: Append fails or ends
- **WHEN** append fails or the catalogue ends
- **THEN** the pill retains the real loaded count and known total without becoming a button or replacing footer Retry

#### Scenario: Counter is not applicable
- **WHEN** initial loading, initial error, empty results or an open keyboard is presented
- **THEN** the pill is hidden

#### Scenario: Last row and Retry clearance
- **WHEN** the user scrolls to the final row or append Retry, including with larger text
- **THEN** the cards and Retry can scroll clear of the counter and system navigation area
