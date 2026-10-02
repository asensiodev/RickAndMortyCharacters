## Context and readiness

C11 closes integrated visual/motion consistency, a Must in PRD. Earlier increments already implement visual foundations; this pass does not presume every component needs a rewrite. The user requested this draft; its draft status is not acceptance evidence. C09's publication acceptance is recorded.

Read [Design handoff](../../../docs/DEVELOPMENT_PROCESS.md#ui-design) for the selected references and handoff corrections; OpenSpec scenarios define screen-state contracts. Home-loading's PNG is stale for the counter; Detail-loading's Share glyph is unsupported. Review against the documented native behavior rather than copying those discrepancies.

## Current source evidence

- `HomeContent` owns saveable grid position, generation resets and UI keyboard handling. Search and chips sit outside `HomeResultsPanel`; preserve this placement during result motion.
- `DetailsContent` renders data states inside a system-safe root and keeps Back outside the state branch. Preserve that availability.
- `CharacterPortrait` uses the caller's Coil loader with constraint-sized requests and local loading/failure; `CharacterCardSkeleton` uses generic placeholders and noninteractive loading semantics.
- `AppNavigation` owns a Navigation 3 `NavDisplay`, typed Home/Detail entries and saveable/ViewModel decorators. It does not specify custom transition arguments in the inspected function. Review actual default behavior before adding overrides.
- `RickAndMortyTheme` supplies `DarkColors`, typography and shapes. Light appearance remains an independently selected Should, not part of C11.

These snippets identify ownership, not visual defects or device performance. No new device review or screenshot comparison was executed while drafting.

## Decisions

### Review first, correct observed issues

Start with a bounded review matrix and record each issue with its screen/state, reproduction, expected contract and owner. Compare actual native captures with the selected reference while allowing documented responsive adjustments. Preserve accepted branding. Correct token values/uses or focused layouts at their existing owner; extract a shared primitive only when actual reuse warrants it. No new module, animation framework, request abstraction or runtime demo is needed.

Review Home Content, Loading, no matches, empty catalogue, initial Error and append loading/error/end; Detail Content, Loading, Error and NotFound. Include unknown status, long metadata, absent Type and pending/failed portraits. Portrait checks use the accepted phone viewport and default font size; enlarged-text and responsive accessibility review are deferred to O02. Record actual viewport, density, font scale, navigation mode and API level.

### Keep motion local and interruptible

Review existing skeleton feedback, Coil fade, chip selection, portrait parallax and native forward/back transitions before changing them. Preserve immediate data rendering; do not hold a fast response to exhibit a skeleton. Retain immediate result replacement unless observation justifies a small effect; record the keep/change decision for skeleton-to-content and query replacement.

Choose the smallest native API after the problem is identified: existing loader behavior for image appearance, a target-state animation for a local value, or a region-level content transition only when subtree identity and interaction are safe. Navigation remains owned by the existing Navigation 3 stack; do not apply a different navigation framework. Recheck the pinned API before selecting overrides. Keep frame-rate reads in the relevant layout/draw work where applicable. Avoid grid-wide entrance effects on keystrokes, append or Back and avoid overlapping interactive old/new query results.

Use native Compose duration handling and shared image-loader defaults. Explicit disabled-motion adaptation for Coil and scroll parallax is deferred to O02 by the user. Back, loading meaning and chip selection must remain visible. Inspect off-screen/disposed loading effects and repeated navigation for unnecessary ongoing work. Shared image transition remains O01.

### Preserve the accepted layout

Use existing string resources and named design/feature tokens. Review top/middle/end of Detail, final Home cards/footer and keyboard-open feedback. Retain the floating counter's accepted visibility and bottom clearance. Keep the accepted dark appearance; light-theme selection needs its own scope.

On 2026-10-02 the user deferred accessibility review to O02: enlarged-text adaptations/previews, narrow enlarged-text tests, contrast and touch-target audits, and TalkBack. The tentative Detail FlowRow with a font-scale threshold was removed; it could force stacked facts on wide screens regardless of available space. Existing accessibility basics and previously accepted behavior remain. The existing enlarged-text Home IDE preview was removed at the user's request; ordinary state previews remain.

## Observable test boundaries

- **Existing screen/component tests:** render real Home/Detail content with controlled data/images; assert available actions, current identity, preserved controls, local portrait feedback and reachable footer behavior for changed code. Reuse existing card/skeleton tests where applicable.
- **Production Activity journeys:** real Home → Detail → both Back paths with fake repository and controlled images. Protect later-page query/status/viewport continuity if affected by rendering/navigation edits.
- **Controlled motion checks:** for a changed finite animation, use the Compose test clock to verify initial/interrupted/completed interaction and disposal where observable. Avoid assertions of incidental easing values. Custom disabled-motion adaptation is outside this revised scope.
- **Visual-only corrections:** actual before/after captures and role/geometry checks. Do not add a screenshot framework or pixel baselines by default; O03 remains separately selectable if a demonstrated regression need justifies it.
- **Native API 37 review:** controlled states plus production smoke for images, keyboard, navigation, native motion, accepted phone rendering and repeated image-heavy scrolling. Record cold/warm image conditions and observations; no smoothness metric or exhaustive accessibility claim without corresponding evidence.

For a behavior correction, observe a failing test for the specific contract before implementing the smallest fix, then verify GREEN. Token/design-only changes use visual validation; environment/compilation failures are not behavioral RED. Name tests and arrange fields per AGENTS.md.

## Verification and acceptance

Run affected JVM/instrumented tests first, then `qualityCheck` and `:app:assembleRelease` through the managed Gradle workflow. Run affected API 37 screen/journey suites and native visual/motion checks. Record exact commands, results, captures, keep/change motion decisions and manual limitations here. Update UI_UX only from verified behavior. C12 owns final delivery README/screenshots and release-candidate instructions.

No new product features are required to close this pass. Unrelated defects or optional enhancements need their own selected scope. Human acceptance precedes archival and C12; commit and push require separate explicit authorization.

## Planning evidence

Codex prepared C11 on 2026-10-01 using BACKLOG, PRD, ARCHITECTURE, UI_UX, DEVELOPMENT, OpenSpec configuration, accepted card/navigation specs and active C09 records. Applied Ponytail for bounded reuse and compose-animations for local motion/lifecycle decisions. Codebase Memory Tier 2 project `Users-angelasensio-Development-RickAndMorty` was ready. The bounded symbol search returned all nine rows without pagination; AppNavigation's complete one-hop outbound trace identified Home/Detail wiring. Exact HomeContent, DetailsContent, CharacterPortrait, CharacterCardSkeleton, AppNavigation and theme snippets supplied the source claims above. Coverage generation `2026-10-01T20:16:37Z` matched all consulted paths with no recorded gaps, a best-effort signal rather than completeness proof.

Draft validation is recorded below after execution. This planning work claims no Android RED/GREEN, Gradle result, new device capture or performance validation.

## Draft validation — 2026-10-01

`openspec validate polish-visual-motion-consistency --strict --no-interactive` passed. `openspec status --change polish-visual-motion-consistency` reported all four planning artifacts complete. `git diff --check` passed; a direct check of the new Markdown files and BACKLOG also verified local links and trailing whitespace, including untracked artifacts. These results validate planning structure, not implementation or visual acceptance. Implementation tasks remain unchecked.

## Implementation record — 2026-10-02

The human authorized C11 implementation and its prepared boundaries. The current repository removes C10 from this increment/queue; C11 preserves the normal browsing contract without adding saved-state work. No commit or push is authorized by this implementation request.

### Initial observed findings and corrections (motion adapter subsequently withdrawn)

1. Both portraits inherited Coil's loader-wide crossfade regardless of Compose motion settings. The pinned [Coil CrossfadePainter source](https://github.com/coil-kt/coil/blob/3.6.3/coil-compose-core/src/commonMain/kotlin/coil3/compose/CrossfadePainter.kt) uses a monotonic clock; it does not read Compose's MotionDurationScale. The initial implementation set the request crossfade from the existing framework signal and render the state's direct painter when motion is disabled, including a pending/completed transition. This retains sized requests, the shared loader, local failure and metadata/selection availability.
2. LoadingPlaceholder continued entering the infinite transition at scale zero. The behavioral screenshot observed a dim pixel (#171819) rather than the solid static equivalent (#343538) after disabling motion. The initial implementation left the animation subtree and uses the existing static branch. The transition is composition-owned; disabling motion or removing the placeholder disposes it.
3. Home and Detail already use the same outlined error asset and visual roles, but Detail's icon/Retry width were 36dp/128dp versus Home's 40dp/140dp. Detail now uses 40dp/140dp through its existing feature tokens. This visual-only correction has no fabricated behavioral RED.

The subsequently withdrawn design-system `isMotionEnabled()` accessor read the snapshot-observable framework scale from the remembered Compose coroutine context. Both portraits and generic loading were consumers. It replaced Detail's local observer/effect and added no platform observer, dispatcher, module or dependency. The default static fallback also suited previews without a framework motion context.

### Motion keep/change decisions

| Region | Decision and reason |
|---|---|
| Skeletons | Retain restrained local pulse with native Compose duration handling |
| Images | Retain brief Coil success fades using shared loader defaults |
| Chips | Retain native contained interaction feedback and persistent static selection |
| Skeleton-to-content | Retain immediate replacement; no minimum display duration or entrance delay |
| Query replacement | Retain immediate current-generation replacement; no interactive outgoing old-query cards |
| Detail parallax | Retain bounded clipped translation and fixed Back |
| Navigation | Retain Navigation 3's native forward/Back transitions and current entry ownership |

### Behavioral RED and targeted GREEN

All Gradle calls use `gradle_run.py`, workflow `c4473029cdb2f81c8a07885dce8637f5`. The initial adb socket startup was denied inside the sandbox; escalated access supplied the API 37 emulator. This environment failure is not behavioral RED.

The first instrumented run executed new Home motion tests and the new Detail disabled-motion test before production edits. Home's portrait fade assertion failed, Detail's fade assertion failed, and the placeholder pixel assertion failed with #171819 instead of #343538. The enabled-motion Home case passed. The initial shared class filter also named each module's test in the other APK, producing two ClassNotFound initialization errors; those are runner-selection errors, not RED. Subsequent runs use module-appropriate filters/suites.

After the correction, the focused HomeMotionTest run passed its three initial cases. The initial full Detail suite passed ten tests, including disabled fade/Back and two enlarged-text checks. The user subsequently deferred those two checks to O02; they were removed. A later stacking assertion produced behavioral RED, but its proposed font-scale layout correction was withdrawn before GREEN following the scope decision. Further completion results are recorded after execution below.

### Deferred accessibility review

Exploratory contrast calculations and enlarged-text captures were collected before the scope revision. They are not C11 acceptance criteria or a claim of completed O02 verification. O02 owns subsequent accessibility review and layout decisions.

### Scope-revision verification — 2026-10-02

Managed workflow run 0009 asked: “Do the quality gate and release assembly pass after withdrawing the C11 enlarged-text changes and preview?” The command `./gradlew qualityCheck :app:assembleRelease :feature:details:compileDebugAndroidTestKotlin` passed (BUILD SUCCESSFUL, 11s). This rechecked quality, release assembly and compilation of the remaining Detail instrumented tests; it did not rerun instrumented tests. `openspec validate polish-visual-motion-consistency --strict` and `git diff --check` passed. Emulator viewport/font settings were restored to their original values, animator scale restored to unset, and transition/window scales restored to 1.0. C11 integrated acceptance remains pending.

### Simplified motion policy — 2026-10-02

The user explicitly withdrew the custom system-animation check for this increment. This revision supersedes the disabled-motion adapter decisions and tests above: remove `isMotionEnabled`, motion-dependent portrait requests, painter bypasses and the static parallax branch. Keep Compose's native duration handling, shared Coil fades, bounded scroll parallax and the existing explicit placeholder `animated` parameter. The removed disabled-motion tests describe the withdrawn contract; existing screen tests remain the regression boundary. Coil fades and scroll-linked parallax no longer receive explicit disabled-animation adaptation; O02 owns any later accessibility policy. Earlier RED/GREEN results remain historical evidence only.

Managed Gradle workflow `9214404b8313e9753709235bf5f23f0e` ran `./gradlew :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest`: passed 27 Home and seven Detail tests on Pixel 9a API 37 in 44 seconds. These verify existing screen behavior after withdrawing the custom adapter; no new behavioral RED is claimed for reverting this policy.

`./gradlew qualityCheck :app:assembleRelease` stopped at Detekt: `RateLimitedImageInterceptor.intercept` line 18 and `retryDelayMillis` line 30 exceed ReturnCount. Those belong to the concurrent portrait-recovery task and were left unchanged. The full gate was blocked at that run; final combined validation is recorded below.

Strict OpenSpec validation and `git diff --check` passed. A bounded literal scan found no `isMotionEnabled`, `MotionDurationScale` or `motionEnabled` references in the design-system, Home and Detail source trees. No new manual disabled-animation or visual acceptance claim is made. C11 acceptance remains pending.

The follow-up `./gradlew qualityCheck :app:assembleRelease -x detekt` also stopped at an unrelated Ktlint indentation finding in `app/src/androidTest/kotlin/com/asensiodev/rickandmortycharacters/images/ImageLoaderRecoveryTest.kt` lines 43–49. The structured checkstyle report identified that file; it was left unchanged. Release assembly is checked independently instead of repeatedly bypassing gate tasks.

`./gradlew :app:assembleRelease` passed independently in four seconds. Managed verification questions/results: “¿Pasan las pruebas de Home y Detail tras quitar la comprobación de movimiento?” — yes, 34 tests; “¿Pasan el control de calidad y la compilación de release tras simplificar las animaciones?” — full gate blocked by unrelated Detekt; “¿Pasan el resto del control de calidad y release excluyendo el Detekt bloqueado por la tarea de imágenes?” — blocked by unrelated Ktlint; “¿Compila la release tras retirar la comprobación de movimiento?” — yes. Workflow finish removed only wrapper-owned logs. No commit/push was performed.

## Final combined validation and publication acceptance — 2026-10-02

The portrait-recovery work corrected the temporary blockers above. Managed workflow `213f6e6fe01414edab44c0bf8db0dd92` passed `qualityCheck :app:assembleRelease :app:installDebug` on the final combined sources. The full app suite passed seventeen instrumented tests; after the retry-timing adjustment, all eight image-recovery tests passed. The preceding C11 screen run passed 27 Home and seven Detail tests on API 37. The [portrait-recovery record](../recover-rate-limited-portraits/design.md) owns its timing regression and native image-cache conditions. Its final sixty-swipe run reached 260 loaded characters and all pending visible portraits completed without another gesture. These are behavior observations, not frame-rate measurements.

The user explicitly accepted publication of the implemented C11/polish corrections and portrait recovery. Broader integrated-review items remain unchecked until their matrix is recorded; publication does not claim their completion or authorize archival/C12. OpenSpec validation and unchanged-source gate results are reused for publication.
