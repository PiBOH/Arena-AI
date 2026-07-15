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
