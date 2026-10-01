## ADDED Requirements

### Requirement: Shared bounded API response cache

Character catalogue and detail GETs SHALL use one data-owned singleton API client with one 10 MiB disk cache under the application's cache directory, in a dedicated `character_http` directory distinct from the image cache. Storage and validation SHALL be managed by OkHttp. Domain and feature contracts SHALL NOT expose HTTP cache types. Cache deletion or eviction SHALL remain an ordinary cache miss.

#### Scenario: Both endpoints use the configured cache
- **WHEN** catalogue and detail repository calls repeat eligible fresh requests
- **THEN** both endpoints reuse their own stored responses through the shared configured API client

#### Scenario: Cached storage is absent
- **WHEN** a request has no usable stored entry, including after cache deletion or eviction
- **THEN** the repository obtains its result through the normal network path

### Requirement: Fresh response reuse and disk persistence

An identical GET with an eligible fresh stored response SHALL return the mapped repository result without a second HTTP request. Freshness SHALL follow response headers and age rather than an application-defined lifetime. Fully consumed eligible responses SHALL remain reusable after the client/cache is closed and recreated against the same directory while the entry remains fresh and present.

#### Scenario: A fresh catalogue page is requested again
- **WHEN** an eligible fresh page response has been consumed and the same page/name/status request is repeated
- **THEN** the repository returns the stored characters and metadata with no additional server request

#### Scenario: A fresh detail is requested after reopening storage
- **WHEN** an eligible fresh detail response has been consumed and the client/cache is closed and recreated against the same directory before requesting that ID again
- **THEN** the repository returns the stored detail without an additional server request

### Requirement: Expired response validation and replacement

A stale entry with an ETag SHALL be conditionally validated with `If-None-Match`. A 304 SHALL reuse the stored body and updated HTTP metadata. A changed 200 response SHALL supply the new mapped result and replace the eligible stored response. An expired entry without a usable validator SHALL use the ordinary network request. These outcomes SHALL retain existing repository mapping and cancellation semantics.

#### Scenario: The server confirms an expired response is unchanged
- **WHEN** a repeated stale request with a stored ETag receives 304 and renewed freshness
- **THEN** the server observes the matching validator, the repository returns the stored body and a further identical fresh request needs no network request

#### Scenario: The server supplies changed content
- **WHEN** validation receives a cacheable 200 with changed content and a new ETag
- **THEN** the repository returns the new content and a subsequent fresh identical request reuses that new response

#### Scenario: A stale entry has no validator
- **WHEN** an expired stored response has no usable validator and is requested again
- **THEN** a normal network request obtains the next result without inventing an application validator

### Requirement: Request and response variant isolation

Different page, normalized name, status and detail-ID URLs SHALL retain distinct responses. Applicable `Vary` headers SHALL participate in reuse decisions. Cache identity SHALL remain owned by the HTTP library rather than a custom query-key store.

#### Scenario: Each catalogue constraint changes independently
- **WHEN** requests differ only in page, name or status, including All versus Unknown
- **THEN** each returns its own characters and metadata and repeating a fresh variant reuses only that variant

#### Scenario: Detail IDs differ
- **WHEN** two distinct detail IDs are loaded and each is requested again while fresh
- **THEN** each returns its own detail without mixing IDs or using catalogue bodies

#### Scenario: A varied request header changes
- **WHEN** an eligible response declares Vary for a request header and that header changes on a repeated request
- **THEN** the previous header variant does not satisfy the changed request

### Requirement: Server policy and ordinary failure behavior

The client SHALL respect server freshness, validation and storage directives, including `no-store` and `no-cache`, without rewriting them or hardcoding the previously observed 90-day lifetime. It SHALL NOT add forced cache-only requests or custom stale-on-error behavior. Missing or expired entries that require network access SHALL preserve existing typed failures when that access fails; cancellation SHALL propagate. Fresh hits may work without network access, but the application SHALL NOT promise an offline catalogue.

#### Scenario: Storage is prohibited
- **WHEN** a response specifies no-store and the same request is repeated
- **THEN** a second network request is made rather than reusing the prohibited response

#### Scenario: Reuse requires validation
- **WHEN** a stored response specifies no-cache and has an ETag
- **THEN** its repeated request validates with the server before reusing its body

#### Scenario: An expired entry cannot be validated because of a transport failure
- **WHEN** an expired entry requiring network access is requested and transport fails
- **THEN** the repository returns its existing network failure rather than serving the expired body through a custom fallback

#### Scenario: Validation returns a service failure
- **WHEN** an expired entry requiring validation receives a service error
- **THEN** the repository returns its existing service failure rather than substituting the expired successful body

### Requirement: Observable repository verification

Automated cache verification SHALL call the real repository with the same internal client construction used by production, a temporary cache directory and controlled HTTP responses. Assertions SHALL cover domain results and recorded server requests, including conditional headers. Tests SHALL fully consume or close responses and release clients/cache resources before deleting storage. Header fixtures SHALL establish freshness/expiry deterministically without waiting for long real-time expiry. Validation evidence SHALL distinguish MockWebServer behavior, live header observations and native smoke checks.

#### Scenario: The cache contract is verified
- **WHEN** the C09 integration suite runs
- **THEN** fresh reuse, reopened disk reuse, 304 validation, changed bodies, query/ID/Vary isolation, no-store, no-cache and expired-request failures are checked through repository outcomes and server traffic
