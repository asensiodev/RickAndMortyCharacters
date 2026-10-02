## 1. Candidate and scope

- [x] 1.1 [User] Review and approve the bounded C12 draft and manual checklist before execution.
- [x] 1.2 [Codex + User] Identify the candidate source/working-tree state, build variant/artifact and device; coordinate with the user's parallel documentation edits.

## 2. Automated evidence

- [x] 2.1 [Codex] Run or reuse justified final-candidate `qualityCheck :app:assembleRelease` evidence through the managed workflow; report real task/test results and reviewed nine golden images.
- [ ] 2.2 [Codex, with device provided by User] Verify Home, Details and app instrumented suites on identified targets; recheck the prior physical-device instrumentation failure or record why it remains blocked.
- [x] 2.3 [Codex + User] Verify the existing Quality CI run for the authorized published candidate; record SHA/run URL/outcome and inspect any cross-host screenshot difference before accepting an update.

## 3. Human device review

- [x] 3.1 [User] Execute and mark [the manual QA checklist](../../../docs/MANUAL_QA.md) on a physical phone, recording observations and conditional cases not reproduced.
- [x] 3.2 [Codex + User] Identify an installable release artifact and record its critical-flow smoke result; if only debug is tested, keep release runtime explicitly unverified for acceptance.
- [x] 3.3 [Codex] Investigate reported blocking regressions, apply only agreed bounded fixes and reverify affected boundaries; [User] repeat affected manual cases after those changes.

## 4. Delivery acceptance

- [x] 4.1 [User owns docs; Codex verifies] Check the final documentation/screenshots against actual behavior and results; link evidence without duplicating the user's rewrite.
- [ ] 4.2 [Codex + User] Reconcile remaining C11/O03 review with the checklist, record limitations and validate OpenSpec strictly. Retain the completed manual checklist as dated evidence for the tested revision/device; do not delete it or maintain it as a second active task list after acceptance.
- [ ] 4.3 [User] Explicitly accept or defer delivery from the evidence. Archival, commit and push stay separate, explicitly authorized actions.


## Execution status — 2026-10-02

Candidate `b95a5bc`: local gate/release PASS, exact-SHA CI PASS, 54 fresh instrumented tests PASS on API 37 emulator, and locally signed release emulator smoke PASS. See design for commands, artifact hash, reused outputs and limits. Task 2.2 remains open: the physical Pixel is connected, but its complete instrumentation suite has not passed after the corrections. Task 3.2 records an installable release and emulator smoke; it does not mark the user's physical-device checklist or conditional recovery as passed. Human review of C11/O03, physical-device QA and final acceptance remain pending. The user authorized committing and pushing the current work on 2026-10-02. Final acceptance and archival remain pending; the earlier exact-SHA CI result is historical until the new published revision is checked.


## Human manual validation — 2026-10-02

The user explicitly confirmed that the manual tests are validated and everything works correctly after the fixes. Tasks 3.1 and 3.3 are complete. QC04 and QC06 remain honestly recorded as not reproduced; their status is not converted into a manual pass. Remaining automated closure/archival items retain their own status.
