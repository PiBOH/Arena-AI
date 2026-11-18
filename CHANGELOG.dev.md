# Engineering changelog

Internal notes for whoever maintains Arena AI: root causes, implementation
details, CI and signing changes. User-facing release notes live in
[`CHANGELOG.md`](CHANGELOG.md); this file is not used as the GitHub release
body.

---

## 1.1.9 — 2026-11-18

### Added

- **`ReleaseUpdateChecker`** (`object`, no dependency): `HttpURLConnection`
  against `releases/latest`, 10 s connect/read timeouts, `Accept:
  application/vnd.github+json`, custom user agent, `null` on every failure.
  Assets are scanned for the first `.apk`; `normalizeVersion` strips `v` and
  `-suffix`; `isNewerVersion` compares numeric components with zero padding.
- **Update UI**: automatic check on first composition, manual action in
  Settings, `UpdateAvailableDialog` with truncated notes and the APK asset URL.
- **`version.txt`** as the single version source. Gradle strips `#` comments,
  blanks and whitespace, then reads name/code; the workflow does the same with
  `sed`/`grep` instead of positional line reads.
- **Auto Release workflow** with `build_type` (`release`/`debug`/`both`),
  `allow_unsigned` and `dry_run` inputs, stable + versioned asset names and a
  changelog-extraction step.

### Fixed

- **Captcha overlay blocked the app**: verification URLs were handed to the
  system browser, so the challenge never completed and the overlay stayed over
  the page. They are now loaded in the WebView, `javaScriptCanOpenWindowsAutomatically`
  is on and `onCreateWindow` renders the popup in the same WebView.
- **Release workflow failure** (`ValidateSigningTask: keystore file
  /home/runner/.android/debug.keystore not found`): the debug signing config
  points at a keystore that does not exist on a fresh runner. The workflow now
  creates a throwaway JKS (`androiddebugkey`, password `android`) for debug
  builds.
- **Release published as tag `v`**: the version step read line 1 of
  `version.txt` (a comment), so `version` and `versionCode` were empty. Values
  are now parsed after stripping comments, and the step fails fast when either
  is empty.
- **Launcher logo too large**: adaptive foreground wrapped in an `inset`
  drawable. 1.1.9 increases the inset from 12.5% to 27.5% per side, i.e. 45% of
  the original size — 55% smaller (previous 25% + 30%).

### CI / repository

- `actions/checkout@v5`, `actions/setup-java@v5` (Node 20 deprecation notice on
  `softprops/action-gh-release@v2` remains; the action still works).
- `.gitignore` covers `*.jks` and `.secrets/`; the upload keystore is generated
  locally with `keytool -genkeypair -storetype PKCS12 -alias upload -keysize
  4096 -validity 10000` and stored only in `.secrets/` plus GitHub secrets.
- `CHANGELOG.md` split into a user-facing file and this engineering log.

### Appearance follow-up (1.1.9 re-release)

- **Native light theme**: arena.ai is behind Cloudflare, so its theme keys
  cannot be inspected from CI. The WebView now tries the standard provider
  mechanism instead of assuming one key: it writes `light`/`dark`/`system` to
  every existing `localStorage` key matching `/theme/i` plus `theme`
  (next-themes default), dispatches a `storage` event, sets `data-theme`,
  `data-color-mode`, `color-scheme` and the `light`/`dark` class, and reloads
  once per resolved theme so the site boots with its own palette. A
  `sessionStorage` guard (`arena-theme-sync`) prevents reload loops; the CSS
  inversion is now a fallback applied only when the computed body background
  still has the wrong polarity. The script is limited to
  `arena.ai`/`lmarena.ai`, so OAuth pages are never modified.
- **Failed first attempt**: the first 1.1.9 build inverted every light page with
  a CSS filter. It was published, but it was not the site's own theme; this
  release replaces it with the native-first approach above and keeps the filter
  only as a safety net.
- **Settings palette**: the sheet used Slate plus `#38BDF8` (sky blue) accents.
  Replaced with the neutral near-black/white palette, outlined Material icons,
  pill buttons and 12 dp cards to match arena.ai on mobile. The in-page
  "App Options" menu CSS was neutralised the same way.
