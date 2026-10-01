**Status:** C05A accepted on 2026-10-01, locally validated and archived.

## Why

Home and Details now exercise the shared data boundary and ViewModel state contracts. Add focused executable architecture checks to preserve their encapsulation as pagination, search and filters extend the code.

## What Changes

- Add pinned Konsist and JUnit test dependencies to the existing pure-JVM `:domain:characters` test sources, without changing production dependencies or the six-module graph.
- Check that data transport/repository implementation declarations remain internal or private, feature ViewModels remain internal, and their exposed `state` is an explicitly typed read-only `StateFlow` property.
- Give the two existing `state` properties explicit return types, preserving their current behavior and private mutable owners.
- Expose `./gradlew konsistCheck`, include it in `qualityCheck`, and track the production sources inspected by the rules as task inputs.
- Observe meaningful rule failures against temporary, compilable source violations; restore valid source and verify the same local/CI gate recovers.

Keep the existing lightweight pre-commit checks. No new production module, Android test dependency, architecture framework or naming-rule collection is introduced.

## Capabilities

### New Capabilities

- `architecture-checks`: bounded Kotlin source rules, reproducible execution and aggregate-gate integration.

### Modified Capabilities

None. The existing shared quality gate retains its required checks and adds architecture validation.

## Impact

Version catalogue, root and domain test-task configuration, one focused architecture test suite, explicit Home/Details state signatures, and setup documentation. The Quality workflow already invokes `qualityCheck`; the new task joins that command without requiring a second workflow. Local execution verifies the pinned dependency against the implemented sources/toolchain. Remote CI execution for C05A remains pending.
