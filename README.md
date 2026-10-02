<p align="center">
  <img src="app/src/main/res/drawable-nodpi/portal_gun.png" width="112" alt="Rick And Morty Characters portal gun app icon" />
</p>

<h1 align="center">Rick And Morty Characters</h1>

<p align="center">
  Browse characters, search by name, filter by status,<br />
  and explore character details and episode appearances.
</p>

<p align="center">
  <a href="https://github.com/asensiodev/RickAndMortyCharacters/actions/workflows/quality.yml"><img src="https://github.com/asensiodev/RickAndMortyCharacters/actions/workflows/quality.yml/badge.svg" alt="Quality workflow status" /></a>
  <a href="app/build.gradle.kts"><img src="https://img.shields.io/badge/App-0.1.0-2F3437" alt="App version 0.1.0" /></a>
  <img src="https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&amp;logoColor=white" alt="Android API 26 and above" />
  <a href="gradle/libs.versions.toml"><img src="https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&amp;logoColor=white" alt="Kotlin 2.4.20" /></a>
  <a href="gradle/libs.versions.toml"><img src="https://img.shields.io/badge/Compose%20BOM-2026.09.00-4285F4?logo=jetpackcompose&amp;logoColor=white" alt="Jetpack Compose BOM 2026.09.00" /></a>
</p>

## App screenshots

<p align="center">
  <img src="docs/screenshots/home-2026-10-02.png" alt="Home with search, status filters, character cards and loaded counter" width="280" />
  <img src="docs/screenshots/detail-2026-10-02.png" alt="Rick Sanchez detail with matching Alive and Human badges, portrait, character facts and episode cards" width="280" />
</p>

[Stitch references](docs/design/stitch/) document the original visual design.

## Development setup

**Build requirements:** JDK 21, Android SDK Platform 37.0 and Build Tools 36.0.0.

```sh
./gradlew :app:assembleDebug
./gradlew qualityCheck
```

## Architecture

Material 3 and Navigation 3 provide the UI/navigation; ViewModel, StateFlow and Coroutines coordinate state. Hilt composes dependencies; Retrofit, OkHttp, kotlinx.serialization, Paging and Coil handle remote data and images. ViewModels consume repositories directly; there are no use-case classes.

| Module | Responsibility |
|---|---|
| `:app` | Entry point, Hilt composition, navigation and shared image loader |
| `:feature:home` | Catalogue UI, query state and Paging integration |
| `:feature:details` | Character/episode UI and request state |
| `:domain:characters` | Pure Kotlin models, repository contracts and results |
| `:data:characters` | API/DTOs, mapping, repositories and HTTP cache |
| `:core:designsystem` | Theme, tokens and generic Compose primitives |
| `:core:testing` | Shared test support; no production dependency |

```mermaid
flowchart TB
    App[":app"]

    subgraph Features["Features"]
        direction LR
        Home[":feature:home"]
        Details[":feature:details"]
    end

    subgraph Shared["Shared modules"]
        Data[":data:characters"]
        Domain[":domain:characters"]
        UI[":core:designsystem"]
    end

    Testing[":core:testing"]

    App --> Home
    App --> Details
    App --> Data
    App --> UI
    Home --> Domain
    Details --> Domain
    Home --> UI
    Details --> UI
    Data --> Domain
    Home -. testImplementation .-> Testing
    Details -. testImplementation .-> Testing

    Home ~~~ Data
    Details ~~~ UI
```

Solid arrows are production project dependencies; dotted arrows are test dependencies. Home and Detail do not depend on each other or on data implementation. Domain has no Android, Compose, Retrofit or Paging dependency.

Coil owns the shared image cache; a separate bounded OkHttp cache reuses eligible API responses according to HTTP headers. Portrait failures remain local, and neither cache guarantees offline browsing.

## Quality checks

`qualityCheck` runs JVM tests, Konsist checks for internal implementation types and read-only exposed state, ktlint, Detekt, Android Lint, Paparazzi baseline verification and debug assembly. Repository tests use deterministic HTTP fixtures; ViewModel tests use controlled responses and scheduling. Compose tests exercise screen states and production navigation. The configured [GitHub Actions workflow](.github/workflows/quality.yml) runs the aggregate gate on pushes and pull requests. Check reports are uploaded as artifacts. Instrumented suites run locally on a connected emulator or device. Instrumentation includes Home large-text and control-reachability regressions; it does not validate TalkBack or certify accessibility.

Run instrumented screen/navigation tests with a connected API 37 emulator or device:

```sh
./gradlew :feature:home:connectedDebugAndroidTest :feature:details:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

### Screenshot regressions

Paparazzi checks bounded visual contracts using controlled fixtures and reviewed PNG baselines:

```sh
./gradlew :feature:home:verifyPaparazziDebug :feature:details:verifyPaparazziDebug
```

Screenshot tests complement interaction tests and hands-on app review.

## Development approach

Built with OpenSpec, behavior-focused TDD and AI assistance, with human review and hands-on app validation. [Development process](docs/DEVELOPMENT_PROCESS.md) documents the workflow, toolchain and UI design process.

## Delivery scope

The app targets portrait phones with an English interface and the selected dark appearance. The browsing flow and bounded accessibility support were manually validated in debug on a physical Pixel 9a.

| Status | Scope |
|---|---|
| Implemented | Paginated grid/counter, combined name/status search, character facts and episode cards, retained browsing context on Back |
| Implemented | Local image feedback, loading/empty/error states, contextual Retry, image/HTTP caching, automated checks and reproducible setup |
| Implemented and manually validated | Bounded accessibility support in Home and Detail |
| Outside this delivery | Process-death restoration of search/filter state and loaded results; a complete accessibility audit across both screens |
| Outside this delivery | Light/system-theme variants, connectivity snackbar and shared-image navigation |
| Outside this delivery | Guaranteed offline catalogue, accounts, onboarding, favourites and additional destinations |

Accessibility support in Home and Detail includes labelled actions, screen-reader headings and grouped content, status/error announcements, and layouts that accommodate larger text.

[OpenSpec](openspec/) preserves change specifications and evidence as optional deeper reading.

Data and character artwork: [The Rick and Morty API](https://rickandmortyapi.com/documentation).
