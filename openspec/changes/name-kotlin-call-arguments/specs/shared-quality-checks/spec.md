## ADDED Requirements

### Requirement: Readable Kotlin call arguments
Project-owned Kotlin calls SHALL use named arguments when supplying function or constructor parameters. Idiomatic trailing lambdas and callable references MAY retain their normal syntax. Java calls, vararg collection factories and function-type invocations SHALL retain supported positional syntax.

#### Scenario: Multiple model fields
- **WHEN** a Kotlin call supplies several character model fields
- **THEN** each supplied field is associated visibly with its declared parameter name without changing values or evaluation order

#### Scenario: Verification tool limitations
- **WHEN** development documentation describes automatic named-argument enforcement
- **THEN** it identifies Detekt's type-resolution requirement and does not claim that the current untyped analysis enforces the convention
