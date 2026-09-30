## ADDED Requirements

### Requirement: Reproducible shared quality gate

The project SHALL provide a root `qualityCheck` command using pinned compatible tooling to check Kotlin/Kotlin DSL formatting, Kotlin static analysis, applicable Android debug lint, JVM tests and debug app assembly. The gate SHALL fail when any constituent check fails, exclude generated build output and never automatically format source. Modules with no tests SHALL NOT be described as tested.

#### Scenario: Valid source
- **WHEN** a developer with the documented JDK/SDK runs `./gradlew qualityCheck`
- **THEN** the applicable checks, JVM test tasks and debug assembly complete successfully and reports identify actual executed checks

#### Scenario: Violation or failing test
- **WHEN** an isolated validation checkout contains a formatting violation, a supported static-analysis violation or a failing JVM test
- **THEN** the corresponding check and the aggregate command fail, and restoring valid source permits a successful run

### Requirement: Explicit safe hook installation

The project SHALL offer `installGitHooks` for an explicit per-clone installation of a tracked pre-commit hook. Installation SHALL be repeatable and SHALL preserve unrelated hook configurations. The hook SHALL run read-only formatting and Kotlin-analysis checks from the repository root, return failure when a check fails and never modify source, stage, stash or commit files.

#### Scenario: Fresh clone and repeated installation
- **WHEN** a developer installs the hook in a clone without custom hooks and repeats the installation
- **THEN** the configured pre-commit hook remains usable without duplicating installation or changing global Git settings

#### Scenario: Existing custom hook
- **WHEN** installation encounters another custom hooks path or an unrelated existing local pre-commit hook
- **THEN** installation stops with guidance and leaves the existing configuration and hook unchanged

#### Scenario: Failed check during commit
- **WHEN** a configured pre-commit check detects a violation, including a working-tree violation outside staged changes
- **THEN** it blocks the commit, reports the failed check and leaves staged/unstaged source changes intact

### Requirement: Automated CI validation

GitHub Actions SHALL run the same full quality gate for pull requests, pushes to main and manual dispatch with documented JDK/SDK prerequisites, pinned action revisions and read-only repository permissions. A failed check SHALL fail the job. Signing and deployment secrets SHALL NOT be required.

#### Scenario: Pull request or main update
- **WHEN** GitHub runs the workflow for a pull request or main push
- **THEN** it prepares the pinned prerequisites and invokes `./gradlew qualityCheck`, preserving failure status and available diagnostic reports

#### Scenario: Remote run not yet executed
- **WHEN** only local checks and workflow inspection have completed
- **THEN** documentation identifies remote CI execution as pending and makes no successful GitHub-run claim

### Requirement: Observable development setup

Documentation SHALL explain the full gate, lightweight hook checks, explicit installation for each clone, reports and the actual validation outcomes. Build caching and parallel task/tooling settings SHALL retain the existing heap limit and SHALL NOT be presented as measured performance improvements.

#### Scenario: Contributor follows setup
- **WHEN** a contributor follows README from a clone with the documented prerequisites
- **THEN** they can invoke the full gate and explicitly install hooks, and can distinguish configuration/build evidence from executed product tests and remote CI results
