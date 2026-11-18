# AGENTS.md

Instructions and map for anyone — human or AI — working on **Arena AI**.

This file is the operational reference of the repository. `README.md` is for
users; this is for whoever edits the code. If the two disagree, this file wins
and the README gets corrected.

---

## 1. Project overview

**Arena AI** is an unofficial, MIT-licensed Android shell for
[LMSYS Chatbot Arena](https://arena.ai). It is a native Kotlin/Jetpack Compose
app around a tuned `WebView`, plus the things a mobile browser cannot give:
native downloads with progress notifications, an in-app GitHub release check,
a custom in-page control menu and a dark Material 3 interface.

- **Language / build**: Kotlin 2.2.10, Jetpack Compose (BOM 2024.09.00),
  AGP 9.1.1, Gradle Kotlin DSL wrapped at 9.3.1, JDK 21 in CI.
- **Android**: `applicationId = "com.arenaai.app"`, `minSdk 24`,
  `targetSdk 36`, `compileSdk 36` (AGP `release(36) { minorApiLevel = 1 }`).
- **Namespace / source package**: `com.example` (historical, from the AI Studio
  template). Do not rename it in isolation: `applicationId` already differs and
  the release keystore is tied to the current package metadata.
- **No backend**: the app talks only to `arena.ai` through the WebView and to
  the public GitHub API for updates.
- **Not affiliated** with LMSYS, UC Berkeley or arena.ai. Keep that disclaimer
  wherever the project describes itself.

---

## 2. Repository layout

| Path | Role |
|---|---|
| `version.txt` | **single source of truth** for version name, versionCode and channel |
| `app/build.gradle.kts` | Android config, version parsing, signing configs, dependencies |
| `app/src/main/java/com/example/MainActivity.kt` | entry activity, Compose UI, WebView, downloads, settings sheet |
| `app/src/main/java/com/example/ReleaseUpdateChecker.kt` | GitHub release fetch and version comparison |
| `app/src/main/java/com/example/MyFirebaseMessagingService.kt` | FCM entry point and notification builder (see §9) |
| `app/src/main/java/com/example/ui/theme/` | `Color.kt`, `Theme.kt`, `Type.kt` — Material 3 defaults |
| `app/src/main/AndroidManifest.xml` | permissions, activity, FCM service, `adjustResize` |
| `app/src/main/res/drawable/ic_launcher_foreground_scaled.xml` | inset wrapper that shrinks the adaptive launcher logo |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher*.xml` | adaptive icon (background + scaled foreground + monochrome) |
| `.github/workflows/auto-release.yml` | builds the APK and publishes the GitHub release |
| `CHANGELOG.md` | user-facing release notes (source of the GitHub release body) |
| `CHANGELOG.dev.md` | engineering log: internals, CI, signing, gotchas |
| `docs/APK-BUILD.md` | long-form signing/build guide (partly AI-Studio era, see §10) |
| `README.md` | user documentation, badges, install and build instructions |
| `LICENSE` | MIT, Pietro Bonaldo (PiBOH), 2026 |

Only `README.md`, `AGENTS.md`, `CHANGELOG.md`, `CHANGELOG.dev.md` and `LICENSE`
live at the repository root. New long-form documents go under `docs/`.

---

## 3. Build, run and test

Local requirements: **JDK 17+** (JDK 21 recommended; Gradle 9 refuses Java 8),
Android SDK 36, and Android Studio or the command-line tools.

```bash
./gradlew assembleDebug                    # debug APK, signed with debugConfig
./gradlew assembleRelease                  # release APK, needs the env vars below
./gradlew test                             # JVM tests (JUnit4 + Robolectric)
./gradlew testDebugUnitTest --tests '*ExampleRobolectricTest*'
```

Release signing is read from the environment, never from the repository:

```bash
export KEYSTORE_PATH="$PWD/.secrets/arena-upload.jks"   # alias: upload
export STORE_PASSWORD='…'
export KEY_PASSWORD='…'
./gradlew assembleRelease
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk` and
`app/build/outputs/apk/release/app-release.apk`.

`app/build.gradle.kts` also creates a `debugConfig` signing config: it uses
`<root>/debug.keystore` when present, otherwise `$HOME/.android/debug.keystore`.
**A fresh CI runner has neither**, which is why the workflow creates a throwaway
debug keystore before `assembleDebug` (see §7 and `CHANGELOG.dev.md`).

The Secrets Gradle Plugin reads `.env` with `.env.example` as fallback
(`secrets { propertiesFileName = ".env" … }`). `.env` is gitignored; never
commit real keys. `google-services` uses `WARN` on a missing
`google-services.json` so the build works without Firebase config.

---

## 4. Versioning

`version.txt` has **three value lines**, preceded by comment lines:

```text
1.1.9
14
stable
```

- Comments start with `#`; blank lines and comments are ignored.
- Line 1 = version name (SemVer), line 2 = versionCode (monotonic integer),
  line 3 = channel (`stable` → normal release, anything else → pre-release).
- `app/build.gradle.kts` (top of file) strips comments and reads the three
  values into `versionName` / `versionCode`. **Never hard-code a version**
  anywhere else — not in Kotlin, not in the README, not in the workflow.
- Bump both line 1 and line 2 for a release; Android rejects a lower
  `versionCode` on update.

The same parsing exists in the workflow's *Resolve version* step. Both must
ignore comment lines: an earlier workflow version read line 1 literally, got
the first comment, and published a release tagged `v` with an empty version.
Do not reintroduce positional `sed` reads that skip the filtering.

---

## 5. How the app works

### 5.1 `MainActivity.kt`

**`MainActivity`** (`ComponentActivity`) owns the WebView and the whole UI.

- `webView`, `isOnline` (`mutableStateOf`), `backPressedTime`, `targetUrl`
  (`https://arena.ai/`), `filePathCallback` and `fileChooserLauncher`
  (`GetMultipleContents`) are the activity-level state.
- `onCreate`:
  1. `enableEdgeToEdge()`.
  2. Deletes `cacheDir/WebView/Default/HTTP Cache/Code Cache` and recreates
     `wasm/` and `js/`, silencing Chromium `simple_file_enumerator` errors.
  3. Initialises `isOnline` from `isHasNetworkConnection` **before** composing,
     then registers the network callback.
  4. `setContent { MyApplicationTheme(darkTheme = true, dynamicColor = false) }`
     — the app is always dark and ignores dynamic color.
  5. Hoists UI state with `remember { mutableStateOf(...) }`: `canGoBack`,
     `canGoForward`, `isLoading`, `loadingProgress` (float), `showSettings`,
     `isHardwareAccelerated`, `currentUrl`, update state.
  6. Reads a deep link from `intent.getStringExtra(EXTRA_URL)` (notification
     tap) as the initial URL.
  7. Warns once if `POST_NOTIFICATIONS` is not granted (API 33+).
  8. Runs the update check once (`LaunchedEffect(Unit) { checkForUpdates(false) }`)
     and exposes the same lambda to Settings for manual checks. A failure is
     silent; a manual check that finds nothing shows "already latest".
  9. `BackHandler`: goes back in the WebView, otherwise requires two presses
     within 2 s to exit.
  10. `Scaffold` → `Box` (slate `0xFF0F172A`, `imePadding`) → either
      `WebViewContainer` or `OfflineScreen`, plus a 3 dp top
      `LinearProgressIndicator`.
  11. Hosts `SettingsBottomSheet` and `UpdateAvailableDialog` when needed.
- `onNewIntent` reloads the URL carried by a notification tap
  (`FLAG_ACTIVITY_CLEAR_TOP or SINGLE_TOP` in the service).
- `registerNetworkCallback` prefers `registerDefaultNetworkCallback`; if that
  throws (older/OEM stacks) it falls back to a `NetworkRequest` with
  `NET_CAPABILITY_INTERNET`. `onLost` re-checks the active network so a
  handover does not show the offline screen.
- `isHasNetworkConnection` = `activeNetwork` + `NetworkCapabilities` with
  `NET_CAPABILITY_INTERNET`.

**`WebAppInterface`** is the `@JavascriptInterface` bridge named `AndroidApp`.
Each method (`goBack`, `goForward`, `reload`, `goHome`, `openSettings`) posts to
the WebView/Compose thread; never touch UI directly from the JS thread.

**`trackDownloadProgress`** polls `DownloadManager.Query` on
`Dispatchers.IO` and maintains one notification per download:

- channel `downloads_channel`, `IMPORTANCE_LOW`, created on API 26+;
- poll delays: 3 s running, 5 s other states, 6 s paused (battery);
- computes speed from the byte delta over wall-clock time and ETA from the
  remaining bytes;
- only calls `notify` when the percentage or the text actually changed;
- terminal states post "Download completed"/"Download failed" and a Toast on
  the main thread; the loop stops on success, failure, or a missing cursor.

`formatSpeed` (B/s, KB/s, MB/s), `formatTimeRemaining` (h/m/s) and
`formatSize` (B/KB/MB/GB) are pure helpers — keep them free of Android types.

**`injectCustomMenuJavascript`** evaluates one IIFE in the page:

- injects a `<style id="android-three-dots-patch">` that forces chat/sidebar
  three-dots buttons to stay visible on touch devices;
- `optimizeThreeDots()` selects only sidebar/history containers, marks buttons
  once with `data-three-dots-optimized`, and stops click propagation so opening
  the dropdown does not also select the chat row;
- `injectCustomTabs()` clones the styling of a nearby sidebar button and inserts
  an "📱 App Options" toggle with Back / Forward / Reload / Settings, each
  calling `window.AndroidApp.<method>()`; stale wrappers are removed first;
- a `MutationObserver` on `document.body` re-runs the three-dots pass on every
  mutation and re-injects the wrapper at most every 2 s. The observer is stored
  on `window.androidMenuObserver`.
- It runs on `onPageFinished` only — do **not** call it from
  `onProgressChanged`, which fires dozens of times per navigation.

**`WebViewContainer`** builds the view tree:

- `SwipeRefreshLayout` with `isEnabled = false` (pull-to-refresh would lose an
  active chat) and `setOnChildScrollUpCallback` so it never steals touches.
- `WebView`: `overScrollMode = NEVER`, JS/DOM/database on, wide viewport,
  file/content access (uploads), `LOAD_DEFAULT` cache, popups allowed, mixed
  content allowed, and a desktop-Chrome-on-Android UA string so arena.ai serves
  the mobile layout it expects.
- Cookies: `setAcceptCookie(true)`, `setAcceptThirdPartyCookies(this, true)`,
  and `CookieManager.flush()` on `onPageFinished` to persist logins.
- `WebViewClient.onPageFinished` also reports back/forward/current URL and
  re-injects the JS menu.
- `shouldOverrideUrlLoading` is the traffic policy:
  - stays inside: `arena.ai`, `lmarena.ai`, `lmsys.org`;
  - stays inside: auth (`google.com/.it`, `huggingface.co`, `github.com`,
    `apple.com`, `oauth`, `login`, `signin`, `signup`, `auth`, `checkpoint`,
    `recaptcha`, `gstatic`);
  - **stays inside**: bot verification (`cloudflare.com`,
    `cdn-cgi/challenge-platform`, `turnstile`, `hcaptcha.com`, `captcha`) —
    sending these to the system browser left the "I'm not a robot" overlay
    stuck over the app;
  - non-http schemes go through `Intent.parseUri` (with
    `browser_fallback_url` fallback), and other http(s) domains open in the
    system browser. Return values matter: `false` = load in WebView.
- `setDownloadListener` guesses the filename, warns if notifications are
  denied, enqueues a `DownloadManager.Request` with the page cookies and UA,
  `VISIBILITY_HIDDEN` (the app draws its own notification), saves to
  `Downloads/`, then calls `trackDownloadProgress`. Failures fall back to the
  system browser.
- `WebChromeClient`: forwards progress; `onCreateWindow` renders popups in the
  **same** WebView (needed by verification challenges); `onShowFileChooser`
  forwards to `MainActivity.fileChooserLauncher` and clears the callback when
  cancelled.

**`UpdateAvailableDialog`** shows version, up to 600 characters of release
notes, and opens the APK asset URL (or the release page) through
`LocalUriHandler`.

**`OfflineScreen`** is the centred Wi-Fi-off state with a Retry button that
re-checks connectivity and reloads.

**`SettingsBottomSheet`** (Material 3 modal sheet) contains four cards:

1. *Push Notification Center*: requests `POST_NOTIFICATIONS` on API 33+ and
   sends a mock notification through `MyFirebaseMessagingService.sendNotification`.
2. *Performance & Fluidity*: hardware-acceleration switch (changes the WebView
   layer type) and "Clear Cache & Cookies" (cache, history, all cookies, flush,
   reload home).
3. *App Information*: version from `PackageManager` with `BuildConfig`
   fallback, "Check for updates", author link to `https://piboh.github.io/`.
4. *GitHub Community & Feedback*: new issue / pull requests buttons.

The footer prints the current page URL.

### 5.2 `ReleaseUpdateChecker.kt`

`object` with one suspend entry point and two pure functions:

- `fetchLatestRelease()` calls
  `https://api.github.com/repos/PiBOH/Arena-AI/releases/latest` with
  `HttpURLConnection`, 10 s timeouts, GitHub JSON accept header and a custom
  user agent. It returns `null` on any non-2xx response or exception — an update
  check must never crash or block the app. It picks the first asset whose name
  ends in `.apk` and returns `GitHubRelease(version, pageUrl, notes, apkUrl)`.
- `normalizeVersion` strips a leading `v` and any `-suffix`.
- `isNewerVersion` compares dotted components numerically (so `1.10.0` beats
  `1.9.9`) and pads missing components with `0`.

No token is used and no dependency was added.

### 5.3 `MyFirebaseMessagingService.kt`

Standard `FirebaseMessagingService`: `onMessageReceived` reads title/body from
the notification payload or the data map, reads `url`/`link`, and calls
`sendNotification`; `onNewToken` only logs (there is no backend to register
with). `sendNotification` creates the `arena_push_notifications` channel, builds
a `PendingIntent` to `MainActivity` carrying `EXTRA_URL` (`target_url`), and
posts the notification. The same function backs the in-app test button.

**Important**: the repository has **no `google-services.json`**, so the
`google-services` plugin only warns and FCM cannot receive real pushes until a
Firebase project is configured. Treat push as optional/experimental. The
Downloads channel never depends on it.

### 5.4 Theme

`Theme.kt` defines Material 3 dark/light schemes from `Color.kt` and supports
dynamic color on Android 12+, but `MainActivity` always calls
`MyApplicationTheme(darkTheme = true, dynamicColor = false)`. `Type.kt` only
overrides `bodyLarge`. The real palette is the slate/sky-blue set used inline
in `MainActivity` (`0xFF0F172A`, `0xFF1E293B`, `0xFF38BDF8`, `0xFF94A3B8`,
`0xFFEF4444`).

---

## 6. Launcher icon

The adaptive icon is a 108 dp canvas; only the inner 72 dp is guaranteed
visible. The logo lives in `ic_arena_logo.xml` and is wrapped by
`ic_launcher_foreground_scaled.xml`:

```xml
<inset android:drawable="@drawable/ic_arena_logo" android:inset="27.5%" />
```

Inset per side `i` leaves `1 - 2i` of the canvas: `27.5%` → **45% of the
original size**, i.e. **55% smaller** (the first release shrank it 25% with a
12.5% inset; 1.1.9 adds another 30%, and the changelog states 25 + 30 = 55).
Both `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` point
foreground and monochrome at the scaled wrapper. To resize again, change only
the inset, never the source drawable.

The legacy `mipmap-*/ic_launcher*.webp` files (API 24–25) are **corrupt**: their
VP8X headers declare impossible canvas sizes, so Pillow and Android tooling
cannot decode them. They are unused on Android 8+; do not "fix" them blindly and
do not regenerate them unless you can verify the output on a real API 24 device.

---

## 7. Release pipeline (`.github/workflows/auto-release.yml`)

Triggers:

- **push to `main`** whose head commit message starts with `v` (for example
  `v1.1.9 …`) — same signal as the other PiBOH repositories;
- **manual dispatch**, with inputs:

| Input | Values | Meaning |
|---|---|---|
| `version` | string | override the version read from `version.txt` |
| `build_type` | `release` (default), `debug`, `both` | what to build and publish |
| `allow_unsigned` | boolean, default `true` | with no signing secret, publish a debug APK instead of failing |
| `dry_run` | boolean, default `false` | compile only, skip the release step |

Steps:

1. **Checkout** (full history, needed by the changelog fallback) and
   **JDK 21**.
2. **Resolve version**: strips comments/blanks from `version.txt`, refuses an
   empty version or versionCode, computes `prerelease` from the channel and
   `tag = v<version>`.
3. **Build APK**:
   - `release`: decode `KEYSTORE_BASE64` to `my-upload-key.jks`, export
     `KEYSTORE_PATH`, run `assembleRelease`;
   - `debug`: create `~/.android/debug.keystore` if missing, run
     `assembleDebug`;
   - `both`: both builds;
   - no keystore + `allow_unsigned`: debug build only, with a warning;
   - no keystore + `allow_unsigned=false`: hard failure.
4. **Assets**: the built file is copied under the published names —
   - release: `Arena-AI.apk` and `Arena-AI_v<version>.apk` (byte-identical);
   - debug: `Arena-AI-debug.apk` and `Arena-AI_v<version>-debug.apk`;
   - both: all four.
5. **Changelog**: extracts the `## [<version>]` section from `CHANGELOG.md`;
   if absent, falls back to the last 30 commit subjects. It also writes the
   same section, heading included, to `changelog.md` for the release asset.
6. **Publish**: `softprops/action-gh-release@v2` with tag, title
   `Arena AI v<version>`, `prerelease` from the channel, the APKs above plus
   `changelog.md`, and a body that starts with the standard
   "Watch > Custom > Releases" notice and then channel, versionCode, build
   variant and notes. Every release opens with that notice, scrcpy-style:

   ```markdown
   ## Arena AI v1.1.9

   _To receive a notification when a new release is available, click on **Watch > Custom > Releases** at the top._

   **Channel:** `stable` · **versionCode:** `14` · **Build:** signed release

   <the `## [1.1.9]` section of CHANGELOG.md, heading excluded>

   ---
   Automatic build from commit `<sha>`.
   ```

   `changelog.md` holds the same section **including** its heading, so a 1.1.9
   release ships exactly the 1.1.9 notes. Keep the 1.1.9 wording (55% icon,
   captcha fix) unless `version.txt` itself is bumped.

Signing secrets (repository settings):

| Secret | Content |
|---|---|
| `KEYSTORE_BASE64` | base64 of the upload keystore (`alias upload`) |
| `STORE_PASSWORD` | keystore password |
| `KEY_PASSWORD` | key password |

The keystore itself lives **only** in the gitignored `.secrets/` folder of the
owner's machine and in GitHub secrets. Never commit it, never print it, never
attach it to a release. Losing it means future APKs cannot update existing
installations.

The workflow logs a benign KSP/AWT `NullPointerException`
(`ApplicationManager.getApplication()` in `BinaryFileTypeDecompilers`) on the
release build; the build still succeeds. Do not "fix" it by disabling KSP or by
turning the annotation into a failure.

---

## 8. Changelog policy

Two files, two audiences:

- **`CHANGELOG.md`** — user-facing, [Keep a Changelog](https://keepachangelog.com)
  sections `Added` / `Changed` / `Fixed`, short sentences, no internal class
  names. **The release workflow reads its `## [<version>]` section as the
  GitHub release body**, so the heading format is part of the contract.
- **`CHANGELOG.dev.md`** — engineering log: root causes, implementation
  details, CI/signing changes, gotchas worth remembering. Anything that would
  confuse a user belongs here.

Both are updated in the same commit as the change. A release that adds detail
only to one of them is incomplete. The release publishes the matching
`CHANGELOG.md` section as the `changelog.md` asset: never attach the whole
file, only the section for that version.

---

## 9. Conventions

- **Language**: English for code, comments, commit messages, docs and release
  notes. Italian only in owner-facing chat/instructions.
- **Kotlin**: official style (`kotlin.code.style=official`), 2-space indent in
  Gradle files, 4-space in Kotlin. Keep functions small and pure when possible.
- **Comments explain why**, not what. The WebView workarounds especially need
  the reason (site DOM, Chromium quirks) or they get "cleaned up" and break.
- **No new dependencies** for something the platform already provides: the
  updater uses `HttpURLConnection` + `org.json`, downloads use
  `DownloadManager`, notifications use `NotificationCompat`.
- **Do not** re-enable pull-to-refresh, `onProgressChanged` JS injection,
  swipe gestures, or send captcha URLs to the external browser: each of those
  was a bug fixed in 1.1.x.
- **Do not** change `versionName`/`versionCode` outside `version.txt`.
- **Do not** commit `.secrets/`, `*.jks`, `*.keystore`, `.env`,
  `google-services.json` (if it ever exists) or `local.properties`.
- Commit messages are short, imperative, and **do not start with `v`** unless
  the commit is meant to publish a release immediately.
- Do not add AI attribution or `Co-Authored-By` trailers to commits in this
  repository: the owner asked for clean messages.

---

## 10. Known limitations and traps

- **Local builds need JDK 17+.** The only JDK on the owner's Windows machine is
  1.8, so compile errors cannot be reproduced locally; CI is the source of
  truth.
- **`docs/APK-BUILD.md` predates the current setup** (it mentions Google AI
  Studio export and "unsigned" release output). The real behaviour is in this
  file and `app/build.gradle.kts`.
- **`package com.example`** is intentional legacy; renaming it touches every
  file, the namespace, the manifest and the JS bridge — do it as a dedicated
  change, not as cleanup.
- **Legacy webp icons are corrupt** (see §6).
- **Firebase** has no configuration in the repo (§5.3): do not advertise push
  notifications in user-facing docs until a `google-services.json` exists.
- **GitHub API rate limit**: anonymous requests are limited to 60/h per IP; the
  updater fails silently, which is acceptable for a once-per-launch check.
- **Release assets are overwritten** when the same tag is republished: this is
  how 1.1.9 was re-cut after the icon change, and `Arena-AI.apk` is the stable
  download URL used by the updater.

---

## 11. Reusing this project as a starting point

This app is a small, dependency-light template for "native shell around a web
app". The reusable parts:

1. **WebView hardening**: cookie persistence, mixed content, popup handling in
   the same WebView, a whitelist for auth and bot-verification domains, and an
   external-browser hand-off for everything else.
2. **Download pipeline**: `DownloadManager` + `VISIBILITY_HIDDEN` + a custom
   low-importance progress notification with speed/ETA and change detection.
3. **Self-update**: GitHub `releases/latest` + numeric version comparison +
   an in-app dialog, no dependency and no token.
4. **JS bridge**: expose only the few actions you need, post every call back to
   the UI thread, and inject the UI from a `MutationObserver` rather than a
   timer.
5. **CI release**: `version.txt` → single source of truth, `v`-prefixed commits,
   optional signing with a graceful debug fallback, and stable + versioned asset
   names.
6. **Docs split**: user changelog vs engineering log vs agent instructions.

When adapting it, change `targetUrl`, the domain whitelist, `applicationId`,
the icon drawables and `version.txt` first, then delete the FCM service if the
new app has no push backend.
