# Development process

Development combines specification-driven development (SDD), behavior-focused TDD and AI assistance. The human author owns product scope, architecture and acceptance. [README](../README.md) covers setup and executable checks; [Delivery scope](../README.md#delivery-scope) and [Architecture](../README.md#architecture) define scope and technical decisions.

## Workflow and human checkpoints

1. **Select and refine:** the author chooses the feature, methodology and architectural tradeoffs. OpenSpec captures a bounded proposal, design, observable scenarios and tasks; the author refines them and agrees the test boundaries before implementation.
2. **Implement with TDD:** for behavior changes, observe a failing test at the agreed boundary, implement the smallest passing solution and continue one behavior at a time. Refactor with passing tests. Layout/documentation changes use appropriate review rather than fabricated RED phases; compilation or environment failures are not behavioral RED.
3. **Review:** the author reviews each implementation against the specification and intended experience. Source inspection and API checks challenge unsupported assumptions; unnecessary abstractions can be rejected or simplified.
4. **Verify:** run affected checks, then the completion gate and release assembly. The author exercises real interactions, layout and recovery in the app. Record commands, device/conditions, results and remaining limitations with the change.
5. **Accept:** human review determines whether the increment is complete. Passing tests, accepted implementation and final delivery are distinct states. Archive accepted implementation changes through OpenSpec; commit/push require explicit authorization.

OpenSpec preserves specifications and evidence in [changes](../openspec/changes/) and accepted [capability specs](../openspec/specs/). Documentation-only delivery edits can be reviewed directly without creating a new spec.

## AI toolchain

Codex assists with planning, code, tests, investigation and documentation. The author retains ownership through the checkpoints above, including hands-on validation. Codebase Memory supports symbol/dependency discovery; coverage checks and direct source reads qualify its results. AI output is reviewed against requirements and observed behavior; generated claims are not verification evidence.

These controls help identify hallucinated assumptions, incorrect implementations and overengineering. They do not guarantee defect-free code. The Android app builds and runs without the AI tools or skills installed.

### Skills used

| Guidance | Purpose |
|---|---|
| `ponytail` | Smallest justified solution; existing platform/library capabilities before new abstractions |
| [Matt Pocock's skills](https://github.com/mattpocock/skills): `tdd`, `codebase-design` | Observable behavior tests and focused module interfaces |
| [Chris Banes' skills](https://github.com/chrisbanes/skills): Compose animation, component, state/effect and UI-testing guidance | Native APIs, ownership and screen-level verification |
| `codebase-memory` | Graph discovery with source/coverage verification |
| `gradle-run` | Managed build execution and bounded verification summaries |
| `grounded-writing` | Documentation grounded in decisions and evidence |

## UI design

The author defined screen content, states and interaction constraints, then used AI-assisted metaprompting to prepare Google Stitch prompts. Iteration in Stitch produced the selected Home/Detail designs, downloaded as PNG/HTML exports. Those reviewed designs guide Compose appearance; the specification owns behavior. Native tokens, layouts and controls incorporate the documented handoff corrections, followed by preview and hands-on review. [Stitch exports](design/stitch/) preserve the visual references; [README](../README.md#app-screenshots) shows actual app captures.

### Native design handoff

Generated designs are visual references, not production behavior contracts. The reviewed corrections were:

- The outdated Home-loading export includes a counter/tune icon. Retain skeleton geometry, reuse persistent search/chips and follow the implemented counter visibility policy.
- Detail loading includes an unsupported Share glyph. Omit it and preserve Back and portrait bounds across states.
- The selected Alive chip needs one consistent readable treatment across states. Native theme roles and touch targets require their own verification; generated web measurements are not verified dp/sp values.

Footer padding lets final cards and Retry clear the floating counter. App behavior is defined by the accepted OpenSpec scenarios; actual screenshots and device checks complement the mockups.
