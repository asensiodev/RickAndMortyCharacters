## ADDED Requirements

### Requirement: Dismiss search keyboard on manual result scrolling

Home SHALL request keyboard dismissal when the user begins dragging its result grid. Dismissal SHALL clear input focus while preserving input text and the selected status, without submitting or changing the query. Programmatic scrolling and automatic result-generation resets SHALL NOT request keyboard dismissal. The user SHALL be able to reopen the keyboard by tapping the input.

#### Scenario: The user browses after typing
- **WHEN** the user starts dragging the result grid while editing a name
- **THEN** the keyboard hides, input focus clears and the current input and status remain unchanged

#### Scenario: The list scrolls automatically
- **WHEN** programmatic scrolling or a new query generation resets the grid
- **THEN** it does not dismiss the keyboard or remove input focus

#### Scenario: The user resumes editing
- **WHEN** the user taps the search input after dismissing the keyboard by scrolling
- **THEN** the keyboard can reopen with the retained search text


### Requirement: End editing when the software keyboard closes

Home SHALL clear input focus when the visible software keyboard closes, including dismissal through system Back. It SHALL retain search text and the selected status, and SHALL allow tapping the input to resume editing. Initial hidden keyboard state SHALL NOT clear focus or interfere with hardware-keyboard editing.

#### Scenario: The user closes the keyboard
- **WHEN** the user dismisses the visible search keyboard with system Back
- **THEN** the search field loses focus and its cursor disappears without changing search text or status

#### Scenario: The user resumes editing
- **WHEN** the user taps the retained search input again
- **THEN** the input regains focus and the software keyboard can reopen
