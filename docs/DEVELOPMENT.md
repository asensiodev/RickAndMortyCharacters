# Development

Status: planning. [PRD](PRD.md) owns product requirements and priorities; [ARCHITECTURE](ARCHITECTURE.md) owns module responsibilities and technical decisions. This document defines the shared development process. Work proceeds through individually reviewed changes without task estimates.

## Sequence

1. Review requirements, module interfaces and stack compatibility.
2. Define home and detail content from the API; explore them together in one visual system.
3. Correct the existing Home/Detail content screens in place, preserving their appearance and the selected export. Review the pair, then create additional state frames and validate navigation/motion.
4. Create the Android project, configure checks and initialize OpenSpec.
5. Implement small changes with behavior-focused TDD and human review.
6. Validate the application and update setup instructions, screenshots and documentation from actual results.

The temporary [BACKLOG](BACKLOG.md) preserves the pending work. When a change is prepared in OpenSpec, its detailed tasks and evidence move there; keep only a link in the queue. Do not maintain duplicate task lists.

## Proposed Android toolchain

| Area | Selection | Responsibility |
|---|---|---|
| Build | Kotlin, Gradle Kotlin DSL, version catalogue | Compatible pinned AGP/Kotlin/Compose/JDK versions |
| UI | Compose and Material 3 | Grid, details, search, status chips and shared visual foundations |
| Navigation | Navigation 3, subject to scaffold compatibility checks | Typed destinations, back stack and restoration |
| State | ViewModel, StateFlow and Coroutines | Actions, state production, cancellation and lifecycle-aware collection |
| Dependency injection | Hilt | Composition and test dependency substitution |
| Network | Retrofit, OkHttp and kotlinx.serialization | Remote requests, DTOs and mapping within data |
| HTTP cache | Bounded OkHttp disk cache | Header-driven JSON response reuse and validation |
| Pagination | Paging 3, subject to contract review | One owner of page generation, loading and retry state |
| Images | Coil 3 | Shared loader, request sizing, memory/disk caching and fallback |
| JVM tests | JUnit, coroutines-test, Turbine, MockWebServer | State behavior and HTTP integration with deterministic fixtures |
| Instrumented tests | AndroidX Test and Compose UI Test | Semantics, interactions and navigation journeys |
| Quality | ktlint, Detekt and Android Lint | Formatting, Kotlin analysis and Android checks |
| CI | GitHub Actions | Automated validation of changes |
| Local checks | Explicit hook installation, planned `installGitHooks` task | Pre-commit feedback without automatic staging |

Exact versions and runnable commands will be recorded when the project exists. Prefer fakes at repository interfaces; introduce MockK only for a concrete external dependency. Robolectric is not initially required by the planned JVM test boundaries. Screenshot tests, Konsist and Kover remain candidates for a demonstrated verification need.

## Visual design

[UI/UX Definition](UI_UX.md) owns the API-to-screen mapping, component responsibilities, state variants, motion and Stitch prompt. Explore both screens together, refine their components and states separately, then review the complete journey. Choose tokens in screen context and refine the selected design before translating its reviewed colour palette into Compose Material 3 roles, checking foreground/background contrast.

The selected export is stored in `docs/design/stitch/` and linked through UI/UX Definition. It supplies sufficient content and state references for implementation; no further Stitch generation is queued. Its handoff records an outdated Home-loading PNG, an unsupported Share glyph in Detail loading and selected-chip contrast to correct in Compose. Use the reviewed components and state contracts, then validate previews and device behavior. Final-card/Retry clearance, native scrolling, large text and contrast remain pending. Generated HTML and PNGs are visual references; the application will use native Compose components.

