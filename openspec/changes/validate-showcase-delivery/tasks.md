## 1. Candidate and scope

- [x] 1.1 [User] Review and approve the bounded C12 draft and manual checklist before execution.
- [x] 1.2 [Codex + User] Identify the candidate source/working-tree state, build variant/artifact and device; coordinate with the user's parallel documentation edits.

## 2. Automated evidence

- [x] 2.1 [Codex] Use the managed workflow to run or reuse justified candidate qualityCheck/build evidence for the selected variant; keep earlier release evidence dated and report real task/test results and reviewed nine golden images.
- [ ] 2.2 [Codex, with device provided by User] Verify Home, Details and app instrumented suites on identified targets; recheck the prior physical-device instrumentation failure or record why it remains blocked.
- [x] 2.3 [Codex + User] Verify the existing Quality CI run for the authorized published candidate; record SHA/run URL/outcome and inspect any cross-host screenshot difference before accepting an update.
- [x] 2.4 [Codex] Evaluate instrumented CI and remove its emulator job at the user’s request after failed environment trials; retain local instrumented suites and automatic qualityCheck.

## 3. Human device review

- [x] 3.1 [User] Execute and mark [the manual QA checklist](../../../docs/MANUAL_QA.md) on a physical phone, recording observations and conditional cases not reproduced.
- [x] 3.2 [Codex + User] Record the earlier installable release/smoke and the subsequent user-selected debug review separately; do not claim final release-runtime acceptance.
- [x] 3.3 [Codex] Investigate reported blocking regressions, apply only agreed bounded fixes and reverify affected boundaries; [User] repeat affected manual cases after those changes.
- [x] 3.4 [User] Execute the bounded Home accessibility demonstration added to the manual checklist; preserve basic accessibility on both screens without claiming a complete audit.
- [x] 3.5 [Codex] Apply the user-selected Home semantics improvements and verify compilation and the quality gate; leave manual TalkBack verification to the user.
- [x] 3.6 [Codex] Complete the user-selected Detail grouping, headings and feedback semantics; verify compilation and the quality gate.
- [x] 3.7 [User] Verify Detail with TalkBack and large text using DA01/DA02.

## 4. Delivery acceptance

- [x] 4.1 [User owns docs; Codex verifies] Check the final documentation/screenshots against actual behavior and results; link evidence without duplicating the user's rewrite.
- [ ] 4.2 [Codex + User] Reconcile remaining C11/O03 review with the checklist, record limitations and validate OpenSpec strictly. Retain the completed manual checklist as dated evidence for the tested revision/device; do not delete it or maintain it as a second active task list after acceptance.
- [ ] 4.3 [User] Explicitly accept or defer delivery from the evidence. Archival, commit and push stay separate, explicitly authorized actions.


## Current status — 2026-10-02

Manual browsing/recovery and bounded Home/Detail accessibility are validated by the user; QC04/QC06 remain not reproduced. Current delivery review uses debug. Earlier release builds/smokes remain dated historical evidence, not final release-runtime acceptance.

Quality CI passed for `9fdb7fb`: [run 37039977135](https://github.com/asensiodev/RickAndMortyCharacters/actions/runs/37039977135). The emulator CI job was removed after failed trials. Task 2.2 remains open because no complete final physical-device instrumented suite has passed; compiled or aborted semantic tests are not successful executions.

Publication is authorized and completed. Tasks 4.2/4.3 track formal closure/archival decisions separately from the completed manual app review; no new acceptance or archival is inferred from this documentation update.
