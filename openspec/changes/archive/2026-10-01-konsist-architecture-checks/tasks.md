## 1. Review and setup

- [x] 1.1 Obtain human review of the bounded rules, test boundaries and task ownership before implementation.
- [x] 1.2 Pin and resolve Konsist/JUnit in domain test sources; verify parsing and execution with the current toolchain without changing production dependencies or modules.
- [x] 1.3 Configure explicit production scopes and deterministic task working directory; declare scanned sources as inputs and prevent duplicate architecture-suite execution.

## 2. Rule slices

- [x] 2.1 Observe a compilable data visibility violation fail; implement the bounded internal/private rule, restore source and verify GREEN, preserving public DI replacement.
- [x] 2.2 Observe public feature ViewModel visibility fail; implement internal-visibility checks and verify GREEN with nonempty production selectors.
- [x] 2.3 Observe writable or untyped screen state fail; add explicit read-only state signatures and assertions, restore temporary edits and verify GREEN.

## 3. Shared gate and acceptance

- [x] 3.1 Expose root `konsistCheck`, wire it into `qualityCheck` and module `check`, and verify a scanned feature/data source edit invalidates the architecture task.
- [x] 3.2 Observe a representative architecture violation fail through the aggregate gate; restore it and run affected JVM tests, `qualityCheck` and release assembly through the managed wrapper.
- [x] 3.3 Update setup commands, reports and architecture status from executed evidence; validate OpenSpec, documentation links and diff cleanliness.
- [x] 3.4 Complete human review before archival/C06; do not commit or push without separate explicit authorization.
