## Diagnosis

On API 37 with disposable image cache cleared, sixty rapid scroll gestures reached 280 loaded characters. Four visible portrait failures persisted after five seconds. A temporary application listener captured 232 HTTP 429 failures, with Retry-After between 1 and 10 seconds. The native reproduction asserted that no visible portrait remained in Error and failed. The temporary listener was removed.

## Policy and ownership

The shared Coil interceptor repeats only HTTP 429 image requests once. Respect Retry-After seconds or HTTP-date with a one-second timing margin; use ten seconds when absent or malformed. Limit automatic wait including the margin to sixty seconds, returning the existing fallback rather than retrying early when the server requests longer. Never cache error outcomes or add an item list, job, scroll owner or request loop to presentation. Coil's existing coroutine owns the wait; cancellation propagates.

The interceptor hides the intermediate error from the painter, retaining Loading through the delay. Metadata and navigation stay available. A second 429 or any other HTTP error returns Error. Existing image sizing, cache identities, crossfades and motion settings are preserved.

## Test boundaries

Reuse the approved screen-with-controlled-images boundary for visible metadata/selection and portrait recovery without scrolling. Verify the actual application image-loader factory with controlled downstream Coil results for bounded retries, permanent failures, cancellation and Retry-After. Do not test private parsing helpers or recreate the interceptor's implementation in assertions.

## Validation

RED/GREEN and executed checks are recorded below as they occur. No commit, push or archival is implied.

### RED and focused GREEN

The native rapid-scroll reproduction above established behavioral RED before the policy was added. The initial synthetic fixture replaced Coil's registered components and then placed controlled responses after its terminal EngineInterceptor; its failed assertions were not evidence about the new policy. The corrected fixture retains the interceptor registered by the real application factory and supplies controlled downstream results before Coil's engine.

Managed workflow `213f6e6fe01414edab44c0bf8db0dd92` passed the single recovery test, then all seven `ImageLoaderRecoveryTest` instrumented tests on API 37. They verify seconds/date timing, absent-header fallback, cancellation, permanent errors, excessive/overflowing waits, exactly one retry on persistent failure, and Home metadata/selection while the same visible portrait recovers without scrolling. Tests exercise public ImageLoader request outcomes and Home screen semantics; no private parser tests or production test hooks are added.

### Native boundary regression

The initial exact-delay policy passed controlled tests but the native reproduction still left four portraits in Error after scrolling stopped. A second temporary listener captured terminal HTTP 429 with `Retry-After: 0`, no Age header and Cache-Control forbidding storage. A new request-outcome test requiring a non-immediate zero-header retry failed its elapsed-time assertion (behavioral RED). The policy adds a one-second margin to supplied guidance to avoid that boundary; it still issues at most one retry. Both temporary listeners have been removed.

### Final validation

The boundary test and all eight ImageLoaderRecoveryTest instrumented tests pass on API 37 after the timing adjustment. The preceding complete app run passed seventeen tests (seven initial recovery checks and ten navigation journeys). The final `qualityCheck :app:assembleRelease :app:installDebug` passes, including formatting, Detekt, Android Lint, JVM tests and Konsist. OpenSpec strict validation and `git diff --check` pass.

The final clean application, with only its image disk cache cleared, received sixty rapid native swipes. One run reached sixty loaded characters with a separate append error; all four pending visible portraits completed without further scrolling. A subsequent run reached 260 loaded characters: immediately after scrolling there were six portrait-loading nodes and zero unavailable portraits; the next observation after a two-second wait found zero loading and zero unavailable portraits, with no intervening gesture. Its screenshot confirms the visible real portraits. An intermediate repetition was interrupted by ADB seeing a newly connected unauthorized second device; the completed repetition explicitly targeted emulator-5554 without accessing that device.

These observations verify recovery for the reproduced burst, not immunity to a persistent server limit. A failed second attempt still produces the existing placeholder intentionally. The user accepted the observed behavior and explicitly authorized commit and push on 2026-10-02. Archival remains separate.
