## Current delivery status — 2026-10-02

The user validated the normal browsing/recovery flow and bounded Home/Detail accessibility on a physical Pixel 9a in debug. The current CI runs only qualityCheck; [run 37039977135](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/37039977135) passed for `9fdb7fb`. Instrumented suites remain local after two failed remote emulator trials. Final full-device instrumentation, release-runtime acceptance and formal OpenSpec closure are not inferred from manual debug validation.

The dated records below describe the candidate and policy at each point in time. Later decisions supersede earlier pending states, release preparation and CI configuration. Current scope lives in README; current formal work lives in tasks.md. This documentation reconciliation creates no new product spec, test result or archival claim.

## Context

C12 closes the current product rather than extending it. Home includes paging, name/status constraints, recovery and image/HTTP caching; Detail includes the square portrait, facts and horizontal episode cards. O03 protects nine real-rendering scenes, with local photo fixtures and fixed animation frames. Recent local `qualityCheck :app:assembleRelease` passes are recorded in O03; they do not prove a remote CI run or manual physical-device acceptance.

## Decisions

### Split tasks from the manual procedure

OpenSpec tasks own milestones and evidence. [docs/MANUAL_QA.md](../../../docs/MANUAL_QA.md) owns the steps the human executes, expected results, checkboxes and observation log. The user marks a box only after observing its expected outcome. Failed, blocked and not-reproduced cases remain distinguishable; optional/conditional failures cannot be silently counted as passes. No second per-screen checklist is maintained in tasks.

The user owns device testing, documentation edits and final acceptance. Codex owns automated verification/evidence and investigation of reported failures after scope approval. Neither preparation nor an agent may mark the user's manual checks as passed. Reuse the user's final screenshots/docs; coordinate before editing the same files.

After acceptance, retain the completed manual checklist as dated evidence tied to its tested revision and device. Its checked cases and observations are historical results, not a second active backlog. Do not delete that evidence or silently overwrite it to describe a later candidate; record any later execution separately.

### Freeze the test candidate

Record source commit plus any uncommitted changes, APK/build variant, device/model/API, font/display settings and test date. Evidence must refer to that candidate; changes affecting tested behavior invalidate the relevant checks and require a targeted rerun. Documentation-only changes do not require blindly repeating unchanged automated suites. A previously green CI run for another revision is historical evidence.

Only regressions blocking the accepted browsing/recovery/navigation flow justify production corrections in C12. Scope and test boundary are stated before each correction; observe behavioral RED when applicable, implement GREEN and repeat affected checks. No fabricated RED is needed for documentation or cosmetic baseline updates. Keep new feature ideas outside this closure.

### Local automation and device evidence

Use the documented managed Gradle wrapper to run/reuse justified evidence for qualityCheck and assembly of the selected variant. The user selected debug for delivery review; earlier release results retain their tested revision. The gate includes Paparazzi verification, not recording. Recheck the final nine goldens visually; never change them just to make CI pass. Instrumented suites remain separate from the aggregate gate and run locally on a connected emulator or device:

