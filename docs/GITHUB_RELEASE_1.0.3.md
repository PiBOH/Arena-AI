# 🚀 Arena AI — Release v1.0.3

Welcome to **Arena AI v1.0.3**! This release implements support for native LMArena / Chatbot Arena account sign-ins, and embeds clickable links inside the application settings for opening issues and submitting contributions directly on GitHub.

---

## ✨ What's New

### 🔐 Domain & Login Enhancements
* **Complete LMArena Support**: Added native handling for `lmarena.ai` alongside `arena.ai` and `lmsys.org` domains inside the app.
* **Unified OAuth Authentication**: Fully optimized and authorized all OAuth login redirection flows (such as Google Accounts, Hugging Face, GitHub, and Apple ID). Logging in now works seamlessly and retains cookies securely.

### 🛠️ Embedded Contributor Controls (Clickable Links)
* **Direct Issue Reporter**: Added a beautiful, clickable integration button directly in the application's settings to file a bug report or feature request at [github.com/PiBOH/Arena-AI/issues/new/choose](https://github.com/PiBOH/Arena-AI/issues/new/choose).
* **Direct Pull Request Navigator**: Contributor workflows are made incredibly easy with a quick link button that navigates directly to [github.com/PiBOH/Arena-AI/pulls](https://github.com/PiBOH/Arena-AI/pulls) to submit or review code.

### 📦 Repository & Tooling Infrastructure
* **Version Bump**: Formally migrated the build definitions (`versionCode = 4`, `versionName = "1.0.3"`).
* **Bilingual Contributor Templates**: Integrated standardized issue reporting templates (`bug_report.md`, `feature_request.md`) and a pull request template (`pull_request_template.md`) under `.github/`.

---

## 📦 Assets & Installation

You can download the compiled APK file directly from this release:
* **`app-release.apk`**: The production-ready release variant (needs to be signed or installed with developer permission overrides).
* **`app-debug.apk`**: Pre-signed debug variant, ready to install immediately for quick testing on any Android device.

### 📥 Manual Installation Steps:
1. Download the `.apk` file to your Android smartphone.
2. Tap the file in your preferred File Manager.
3. Allow installations from "Unknown Sources" if prompted by your system.
4. Launch **Arena AI** and start comparing chatbot models!

---

## 👥 Contributors & Feedback
* Developed with ❤️ by [PiBOH](https://github.com/PiBOH).
* Encountered a bug or have a feature suggestion? Open a structured ticket using our brand-new issue templates in the **Issues** tab!
