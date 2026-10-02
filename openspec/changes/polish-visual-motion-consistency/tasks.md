## 1. Readiness and integrated review

- [x] 1.1 Review C11 scope and observable test boundaries before implementation.
- [ ] 1.2 Capture/review the Home/Detail state matrix against selected references; record observed inconsistencies, owners and bounded corrections without adding optional features.

## 2. Visual corrections

- [x] 2.1 Correct observed token/component inconsistencies and record before/after evidence; use behavioral RED → GREEN only where behavior changes.
- [ ] 2.2 Verify the accepted phone layout at default font size, reachable controls, system/keyboard insets and final-card/footer clearance; add focused regressions for corrections.
- Accessibility adaptations/previews and contrast/target audits are deferred to O02 by the user; preserve existing basics.

## 3. Motion continuity

- [x] 3.1 Review and record keep/change decisions for skeleton feedback, image fades, chip feedback, skeleton-to-content, result replacement, detail parallax and forward/back transitions; implement only justified native corrections.
- [x] 3.2 Verify fast/slow/failed images, rapid query changes, append, repeated navigation and both Back paths preserve current identity, persistent controls and accepted browsing behavior.
- [ ] 3.3 Verify native motion and effect disposal/off-screen work on API 37; custom disabled-motion adaptation is deferred to O02 by the user.
- [x] 3.4 Review image-heavy scrolling with recorded device/viewport/cache conditions; distinguish observations from measured performance claims.

## 4. Completion and acceptance

- [x] 4.1 Run affected tests, the completion quality gate and release assembly; record exact executed commands/results and actual regression evidence.
- [ ] 4.2 Complete native visual/motion matrix, record captures and remaining manual limitations, and update UI_UX only from verified behavior.
- [ ] 4.3 Validate OpenSpec strictly and obtain human acceptance before archival and C12; commit/push only with separate explicit authorization.

## User-approved motion simplification — 2026-10-02

- [x] Withdraw the custom motion-scale adapter and its dedicated tests; retain native APIs and bounded parallax.
- [x] Verify remaining Home and Detail screen tests on API 37 (34 passing).
- [x] Record the full gate's unrelated Detekt blocker without modifying the concurrent portrait-recovery work.

## Publication acceptance — 2026-10-02

- [x] Obtain explicit user acceptance to commit and push the implemented C11/polish corrections together with portrait recovery. Remaining integrated-review tasks and archival stay separate.
