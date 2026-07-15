# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.8] - 2026-07-15

### Added
- **Developer & Version Metadata Section**: Introduced a beautifully formatted, dynamic App Information section inside the Settings Bottom Sheet.
  - Automatically loads and displays the active application version dynamically.
  - Features the developer credit (**PiBOH**) styled as an interactive link that navigates directly to their personal portfolio (**https://piboh.github.io/**).

---

## [1.1.7] - 2026-07-15

### Fixed
- **Home Layout & Interaction Reset**: Reverted generalized CSS rules (such as `button:has(svg)`) and global container overrides (`overflow: visible`, `position: relative`) that caused elements on the main arena dashboard to align incorrectly ("storte").
- **Login and Chat Selector Restoration**: Resolved the non-responsive main button bug (including login and chat type selectors). Replaced over-eager event capturing blockages with precise click-propagation filtering scoped solely to elements within chat rows, allowing main buttons to be clicked and tapped normally.
- **Robust Three-Dots Popups**: Maintained stable visibility of options buttons next to chat entries while allowing their action dropdowns to toggle normally without switching the active chat.

---

## [1.1.6] - 2026-07-15

### Fixed
- **Mobile Chat Menu Options (Three-Dots) Fix**: Patched mobile browser limitations by injecting custom touch-optimization stylesheets and event handlers.
  - *No-Hover Workaround*: Removed desktop-only `:hover` visibility dependencies inside chat list sidebars, forcing options/three-dots buttons to be permanently visible and accessible on touchscreen devices.
  - *Tap Propagation Shielding*: Added capturing-phase event listeners to stop `click`, `touchstart`, `touchend`, and `mousedown` event propagation on the three-dots buttons. This ensures that tapping chat options triggers the corresponding action dropdown menu instead of bubbling up and activating the parent row container (which selected/switched chats and dismissed menus).
  - *Generous Touch Target Scaling*: Scaled three-dots options tap regions to a highly precise `40dp` minimum boundary to guarantee seamless, error-free mobile selection.

---

## [1.1.5] - 2026-07-15

### Fixed
- **Chromium Simple File Enumerator Fix**: Implemented startup sanitation of the WebView `Code Cache` folder, clearing potentially corrupted or stale V8 JavaScript compile cache bytecode files. This prevents Chromium's async background file enumerator thread from throwing metadata lookup error logs, ensuring pristine runtime execution.

---

## [1.1.4] - 2026-07-15

### Fixed
- **Soft Keyboard Covering Input Fields Fix**: Configured the standard Android `adjustResize` window soft input mode within `AndroidManifest.xml` and integrated Compose `.imePadding()` dynamically on the main UI content container. This resizes the WebView when the soft keyboard is shown, ensuring focused chat inputs are automatically scrolled above the keyboard, preventing overlay/obscuration.
- **Incremental Build Parameters**: Incremented the build parameters to `versionCode = 9` and `versionName = "1.1.4"`.

---

## [1.1.3] - 2026-07-15

### Fixed
- **Stable Network Monitoring**: Resolved the false "No Internet Connection" screen bug by replacing the generic network request callback with the modern `registerDefaultNetworkCallback` API. 
- **Graceful Network Handover**: Implemented a secondary active capability check during network lost events to filter out false-positives and ensure smooth handovers between mobile data and Wi-Fi.
- **Immediate Offline Startup Initialization**: Correctly initialized `isOnline` with the actual device connection status at launch, avoiding race conditions prior to the first callback update.

---

## [1.1.2] - 2026-07-15

### Fixed
- **Definitive Background Idle Battery Fix**: Replaced the continuous background `setInterval` polling with an ultra-efficient, debounced `MutationObserver` web API. The observer automatically sleeps when the DOM is stable and executes single-operation O(1) checks only when mutations occur, bringing idle CPU consumption to a pristine 0%.
- **Adaptive Polling Rates**: Configured progressive delays (3s during active downloads, 5s during pending states, and 6s when paused) to prevent battery drain during file transfers.
- **WebView Render Optimization**: Patched the Compose `AndroidView.update` block to only update WebView layer types if they actually changed, avoiding costly layout and rendering invalidations.
- **Redundant JS Cleanups**: Entirely eliminated duplicate JavaScript injections from page navigation and progress events.
- **Incremental Build Parameters**: Fully restored the versioning setup to `versionCode = 7` and `versionName = "1.1.2"`.

---

## [1.1.1] - 2026-07-15

### Added
- Proactive startup checks that warn users with a Toast if system notification permission has not been authorized.
- Direct runtime alert check inside the Web download listener to warn the user that they won't receive download progress notifications if permissions are disabled.

### Fixed
- Fixed critical background battery drain and device overheating by introducing a lightning-fast element check inside the WebView options injection, reducing idle CPU usage to 0%.
- Increased background options-injection checking interval from 1s to 3s to reduce CPU wakeups.
- Completely removed redundant and highly repetitive `injectCustomMenuJavascript` calls from `WebChromeClient.onProgressChanged` which were triggering 50 times per page navigation.
- Fixed the "download progress stuck at 0%" bug; if a server omits Content-Length, the progress is now cleanly displayed as a live size counter (e.g. `Downloaded: 12.4 MB`) with a fluid, animated indeterminate progress bar.
- Added file size formatting helper (`formatSize`) for readable download statuses in notifications (KB, MB, GB).

### Changed
- Translated all alert, status, and notification warning messages to English for consistency and internationalization compatibility.
- Incremented build configuration to `versionCode = 6` and `versionName = "1.1.1"`.

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
