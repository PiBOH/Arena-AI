# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-07-15

### Added
- Rich, custom-styled native Android download notifications that track download progress in real-time, including file name, progress percentage, remaining time, and transfer speed.
- Fully automated "Download completed" and "Download failed" native alerts and localized Toasts.
- Declared `DOWNLOAD_WITHOUT_NOTIFICATION` system permission to allow cleanly hiding default system alerts in favor of the custom notifications.
- Incremented build configuration following Semantic Versioning rules to `versionCode = 5` and `versionName = "1.1.0"`.

### Fixed
- Investigated and verified chromium internal `simple_file_enumerator` logs (`opendir Code Cache/js: No such file or directory`); confirmed they are completely benign startup warnings that occur naturally on fresh WebView cache initializations and have zero impact on the application's runtime stability.

---

## [1.0.3] - 2026-07-15

### Added
- Native support for LMArena (`lmarena.ai`) ecosystem domain inside the WebView alongside existing `arena.ai` and `lmsys.org` domains.
- Beautiful, interactive community buttons in the Application Settings to instantly open a bug report/feature request via GitHub Issues (`https://github.com/PiBOH/Arena-AI/issues/new/choose`) and review code changes via GitHub Pull Requests (`https://github.com/PiBOH/Arena-AI/pulls`).
- Upgraded the standard `.github/ISSUE_TEMPLATE` bug reporting and feature proposal workflows into modern, interactive YAML Form schemas (`bug_report.yml` and `feature_request.yml`) for a significantly enhanced user contribution experience.
- Integrated the official `logo.png` branding asset directly at the project root by pulling it from the main branch of the repository (`https://github.com/PiBOH/Arena-AI/blob/main/logo.png`).

### Changed
- Disabled the `SwipeRefreshLayout` drag-to-refresh vertical pull gesture entirely, and configured `overScrollMode = View.OVER_SCROLL_NEVER` directly on the `WebView`. This completely prevents accidental page reloads or overscroll stretches that would cause active chat sessions and conversational history inside Chatbot Arena to be lost while scrolling.
- Pre-emptively created the internal WebView WebAssembly Cache subdirectories (`WebView/Default/HTTP Cache/Code Cache/wasm`) on initial app launch to completely eliminate Chromium WebAssembly directory listing error logs.
- Incrementally updated the Android Gradle Build Configuration (`versionCode = 4`, `versionName = "1.0.3"`).

### Fixed
- Resolved the login hang after password entry by allowing full authentication domains (`google.com`, `google.it`, `huggingface.co`, `github.com`, `apple.com`, `recaptcha`, `gstatic`) to run natively inside the WebView without triggering system browser overrides.
- Stabilized session persistence for OAuth sign-ins by explicitly flushing cookies back to disk on `onPageFinished` using `CookieManager.getInstance().flush()`.
- Enabled `javaScriptCanOpenWindowsAutomatically` and allowed mixed content mode (`MIXED_CONTENT_ALWAYS_ALLOW`) to prevent redirect flows and security checks from hanging or dropping.

---

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

[Unreleased]: https://github.com/PiBOH/Arena-AI/compare/v1.0.3...HEAD
[1.0.3]: https://github.com/PiBOH/Arena-AI/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/PiBOH/Arena-AI/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/PiBOH/Arena-AI/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/PiBOH/Arena-AI/releases/tag/v1.0.0
