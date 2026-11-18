# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Engineering details live in [`CHANGELOG.dev.md`](CHANGELOG.dev.md).

---

## [1.1.9] - 2026-11-18

### Added
- **Check for updates**: the app reads the latest GitHub release on startup and offers a direct download when a newer version exists, plus a manual **Check for updates** action in Settings.
- **`version.txt`**: one file now holds the version name, version code and release channel.
- **Automatic releases**: a `v`-prefixed commit builds the APK and publishes a GitHub release, with debug, release or both variants.

### Changed
- Versioning is centralized, and the version shown in Settings comes from the installed build.
- The project is released under the **MIT License**.

### Fixed
- **The "I'm not a robot" check no longer blocks the app**: verification challenges now complete inside the app instead of opening in the browser and leaving the overlay stuck on screen.
- **Launcher icon size**: the logo is now drawn 55% smaller (the previous 25% plus a further 30%, 25 + 30 = 55), so it no longer looks oversized in the Android app list.

---

## [1.1.8] - 2026-07-15

### Added
- An **App Information** card in Settings with the installed version, the developer credit and a link to the author's site.

---

## [1.1.7] - 2026-07-15

### Fixed
- Restored the correct home layout and the main buttons (login, chat selector).
- Chat options no longer switch the active conversation when opened.

---

## [1.1.6] - 2026-07-15

### Fixed
- The three-dots menu in the chat list is now always visible and easy to tap on touchscreens.

---

## [1.1.5] - 2026-07-15

### Fixed
- Removed Chromium cache errors at startup by cleaning the WebView code cache.

---

## [1.1.4] - 2026-07-15

### Fixed
- The keyboard no longer covers the chat input field.

---

## [1.1.3] - 2026-07-15

### Fixed
- The "No Internet Connection" screen no longer appears when the connection is actually available.
- Seamless handover between Wi-Fi and mobile data.

---

## [1.1.2] - 2026-07-15

### Fixed
- Idle battery drain and overheating eliminated.
- More efficient WebView rendering.

---

## [1.1.1] - 2026-07-15

### Added
- Warnings when notification permission is missing.

### Fixed
- Download progress no longer stays at 0% when the server does not report the file size.
- Reduced background CPU usage.

---

## [1.1.0] - 2026-07-15

### Added
- Native download notifications with file name, percentage, speed and time remaining.
- Completion and failure alerts.

---

## [1.0.3] - 2026-07-15

### Added
- Support for `lmarena.ai` and community links in Settings.

### Fixed
- Login flows for Google, Hugging Face, GitHub and Apple stay inside the app.
- Accidental pull-to-refresh no longer loses an active chat.

---

## [1.0.2] - 2026-07-14

### Added
- Build and signing documentation, plus issue and feature-request templates.

### Fixed
- Authentication flows no longer hang or escape to the external browser.

---

## [1.0.1] - 2026-07-14

### Added
- Italian launcher metadata and Material 3 interface.

### Changed
- Updated the target SDK and dependencies.

---

## [1.0.0] - 2026-07-12

### Added
- First release of **Arena AI**, a native Android wrapper for LMSYS Chatbot Arena.
- Dark/light interface, file uploads and hardware-accelerated rendering.

[Unreleased]: https://github.com/PiBOH/Arena-AI/compare/v1.1.9...HEAD
[1.1.9]: https://github.com/PiBOH/Arena-AI/compare/v1.1.8...v1.1.9
[1.1.8]: https://github.com/PiBOH/Arena-AI/compare/v1.1.7...v1.1.8
[1.1.7]: https://github.com/PiBOH/Arena-AI/compare/v1.1.6...v1.1.7
[1.1.6]: https://github.com/PiBOH/Arena-AI/compare/v1.1.5...v1.1.6
[1.1.5]: https://github.com/PiBOH/Arena-AI/compare/v1.1.4...v1.1.5
[1.1.4]: https://github.com/PiBOH/Arena-AI/compare/v1.1.3...v1.1.4
[1.1.3]: https://github.com/PiBOH/Arena-AI/compare/v1.1.2...v1.1.3
[1.1.2]: https://github.com/PiBOH/Arena-AI/compare/v1.1.1...v1.1.2
[1.1.1]: https://github.com/PiBOH/Arena-AI/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/PiBOH/Arena-AI/compare/v1.0.3...v1.1.0
[1.0.3]: https://github.com/PiBOH/Arena-AI/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/PiBOH/Arena-AI/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/PiBOH/Arena-AI/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/PiBOH/Arena-AI/releases/tag/v1.0.0