- **Build failures before this commit**: two automated 1.1.9 runs failed with
  `Unresolved reference 'contentColor'` (MainActivity.kt:1485) and
  `Unresolved reference 'fontWeight'` (1778). Cause: the bulk regex that
  replaced hardcoded palette values also stripped trailing commas in
  `containerColor = sheetBackground` and `color = primaryText`. Fixed by
  restoring the commas in `063e2c3` and `0688d88`.
- **Launcher icon**: the adaptive icon no longer declares `<monochrome>`, so
  Android's themed-icon recolouring cannot change it with the app/system theme.
  The settings sheet still swaps `ic_arena_logo_dark`/`ic_arena_logo_light` for
  contrast on its own surfaces.
- **Release default**: a `v` push has no `build_type` input; the workflow now
  defaults to `both`, so 1.1.9 publishes the signed release and the debug APK.

### Known noise

- Release builds log
  `java.lang.NullPointerException: Cannot invoke "…Application.getService(…)"`
  from `ksp.com.intellij.openapi.fileEditor.FileDocumentManager` on an AWT
  thread. It is a KSP/IntelliJ-platform initialisation bug in headless CI; the
  APK is produced and signed. It appears as an error annotation on the run.

---

## 1.1.8 — 2026-07-15

- Developer & version card in the settings sheet: version read dynamically,
  PiBOH credit linking to `https://piboh.github.io/`.

## 1.1.7 — 2026-07-15

- Reverted over-broad CSS (`button:has(svg)`, global `overflow`/`position`)
  that tilted the dashboard layout.
- Restored main action buttons (login, chat selector) by replacing capture-phase
  click blocking with precise filtering inside chat rows only.
- Kept three-dots popups usable without switching the active chat.

## 1.1.6 — 2026-07-15

- Touch-only three-dots fix: stylesheet forcing visibility, capture-phase
  `stopPropagation` on the menu buttons, 40 dp minimum touch target.

## 1.1.5 — 2026-07-15

- Startup sanitation of the WebView `Code Cache` folder (stale V8 bytecode) to
  silence `simple_file_enumerator` metadata errors.

## 1.1.4 — 2026-07-15

- `adjustResize` + `imePadding()` so the keyboard does not cover chat inputs.

## 1.1.3 — 2026-07-15

- `registerDefaultNetworkCallback` instead of a generic request; `onLost`
  re-verifies the active network to survive Wi-Fi/mobile handover; `isOnline`
  initialised from the real state before first composition.

## 1.1.2 — 2026-07-15

- Replaced `setInterval` polling with a debounced `MutationObserver`; adaptive
  polling delays (3 s downloading, 5 s pending, 6 s paused); WebView layer type
  only updated when it changes; removed duplicate JS injections.

## 1.1.1 — 2026-07-15

- Startup and download-time warnings when notification permission is missing.
- Idle CPU fixes (element check, slower injection loop, removed per-progress
  injections).
- Indeterminate progress text when the server omits `Content-Length`.

## 1.1.0 — 2026-07-15

- Custom download notifications (name, percentage, speed, ETA), completion and
  failure notifications, `DOWNLOAD_WITHOUT_NOTIFICATION`.

## 1.0.3 — 2026-07-15

- Added `lmarena.ai`; disabled pull-to-refresh and overscroll to avoid losing
  chats; pre-created WASM cache dirs; OAuth domains kept inside the WebView;
  `CookieManager.flush()` after page load.

## 1.0.2 — 2026-07-15

- Bilingual build guide, YAML issue forms, keystore fallback for local debug,
  `fragment-ktx` 1.8.2 to satisfy the release lint check.

## 1.0.1 — 2026-07-14

- Italian launcher metadata, Compose/Material 3 scaffolding, SDK 36 alignment.

## 1.0.0 — 2026-07-12

- Initial WebView shell: hardware acceleration, DOM storage, native uploads,
  dark theme, 48 dp touch targets.
