## 1. Scope and compatibility

- [x] 1.1 Review the bounded visual contracts and approve this O03 draft before implementation.
- [x] 1.2 Prove a minimal Compose snapshot on the current toolchain; record compatible Paparazzi/renderer/API/Java configuration and limitations.
- [x] 1.3 Pin the verified dependency/plugin in the catalogue and configure only the two feature modules, preserving existing JVM tests and hooks.

## 2. Deterministic baselines

- [x] 2.1 Add controlled data/images and fixed profile/animation fixtures at the existing rendering boundaries.
- [x] 2.2 Record/review the five Home baselines and four Detail baselines from the design matrix; keep generated reports/diffs out of source control.
- [x] 2.3 Verify repeatedly without image drift; document actual host/render settings and fixture synchronization.
- [x] 2.4 Observe an intentional visual diff, restore the temporary change and verify GREEN without rerecording accepted goldens.

## 3. Quality and handoff

- [x] 3.1 Connect verification to qualityCheck and the existing CI job, with no automatic baseline recording or missing-baseline success.
- [x] 3.2 Run affected checks, screenshot verification, full quality gate and release assembly through the managed workflow; record executed outcomes and CI status honestly.
- [x] 3.3 Document record/verify/report commands and baseline review policy; preserve native motion/navigation verification and note remaining C11/device limitations.
- [ ] 3.4 Validate OpenSpec strictly and obtain human acceptance before archival and C12; commit and push require explicit authorization.

Strict OpenSpec validation passed locally. Task 3.4 remains open for human acceptance; remote CI/native final-delivery checks are not inferred from layoutlib output.
