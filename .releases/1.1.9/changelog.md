## [1.1.9] - 2026-11-18

### Added
- 🎨 **Theme selection**: choose **System**, **Light** or **Dark** in Settings. Light mode asks arena.ai for its **native** light theme first and only falls back to a color inversion when the site has no light palette; the choice applies to the whole rendered site, not only to the native controls.
- 🔄 **Check for updates**: the app reads the latest GitHub release on startup and offers a direct download when a newer version exists, plus a manual **Check for updates** action in Settings.
- **`version.txt`**: one file now holds the version name, version code and release channel.
- **Automatic releases**: a `v`-prefixed commit builds and publishes a GitHub release with **both** the signed release and the debug APKs.

### Changed
- 🖤 **Settings redesigned** after arena.ai on mobile: neutral black/white surfaces, muted line icons and pill-shaped buttons, with no colored accent.
- 📦 Every release now ships a `changelog.md` asset with only the notes for that version.
- Versioning is centralized, and the version shown in Settings comes from the installed build.
- The project is released under the **MIT License**.

### Fixed
- **The launcher icon no longer changes color with the app theme**: it keeps the same white mark on the dark adaptive background and is no longer offered as a themed (monochrome) icon.
- **The "I'm not a robot" check no longer blocks the app**: verification challenges now complete inside the app instead of opening in the browser and leaving the overlay stuck on screen.
- **Launcher icon size**: the logo is now drawn 55% smaller (the previous 25% plus a further 30%, 25 + 30 = 55), so it no longer looks oversized in the Android app list.

---
