# Rick And Morty Characters

An Android app for exploring Rick and Morty characters, searching by name, filtering by status and viewing character details.

**Status:** Android foundation implemented and validated; final review of [C01](openspec/changes/android-foundation/proposal.md) is pending. The current app is a minimal Compose shell; character screens remain planned.

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

Presentation follows unidirectional data flow with ViewModel/StateFlow and explicit actions. ViewModels consume repository interfaces directly; use cases are introduced where business logic warrants them. The proposed stack includes Hilt, Retrofit/OkHttp, Coil 3, unit and instrumented tests, static analysis and GitHub Actions. The foundation toolchain and setup are documented below; the remaining libraries and checks land in their corresponding changes.

## Development setup

Open this repository root in Android Studio and sync Gradle. Use JDK 17 for Gradle and install Android SDK Platform 37.0, Build Tools 36.0.0 and Platform Tools. Configure the SDK through Android Studio, `ANDROID_HOME` or an untracked `local.properties` containing `sdk.dir=/path/to/android-sdk`.

| Foundation tool | Pinned version |
|---|---|
| Gradle wrapper | 9.4.1; distribution checksum pinned |
| Android Gradle Plugin | 9.2.1 |
| Kotlin / Compose compiler plugin | 2.4.20 |
| Compose BOM | 2026.09.00 |
| Activity Compose | 1.13.0 |
| Java / JVM target | 17 |
| Android compile / target / minimum SDK | 37.0 / 36 / 26 |

Plugin/library versions and SDK levels live in [the version catalogue](gradle/libs.versions.toml). AGP provides Kotlin support in Android modules; the domain module applies Kotlin/JVM.

With JDK 17 selected, run from the repository root:

```sh
./gradlew :app:assembleDebug :domain:characters:build
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Run the `app` configuration in Android Studio, or install and launch on a connected device:

```sh
adb install --no-streaming -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n com.asensiodev.rickandmortycharacters/.MainActivity
```

The shell displays the resource-based application name in a native dark Material 3 surface. The final theme and character UI follow in later changes. Debug assembly, a source-only build copy, module dependencies and startup/portrait behavior on a Pixel 9a emulator (API 37) have been checked; see [C01 evidence](openspec/changes/android-foundation/design.md#assistance-and-validation-record). Formatting/static-analysis gates, hooks and CI are scheduled in C02. Behavioral tests begin with the capabilities they verify.

## Documentation

| Document | Purpose |
|---|---|
| [PRD](docs/PRD.md) | Product scope, priorities and acceptance |
| [UI/UX Definition](docs/UI_UX.md) | Screen content, components, interactions and reviewed visual references |
| [ARCHITECTURE](docs/ARCHITECTURE.md) | Module graph, state/data contracts and technical decisions |
| [DEVELOPMENT](docs/DEVELOPMENT.md) | Toolchain, design workflow, tests, CI, TDD and AI assistance |

OpenSpec holds each prepared change's requirements, tasks and evidence. [BACKLOG](docs/BACKLOG.md) preserves the remaining ordered queue and links to migrated changes; it will be retired when migration is complete. Review one bounded change before implementing it and advancing to the next.

Implementation claims, screenshots and setup instructions are added with verified changes.

Data source: [The Rick and Morty API](https://rickandmortyapi.com/documentation).
