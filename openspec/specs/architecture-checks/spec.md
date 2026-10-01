# architecture-checks Specification

## Purpose
Protect implementation visibility and read-only ViewModel state through focused Kotlin source rules, reproducible Gradle tasks and shared quality-gate integration.
## Requirements
### Requirement: Bounded implementation visibility

Architecture checks SHALL inspect production declarations in character data remote/repository packages and feature ViewModels. Top-level classes, interfaces and objects in the selected data packages SHALL be internal or private; feature ViewModels SHALL be internal. Data DI bindings SHALL retain their existing public test-replacement boundary. A missing or empty expected selector SHALL fail visibly rather than report a passing rule.

#### Scenario: Existing production encapsulation
- **WHEN** the suite scans the current internal DTOs, transport declarations, repository implementation and feature ViewModels
- **THEN** visibility checks pass without rejecting the public DI replacement module or scanning test-only declarations

#### Scenario: Compilable public implementation
- **WHEN** a selected declaration becomes public, including Kotlin default visibility
- **THEN** its architecture rule fails and identifies the violating source declaration

#### Scenario: Missing selection
- **WHEN** an expected source root or rule's declaration selection is missing or empty
- **THEN** verification fails with scope information instead of accepting an empty assertion

### Requirement: Explicit read-only screen state

Each selected feature ViewModel SHALL expose its directly declared `state` as a visible `val` with an explicit read-only `StateFlow` type. Its existing mutable owner SHALL remain private. Directly declared outward properties SHALL have explicit `val` types and SHALL NOT expose `MutableStateFlow` or `MutableSharedFlow`, so an inferred mutable owner cannot bypass inspection. Checks SHALL inspect the declared state contract rather than match initializer text or rely on inferred-type resolution.

#### Scenario: Existing read-only behavior
- **WHEN** Home and Details gain explicit read-only state signatures
- **THEN** architecture checks and their existing behavior tests pass without changing screen outcomes

#### Scenario: Mutable or ambiguous state contract
- **WHEN** a ViewModel exposes `state` as a writable property, a mutable flow type, or omits its declared type
- **THEN** the state rule fails rather than assuming its initializer is safe

### Requirement: Reproducible architecture gate

The project SHALL pin Konsist as a test-only dependency in an existing JVM module and provide a root `konsistCheck` task included in `qualityCheck` and the owning module's `check`. The architecture suite SHALL execute once per gate, scan deterministic production roots and declare all inspected source files as inputs. Checks SHALL produce normal JVM test reports and preserve the current lightweight pre-commit command. No production module or runtime dependency SHALL be added.

#### Scenario: Local and CI commands
- **WHEN** a developer invokes `konsistCheck` or the existing Quality workflow invokes `qualityCheck`
- **THEN** architecture checks participate with failure propagation, alongside the aggregate gate's existing checks where applicable

#### Scenario: Source changes outside the JVM host
- **WHEN** an inspected feature or data source file changes, is added or is removed after a successful run
- **THEN** the next architecture invocation evaluates the current files instead of reusing an unrelated up-to-date result

#### Scenario: Failure and recovery
- **WHEN** a compilable forbidden declaration is introduced, checked and then restored
- **THEN** the architecture task and aggregate gate reject the violation, and verification recovers after restoration with actual outcomes recorded
