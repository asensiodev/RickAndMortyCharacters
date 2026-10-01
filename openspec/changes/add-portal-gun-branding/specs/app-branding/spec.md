## ADDED Requirements

### Requirement: Portal gun launcher identity
The app SHALL use the selected blue portal gun, green vial and broken blue ring on a charcoal background as its adaptive launcher icon.

#### Scenario: Launcher masks the icon
- **WHEN** a launcher displays the app with a circular or rounded-square mask
- **THEN** the complete gun and ring remain visible within the mask

### Requirement: Native branded startup
The app SHALL show the selected identity on a charcoal native startup surface without an artificial delay or an additional destination.

#### Scenario: Cold startup
- **WHEN** the user launches the stopped app
- **THEN** its native startup surface displays the portal gun before the existing app content

#### Scenario: Returning to existing content
- **WHEN** the user resumes an already running activity
- **THEN** branding introduces no additional screen or timer
