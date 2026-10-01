## ADDED Requirements

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

## MODIFIED Requirements

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
