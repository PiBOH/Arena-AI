## [1.1.9] - 2026-11-18

### Added
- 🎨 **Theme selection**: choose **System**, **Light** or **Dark** in Settings. The choice is remembered, follows the Android theme by default and applies to the whole rendered site, not only to the native controls.
- 🌗 **Adaptive logo**: dark mode keeps the white logo, light mode uses the inverted (dark ink) variant, and on **System** the logo follows the Android theme automatically.
- **Check for updates**: the app reads the latest GitHub release on startup and offers a direct download when a newer version exists, plus a manual **Check for updates** action in Settings.
- **`version.txt`**: one file now holds the version name, version code and release channel.
- **Automatic releases**: a `v`-prefixed commit builds the APK and publishes a GitHub release, with debug, release or both variants.

### Changed
- 📦 Every release now ships a `changelog.md` asset with only the notes for that version.
- Versioning is centralized, and the version shown in Settings comes from the installed build.
- The project is released under the **MIT License**.

### Fixed
- **The "I'm not a robot" check no longer blocks the app**: verification challenges now complete inside the app instead of opening in the browser and leaving the overlay stuck on screen.
- **Launcher icon size**: the logo is now drawn 55% smaller (the previous 25% plus a further 30%, 25 + 30 = 55), so it no longer looks oversized in the Android app list.

---
