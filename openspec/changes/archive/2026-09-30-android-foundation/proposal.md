**Status:** implemented, validated and accepted.

## Why

The product and visual references are defined, but there is no runnable Android project. A small buildable foundation establishes the agreed module boundaries before character behavior is implemented.

## What Changes

- Create the six Gradle modules defined in [ARCHITECTURE](../../../../docs/ARCHITECTURE.md).
- Pin a compatible build toolchain with a Gradle wrapper and version catalogue.
- Add a minimal launchable Compose application with English resources and portrait phone configuration.
- Document reproducible setup, build and launch checks.

## Capabilities

### New Capabilities

- `android-foundation`: reproducible Android assembly, application startup and enforced module boundaries.

### Modified Capabilities

None; this is the first capability specification.

## Impact

Root build configuration, Gradle wrapper/catalogue, the six module build files, application manifest/resources/entry point and setup documentation. Character screens and their runtime libraries are introduced in subsequent changes. The foundation is implemented and validated. The reviewer authorized commit/push and progression to C02; subsequent changes introduce character behavior and shared quality gates.
