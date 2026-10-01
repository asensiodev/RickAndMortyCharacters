## Decisions and approved boundary

The human approved this refinement on 2026-10-01. Home already owns its LazyGridState, focus manager and software keyboard controller. Collect the grid's native drag interactions in a composition-owned LaunchedEffect and hide the keyboard on DragInteraction.Start. Do not observe isScrollInProgress, which also includes automatic scrolling. Preserve focus and query state; no ViewModel or new dependency is needed.

Use the real HomeContent screen with controlled images and a recording keyboard controller to verify a gesture requests dismissal, while programmatic scroll and a changed generation do not. Verify retained input/selection/status and no search actions from dragging. Run one behavioral RED before implementing GREEN. An API 37 native check verifies actual IME disappearance and reopening; controller assertions alone do not prove platform behavior.

Run the affected Home screen suite, documented quality gate and release assembly, then record commands/results and native observations. Human acceptance precedes archival. Preserve the existing C09 changes; do not commit or push.

## Implementation and verification record

Codex implemented the human-approved refinement on 2026-10-01. HomeContent collects `gridState.interactionSource.interactions` in `LaunchedEffect(gridState, keyboard)` and requests `keyboard.hide()` on `DragInteraction.Start`. It preserves input focus; query state, debounce, selected status and generation handling remain owned by their existing components. No API, ViewModel or dependency was added.

Codebase Memory Tier 2 identified HomeContent and HomeSearchTest, supplied exact rendering source and inbound caller evidence, and reported matching coverage metadata without recorded gaps for the consulted Home source/tests. Direct reads supplied existing screen-test patterns. This is task-directed evidence, not an exhaustive graph claim.

The HomeContent seam uses controlled image loading and a recording platform keyboard controller. A real grid swipe verifies dismissal, retained input/selection/focus/status and absence of search actions. A second test programmatically scrolls and changes generation, then verifies the grid returns to the first card without a hide request or loss of focus. The controller is injected only via its existing CompositionLocal; production APIs were not expanded for tests.

All Gradle commands use the managed wrapper, workflow `753818590841d1ff2cbead1a0896d6b7`:

| Question and nested command | Observed result |
|---|---|
| Does a manual result drag dismiss the keyboard before the change? `:feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.rickandmortycharacters.feature.home.composables.HomeSearchTest#GIVEN_an_active_search_WHEN_the_user_drags_results_THEN_it_hides_the_keyboard_and_preserves_the_query` | Behavioral RED: the hide-request assertion failed because no dismissal was requested |
| Do manual keyboard dismissal and programmatic-scroll preservation pass? `:feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.rickandmortycharacters.feature.home.composables.HomeSearchTest` | GREEN: all 10 search/status tests passed on API 37 |
| Do all Home screen regressions pass after keyboard dismissal on drag? `:feature:home:connectedDebugAndroidTest` | PASS: all 27 Home screen tests passed on API 37 |
| Do the quality gate and release assembly pass after the keyboard refinement? `qualityCheck :app:assembleRelease` | First run failed on four argument-wrapping format errors in the new test; parent inspected the bounded ktlint XML report and corrected formatting |

The gradle-run skill required a read-only diagnostic owner after the format-check failure; the parent retained ownership of source edits. Subsequent managed checks passed:

| Question and nested command | Observed result |
|---|---|
| Does ktlintCheck pass after formatting the HomeSearchTest arguments? `ktlintCheck` | PASS |
| Do the documented quality gate and release assembly pass with the keyboard-on-drag change? `qualityCheck :app:assembleRelease` | PASS, 460 tasks, no warning/failure fingerprints in the bounded summary |
| Does installDebug install the updated app for native keyboard validation? `:app:installDebug` | PASS, installed on one API 37 device |

### Native keyboard verification

On emulator-5554, API 37, the real production app was launched after installation. Typing Rick and selecting Alive applied the combined result while `dumpsys input_method` still reported `mInputShown=true`. Dragging inside the grid changed it to `false`; the UI hierarchy retained Rick with focused=true, the selected Alive chip and the restored `Loaded 20 of 29 characters` counter. Tapping the search input reopened the IME (`mInputShown=true`) with Rick retained. This verifies actual platform dismissal/reopening separately from the recording-controller tests.

The affected screen suite has 27 passing tests, including the two new regressions. Final OpenSpec strict validation and whitespace checks pass. Human acceptance and archival remain pending; no commit or push was performed.

`gradle_run.py finish --workflow 753818590841d1ff2cbead1a0896d6b7` completed successfully and removed only wrapper-owned logs.

## Publication acceptance

On 2026-10-01, the human requested commit and push of C09 and the keyboard fix after their verification reports. This records acceptance for publication and explicit commit/push authorization. Archival remains a separate workflow step.
