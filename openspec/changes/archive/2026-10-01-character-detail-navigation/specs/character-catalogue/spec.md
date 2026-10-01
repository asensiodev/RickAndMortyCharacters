## MODIFIED Requirements

### Requirement: Screen selection and independent portrait feedback

Home SHALL expose character selection through an ID callback connected by app navigation to the selected Detail destination. Image requests SHALL use the app-owned shared loader; pending, failed or absent portraits SHALL NOT suppress real metadata or prevent selection. Home SHALL NOT own the detail implementation or its navigation types.

#### Scenario: Select a character with a failed portrait
- **WHEN** a real Home card has a failed or absent portrait and the user selects it
- **THEN** its metadata remains available and app navigation receives its ID to open Detail

#### Scenario: Portrait loading completes
- **WHEN** a controlled image request moves from pending to success within Home
- **THEN** its local loading feedback disappears without replacing character data or reloading the catalogue
