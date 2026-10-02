## ADDED Requirements

### Requirement: Reproducible build configuration

The project SHALL include a Gradle wrapper, a version catalogue with pinned compatible versions, and documented JDK/Android SDK prerequisites. It SHALL assemble the debug application from a clean checkout using the documented command without credentials or AI tooling.

#### Scenario: Fresh checkout builds

- **WHEN** a developer installs the documented prerequisites, configures the local Android SDK and runs the documented debug assembly command
- **THEN** Gradle produces an installable debug APK without private repositories, signing secrets or agent services

### Requirement: Six-module dependency boundaries

The project SHALL contain `:app`, `:feature:home`, `:feature:details`, `:domain:characters`, `:data:characters` and `:core:designsystem`. Project dependency declarations SHALL follow [ARCHITECTURE](../../../../../../README.md#architecture).

#### Scenario: Independent feature compilation

- **WHEN** the home and details modules are compiled
- **THEN** each compiles without depending on the other feature or on the data implementation

#### Scenario: Pure domain build

- **WHEN** the domain module is built and its dependency report is inspected
- **THEN** it uses the Kotlin/JVM toolchain without Android, Compose, Paging or HTTP-client dependencies

### Requirement: Launchable native shell

The application SHALL launch into a minimal native Compose surface, respect Android system safe areas and expose the English resource-based application name “Rick And Morty Characters”. Phone configuration SHALL target portrait orientation for this iteration.

#### Scenario: Device startup

- **WHEN** the debug APK is installed and launched on a supported phone or emulator
- **THEN** the application opens its Compose surface without a crash and respects system insets

#### Scenario: Portrait phone configuration

- **WHEN** a phone honoring the application's requested orientation launches or rotates the shell
- **THEN** the activity remains in portrait orientation

### Requirement: Evidence-backed setup instructions

The setup documentation SHALL record the chosen toolchain, actual reproducible commands and observed validation results. Unexecuted device or build checks SHALL remain explicitly pending.

#### Scenario: Foundation handoff

- **WHEN** the foundation is submitted for review
- **THEN** a developer can find its prerequisites and commands in README and its executed checks and remaining limitations in the change record
