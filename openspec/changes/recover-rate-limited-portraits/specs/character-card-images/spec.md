## ADDED Requirements

### Requirement: Rate-limited portraits recover within their request lifetime

The shared image loader SHALL retry a portrait once after HTTP 429 when the required wait is within its automatic wait budget. It SHALL respect Retry-After with a one-second timing margin, preserve request cancellation and keep metadata and selection available. A missing or malformed header SHALL use the documented fallback delay. Other failures, exhausted retries and waits exceeding the budget SHALL retain the existing portrait fallback.

#### Scenario: A visible portrait recovers without scrolling

- **GIVEN** a character's metadata is available and its portrait receives HTTP 429
- **WHEN** the indicated wait completes and the repeated request succeeds
- **THEN** the same visible card shows its portrait without scrolling away, with metadata and selection available during loading

#### Scenario: Retry remains bounded

- **GIVEN** a portrait has already retried HTTP 429 once
- **WHEN** the next attempt fails
- **THEN** it shows the existing portrait fallback without another automatic request

#### Scenario: A request is cancelled during the wait

- **GIVEN** a portrait is waiting before its permitted retry
- **WHEN** Coil cancels the request
- **THEN** the wait cancels and no retry is issued

#### Scenario: The server requires a longer wait

- **GIVEN** Retry-After exceeds the automatic wait budget
- **WHEN** the image pipeline handles the response
- **THEN** it returns the existing error without retrying earlier than requested
