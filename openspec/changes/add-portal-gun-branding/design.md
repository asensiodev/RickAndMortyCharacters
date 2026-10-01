## Scope and decision

The user selected the generated portal gun after comparing launcher-size proposals. The chosen artwork is preserved; imagegen's built-in background extraction prepares a transparent foreground. Resources own the background and safe padding. Use platform adaptive icons from the minimum API 26, native SplashScreen theme attributes from API 31, and a static starting-window drawable below API 31. No splash Activity, navigation route, timer or library is needed.

Background: existing app charcoal #121316. The foreground is inset to stay within Android's central 66/108 safe area. A monochrome layer uses the foreground alpha for supported themed launchers.

## Validation boundaries

This is asset/theme configuration: validate packaged resources and the manifest, run the existing quality gate/release build, and inspect launcher and cold startup on API 37. Do not invent behavioral RED tests for static resource definitions. API 26–30 starting-window appearance and other OEM masks require separate device review if no corresponding emulator is available.

## Sources

- [Android adaptive icon requirements](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive)
- [Native splash theme attributes](https://developer.android.com/develop/ui/views/launch/splash-screen)

## Asset provenance

Generated through Codex's built-in imagegen tool. Extraction prompt: remove only the dark background; preserve the selected gun silhouette, diagonal orientation, colors, shading, green vial and broken blue ring; return true transparent alpha without redesign, text or a background plate.

## Verification record

- Asset: `app/src/main/res/drawable-nodpi/portal_gun.png`, 512×512 RGBA, 191 KiB. Built-in imagegen extraction followed by native `sips` downsampling; no redesign. Foreground uses 15% inset on each side around the transparent artwork.
- Managed Gradle workflow `2902c559de397ae834fa717bc0a561b8`: `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow 2902c559de397ae834fa717bc0a561b8 --scope broad --question '¿Pasan qualityCheck, release e instalación con el nuevo icono y splash?' -- ./gradlew qualityCheck :app:assembleRelease :app:installDebug` passed, exit 0, 18.047 s. Only warning: multiple Kotlin daemon sessions. Finish removed only wrapper-owned logs.
- `openspec validate add-portal-gun-branding --strict --no-interactive` and `git diff --check` passed. `aapt2 dump resources` and manifest inspection confirm packaged adaptive icon, #121316 background and API 31 splash attributes.
- Pixel 9a emulator, API 37, 420 dpi: native cold launch succeeded (757 ms total Activity launch time; this is an observation, not a performance claim). Recorded frames show the complete gun/ring on charcoal, then existing Home content. Bringing the existing task forward completes without launching another Activity (18 ms observed wait).
- [Actual launcher screenshot](../../../docs/design/branding/launcher-api37.png) shows the complete mark with the circular mask. Pixel's suggested dock icon adds its own pale-blue suggestion ring; the app drawer uses the intended charcoal background.
- [Actual native splash screenshot](../../../docs/design/branding/native-splash-api37.png) shows the selected identity without clipping. Initial recording was interrupted by a concurrent emulator task uninstalling the package; reinstalled the already verified APK with `adb install -r` and repeated native review successfully.
- API 26–30 starting-window rendering, other OEM masks and user-enabled themed icon appearance have not been exercised on devices. XML compiles at minimum API 26; the foreground follows the documented safe area. No new instrumentation suite was required for this resource-only change.

The user accepted the result on 2026-10-01 and explicitly authorized commit and push. Archival has not been performed.
