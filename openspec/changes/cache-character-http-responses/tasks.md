## 1. Readiness and scope

- [x] 1.1 Confirm C08 acceptance and reconcile this draft with its final repository/network contracts.
- [x] 1.2 Obtain human review of C09 scope, cache ownership and observable test boundaries before implementation.
- [x] 1.3 Recheck real catalogue/detail GET headers and record observations without treating provider freshness as an application constant.

## 2. Shared cache and first behavior slice

- [x] 2.1 Observe a repository HTTP test failing fresh reuse because a repeated identical request reaches the server.
- [x] 2.2 Configure the shared 10 MiB data-owned disk cache and minimal internal client factory; verify fresh page/detail reuse and production Hilt wiring.
- [x] 2.3 Verify sequential cache/client recreation reuses a fresh detail response from the same directory.

## 3. Validation and isolation regressions

- [x] 3.1 Verify stale ETag validation/304, renewed freshness, changed-200 body replacement and normal requests without a validator.
- [x] 3.2 Verify independent page/name/status URLs, All versus Unknown, distinct detail IDs and applicable Vary headers through results and recorded traffic.
- [x] 3.3 Verify no-store and no-cache policy, stale transport/service failures without custom fallback, and existing cancellation/contextual error regressions.

## 4. Completion and acceptance

- [x] 4.1 Run affected data JVM checks, the documented quality gate and release assembly; record actual RED/GREEN and regression commands/results.
- [x] 4.2 Verify affected API 37 journeys and native production composition; distinguish UI smoke checks from HTTP-cache evidence.
- [x] 4.3 Update documentation to match verified behavior and record cache/offline limitations.
- [x] 4.4 Obtain human acceptance before archival and C10 implementation; commit/push only with their separate explicit authorizations.