Google Stitch is the initial tool for exploring alternatives. Its FAQ stated free access with daily credits when checked on 2026-09-30. Figma can refine editable components and prototypes; AI availability depends on plan/access. Penpot remains a free alternative. Review generated designs before translating them into Compose. [Stitch](https://stitch.withgoogle.com/), [Figma AI](https://help.figma.com/hc/en-us/articles/24039793359767-Get-started-with-Figma-AI), [Penpot plans](https://penpot.app/pricing).

### Design handoff to Compose

The current route uses reviewed Stitch PNGs for appearance, HTML for structure/icon names and DESIGN.md as a proposal to reconcile with the actual screens. The [component and icon inventory](UI_UX.md#component-and-icon-inventory) keeps the handoff compact. Implement native Compose layouts and states from these references, extract consistent tokens, then compare previews and device behavior with the design. HTML/CSS measurements and generated prose are not automatically valid dp/sp values or production Kotlin.

Figma is optional when editable layers, manual refinement or design collaboration provide a concrete benefit. Stitch has a [direct Figma export integration](https://divriots.com/blog/divriots-powers-google-stitch-export-feature); [html.to.design also accepts Stitch ZIP exports](https://html.to.design/docs/file-tab/). Check the available export control and imported assets/layout if that route is selected. Editable imported layers do not establish a reviewed reusable component library.

Once a Figma file is connected, its [MCP tools](https://developers.figma.com/docs/figma-mcp-server/tools-and-prompts/) can supply structure, screenshots and variables. Figma describes this as [design context rather than production-ready framework code](https://developers.figma.com/docs/figma-mcp-server/server-returning-web-code/); native implementation and validation remain necessary. No Figma import or MCP connection has been performed for this project. It is not a prerequisite for the planned Compose work.

## Tests, CI and delivery

Unit tests cover observable ViewModel state, query coordination, cancellation and model mapping. JVM integration tests exercise real repositories against MockWebServer, including parameters, failures, pagination and the HTTP caching contract. Instrumented tests cover grid → detail → back and search/filter behavior with controlled data and images. Exercise contextual error/retry paths as well as successful content. Use focused Compose tests for persistent input/chips across loading/empty/error states, filter-only emptiness, Retry callbacks and keyboard Search; use navigation tests to show that those states add no route and that returning from detail preserves the browsing context. Repository fixtures distinguish the observed first-page/detail/append 404 outcomes. If the Should connectivity monitor is implemented, verify its shared snackbar through a fake monitor, independently from API error tests.

CI initially runs JVM tests, formatting, static analysis, Android Lint and debug assembly. Add the instrumented journey when it exists and runs deterministically. Actual GitHub Actions availability and cost depend on runner, plan and project visibility; a written workflow is not evidence of a successful run.

Install hooks explicitly for each clone. They complement CI and must not stage or modify unrelated changes. Before publishing an implementation claim, validate release assembly and a real-device/emulator journey, including R8 behavior if minification is enabled. Performance claims require observations or measurements.

## AI toolchain

| Tool | Role | Current status |
|---|---|---|
| Codex app | Planning, source inspection, implementation assistance and diff review | Used for documentation and research |
| OpenSpec | Capability requirements, change proposals, tasks and archives | CLI available; project initialization pending |
| Codebase Memory | Symbol/dependency exploration and impact analysis | New-project connection/indexing pending; graph tools unavailable in the planning session |
| Focused development skills | TDD, module design and Kotlin/Compose guidance | Relevant planning, component and animation guidance consulted |
| Visual design tooling | Generate or refine UI alternatives | Selected PNG/HTML reviewed; visual handoff ready with bounded Compose corrections; native validation pending |

AI and skills assist the work; human review accepts scope, design and behavior. Apply skills to the actual concern rather than loading every available guide. The application must build and run without AI tooling.

## OpenSpec and TDD workflow

One capability spec can evolve through several changes, such as grid, pagination, search and filters. Each change completes a bounded observable result and may cross layers. [OpenSpec concepts](https://github.com/Fission-AI/OpenSpec/blob/main/docs/concepts.md).

1. Review the goal, acceptance scenarios and public test interfaces before implementation.
2. Record material AI suggestions and technical decisions in the change.
3. Observe an expected failing behavior test, then implement the smallest passing behavior. Continue one RED → GREEN slice at a time.
4. Review the implementation, refactor where justified with green tests and rerun affected checks.
5. Record commands, observed results, manual validation and limitations.
6. Complete human review before the next change; update specifications and archive through the configured OpenSpec workflow.

Design and configuration use appropriate validation, not fabricated TDD cycles. Environment failures are not evidence of a missing behavior. Never reconstruct a RED phase retrospectively or infer passing tests from generated code.

Each change records its identifier/objective, AI tool and task (model when known), material decisions, verification evidence, human review and remaining limitations. Attribute meaningful external references and actual code reuse. Keep this evidence with the change; do not duplicate it in a parallel development diary.

## Planning record

On 2026-09-30, planning selected the two-screen browsing flow, Must search/chips and image/HTTP caching, and separate presentation features with shared domain/data. Primary sources and Pokedex Compose informed the technical review. The UI brief and Stitch prompts define a shared dark theme with a nuanced blue-family accent chosen during design exploration, card loading and detail parallax without standard top app bars or bottom navigation. A review across all six public documents clarified in-place empty/error states, keyboard behaviour, retry scope and responsive layout. Three additional live API probes informed contextual 404 mapping. Connectivity feedback remains a separate Should; offline browsing stays outside scope.

Two API GET probes returned cacheable responses and ETags; the observation and policy are recorded in [ARCHITECTURE](ARCHITECTURE.md). Documentation checks cover consistency and local links. Android implementation, Gradle/tests, OpenSpec changes and CI execution have not started.
