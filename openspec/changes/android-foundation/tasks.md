## 1. Toolchain and build

- [x] 1.1 Inspect the available JDK/SDK, verify official compatibility guidance and record the pinned toolchain, SDK levels, JVM targets and application namespace.
- [x] 1.2 Create the Gradle wrapper with checksum, root settings/build files and version catalogue; keep machine-specific SDK paths untracked.
- [x] 1.3 Configure the six modules and selected project dependencies, with Kotlin/JVM-only domain and Compose enabled only in UI modules.

## 2. Runnable shell

- [x] 2.1 Add the app manifest, English application-name resource and minimal activity/Compose surface with system insets and portrait phone configuration.
- [x] 2.2 Verify module compilation and inspect dependency reports for feature/data separation and domain purity; record the commands and findings.
- [x] 2.3 Assemble the debug APK and install/launch it on a supported phone or emulator; check startup, insets and portrait behavior. Record any unavailable runtime check as pending.

## 3. Documentation and review

- [x] 3.1 Update README with actual prerequisites and verified setup/build commands; append observed checks and AI assistance to this change's design record.
- [ ] 3.2 Review the complete foundation diff and acceptance scenarios with the human reviewer before preparing C02; archive only after acceptance.
