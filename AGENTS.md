# Project agent instructions

## Scope and workflow

- Before implementation, read the approved change in `openspec/changes/` and its specs, design and tasks; implement only that increment.
- For product scope, read `README.md` (Delivery scope section); for module/API changes, `README.md` (Architecture section); for UI behavior, read the relevant `openspec/specs/` contract and `docs/DEVELOPMENT_PROCESS.md` (UI design section); for validation or archival, `docs/DEVELOPMENT_PROCESS.md` and `openspec/config.yaml`.
- Use the approved test boundaries for behavior-focused TDD: observe RED, implement GREEN, then refactor. Record actual commands/results in the change; environment or compilation failures are not behavioral RED.
- Check tasks after verification. Human acceptance precedes archival and the next change.

## Architecture and Kotlin

- Keep domain pure Kotlin. Data owns internal DTOs, transport errors and mapping; features consume domain contracts and remain independent. App owns dependency composition and navigation.
- Inject repositories directly into ViewModels; introduce use cases for concrete business behavior or reuse, rather than forwarding calls.
- Prefer the existing modules and native/library capabilities. Extract abstractions or shared utilities only for an actual boundary or consumer. Pin dependencies in the version catalogue.
- Give distinct domain contracts/model types their own files; keep sealed variants with their result owner.
- Keep constants with their owner; share tokens only for shared meaning. Express intent through names and focused functions, without comments in production code or tests.

## State and coroutines

- Render immutable UI state, accept explicit actions and expose read-only `StateFlow`. Collect with `collectAsStateWithLifecycle()`; composables own UI-only scroll, keyboard and focus objects.
- Own request jobs in the ViewModel lifetime; initialize idempotently with `LaunchedEffect(viewModel)`. Guard pending retries and define cancellation/latest-wins behavior when queries change.
- Propagate `CancellationException`; cancellation is never a user error. Use structured concurrency, suspend APIs and injected dispatchers for blocking work; never `GlobalScope` or blocking work on the main thread.
- Represent durable outcomes in state. Prefer navigation callbacks; introduce transient effects only with explicit lifecycle/delivery semantics.

## Compose and product behavior

- Consuming composables use named spacing, size, shape and typography tokens, not raw `dp`/`sp`. Literal definitions belong in focused design-system or feature token files.
- Preserve the selected design and implement only working controls. Keep runtime code focused on product flows; IDE previews are allowed, runtime demo/debug/gallery screens are not.
- Keep private `@Preview` functions and preview-only helpers in the same file as their rendering composable.
- Model applicable Loading, Content, Empty and Error states with contextual Retry. Retain existing content on append failure; keep controls mounted across results changes.
- Keep strings in resources, targets at least 48dp, and text readable at increased font sizes. Respect system insets; use stable keys/content types in lazy layouts.
- Reuse the shared image loader and size requests to their bounds. Image loading/failure stays local: metadata and selection remain available.

## Verification and Git

- Write test names as readable sentences with uppercase `GIVEN / WHEN / THEN`: state the condition, an action with a verb, and the observable outcome with a verb. Use backticks/spaces for JVM tests and underscores between words for instrumented tests while minimum API is below 30. Separate setup, action and assertions with blank lines.
- Keep framework rules above dependencies. Group dependency/mock/fake fields without blank separator lines; initialize them as `val` (lazily when framework context is required). Reserve `lateinit var` exclusively for the subject under test, named by its type (for example `homeViewModel`), after one blank line and initialized after its dependencies. Test composables and source rules directly without inventing a subject wrapper.
- Test repository and ViewModel behavior through their contracts, using controlled responses/scheduling. Compose tests cover implemented screens with controlled images; add geometry screenshots only when their increment is selected.
- Run affected checks first, then the documented completion gate. Report executed results and remaining manual checks; do not infer coverage from empty test tasks or generated code.
- Preserve unrelated changes. Commit only on explicit request; push requires separate explicit authorization and is never implied by commit approval.
