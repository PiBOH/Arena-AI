## [1.1.0] - 2026-07-15

### Added
- Rich, custom-styled native Android download notifications that track download progress in real-time, including file name, progress percentage, remaining time, and transfer speed.
- Fully automated "Download completed" and "Download failed" native alerts and localized Toasts.
- Declared `DOWNLOAD_WITHOUT_NOTIFICATION` system permission to allow cleanly hiding default system alerts in favor of the custom notifications.
- Incremented build configuration following Semantic Versioning rules to `versionCode = 5` and `versionName = "1.1.0"`.

### Fixed
- Investigated and verified chromium internal `simple_file_enumerator` logs (`opendir Code Cache/js: No such file or directory`); confirmed they are completely benign startup warnings that occur naturally on fresh WebView cache initializations and have zero impact on the application's runtime stability.
