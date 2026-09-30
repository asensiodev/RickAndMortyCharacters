# Rick And Morty Characters

An Android app for exploring Rick and Morty characters, searching by name, filtering by status and viewing character details.

**Status:** product definition and technical design. Android implementation and OpenSpec initialization have not started.

## Planned experience

- Paginated character grid with images, names and status.
- Name search combined with All, Alive, Dead and Unknown filter chips, plus quick searches when no matches are found.
- Character details and navigation back to the browsing context.
- Loading, empty and error states, retries and coherent motion.
- English UI, Compose/Material 3, image caching and HTTP response caching.

## Design preview

Selected Stitch mockups for the planned native app. See [UI/UX Definition](docs/UI_UX.md) for states and implementation adjustments.

| Home | Character detail |
|---|---|
| ![Home design](docs/design/stitch/home/content/screen.png) | ![Character detail design](docs/design/stitch/details/content/screen.png) |

## Technical direction

Home and details have separate feature modules. They share pure character-domain contracts, data access and a design system; the app composes navigation and dependencies. The six-module graph is defined in [ARCHITECTURE](docs/ARCHITECTURE.md).

Presentation follows unidirectional data flow with ViewModel/StateFlow and explicit actions. ViewModels consume repository interfaces directly; use cases are introduced where business logic warrants them. The proposed stack includes Hilt, Retrofit/OkHttp, Coil 3, unit and instrumented tests, static analysis and GitHub Actions. Exact versions and build commands will be documented when the Android project is configured.

## Documentation

| Document | Purpose |
|---|---|
| [PRD](docs/PRD.md) | Product scope, priorities and acceptance |
| [UI/UX Definition](docs/UI_UX.md) | Screen content, components, interactions and reviewed visual references |
| [ARCHITECTURE](docs/ARCHITECTURE.md) | Module graph, state/data contracts and technical decisions |
| [DEVELOPMENT](docs/DEVELOPMENT.md) | Toolchain, design workflow, tests, CI, TDD and AI assistance |

OpenSpec will hold capability specifications and individual change tasks once initialized. Until then, [BACKLOG](docs/BACKLOG.md) preserves the ordered planning queue; it will be retired after migration. Each change's tasks and evidence have one authoritative location.

Implementation claims, screenshots and setup instructions are added with verified changes.

Data source: [The Rick and Morty API](https://rickandmortyapi.com/documentation).
