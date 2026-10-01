## ADDED Requirements

### Requirement: App-owned Home and Detail navigation

The app SHALL own exactly the Home and Detail destinations and navigate to Detail using the selected ID. Features SHALL expose IDs/callbacks rather than depend on each other or navigation implementation types. Missing/error detail outcomes SHALL stay inside Detail. A rapid repeated selection from outgoing Home SHALL NOT stack duplicate detail entries.

#### Scenario: A Home character is selected
- **WHEN** the user selects a real character card
- **THEN** app navigation opens Detail for that supplied ID

#### Scenario: Selection repeats before Home becomes inactive
- **WHEN** the same outgoing Home interaction submits selection repeatedly
- **THEN** only one Detail entry is opened

### Requirement: Entry-scoped ViewModels and retained Home context

Navigation SHALL scope ViewModels to their respective entries and preserve Home's loaded content and saveable lazy-grid position while Detail is open. Normal return SHALL NOT trigger another first-page request solely because Home is recomposed. Removing Detail SHALL clear its request owner; opening another character SHALL load that identity independently.

#### Scenario: Return to a scrolled Home
- **WHEN** the user opens Detail from a scrolled Home and returns
- **THEN** Home retains its loaded first page and browsing position without a navigation-induced reload

#### Scenario: A detail entry is removed and another opened
- **WHEN** Detail is popped and a different character is selected
- **THEN** the former owner is cleared and the new entry requests the new ID

### Requirement: Consistent screen and system Back

The detail Back control and system Back SHALL return to Home through the same app-owned navigation behavior. Detail callbacks SHALL NOT remove the root Home entry. Native destination transitions SHALL respect system motion settings. Error, loading and missing-character states SHALL NOT create additional routes.

#### Scenario: Back from any detail state
- **WHEN** the user invokes screen Back or system Back from Loading, Content, Error or NotFound
- **THEN** the detail entry is removed and Home is shown

### Requirement: Deterministic production journey

A test SHALL exercise the production Home → Detail → Home wiring with controlled repository data, selected ID and retained browsing state. Tests SHALL use real product destinations and SHALL NOT add production fakes or a custom runtime gallery.

#### Scenario: The navigation journey runs on API 37
- **WHEN** instrumented verification selects a character, loads its detail and returns
- **THEN** it observes the selected identity and preserved Home content/scroll without an uncontrolled API or image request
