## Decision

Apply a source-only readability pass to project-owned Kotlin calls, including test fixtures and previews, and ambiguous Kotlin library calls. Keep parameter names tied to inspected declarations; preserve argument order, values and trailing lambda structure. Preserve concurrent UI edits.

Existing Detekt 2.0.0-alpha.6 provides `complexity.NamedArguments` with `allowedArguments` and `ignoreArgumentsMatchingNames`, but requires full type analysis. The root `detekt` task deliberately runs CLI without compile classpaths. Enabling that rule in YAML alone would not enforce it. Ktlint's standard rules handle formatting, not resolving called parameter names. A compiler-aware Detekt setup per module/source set is the appropriate future CI option; no custom linter, dependency or inert configuration is added in this pass.

## Validation

Run formatting/static analysis first, then the existing quality gate, release assembly and Android-test compilation because call sites in instrumentation sources are also changed. Source syntax changes do not require new behavior tests or a fabricated RED cycle. Record actual results before marking tasks complete.

## Sources

- [Kotlin named-argument conventions](https://kotlinlang.org/docs/coding-conventions.html#named-arguments)
- [Detekt NamedArguments](https://detekt.dev/docs/rules/complexity/#namedarguments)
- [Detekt full type analysis](https://detekt.dev/docs/gettingstarted/type-resolution/)
- [Ktlint standard rules](https://ktlint.github.io/ktlint/latest/rules/standard/)

## Verification record

Pending.
