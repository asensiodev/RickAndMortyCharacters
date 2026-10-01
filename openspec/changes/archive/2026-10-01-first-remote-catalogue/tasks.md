## 1. Scope and compatibility

- [x] 1.1 Review the C04 proposal, observable scenarios and repository/ViewModel/Home screen test boundaries before implementation.
- [x] 1.2 Verify and pin compatible Hilt/compiler, network/serialization, lifecycle/coroutines and test dependencies in the existing modules; retain target API 37 and the module graph.

## 2. Successful first-page slice

- [x] 2.1 Define the pure repository/summary/page contract and observe a repository success test fail through MockWebServer; implement internal DTO/client mapping and verify its request/metadata behavior.
- [x] 2.2 Observe first-load ViewModel state behavior fail with a fake repository; implement read-only state/actions, idempotent initialization and content mapping; verify repeated initialization/subscription does not reload.
- [x] 2.3 Configure compatible Compose instrumentation and observe the Home content/selection screen contract fail on API 37; implement the lazy token-based grid with the accepted cards and verify the screen-level behavior.
- [x] 2.4 Wire Hilt, HomeRoute and the app entry point to the real repository and shared image loader; verify lifecycle-aware state rendering without a custom gallery/debug activity.

## 3. Failure, retry and empty slices

- [x] 3.1 Observe repository transport/HTTP/invalid-response behavior fail, then implement typed errors without treating unrecognized first-page 404 as empty; verify cancellation propagates.
- [x] 3.2 Observe ViewModel Error → Retry → Loading → outcome and duplicate-pending-action tests fail, then implement guarded contextual recovery and verify request cancellation on owner teardown.
- [x] 3.3 Observe Home loading/error/Retry screen tests fail, then implement skeletons, resource-based feedback and reachable Retry; verify pending Retry cannot submit repeated work.
- [x] 3.4 Observe valid empty-response/state/screen behavior fail, then implement “No characters available.” in Home with no Retry, Back or destination.
- [x] 3.5 Verify controlled Home portrait pending/success/failure/absence cases preserve metadata and selection; retain those assertions at the screen boundary.

## 4. Validation and review

- [x] 4.1 Run affected JVM tests, retained Home Compose tests on API 37, the shared quality gate and debug/release builds; record actual results and any unresolved limits.
- [x] 4.2 Inspect production Home on API 37, narrow/large-text behavior and real request recovery; check no raw dp/sp in consuming components, no inactive future controls and no test-only runtime destination.
- [x] 4.3 Update implemented-status/setup information, validate OpenSpec and local links, and record actual AI assistance and RED/GREEN evidence without duplicating the backlog.
- [x] 4.4 Human review accepted C04 and authorized archival after the verified test-convention update; no commit or push is authorized.