```sh
./gradlew :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

Record actual connected targets, test counts/results and command environment; empty/cached tasks are not newly executed behavior coverage. Revisit the earlier Pixel instrumentation failure on a connected physical target when available. If it recurs, capture the failing test/runner evidence and distinguish harness/device failure from an application crash; manual success alone does not diagnose its cause. Any unavailable/blocked check remains explicit for human disposition.

The user executes the normal-flow manual checklist on at least one physical Android phone; an API 37 emulator remains useful for automated regressions and supplementary review. Do not infer a broad device/API matrix from that phone. UI review includes current font/display settings, safe areas, keyboard, portrait loading/crop, footer/counter clearance, episode scrolling, fixed Back and observed native motion. The selected Home/Detail accessibility review is completed as recorded in the manual results; it includes the reported TalkBack and enlarged-text cases and bounded Home contrast/touch-target review. A full accessibility audit remains outside this delivery; existing readable labels and reachable controls must be preserved on both screens.

### Release artifact and CI

Assembling a release APK is distinct from installing/running it. Current app configuration has no release signing setup and no explicitly enabled minification. Retain the earlier installable release artifact and its tested revision as historical evidence; current runtime review uses debug. Keep signing material outside Git. Do not introduce publishing/signing services or enable R8 merely for this closure. If only debug is exercised, mark release runtime as unverified rather than equating it with successful assembly; final acceptance must explicitly address that limitation.

Run or inspect the GitHub Actions Quality workflow for the candidate's published commit after publication is authorized. Record run URL, SHA and each job's outcome. CI uses Ubuntu/Temurin while current goldens were generated on macOS/Zulu; any renderer mismatch requires review of the diff and environment before an intentional baseline update. The workflow runs the aggregate gate on pushes, pull requests and manual dispatch. Instrumented suites run locally; release assembly/runtime and manual accessibility remain separate evidence. No commit, push or remote dispatch is authorized by preparing this change alone.

### Documentation and acceptance

The user is updating documentation separately. C12 performs a consistency pass over their final setup instructions, feature claims, native screenshots and limitations, and adds links to verification evidence when appropriate. Delivery screenshots are actual app captures; local synthetic Paparazzi fixtures and Stitch references are not substituted for them.

Summarize evidence and every remaining limitation in this design record. Human review closes the remaining C11/O03 acceptance items from actual results, then accepts C12; archival is a separate explicit step. Do not describe the candidate as completely verified while required checks are blocked or unexecuted.

## Preparation record — 2026-10-02

Read current backlog, PRD, UI/UX, development process, OpenSpec rules, app build configuration and CI workflow. Added only this draft and the manual checklist, with a C12 backlog link. Tests and CI were not executed during preparation. Existing local results remain historical evidence at their recorded revisions; all new execution and human acceptance tasks remain unchecked.

Preparation validation: `openspec validate validate-showcase-delivery --strict` passed; local Markdown links and `git diff --check` passed. No Gradle/device/CI checks were launched during preparation.


## Candidate verification — 2026-10-02

The user authorized C12 execution. Candidate: `b95a5bcd1475ef1c3e27cb87a608458e7d62c855` on `main`; the working tree differs only in C12 documentation/backlog/checklist. No production source changed during this execution. App version `0.1.0` / code `1`. The available target is `emulator-5554`, arm64 Android API 37, font scale 1.0. No physical phone was connected, so the earlier physical Pixel runner failure cannot yet be rechecked.

### Automation and visual references

Managed workflow `78a355aa03eb8410283d71d919cb421d` ran:

- “¿Pasan qualityCheck, Paparazzi y assembleRelease sobre el candidato final b95a5bc de C12?” — `./gradlew qualityCheck :app:assembleRelease`: PASS, 4.147 s, 463 tasks (10 executed, 453 UP-TO-DATE). Current XML records data 53, Home 31, Detail 22 and Konsist 3 tests, with no failures/errors/skips. Unit/Paparazzi results were reused; this is not a fresh execution of every test. The initial sandbox attempt failed on a Gradle cache lock; the escalated wrapper succeeded. This environment failure is not behavioral RED.
- “¿Pasan las suites instrumentadas Home, Detail y navegación App de C12 en emulator-5554 API 37?” — `./gradlew :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest`: PASS, 92.851 s, 232 tasks (78 executed, 25 cache, 129 UP-TO-DATE). Fresh connected XML: Home 27, Detail 9, App 18 — 54 tests, zero failures/errors/skips.

Workflow finish removed only wrapper-owned logs. All nine source goldens were visually reviewed: card/skeleton/fallback bounds, selected chips and no-match suggestions, append Retry/counter clearance, square Detail portrait/facts, and episode loading/error/wrapping. No golden was recorded or changed. Synthetic screenshot data does not replace native or physical-device evidence.

[Quality CI run 37019154609](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/37019154609) completed successfully for the exact candidate SHA. CI includes screenshot verification; no cross-host baseline change was needed. CI does not provide device or release runtime evidence.

### Installable release and emulator smoke

`app/build/outputs/apk/release/app-release-unsigned.apk` was signed into `app/build/outputs/apk/release/app-release-local.apk` with the existing local Android debug certificate, solely for installable release QA. No signing configuration or key was added to Git, and minification was not enabled. `apksigner verify --print-certs` passed. APK SHA-256: `284d0bcdf0fe7cd6348bbf66e2376d5fe23f84fe7a1694b72dd7a0e28d63e6ed`; size 10,205,293 bytes. Certificate SHA-256: `ec27e84c7b10ec6ef6d810b25b441afe7dccdc09595c3d31b7cf2bce99c2214c`.

The signed release installed successfully on the API 37 emulator. Cold launch reported `Status: ok`. ADB input and native UI hierarchy observations verified Home content, combined Rick/Alive search via a suggestion, pagination from 20 to 29 of 29 matches, Rick's identity/facts and 51 appearances, vertical Detail scrolling and an episode-list horizontal gesture. Detail Back returned to the retained query/filter. Opening Modern Rick from the scrolled list and returning with system Back restored the same visible row bounds and loaded counter. No blocking failure was observed in this bounded smoke. It does not demonstrate physical-phone motion, conditional recovery, process-death restoration or performance.

### Documentation and remaining acceptance

README requirements match the single-JDK 21 configuration and actual SDK/build-tools versions. Feature/module claims, release/CI distinctions and delivery exclusions were checked against configuration, passing tests and the release smoke. Home/Detail delivery screenshots were reviewed and remain consistent with the implemented layout and live character facts. Local Markdown links pass. README correctly retains pending final acceptance; no broad accessibility/offline/performance claim is introduced.

The user-owned [manual checklist](../../../docs/MANUAL_QA.md) remains unexecuted; no human checkbox was marked by Codex. Physical-device normal-flow review, the previous Pixel instrumentation failure and conditional release recovery remain pending. C11 native visual/motion/insets review and O03 baseline human acceptance remain open for the user's review. C12 is not accepted or archived, and this execution does not authorize committing or pushing C12.


### Physical phone ready for human review — 2026-10-02

The user connected Google Pixel 9a (`55211JEBF14578`), API 37, resolution 1080 × 2424, font scale 1.0. The same locally signed release APK installed successfully and cold-launched with `Status: ok`. Technical execution metadata was filled in the manual checklist; all user-owned observations/checks remain unmarked. Physical instrumentation is deferred until the user finishes manual review to avoid taking over their screen. Connection/installation alone does not resolve the previous instrumentation failure or establish manual acceptance.


### Bounded focus correction authorized — 2026-10-02

The user reported a blinking search cursor after leaving editing during physical-device QA and requested a fix/reinstallation. Existing HomeContent explicitly hides the keyboard on a manual drag while retaining focus. The user now overrides that earlier focus policy: clear focus on manual browsing and when a previously visible software keyboard closes (including Back), retaining text/status and permitting editing to resume. Automatic result resets and initially hidden/hardware-keyboard editing must retain focus. Use the already approved HomeContent screen test seam in HomeSearchTest: focused semantics, native user gesture/keyboard dismissal, retained input/status and reopening. Observe behavioral RED before each minimal correction; no new component API, ViewModel state or dependency is needed. Prior automation/CI remains historical for the pre-fix revision and must be qualified until the changed candidate is reverified.


### Focus correction verified and installed — 2026-10-02

Home now clears focus on manual result dragging and on the visible-to-hidden IME transition, retaining search text/status and supporting reopening. Focus effects stay local to HomeContent through a private composable helper; automatic generation resets retain focus. Both focus regressions produced behavioral RED (`Focused=true` after drag/Back). Managed workflow `403a37dbc130aef3256e5eb8067957e4` then passed the focused drag test, 46 emulator Home/navigation tests (28 + 18), Detekt, `qualityCheck :app:assembleRelease`, and the final 11-test HomeSearchTest after extracting effects. The gate initially found HomeContent LongMethod; extraction resolved it. A removed TextRange import briefly caused compilation failure and was restored. Selection collapses normally when focus clears, so the old selection-preservation assertion was removed. The first two instrumented runs inadvertently selected both connected targets: physical-device counter-visibility assertions failed in that interim class run; these are not a passed complete physical suite. Subsequent runs explicitly selected the emulator through ANDROID_SERIAL. Finish removed only wrapper-owned logs; scratch launcher/session cleanup was separate.

The corrected APK installed and cold-launched successfully on the physical Pixel 9a; SHA-256 `08c3ed0f46c028d8ef3c5cae25a5735d6ce5d589ba92b4b557779640cee53222`. Candidate is `b95a5bc` plus the HomeContent/HomeSearchTest focus correction; earlier CI/runtime evidence is historical for the pre-fix candidate. Human retest remains pending; no manual checkbox was marked and no commit/push occurred. The user then requested prioritizing immediate app iteration without release preparation; further QA iterations should install debug unless explicitly requested otherwise.


### Fast-scroll append diagnosis — 2026-10-02

The user reported append Retry failing until preceding portraits finish. The user also narrowed the manual checklist to debug and removed its release smoke and final boilerplate; existing checked results are preserved as historical observations, not relabeled as debug execution. On a fresh-cache debug API 37 emulator, 16 fast grid swipes reproduced portrait HTTP 429 plus page 3 HTTP 429 with `Retry-After: 9`; page 2 had returned HTTP 200. Image and API clients are separate, but the upstream rejection affects both. Temporary tagged HTTP probes collected only status/page/wait guidance and were removed after diagnosis.

Bounded correction: at the existing CharactersRepository HTTP fixture seam, verify that a numeric, bounded Retry-After on HTTP 429 suspends the current page request and retries that identical request once. Loading therefore stays observable during the wait; retained cards and current name/status do not change. Persistent rejection remains contextual failure; no unbounded retry loop, busy waiting, new domain result or dependency. Unknown/excessive wait guidance remains failure for explicit Retry. Normal IO/cancellation behavior remains unchanged. User-requested iteration uses debug only.


### Fast-scroll correction verified and installed — 2026-10-02

Managed workflow `4dce80fe4d559f4a1e0ca8ff77ec3715` observed behavioral RED for server-guided retry before the correction and GREEN afterward. Affected Data (56) and Home (31) unit tests passed without failures; coverage includes the identical page/query/status retry, persistent rejection without looping, and invalid or excessive retry guidance. `qualityCheck :app:assembleDebug` passed on the shared working tree after fixing Detekt MaxLineLength. Finish removed only wrapper-owned logs. Temporary HTTP probes are absent and `git diff --check` passed. Concurrent Detail changes were preserved and are not part of this correction.

The debug APK installed successfully and launched on the physical Pixel 9a (`55211JEBF14578`). SHA-256: `8fbca399e9c964f29672b0444424e1950d31956bf16d94306caffeea28e029fd`. No new release was prepared. Human fast-scroll/retry retest remains pending; existing manual checks were preserved and no acceptance, commit or push is inferred.


### Publication authorization — 2026-10-02

The user requested commit and push of the current working tree, including the bounded focus/pagination corrections, Detail badge consistency, native README screenshots and ongoing C12 evidence. Publication does not mark the pending physical retests, final CI verification, delivery acceptance or archival complete. Prior results remain tied to the revisions and artifacts recorded above.


### Human manual validation — 2026-10-02

During publication the user explicitly stated that the manual tests were already validated and everything is fine. This closes the physical manual review and affected-fix retest, including the reported pagination regression. QC04/QC06 remain not reproduced as recorded by the user. This confirmation does not manufacture missing automated device/CI results or archive the change.

### Bounded interview-readiness follow-up — 2026-10-02

The user excluded process-death restoration from this MVP and selected CI instrumentation plus a Home-only accessibility demonstration. No production feature or dependency was added. Home query state remains owned by its ViewModel; native navigation and local Compose saving remain because they support the existing Back/configuration behavior. Whole-session recovery after process death is explicitly outside scope. The review found no stub or unimplemented control requiring deletion in the implemented two-screen flow; pending manual verification is recorded separately from missing implementation.

Quality now has a second job, restricted to workflow_dispatch and dependent on the aggregate gate, running Home, Detail and app instrumented suites on Ubuntu/KVM with the official `system-images;android-37.0;google_apis;x86_64` image and pinned emulator-runner action. Reports are uploaded even after failure. Existing Home large-text/reachability tests run with the full suite. No TalkBack automation or complete accessibility certification is implied; new manual Home cases remain unchecked and earlier human results are preserved.

Validation: actionlint 1.7.12 passed for the workflow, Ruby YAML parsing passed, `openspec validate validate-showcase-delivery --strict` passed and `git diff --check` passed. The Android image was confirmed against Google's SDK catalogue and action inputs/install behavior against the pinned action source. No Gradle tests were rerun for configuration/documentation changes; no changed-workflow remote CI run, commit or push was performed. A remote result remains pending publication and execution of this revision.


### Home accessibility implementation — 2026-10-02

The user requested implementing Home accessibility before their manual review. Kept native search, selected-chip semantics and existing large-font adaptation. Added descriptive card and initial/append Retry action labels, a filter traversal group, feedback headings with polite announcements, and excluded decorative portrait feedback from accessibility traversal. No global theme, navigation or Detail behavior changed. Added two controlled Compose contracts for card action labels/callbacks and error heading/announcement/retry semantics. These instrumented tests compile but were not executed; no behavioral RED or TalkBack pass is claimed.

Managed workflow `6a4ed49e01aad2e446b9cf266eac5103`: initial sandbox cache-lock failure; the first escalated command identified that ktlintCheck belongs to the root, not Home. Corrected command `:feature:home:compileDebugAndroidTestKotlin ktlintCheck` passed; `qualityCheck :app:assembleDebug` passed. The gate reused unchanged task outputs where applicable. APK: `app/build/outputs/apk/debug/app-debug.apk`. Manual Home accessibility remains pending. No installation, commit or push was performed. Workflow finish removes only wrapper-owned logs.


### Detail accessibility implementation — 2026-10-02

The user authorized extending the bounded accessibility work to Detail. Added name/section heading semantics, merged status/species and each episode card, retained existing per-fact merges, kept Back and Retry independent, hid decorative portrait feedback from accessibility traversal, and added polite error/empty announcements plus a descriptive character Retry action. Episode loading exposes indeterminate progress. No new dependency or whole-page merge was introduced. New controlled Compose tests cover identity/fact grouping and episode grouping/non-interactivity; they compile but have not been executed. Manual DA01/DA02 remain pending, and no behavioral RED or TalkBack result is claimed.

Managed workflow `dddfbbe210896c1ce1c56b57e0cae4d3` asked whether Detail and its semantic tests compile and ktlint passes: PASS after ordering test imports. The aggregate quality/APK question initially found DetailsFeedback LongMethod at 61 lines; concise formatting of the existing arrangement reduced it without adding an abstraction. Final `qualityCheck :app:assembleDebug`: PASS, 40 tasks executed and 227 up-to-date, including Detail unit/Paparazzi verification. Unchanged outputs were reused; instrumentation was not run. OpenSpec strict validation and diff whitespace checks passed. APK remains `app/build/outputs/apk/debug/app-debug.apk`; no installation, commit or push. Workflow finish removes only wrapper-owned logs.


### Duplicate search announcement correction — 2026-10-02

The user reported that TalkBack reads Search characters twice. The empty field exposed both its contentDescription and the same text from the placeholder. A controlled HomeSearchField semantics regression failed on emulator-5554 before the fix. Kept the persistent field label and cleared only the decorative placeholder semantics, preserving its visual text and editable field actions. Updated the one search test that selected the placeholder to select the field label.

Managed workflow `6ff3c3354c11849fe0f71f84f082e597` generated the instrumented APK before and after the fix; ktlint passed. ADB explicitly installed/reran the Home library test APK on emulator-5554 only, without using the physical phone. Full HomeSearchTest passed: 12 tests, including the new regression, in 23.888 seconds. This verifies semantics, not actual TalkBack speech. `qualityCheck :app:assembleDebug` passed: 35 tasks executed, 5 from cache and 227 up-to-date. APK: `app/build/outputs/apk/debug/app-debug.apk`; physical manual retest remains pending. No commit or push. Workflow finish removes only wrapper-owned logs.


### Human accessibility acceptance and publication authorization — 2026-10-02

The user explicitly confirmed completing the remaining Home/Detail accessibility validation and authorized commit and push of both screens. HA01–HA03, DA01–DA02 and tasks 3.4/3.7 are closed from that confirmation without inventing individual measurements or scanner captures. This accepts the bounded implementation and manual review; it does not convert unexecuted instrumented tests or remote CI to passes, and does not archive C12 or imply final release acceptance. The accessibility publication includes screen code, tests and its scoped validation record; the separate CI extension and unrelated documentation edits remain outside the commit. Latest qualityCheck/assembleDebug and twelve HomeSearchTest results are reused because no production source changed after those checks.


### CI execution policy refinement — 2026-10-02

The user selected automatic qualityCheck for pushes/PRs and manual-only API 37 instrumentation to keep routine feedback short while the emulator job gains remote execution evidence. The instrumented job is guarded by github.event_name == workflow_dispatch; README reflects that policy. Publication is authorized, but no remote instrumented success is claimed.


### Parallel CI trial — 2026-10-02

The user authorized removing the instrumented job dependency on quality, publishing the change and dispatching a manual trial. Both jobs can now start independently on workflow_dispatch; pushes/PRs still run only quality. Record measured job durations and actual emulator outcome before deciding whether to enable automatic instrumentation.


Parallel trial [37036912920](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/37036912920), SHA 6c12a12: quality PASS in 3m34s; instrumentation FAILED in 6m19s before tests (all three XML suites report zero executed). App/Detail installation reported insufficient internal storage; Home installation reported unavailable package service. This is infrastructure failure, not failed app assertions. Configure the pinned emulator action supported disk-size input to 4G and repeat manually; automatic PR instrumentation remains conditional on a successful acceptable-duration run.


Second parallel trial [37038001176](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/37038001176), SHA bb04492, disk-size 4G: quality PASS in 1m40s; instrumentation FAILED in 5m00s. App XML reports zero tests and INSTRUMENTATION_ABORTED: System has crashed; Home XML reports zero tests and a broken package-service pipe during APK installation. Detail records 11 failed cases at ActivityScenario startup, all unable to resolve ComponentActivity, before screen assertions. The reports do not establish whether the Detail launch failure is independent of the system/installation instability. Automatic PR instrumentation was not enabled because the user made it conditional on successful acceptable-duration execution. Keep the manual parallel job for diagnosis; no passing instrumented CI evidence is claimed.


### Final CI policy — 2026-10-02

After the two failed remote emulator trials, the user requested removing that job rather than introducing unreliable CI for delivery. Quality now runs only qualityCheck and uploads its reports on push, PR and manual dispatch. Home/Detail/app instrumented suites and local commands remain intact. Earlier emulator configuration and results above are historical; no instrumented CI success is claimed. This removes the observed emulator failure source without suppressing qualityCheck failures.


## Historical episode implementation evidence

The following original 2026-10-02 record predates the completed C12 manual review and subsequent publication. Its test results and temporary failures apply to the recorded revision; earlier publication restrictions and manual-pending statements are historical.

### O04 implementation and verification — 2026-10-02

Implemented at the user's request without creating or modifying an OpenSpec change. Observable boundaries reuse the existing repository, Details ViewModel, state-driven screen and production-navigation tests. Data resolves episode references through the existing HTTP client/cache, normalizes single-object/batch-array responses and validates identity/facts before restoring requested order. The ViewModel keeps character content and count available, guards pending section retries and owns cancellation. Text cards use existing theme/spacing and focused size tokens; no new dependency, artwork, destination or parallax tuning.

Managed Gradle workflow `428af206b3d3d5cf12733b5475e67448` ran the following checks. Behavioral RED was observed before repository, state and rendering implementations. Compilation/static-analysis failures are not RED.

| Run and question | Nested Gradle tasks | Observed result |
|---|---|---|
| 0001: Does the repository return episode facts in character order before implementation? | `:data:characters:testDebugUnitTest --tests '*RemoteEpisodesRepositoryTest'` | RED: expected episode facts, received empty list |
| 0002: Does the implemented repository load ordered episode facts with one batch request? | Same focused repository test | GREEN |
| 0003: Does Detail keep character facts visible while episode requests are pending before the state integration? | `:feature:details:testDebugUnitTest --tests '*DetailsViewModelTest'` | RED: character content had Empty rather than Loading episodes |
| 0004: Do Detail facts remain visible during episode loading and do existing detail contracts still pass? | `:feature:details:testDebugUnitTest :data:characters:testDebugUnitTest` | GREEN |
| 0005: Do episode scrolling and section-only Retry exist before rendering the section? | `:feature:details:connectedDebugAndroidTest`, filtered to DetailsContentTest | RED: episode list and section Retry were absent |
| 0006: Do the episode data, isolated retry and cancellation tests pass, and does Detail support vertical and horizontal scrolling with Back? | Both affected JVM suites and Detail instrumentation | Compilation failed: placeholder API has no shape parameter; corrected to the existing clip modifier |
| 0007: Do episode contracts and Detail scrolling pass after using the existing placeholder modifier API? | Both affected JVM suites and Detail instrumentation | GREEN: 47 data JVM, 11 Details ViewModel tests; nine screen tests on each API 37 Pixel/emulator |
| 0008: Do the completion quality gate, release assembly and existing navigation journeys pass with the episode section? | `qualityCheck :app:assembleRelease :app:connectedDebugAndroidTest` | Detekt flagged return count/complex condition; simplified the identified branches |
| 0009: Do quality, release assembly and navigation pass after simplifying episode validation to the project lint limits? | Same completion tasks | Navigation fixture compilation lacked the new episode binding; added a controlled dependency and episode data |
| 0010: Do quality, release assembly and production navigation regressions pass with controlled episode dependencies? | Same completion tasks | Emulator: 18 navigation tests passed. Connected Pixel: instrumentation crashed during a test, without an assertion message; device navigation acceptance remains incomplete |
| 0011: Do the standalone quality gate and release assembly pass independently of device instrumentation? | `qualityCheck :app:assembleRelease` | GREEN; aggregate JVM reports contain 87 tests including three architecture checks, zero failures/errors/skips |

The wrapper rejected a `--scope completion` invocation and an attempt to select the emulator via an `env` launcher; neither executed Gradle or counts as a verification run. All successful invocations above used the managed launcher. Wrapper-owned logs were removed on finish; source and test reports remain.

Native production smoke used emulator-5554 (Pixel 9a, API 37, 1080×2424, density 420, default font size). Rick #1 loaded 51 appearances with actual episode code/title/air date. Vertical scroll exposed the complete cards; horizontal swipe reached S01E03 “Anatomy Park” without moving the fixed Back control. Vertical movement was approximately 201 pixels, enough to inspect the existing subtle parallax but not a long page or measured performance result. Debug APK is installed on that emulator. Temporary visual-review pixels were deleted; no screenshot files are retained. Loading/error/Retry/cancellation and single-episode REST behavior are covered by controlled tests, rather than claimed as a full native failure matrix. Human visual acceptance and the connected Pixel navigation crash remain manual limitations.

### Detail portrait size adjustment — 2026-10-02

At the user's request, increased the shared portrait/skeleton width cap from 256dp to 288dp (12.5%), retaining the square aspect ratio, existing clipping and bounded parallax. No new behavior test or fabricated RED was added for this visual token adjustment. Managed workflow `bbae0baf4bb201c11e9b449c9b34a3f6` asked “Do quality and release assembly pass with the slightly larger square Detail portrait?” and ran `./gradlew qualityCheck :app:assembleRelease`: BUILD SUCCESSFUL in 20s. Native Rick #1 review on emulator-5554 confirmed the larger square image, available Back and episode content below; temporary review pixels were deleted. The debug APK is installed on that emulator. This verification includes the current working tree, including an independently edited Details ViewModel; this adjustment changes only the portrait token and associated documentation. Wrapper finish removed only its managed logs. The user accepted the square presentation and authorized committing this adjustment; no push is authorized in this request.
