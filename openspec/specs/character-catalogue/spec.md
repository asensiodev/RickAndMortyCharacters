# character-catalogue Specification

## Purpose
Define remote character browsing, single-owner pagination, contextual failures and retries, and loaded/total presentation on Home.
## Requirements
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

The repository SHALL translate transport failures, HTTP failures and invalid response bodies into typed request failures. HTTP 404 for page greater than 1 with the recognized API body `There is nothing here` SHALL produce the confirmed catalogue-end outcome. That recognized body on page one with a nonblank name constraint SHALL instead produce an empty page with total zero and no next page. An unrecognized or malformed 404 SHALL NOT become an empty success or catalogue end. A valid successful response with an empty results array SHALL produce an empty page. Cancelling a request SHALL propagate cancellation rather than produce a normal error result.

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

### Requirement: Remote name-scoped pages

The repository SHALL accept an optional name constraint and apply it to every requested page of that result. Blank names SHALL omit the constraint; names SHALL be trimmed for requests without changing the visible input. Name filtering SHALL use the remote catalogue rather than only loaded items. Recognized HTTP 404 `There is nothing here` on a nonblank first-page name request SHALL produce an empty page with zero total and no next page. Other contextual errors and cancellation SHALL preserve the accepted repository contract.

#### Scenario: Search continues beyond the first page
- **WHEN** a name-scoped result loads its next page
- **THEN** both requests carry the same name and each result belongs to that remote query

#### Scenario: A filtered first page has no matches
- **WHEN** a nonblank first-page name request receives the recognized no-match response
- **THEN** the repository returns an empty page with total zero rather than a request failure

#### Scenario: A first-page error is not a confirmed no match
- **WHEN** an unfiltered first-page request receives 404 or a filtered request receives an unrecognized/malformed 404
- **THEN** the outcome remains a typed failure rather than invented empty results

### Requirement: Debounced and immediate name application

Home SHALL keep visible input and the normalized applied name distinct. Typing SHALL apply the latest input after a 300 ms debounce. Keyboard Search SHALL apply immediately and prevent an equivalent delayed request. Clear SHALL immediately remove the name constraint. Equivalent normalized submissions SHALL preserve the existing generation and scroll. Input editing SHALL NOT navigate or automatically open the keyboard.

#### Scenario: Several edits arrive before the debounce expires
- **WHEN** the user types successive name edits within the debounce interval
- **THEN** only the latest normalized name starts a new query after the interval

#### Scenario: Keyboard Search is submitted before debounce
- **WHEN** the user submits the visible name from the keyboard before its debounce expires
- **THEN** the query starts immediately, the keyboard dismisses and no delayed equivalent query restarts it

#### Scenario: An equivalent name is submitted again
- **WHEN** Search is submitted with the same normalized applied name
- **THEN** Home retains its generation and scroll without a duplicate reload

#### Scenario: Search is cleared
- **WHEN** the user selects Clear with a name constraint applied
- **THEN** input becomes empty and the unfiltered query starts immediately while input focus is retained

### Requirement: Isolated search generations

Each changed applied name SHALL start from page one, reset scroll and hide previous-query items and totals while its first request is pending. Only the active generation SHALL publish cards, load outcomes and total metadata. Cancelled or late older work SHALL NOT replace the active result, including when a previous name is selected again. Paging SHALL remain the single cached item/load/retry owner. Normal return from Detail SHALL preserve the surviving Home query, loaded pages and scroll without reopening the keyboard.

#### Scenario: A new name replaces a loaded catalogue
- **WHEN** a different normalized name is applied
- **THEN** old cards and the counter disappear, loading is shown and the new request starts at page one with scroll reset

#### Scenario: An old request finishes late
- **WHEN** an obsolete generation returns items or total metadata after a newer name is applied
- **THEN** it cannot alter the active cards, state or counter

#### Scenario: A name is selected again after another query
- **WHEN** names change from A to B to A and the first A generation completes late
- **THEN** only the current A generation controls the presented result and total

#### Scenario: A name-scoped append fails
- **WHEN** an additional page fails for the active name and the user retries
- **THEN** earlier matching cards remain and Retry repeats the failed page with that same name

#### Scenario: Back returns to searched content
- **WHEN** the user opens detail from a later search page and returns
- **THEN** the applied name, pages, count and browsing position remain without reloading page one or reopening the keyboard

### Requirement: Persistent search and contextual no matches

Search SHALL remain mounted above Home results during loading, content, empty and error, preserving input text, selection and focus unless the user submits or navigates to Detail. Filtered empty results SHALL show the agreed no-match message and fixed Rick, Morty, Beth and Summer shortcuts on Home, without Back or Retry. Suggestions SHALL appear only in no matches. A suggestion SHALL replace the input and submit immediately through the same deduplicated search path, dismissing the keyboard. Initial/query errors SHALL retain input and offer reachable contextual Retry; empty unfiltered results SHALL retain their distinct message. Search controls SHALL use resources, named tokens and adequate interactive targets.

#### Scenario: Results change while the user edits
- **WHEN** a pending query becomes empty or fails while the input is focused
- **THEN** input text, selection and focus remain and feedback/actions stay reachable with the keyboard open

#### Scenario: A name has no matches
- **WHEN** the active name request completes with an empty result
- **THEN** Home shows “No characters found” and the four search shortcuts below the unchanged input without creating a destination

#### Scenario: A suggested name is selected
- **WHEN** the user selects Rick, Morty, Beth or Summer in no matches
- **THEN** input reflects that name and its query starts immediately without a duplicate debounce request, with the keyboard dismissed

#### Scenario: A search request fails
- **WHEN** the active name request returns a failure
- **THEN** Home retains the input and shows contextual Retry rather than no-match suggestions
