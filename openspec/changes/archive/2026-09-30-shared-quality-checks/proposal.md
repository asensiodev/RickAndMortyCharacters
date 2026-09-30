**Status:** implemented, validated locally and in GitHub Actions, accepted and archived.

## Why

The Android shell builds, but formatting and analysis are not shared between local development and CI. Configure a small reproducible quality gate before character behavior introduces tests and more Kotlin code.

## What Changes

- Pin compatible ktlint and Detekt tooling, configure Android Lint and expose a root `qualityCheck` command covering analysis, JVM tests and debug assembly.
- Add an explicitly installed, read-only pre-commit check through `installGitHooks`.
- Add GitHub Actions validation for pull requests, pushes to main and manual runs.
- Document installation, commands, reports and observed validation, including modules with no tests.
- Enable build output caching and parallel tasks/tooling actions while retaining the existing 2 GB heap.

## Capabilities

### New Capabilities

- `shared-quality-checks`: consistent local/CI validation and opt-in pre-commit feedback.

### Modified Capabilities

None. Foundation startup and module contracts remain unchanged.

## Impact

Root/module Gradle configuration, version catalogue, formatting/analysis configuration, a tracked hook, GitHub Actions and setup documentation. No character feature, convention-plugin module, release signing or deployment is introduced. C01 is accepted and archived.
