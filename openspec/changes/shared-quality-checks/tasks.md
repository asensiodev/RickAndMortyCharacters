## 1. Review and compatibility

- [x] 1.1 Review this proposal and acceptance scenarios before implementing the quality gate.
- [x] 1.2 Resolve the portable daemon JDK policy and verify compatible pinned formatter/analyzer versions with the existing Gradle/AGP/Kotlin setup.

## 2. Local checks

- [x] 2.1 Configure ktlint, Detekt and Android Lint for applicable modules/source/scripts, then add the aggregate `qualityCheck` task including JVM test tasks and debug assembly.
- [x] 2.2 Observe independent formatting/static-analysis/test failures and successful recovery in an isolated copy; remove temporary probes and record actual test availability.

## 3. Hook and CI

- [x] 3.1 Add the read-only pre-commit hook and `installGitHooks`; verify repeat installation, custom-hook protection, failure propagation and unchanged source/index.
- [x] 3.2 Add the pinned GitHub Actions workflow invoking the full gate with documented SDK/JDK, minimal permissions and diagnostic reports; validate its configuration locally.

## 4. Evidence and acceptance

- [x] 4.1 Update README/DEVELOPMENT with actual commands and reports; run the complete gate. Record a remote CI result after authorized publication, or explicitly retain it as pending.
- [x] 4.2 Review the complete diff and evidence with the human reviewer before archival and C03.
