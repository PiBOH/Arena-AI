## [1.0.2] - 2026-07-15

### Added
- Comprehensive local compilation documentation (`docs/APK-BUILD.md`) in both English 🇬🇧 and Italian 🇮🇹 covering detailed instructions on how to assemble release and debug builds (`assembleRelease`, `assembleDebug`).
- Robust bilingual issue templates under `.github/ISSUE_TEMPLATE/` (styled with intuitive emoji sets).
- Automated keystore check logic in `debugConfig` to automatically resolve local debug credentials when repo-level keystores are absent, preventing local developer compile failures.

### Changed
- Upgraded `androidx.fragment:fragment-ktx` to `1.8.2` to resolve the fatal `InvalidFragmentVersionForActivityResult` release-build compilation constraint.
- Configured specific, developer-friendly proguard and lint build rules to ensure strict compilation safety.
- Updated the Android Gradle Build Configuration (`versionCode = 3`, `versionName = "1.0.2"`).

### Fixed
- Solved a freezing issue where third-party authentication flow logins (Google, Hugging Face, GitHub, Apple) would hang or trigger external browser redirect escapes. All login sessions are now strictly contained inside the secure native WebView, maintaining persistent cookie scopes.

---

## [1.0.1] - 2026-07-14

### Added
- Custom localized Italian launcher configurations and app metadata resources.
- Deep integration of modern Jetpack Compose layouts designed around standard Material 3 color keys and surface typography.

### Changed
- Refactored native application package configurations and upgraded the target platform SDK levels to SDK 36.
- Aligned internal modular dependencies in accordance with Android Gradle Plugin (AGP) version parameters.

### Fixed
- Fixed layout alignment and navigation state overflows across multiple device configurations.

---

## [1.0.0] - 2026-07-12

### Added
- Initial release of **Arena AI** (native LMSYS Chatbot Arena Android wrapper).
- High-performance, full-viewport standard `WebView` configuration supporting rapid comparison and side-by-side benchmarking of prominent LLMs.
- Embedded local dark/light themes paired with consistent spacing grids (touch targets ≥ 48dp).
- Integrated hardware-accelerated media, DOM storage, and native file uploads.