## ADDED Requirements

### Requirement: Remote combined name and status pages

The repository SHALL accept an optional pure domain status alongside page and name. Data SHALL encode Alive, Dead and Unknown as `alive`, `dead` and `unknown`; All SHALL omit status rather than request Unknown. Every page SHALL use the same combined constraints. Filtering SHALL operate on the complete remote result, not only loaded characters.

#### Scenario: A status is selected without a name
- **WHEN** Alive, Dead or Unknown is applied with blank search
- **THEN** requests carry the corresponding status without a name constraint

#### Scenario: A combined result appends
- **WHEN** a name/status result loads its next page
- **THEN** both constraints are retained and the counter uses that query's API total

#### Scenario: All is selected
- **WHEN** All is applied with a name present
- **THEN** requests retain that name and omit status

### Requirement: Immediate and deduplicated status selection

Home SHALL initially select All and expose one selected chip among All, Alive, Dead and Unknown. Changing status SHALL apply immediately with the latest visible normalized name, cancel pending name debounce and start one new query generation. It SHALL reset pages, scroll and total without changing input selection/focus or dismissing the keyboard. Reselecting the active chip SHALL preserve the result and any pending typing debounce. Clear SHALL preserve status; All SHALL remove only status; suggested-name and keyboard submissions SHALL retain status.

#### Scenario: Status changes during typing debounce
- **WHEN** a different chip is selected before the latest name edit is applied
- **THEN** one immediate query contains that visible name and new status without a delayed duplicate query

#### Scenario: The active chip is selected again
- **WHEN** the user reselects the current chip
- **THEN** current pages, counter and scroll remain and a pending name debounce is not cancelled or forced

#### Scenario: The name is cleared under a status filter
- **WHEN** Clear is selected while Dead is applied
- **THEN** the input empties and the next query retains Dead without a name constraint

#### Scenario: A no-match suggestion is selected under a filter
- **WHEN** Rick, Morty, Beth or Summer is selected with a status applied
- **THEN** its immediate search retains that status and may legitimately return no matches

### Requirement: Persistent filter controls and generation isolation

Search and the horizontally accessible status chips SHALL remain mounted across loading, content, empty and error on Home, with resource labels, named tokens, readable selected styling and adequate interactive targets. Filter-only and combined empty results SHALL use the existing no-match state and suggestions without Back or Retry. Initial/append Retry SHALL retain the failed combined query. Only the current generation SHALL control items, errors and totals. Normal Detail → Back SHALL preserve name, selected status, pages, count and scroll without reopening the keyboard.

#### Scenario: A status-only result is empty
- **WHEN** a constrained status query completes without characters
- **THEN** the selected chip and input remain above the no-match message and suggestions on the same Home destination

#### Scenario: A combined query fails
- **WHEN** its first page or append fails and is retried
- **THEN** Retry repeats the failed operation with the same name/status and append failure retains earlier matching cards

#### Scenario: Obsolete combined work finishes
- **WHEN** a replaced name/status generation returns items, totals or errors, including an older append
- **THEN** it cannot alter the active result or counter, including when its former status is selected again

#### Scenario: Chips do not fit the available width
- **WHEN** a narrow layout or increased text size cannot fit all choices
- **THEN** horizontal scrolling keeps every labelled chip reachable and its selected semantics available

#### Scenario: Return from a filtered later-page character
- **WHEN** the user opens detail from a combined result's later page and returns
- **THEN** Home retains its selected status, name, pages, counter and browsing position without reloading page one or reopening the keyboard

## MODIFIED Requirements

### Requirement: Contextual repository failures and cancellation

The repository SHALL translate transport failures, HTTP failures and invalid response bodies into typed request failures. HTTP 404 for page greater than 1 with the recognized API body `There is nothing here` SHALL produce the confirmed catalogue-end outcome. That recognized body on page one with a nonblank name or selected status constraint SHALL instead produce an empty page with total zero and no next page. An unrecognized or malformed 404 SHALL NOT become an empty success or catalogue end. A valid successful response with an empty results array SHALL produce an empty page. Cancelling a request SHALL propagate cancellation rather than produce a normal error result.

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

#### Scenario: A status-only or combined first page has no matches
- **WHEN** page one has a name or status constraint and receives the recognized no-match 404 body
- **THEN** the repository returns an empty page with zero total and no next page

#### Scenario: Append Retry preserves visible feedback

- **GIVEN** the append Retry is visible above the floating counter
- **WHEN** Retry starts the failed page request
- **THEN** progress replaces the button in the same position, retains loaded cards and prevents duplicate pending requests
- **AND** another failure restores Retry in place without requiring another scroll; success appends characters and removes the footer
