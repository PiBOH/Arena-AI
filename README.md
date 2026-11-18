<p align="center">
 <img src="logo.png" alt="Arena AI logo" width="160">
</p>

<h1 align="center">Arena AI</h1>

<p align="center">
 <b>A native Android home for LMSYS Chatbot Arena.</b><br>
 <sub>The speed and feel of an app — not a browser tab — for <a href="https://arena.ai">arena.ai</a>.</sub>
</p>

<p align="center">
 <a href="https://github.com/PiBOH/Arena-AI/releases">
 <img src="https://img.shields.io/github/v/release/PiBOH/Arena-AI?include_prereleases&label=download&style=for-the-badge&color=4CAF50" alt="Download the latest release">
 </a>
 <a href="https://github.com/PiBOH/Arena-AI/releases">
 <img src="https://img.shields.io/github/downloads/PiBOH/Arena-AI/total?style=for-the-badge&color=blue" alt="Total downloads">
 </a>
 <a href="LICENSE">
 <img src="https://img.shields.io/github/license/PiBOH/Arena-AI?style=for-the-badge&color=brightgreen" alt="MIT license">
 </a>
 <a href="https://github.com/PiBOH/Arena-AI/commits/main">
 <img src="https://img.shields.io/github/last-commit/PiBOH/Arena-AI?style=for-the-badge" alt="Last commit">
 </a>
</p>

<p align="center">
 <img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square" alt="Android 7.0+">
 <img src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin">
 <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
</p>

---

## About Arena AI

**Arena AI** is a lightweight, native Android shell for [LMSYS Chatbot Arena](https://arena.ai): blind side-by-side comparisons of the world's best language models. It wraps the site in a tuned `WebView` and adds the parts a mobile browser cannot give you — native downloads with progress notifications, a dark Material 3 interface, and automatic update checks straight from GitHub.

It is built with **Kotlin** and **Jetpack Compose**, targets **Android 16 (SDK 36)**, and runs on **Android 7.0+**.

> **Unofficial project.** Arena AI is not affiliated with, endorsed by, or supported by LMSYS, UC Berkeley or arena.ai.

---

## Features

<table align="center" width="100%">
<tr valign="top">
<td width="50%">

### Fast and native
- Full-screen tuned `WebView`: no address bar, no browser chrome, no ads.
- Dark Material 3 interface with edge-to-edge layout.
- Hardware-accelerated rendering with a software fallback for older devices.
- Custom in-page menu with back, forward, reload and settings.

</td>
<td width="50%">

### The whole Arena
- Blind battles, leaderboards, direct chats and side-by-side model comparisons.
- Google, Hugging Face, GitHub and Apple sign-in handled inside the app.
- Session, cookies and login state persist between launches.
- File uploads work from the native Android picker.

</td>
</tr>
<tr valign="top">
<td width="50%">

### Native notifications
- Download notifications with percentage, transfer speed and time remaining.
- Completion and failure alerts, with a tap to open the downloaded file.
- No advertising, promotional or tracking notifications.

</td>
<td width="50%">

### Downloads and updates
- Native `DownloadManager` integration with a custom progress notification.
- Clear cache and cookies from the settings sheet.
- **Automatic GitHub release checks** on startup, plus a manual *Check for updates* action, with a direct APK link.

</td>
</tr>
<tr valign="top">
<td width="50%">

### Privacy
- No analytics, no trackers, no third-party advertising SDKs.
- Only the permissions the app actually needs: internet, network state, notifications and downloads.
- Settings, cookies and cache stay on your device.

</td>
<td width="50%">

### Built for contributors
- Centralized versioning in `version.txt`.
- Automatic GitHub releases from CI on every `v*` commit.
- MIT licensed, with beginner-friendly issue templates.

</td>
</tr>
</table>

---

## Install

1. Open the [**Releases**](https://github.com/PiBOH/Arena-AI/releases) page.
2. Download the latest `Arena-AI-v*.apk`.
3. Allow installation from unknown sources when Android asks.
4. Open the app — the Arena loads immediately.

> Requires **Android 7.0 (API 24)** or newer.

---

## Build from source

**Requirements:** [JDK 21](https://adoptium.net/), Android SDK 36 and Android Studio (or the command-line tools).

```bash
# 1. Clone
git clone https://github.com/PiBOH/Arena-AI.git
cd Arena-AI

# 2. Create .env (see .env.example)
# GEMINI_API_KEY=your_key_here

# 3. Build a debug APK
./gradlew assembleDebug

# 4. Or build a signed release APK
export KEYSTORE_PATH="$PWD/.secrets/arena-upload.jks"
export STORE_PASSWORD="your_store_password"
export KEY_PASSWORD="your_key_password"
./gradlew assembleRelease
```

The APK is written to `app/build/outputs/apk/`.

---

## Versioning and automatic releases

The single source of truth for the version is **`version.txt`**:

```text
1.1.9 # version name
14 # version code
stable # release channel (stable / beta)
```

Gradle reads it for `versionName` and `versionCode`, and the `Auto Release` workflow reads it to tag releases.

- Push a commit to `main` whose message **starts with `v`** (for example `v1.1.9`) and the workflow builds the APK and publishes a GitHub release.
- You can also run **Actions → Auto Release → Run workflow** manually.
- If `CHANGELOG.md` has a section for that version, it becomes the release notes and is attached to the release as `changelog.md`; otherwise the recent commits are used.
- The changelog is optional, the release always works.

For **signed** release APKs, add these repository secrets:

| Secret | Description |
| --- | --- |
| `KEYSTORE_BASE64` | base64 of the upload keystore |
| `STORE_PASSWORD` | keystore password |
| `KEY_PASSWORD` | key password |

Without `KEYSTORE_BASE64` the workflow still succeeds and publishes a debug-signed APK.

---

## Project structure

```text
app/src/main/java/com/example/
├── MainActivity.kt # Compose UI, WebView, downloads, settings
├── ReleaseUpdateChecker.kt # GitHub release check and version comparison
└── ui/theme/ # Material 3 colors and typography

.github/workflows/
└── auto-release.yml # APK build + GitHub release
```

---

## Privacy

Arena AI collects nothing. There is no analytics, no telemetry and no advertising. Cookies and local storage belong to the arena.ai session you sign in with, and they stay on your device. The only network call made by the app itself is the optional GitHub release check.

---

## Disclaimer

This project and its contents are **not affiliated with, funded, authorized, endorsed by, or in any way associated with** LMSYS, UC Berkeley, arena.ai, or any of their affiliates and subsidiaries. All trademarks and service marks belong to their respective owners. Arena AI is an independent project made for personal and educational use.

---

## License

Released under the [MIT License](LICENSE).

---

<div align="center">

### Community & support

Report bugs, propose features or contribute code on GitHub.

<a href="https://github.com/PiBOH/Arena-AI/issues/new/choose">
 <img src="https://img.shields.io/badge/Report%20a%20bug-EF4444?style=for-the-badge&logo=github&logoColor=white" alt="Report a bug">
</a>
<a href="https://github.com/PiBOH/Arena-AI/pulls">
 <img src="https://img.shields.io/badge/Pull%20requests-38BDF8?style=for-the-badge&logo=github&logoColor=white" alt="Pull requests">
</a>
<a href="https://github.com/PiBOH/Arena-AI/releases">
 <img src="https://img.shields.io/badge/Releases-4CAF50?style=for-the-badge&logo=github&logoColor=white" alt="Releases">
</a>

<br><br>

<sub>Made with by <a href="https://piboh.github.io/">PiBOH</a></sub><br>
<sub> Star this repo if you find it useful!</sub>

</div>
