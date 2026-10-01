## Context and scope

C03 implements the card and image contract from `docs/UI_UX.md`, using the reviewed Home content export. C04 introduces the real catalogue. The existing six-module graph remains unchanged.

## Decisions

- `CharacterCard` accepts immutable presentation metadata, an `ImageLoader`, a selection callback and a root `Modifier`. Compose behavior tests will exercise selection, metadata and local image feedback through the real Home screen in C04. Visual card/skeleton regression checks remain later work. No ViewModel or domain placeholder is needed yet.
- `CharacterCardUiModel` and its status enum belong in Home. Their conversion from domain belongs in C04; the design system has no character dependency.
- Generic theme, shapes/spacing and the loading placeholder belong in the design system. Keep palette, typography and shape definitions in focused files; consuming components use named tokens rather than raw `dp`/`sp` values. `CharacterCardTokens` owns the card/skeleton geometry, name layout thresholds and status palette. Pulse timing/opacity remain private constants next to the loading placeholder. Character-specific layout and status colors stay in Home. Promote a generic badge only if detail creates real reuse.
- Coil 3.6.3 is pinned from its official documentation. The app implements `SingletonImageLoader.Factory`; Home uses `coil-compose-core` and accepts the configured loader. Coil owns decoding and cache behavior. Use an explicit 20% memory limit and 32 MiB image disk limit in `cacheDir/character_images`, distinct from the future API cache. Do not add an independent bitmap store.
- `rememberAsyncImagePainter` uses an explicit `rememberConstraintsSizeResolver` attached to the square image layout with Crop. The component observes Coil’s state directly, without copying it into separate mutable state or adding per-card subcomposition. Loading/failure stays in the portrait region. Native crossfade and Compose loading motion respect system animation scaling; previews use static loading shapes.
- Keep the reviewed dark surface/blue accent palette and readable foreground pairs. Typography uses the platform sans-serif family and Material 3 roles rather than a remotely fetched font. Light theme remains a later Should.
- Compile and target Android 17 (API 37), with minimum API 26 unchanged. Validate the packaged SDK values and the current production entry point on API 37; screen/journey instrumentation remains with C04/C05.
- Keep only product runtime code and standard Android Studio previews. Configure instrumented Compose tests when the real screens are integrated: screen states, selection, Retry, search/filter interaction and navigation. Unit/repository TDD remains part of the corresponding feature changes. Screenshot regression coverage remains the later O03 optional change; no screenshot framework or instrumented runner is configured in C03. The JDK stays at 17.

## Validation and assistance

Codex prepared and implemented C03 after explicit authorization on 2026-09-30. Component-design, Compose state/animation/testing and TDD guidance informed the public seam and verification. Human acceptance was confirmed on 2026-10-01; archival follows acceptance. Screenshot regression tooling remains later optional work; no Paparazzi dependency is configured.

Official references: [Coil setup](https://coil-kt.github.io/coil/getting_started/), [Compose images](https://coil-kt.github.io/coil/compose/), [image loaders](https://coil-kt.github.io/coil/image_loaders/), [controlled testing](https://coil-kt.github.io/coil/testing/), [Compose testing](https://developer.android.com/develop/ui/compose/testing).


## Validation record

The initial implementation used observed RED → GREEN component slices for metadata/selection, portrait loading/failure and skeleton semantics. Its final managed API 36 run passed six tests with zero failures/skips, including absent URLs and a long-name/Unknown case at 200% font scale. This is historical evidence, not current test coverage: on 2026-10-01 the isolated tests and their dedicated instrumentation configuration were removed when the testing boundary moved to real screens in C04. Runtime component behavior was preserved.

On 2026-10-01, the temporary runtime review screen and its manifest/resources were removed. Standard Android Studio previews remain. C03 is not archived or accepted, and C04 has not started. The current production entry point is still the minimal themed shell.

Cleanup validation passed on 2026-10-01: `./gradlew qualityCheck :app:assembleRelease`, OpenSpec strict validation and all local Markdown link targets. The debug and release merged manifests contain no temporary review activity; `MainActivity` is the only app-owned activity, while debug retains Android Studio’s standard preview tooling; the corrected debug APK replaced the earlier installation on the API 37 emulator. The current source contains no instrumented UI tests; it does not claim integrated Home/Detail coverage. C04 will verify the real Home states, selection and independent image feedback with controlled data/images; C05 adds Detail and the navigation journey. Visual regression tooling remains O03.


Target SDK 37 validation passed on 2026-10-01. `./gradlew qualityCheck :app:assembleRelease` completed successfully. Packaged debug/release manifests declare target 37 and minimum 26; the installed debug package also reports those values. A cold start of `MainActivity` on the API 37 emulator succeeded, the current native shell rendered with system safe areas, and the process remained alive without AndroidRuntime errors during the smoke check.

The Android 17 [target-specific changes](https://developer.android.com/about/versions/17/behavior-changes-17) were reviewed against the current app code. It has no custom reflection into InputManager/MessageQueue, RemoteViews widgets, background activity launches or local-network features requiring a migration here. No production class change was necessary beyond the existing catalogue-based SDK configuration. This validation covers the current shell and build; full screen flows and compatible API 37 instrumentation remain C04/C05 work.

Human review accepted C03 on 2026-10-01 after the token cleanup. That refactor preserved dimensions, palette and behavior; format/static checks, affected-module Android lint and debug assembly passed, along with strict OpenSpec validation. The reviewer authorized committing C03 without pushing and preparing the next change.
