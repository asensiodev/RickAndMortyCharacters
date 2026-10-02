## ADDED Requirements

### Requirement: Identified and bounded delivery candidate

Delivery verification SHALL identify the source revision, relevant working-tree changes, build artifact/variant and execution environment. C12 SHALL verify the accepted product scope without introducing optional features or silently treating earlier results as evidence for changed behavior.

#### Scenario: Verify a changed candidate
- **WHEN** a correction changes behavior or a protected visual contract after a check
- **THEN** the affected checks are repeated and linked to the updated candidate before their result is accepted

### Requirement: Human-owned physical-device review

The project SHALL provide an editable manual QA checklist with steps, expected outcomes, checkboxes and observation fields. The user SHALL execute normal-flow review on at least one physical Android device and own its reported results. Tasks SHALL reference this document without duplicating its individual cases. Unexecuted, failed, blocked and not-reproduced cases SHALL remain distinguishable from successful checks.

#### Scenario: Complete a physical-device journey
- **WHEN** the user exercises browsing, search/status, pagination, Detail/episodes, Back and visual/motion checks
- **THEN** the checklist records observed outcomes with device/API and build information, retaining any failures or limitations for acceptance review

#### Scenario: A conditional error cannot be reproduced
- **WHEN** caching or live API timing prevents a manual image, request, append or episode failure from being induced
- **THEN** that case remains explicitly not reproduced, and controlled automated coverage is recorded separately rather than presented as manual success

### Requirement: Automated and runtime verification evidence

Delivery evidence SHALL record the documented quality gate, Paparazzi verification, release assembly and the implemented instrumented suites for the candidate. Reused outputs and empty tasks SHALL be disclosed. Release runtime review SHALL identify an installable artifact and SHALL NOT be inferred from assembly or debug runtime alone. The earlier physical-device instrumentation failure SHALL be rechecked or remain an explicit limitation for human disposition.

#### Scenario: Verify the accepted automation boundaries
- **WHEN** local quality, release and connected test checks are evaluated
- **THEN** actual commands, targets, counts and outcomes are recorded without inferring instrumentation from qualityCheck or attributing an unknown runner failure to normal app use

#### Scenario: Only the debug build was exercised
- **WHEN** release assembly passes but runtime review uses a debug APK
- **THEN** release runtime remains unverified and is presented explicitly for acceptance rather than marked passed

### Requirement: CI evidence belongs to the candidate

The existing Quality workflow SHALL be verified against the published candidate commit, with a run link and SHA recorded. A written workflow or a successful run for a different revision SHALL NOT count as final CI success. Golden images SHALL NOT be automatically recorded or accepted to suppress a comparison failure.

#### Scenario: Verify remote quality
- **WHEN** the authorized published candidate completes the Quality workflow
- **THEN** its run URL, commit and result are recorded, with instrumentation and release/runtime results identified as separate local evidence

### Requirement: Coordinated documentation and human acceptance

C12 SHALL check the user's independently edited delivery documentation for consistency with verified behavior and remaining limitations. It SHALL preserve their concurrent work and reference actual app captures appropriately. Final acceptance SHALL be explicit, with remaining C11/O03 review reconciled from evidence; archival, commit and push SHALL require their separate authorizations.

#### Scenario: Read Detail with accessibility services
- **WHEN** the character detail is displayed
- **THEN** the name and section titles expose heading semantics, status/species form a group, each fact label/value forms a group and each episode code/title/date forms a group
- **AND** Back and Retry remain independent actions, decorative portrait feedback is hidden from accessibility traversal and error/empty feedback uses polite announcements

#### Scenario: Close the showcase
- **WHEN** candidate evidence and the human-owned checklist are reviewed
- **THEN** failures and unexecuted checks are resolved or explicitly dispositioned, documentation reflects the accepted result and the user decides acceptance before archival
