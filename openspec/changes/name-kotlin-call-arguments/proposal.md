**Status:** Scope authorized by the user's request on 2026-10-01; implementation review pending. Independent source readability adjustment; preserves other active changes.

## Why

Positional arguments obscure the meaning of model fields, boolean flags and calls with several inputs when reading code outside the IDE.

## What Changes

- Name arguments at project-owned Kotlin function and constructor call sites in production, previews and tests.
- Name ambiguous multi-argument Kotlin library calls where their signatures are known.
- Preserve Java calls, varargs, function-type invocation and idiomatic single-argument library operations.
- Document the convention and verify available enforcement. Do not enable a Detekt rule that silently skips analysis under the current untyped CLI task.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `shared-quality-checks`: explicit Kotlin call-site readability convention and tool limitations.

## Impact

Kotlin source call-site syntax and development documentation. No API, behavior, dependency or module change.
