## 1. Review and compatible wiring

- [x] 1.1 Review C05 scope, selected detail references and the repository/ViewModel/Details/app-navigation test boundaries before implementation.
- [x] 1.2 Verify and pin compatible Navigation 3/Lifecycle and Hilt test dependencies; retain the existing module graph, API 37 and minimum API 26.

## 2. Successful detail slice

- [x] 2.1 Define separate pure detail/result contracts; observe detail repository success fail through MockWebServer, implement the existing-client detail request/mapping and verify ID/facts/count without related-resource requests.
- [x] 2.2 Observe Details load/state behavior fail with a fake repository; implement its idempotent, entry-identity request owner and immutable outcomes; verify repeated initialization does not reload.
- [x] 2.3 Observe the whole Details Content/Back contract fail on API 37; implement portrait/identity/grouped facts with resources/tokens and normal-size previews; verify real values and callback behavior.
- [x] 2.4 Wire DetailsRoute/Hilt, app-owned typed Navigation 3 entries and their state/ViewModel decorators; observe and complete the selected-ID → detail → Back journey with test-only controlled dependencies.
- [x] 2.5 Verify normal/system Back retain current Home content/scroll without another first-page request, guard rapid duplicate selection and clear a removed detail owner.

## 3. Recovery and independent portrait slices

- [x] 3.1 Observe repository network/HTTP/invalid-response/cancellation outcomes fail where newly introduced; implement typed failures and recognized detail NotFound without changing Home's unfiltered 404 contract.
- [x] 3.2 Observe Error → Retry → Loading → outcome and pending-action behavior fail; implement guarded same-ID recovery and verify owner cancellation through the ViewModel contract.
- [x] 3.3 Observe loading/error/not-found screen feedback and persistent Back fail; implement skeletons, contextual Retry and unavailable-character feedback in the same detail destination.
- [x] 3.4 Verify absent/empty Type, unknown and long values, one episode-appearance count, and controlled pending/success/failure/absent portraits at the Details screen boundary.
- [x] 3.5 Implement/review bounded scroll parallax, its static motion alternative and native forward/back transitions; inspect the top, middle and final facts with system safe areas.

## 4. Validation and acceptance

- [x] 4.1 Run affected repository/ViewModel tests and retained Home plus new Details/app instrumentation on API 37; run the shared quality gate and debug/release builds through the managed Gradle wrapper.
- [x] 4.2 Compare production detail states with the selected design; verify real API identity/facts, retry, Back, scroll, portrait bounds and disabled-motion behavior.
- [x] 4.3 Record actual AI decisions and RED/GREEN evidence, update implemented status and validate OpenSpec/local links without claiming unexecuted checks.
- [x] 4.4 Complete human review before archival/C05A; commit or push only on separate explicit authorization.
