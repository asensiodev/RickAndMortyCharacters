## ADDED Requirements

### Requirement: Dismiss search keyboard on manual result scrolling

Home SHALL request keyboard dismissal when the user begins dragging its result grid. Dismissal SHALL preserve input text, selection, focus and the selected status, without submitting or changing the query. Programmatic scrolling and automatic result-generation resets SHALL NOT request keyboard dismissal. The user SHALL be able to reopen the keyboard by tapping the input.

#### Scenario: The user browses after typing
- **WHEN** the user starts dragging the result grid while editing a name
- **THEN** the keyboard hides and the current input and status remain unchanged

#### Scenario: The list scrolls automatically
- **WHEN** programmatic scrolling or a new query generation resets the grid
- **THEN** it does not dismiss the keyboard or remove input focus

#### Scenario: The user resumes editing
- **WHEN** the user taps the search input after dismissing the keyboard by scrolling
- **THEN** the keyboard can reopen with the retained search text
